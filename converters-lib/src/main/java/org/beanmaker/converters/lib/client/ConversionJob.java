package org.beanmaker.converters.lib.client;

import org.json.JSONException;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConversionJob {

    private final Path source;
    private final Path result;
    private final Thread.Builder threadBuilder;
    private final String uploadUrl;
    private final String statusUrl;
    private final String downloadUrl;
    private final int checkInterval;

    private final AtomicBoolean jobFinished = new AtomicBoolean(false);
    private final AtomicBoolean processing = new AtomicBoolean(false);
    private final AtomicBoolean inError = new AtomicBoolean(false);

    private final Logger logger = LoggerFactory.getLogger(ConversionJob.class);

    ConversionJob(Path source, Path result, Thread.Builder threadBuilder, String uploadUrl, String statusUrl, String downloadUrl, int checkInterval) {
        this.source = source;
        this.result = result;
        this.threadBuilder = threadBuilder;
        this.uploadUrl = uploadUrl;
        this.statusUrl = statusUrl;
        this.downloadUrl = downloadUrl;
        this.checkInterval = checkInterval;
    }

    public void startConversion() {
        if (inError.get())
            throw new IllegalStateException("Conversion is in error");
        if (processing.get() || jobFinished.get())
            return;

        processing.set(true);
        threadBuilder.start(() -> {
            try {
                String code = upload();
                waitForConversion(code);
                download(code);
                jobFinished.set(true);
                processing.set(false);
            } catch (ConversionException e) {
                logger.error("Error during conversion", e);
                inError.set(true);
            }
        });
    }

    private String upload() throws ConversionException {
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest.BodyPublisher bodyPublisher = ofMultipartData();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uploadUrl))
                    .header("Content-Type", "multipart/form-data; boundary=boundary123")
                    .POST(bodyPublisher)
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException e) {
            logger.error("Error uploading file", e);
            throw new ConversionException(e);
        }

        if (response.statusCode() == 200) {
            logger.info("File uploaded successfully: {}", source.toAbsolutePath());
            return getCodeFromJsonResponse(response.body());
        } else {
            String msg = "File upload of" + source.toAbsolutePath() + " failed with status code: " + response.statusCode();
            logger.error(msg);
            throw new ConversionException(msg);
        }
    }

    private HttpRequest.BodyPublisher ofMultipartData() throws IOException {
        String boundary = "boundary123";
        var byteArrays = new ArrayList<byte[]>();

        byteArrays.add(("--" + boundary + "\r\n").getBytes());
        byteArrays.add(("Content-Disposition: form-data; name=\"file\"; filename=\"" + source.getFileName() + "\"\r\n").getBytes());
        byteArrays.add(("Content-Type: " + Files.probeContentType(source) + "\r\n\r\n").getBytes());
        byteArrays.add(Files.readAllBytes(source));
        byteArrays.add(("\r\n--" + boundary + "--\r\n").getBytes());

        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

    private String getCodeFromJsonResponse(String responseBody) throws ConversionException {
        try {
            var jsonResponse = new JSONObject(responseBody);
            if (!jsonResponse.getString("status").equals("ok")) {
                String msg = "Server returned error condition upon upload: " + jsonResponse.getString("status");
                logger.error(msg);
                throw new ConversionException(msg);
            }
            String code = jsonResponse.getString("code");
            logger.info("Code received from server: {}, for file: {}", code, source.toAbsolutePath());
            return code;
        } catch (JSONException e) {
            logger.error("Error parsing JSON response on upload", e);
            throw new ConversionException(e);
        }
    }

    private void waitForConversion(String code) throws ConversionException {
        while (serverWorking(code)) {
            try {
                Thread.sleep(checkInterval);
            } catch (InterruptedException e) {
                logger.error("Conversion job interrupted", e);
                throw new ConversionException(e);
            }
        }
    }

    private boolean serverWorking(String code) throws ConversionException {
        HttpResponse<String> response;
        try (HttpClient client = HttpClient.newHttpClient()) {

            // Prepare the HTTP request with JSON content
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(statusUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(encodeReference(code)))
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException | URISyntaxException e) {
            logger.error("Error checking conversion status", e);
            throw new ConversionException(e);
        }

        if (response.statusCode() == 200) {
            String status = getStatusFromJsonResponse(response.body());
            logger.info("Checked status for: {}, status = {}", source.toAbsolutePath(), status);
            return !status.equals("ready");
        } else {
            String msg = "Status check for " + source.toAbsolutePath() + " failed with HTTP status code: " + response.statusCode();
            logger.error(msg);
            throw new ConversionException(msg);
        }
    }

    private String encodeReference(String code) {
        return "{ \"reference\": \"%s\" }".formatted(code);
    }

    private String getStatusFromJsonResponse(String responseBody) throws ConversionException {
        try {
            var jsonResponse = new JSONObject(responseBody);
            return jsonResponse.getString("status");
        } catch (JSONException e) {
            logger.error("Error parsing JSON response on status check", e);
            throw new ConversionException(e);
        }
    }

    private void download(String code) throws ConversionException {
        HttpResponse<byte[]> response;
        try (HttpClient client = HttpClient.newHttpClient()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(downloadUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(encodeReference(code)))
                    .build();

            response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException | IOException | URISyntaxException e) {
            String msg = "Error downloading converted file: " + result.toAbsolutePath();
            logger.error(msg, e);
            throw new ConversionException(e);
        }

        if (response.statusCode() == 200) {
            try {
                Files.write(result, response.body());
            } catch (IOException e) {
                logger.error("Error writing converted file: " + result.toAbsolutePath(), e);
                throw new ConversionException(e);
            }
        } else {
            String msg = "Download of converted file: " + result.toAbsolutePath() + " failed with HTTP status code: " + response.statusCode();
            logger.error(msg);
            throw new ConversionException(msg);
        }
    }

    public boolean ready() {
        if (inError.get())
            throw new IllegalStateException("Conversion is in error");

        return jobFinished.get();
    }

    public boolean processing() {
        return processing.get();
    }

    public boolean inError() {
        return inError.get();
    }

}
