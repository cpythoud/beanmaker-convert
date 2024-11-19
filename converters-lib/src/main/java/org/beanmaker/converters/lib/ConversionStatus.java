package org.beanmaker.converters.lib;

import java.nio.file.Files;
import java.nio.file.Path;

public enum ConversionStatus {
    READY, ERROR, PROCESSING, MISSING;

    public static ConversionStatus getStatus(Path workDir, Path resultDir, Path errorDir) {
        if (exists(resultDir))
            return READY;
        if (exists(errorDir))
            return ERROR;
        if (exists(workDir))
            return PROCESSING;
        return MISSING;
    }

    private static boolean exists(Path directory) {
        return Files.isDirectory(directory);
    }

    public String toJson() {
        return "{ \"status\": \"%s\" }".formatted(toString());
    }


    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
