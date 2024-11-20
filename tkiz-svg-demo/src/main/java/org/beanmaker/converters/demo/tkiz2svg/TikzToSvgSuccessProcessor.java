package org.beanmaker.converters.demo.tkiz2svg;

import org.beanmaker.converters.lib.PostProcessor;

import org.beanmaker.v2.util.Files;

public class TikzToSvgSuccessProcessor extends PostProcessor {

    @Override
    public String getDestFilename(String sourceFilename) {
        return Files.replaceExtension(sourceFilename, ".pdf", ".svg");
    }

}
