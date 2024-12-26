package org.beanmaker.converters.lib;

import org.beanmaker.v2.runtime.HttpRequestParameters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.File;

import java.nio.file.Files;
import java.nio.file.Path;

public abstract class ConverterService {

    final Logger logger = LoggerFactory.getLogger(ConverterService.class);

    private final Path parentWorkDir;
    private final Path parentResultDir;
    private final Path parentErrorDir;

    public ConverterService(Path parentWorkDir, Path parentResultDir, Path parentErrorDir) {
        this.parentWorkDir = parentWorkDir;
        this.parentResultDir = parentResultDir;
        this.parentErrorDir = parentErrorDir;
    }

    public ConverterService(String parentWorkDir, String parentResultDir, String parentErrorDir) {
        this(Path.of(parentWorkDir), Path.of(parentResultDir), Path.of(parentErrorDir));
    }

    public ConverterService(File parentWorkDir, File parentResultDir, File parentErrorDir) {
        this(parentWorkDir.toPath(), parentResultDir.toPath(), parentErrorDir.toPath());
    }

    protected Path getParentWorkDir() {
        return parentWorkDir;
    }

    protected Path getParentResultDir() {
        return parentResultDir;
    }

    protected Path getParentErrorDir() {
        return parentErrorDir;
    }

    protected Path getWorkDir(FileCode code) {
        return getParentWorkDir().resolve(code.getCode());
    }

    protected Path getResultDir(FileCode code) {
        return getParentResultDir().resolve(code.getCode());
    }

    protected Path getErrorDir(FileCode code) {
        return getParentErrorDir().resolve(code.getCode());
    }

    @POST
    @javax.ws.rs.Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@Context HttpServletRequest request) {
        try {
            var params = new HttpRequestParameters(request);
            if (!params.isMultipartRequest())
                throw new WebApplicationException("Not a multipart request", Response.Status.BAD_REQUEST);
            if (!params.hasFiles())
                throw new WebApplicationException("No file", Response.Status.BAD_REQUEST);

            var uploadedFile = params.getUploadedFile("file");
            var code = FileCode.create();
            var workdir = getWorkDir(code);
            Files.createDirectory(workdir);
            logger.info("Created directory: {}", workdir);
            var path = workdir.resolve(uploadedFile.getFilename());
            Files.copy(uploadedFile.getInputStream(), path);
            logger.info("Copied file: {}", path);

            // TODO: add file validation (type, content, size, etc.), should call an abstract function

            forkConversion(code);

            return Response.status(Response.Status.OK).entity(code.jsonResponse()).build();
        } catch (WebApplicationException ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @javax.ws.rs.Path("/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkStatus(String jsonInput) {
        try {
            var code = FileCode.fromJson(jsonInput);
            var status = ConversionStatus.getStatus(getWorkDir(code), getResultDir(code), getErrorDir(code));
            return Response.ok(status.toJson(), MediaType.APPLICATION_JSON).build();
        } catch (WebApplicationException ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @javax.ws.rs.Path("/download")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.WILDCARD)
    public Response downloadFile(String jsonInput) {
        try {
            var code = FileCode.fromJson(jsonInput);
            var resultDir = getResultDir(code);
            var fileDetail = SingleFileRetriever.getSingleFile(resultDir);

            String contentType = Files.probeContentType(fileDetail.path());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            var response = Response.ok(fileDetail.path().toFile(), contentType);
            response.header("Content-Disposition", "attachment; filename=\"" + fileDetail.fileName() + "\"");
            return response.build();
        } catch (WebApplicationException ex) {
            logger.error(ex.getMessage(), ex);
            throw ex;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    protected abstract void forkConversion(FileCode code);

    private class ConversionExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            String msg = "An exception occurred while gathering data to compose audit report in thread "
                    + thread.getName() + " (#" + thread.threadId() + ")";
            logger.error(msg, throwable);
            throw new WebApplicationException(msg, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private final Thread.Builder conversionThreadBuilder = Thread.ofVirtual()
            .name("Converter-Thread")
            .uncaughtExceptionHandler(new ConversionExceptionHandler());

    protected void fork(Runnable runnable) {
        conversionThreadBuilder.start(runnable);
    }

}
