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

    public ConverterService(Path parentWorkDir, Path parentResultDir) {
        this.parentWorkDir = parentWorkDir;
        this.parentResultDir = parentResultDir;
    }

    public ConverterService(String parentWorkDir, String parentResultDir) {
        this(Path.of(parentWorkDir), Path.of(parentResultDir));
    }

    public ConverterService(File parentWorkDir, File parentResultDir) {
        this(parentWorkDir.toPath(), parentResultDir.toPath());
    }

    protected Path getParentWorkDir() {
        return parentWorkDir;
    }

    protected Path getParentResultDir() {
        return parentResultDir;
    }

    protected Path getWorkDir(FileCode code) {
        return getParentWorkDir().resolve(code.getCode());
    }

    protected Path getResultDir(FileCode code) {
        return getParentResultDir().resolve(code.getCode());
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

            var fileItem = params.getFileItem("file");
            var code = new FileCode();
            var workdir = getWorkDir(code);
            Files.createDirectory(workdir);
            logger.info("Created directory: {}", workdir);
            var path = workdir.resolve(fileItem.getName());
            Files.copy(fileItem.getInputStream(), path);
            logger.info("Copied file: {}", path);

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

    protected abstract void forkConversion(FileCode code);

}
