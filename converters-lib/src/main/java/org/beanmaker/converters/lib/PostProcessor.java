package org.beanmaker.converters.lib;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;

public abstract class PostProcessor {

    public void process(Path sourceDir, Path destDir, String sourceFilename) {
        try {
            String destFilename = getDestFilename(sourceFilename);
            Files.createDirectory(destDir);
            Files.move(sourceDir.resolve(destFilename), destDir.resolve(destFilename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract String getDestFilename(String sourceFilename);

}
