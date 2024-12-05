package org.beanmaker.converters.demo.tkiz2svg.client;

import org.beanmaker.converters.lib.client.ConversionJobCreator;

public class App {

    //private static final String BASE_URL = "http://localhost:8080/convert/tkiz2svg";
    private static final String BASE_URL = "http://london3.texngine.org:8080/convert/tkiz2svg";
    private static final String TEST_SOURCE_FILENAME = "/home/chris/Prov/tkiz2svg/test-client/source/radar.tex";
    private static final String TEST_DESTINATION_FILENAME = "/home/chris/Prov/tkiz2svg/test-client/result/radar.svg";

    public static void main(String[] args) {
        var jobCreator = ConversionJobCreator.builder(BASE_URL).build();
        var job = jobCreator.create(TEST_SOURCE_FILENAME, TEST_DESTINATION_FILENAME);
        job.startConversion();
        while (!job.ready()) {
            try {
                Thread.sleep(200);
                System.out.println("Waiting for conversion to finish...");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
