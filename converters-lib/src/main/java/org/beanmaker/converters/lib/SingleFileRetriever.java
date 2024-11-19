package org.beanmaker.converters.lib;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import java.io.IOException;

import java.util.Iterator;

public class SingleFileRetriever {

    public static FileDetail getSingleFile(Path directory) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            Iterator<Path> iterator = stream.iterator();

            if (!iterator.hasNext()) {
                throw new WebApplicationException(
                        "No files found in the directory",
                        Response.Status.INTERNAL_SERVER_ERROR);
            }

            Path file = iterator.next();

            if (iterator.hasNext()) {
                throw new WebApplicationException(
                        "More than one file found in the directory",
                        Response.Status.INTERNAL_SERVER_ERROR);
            }

            return new FileDetail(file, file.getFileName().toString());
        } catch (IOException ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    public record FileDetail(Path path, String fileName) { }

}
