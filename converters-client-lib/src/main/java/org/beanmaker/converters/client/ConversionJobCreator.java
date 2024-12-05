package org.beanmaker.converters.client;

import java.io.File;
import java.nio.file.Path;

public class ConversionJobCreator {

    private static final String DEFAULT_THREAD_NAME = "BeanmakerConversionJob";
    private static final Thread.Builder DEFAULT_THREAD_BUILDER =
            Thread.ofVirtual().name(DEFAULT_THREAD_NAME).uncaughtExceptionHandler(new DefaultExceptionHandler());

    private final Thread.UncaughtExceptionHandler exceptionHandler;
    private final Thread.Builder threadBuilder;
    private final String uploadUrl;
    private final String statusUrl;
    private final String downloadUrl;
    private final int checkInterval;

    private ConversionJobCreator(Thread.UncaughtExceptionHandler exceptionHandler, Thread.Builder threadBuilder, String uploadUrl, String statusUrl, String downloadUrl, int checkInterval) {
        this.exceptionHandler = exceptionHandler;
        this.threadBuilder = threadBuilder;
        this.uploadUrl = uploadUrl;
        this.statusUrl = statusUrl;
        this.downloadUrl = downloadUrl;
        this.checkInterval = checkInterval;
    }

    public static Builder builder(String baseUrl) {
        return builder(baseUrl + "/upload", baseUrl + "/status", baseUrl + "/download");
    }

    public static Builder builder(String uploadUrl, String statusUrl, String downloadUrl) {
        return new Builder(uploadUrl, statusUrl, downloadUrl);
    }

    public static class Builder {
        private Thread.UncaughtExceptionHandler exceptionHandler;
        private Thread.Builder threadBuilder;
        private final String uploadUrl;
        private final String statusUrl;
        private final String downloadUrl;
        private int checkInterval = 1000;

        private Builder(String uploadUrl, String statusUrl, String downloadUrl) {
            this.uploadUrl = uploadUrl;
            this.statusUrl = statusUrl;
            this.downloadUrl = downloadUrl;
        }

        public Builder setExceptionHandler(Thread.UncaughtExceptionHandler exceptionHandler) {
            this.exceptionHandler = exceptionHandler;
            return this;
        }

        public Builder setThreadBuilder(Thread.Builder threadBuilder) {
            this.threadBuilder = threadBuilder;
            return this;
        }

        public Builder setCheckInterval(int checkInterval) {
            this.checkInterval = checkInterval;
            return this;
        }

        public ConversionJobCreator build() {
            return new ConversionJobCreator(exceptionHandler, threadBuilder, uploadUrl, statusUrl, downloadUrl, checkInterval);
        }
    }

    public ConversionJob create(Path source, Path result) {
        Thread.Builder threadBuilder;
        if (this.threadBuilder == null) {
            if (exceptionHandler == null)
                threadBuilder = DEFAULT_THREAD_BUILDER;
            else
                threadBuilder = DEFAULT_THREAD_BUILDER.uncaughtExceptionHandler(exceptionHandler);
        } else {
            threadBuilder = this.threadBuilder;
        }
        return new ConversionJob(source, result, threadBuilder, uploadUrl, statusUrl, downloadUrl, checkInterval);
    }

    public ConversionJob create(File source, File result) {
        return create(source.toPath(), result.toPath());
    }

    public ConversionJob create(String source, String result) {
        return create(new File(source), new File(result));
    }

}
