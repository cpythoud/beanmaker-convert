package org.beanmaker.converters.demo.tkiz2svg;

import org.beanmaker.converters.lib.PostProcessor;

class TikzToSvgErrorProcessor extends PostProcessor {

    @Override
    public String getDestFilename(String sourceFilename) {
        return "texngine.log";  // * We assume any error come from LaTeX compilation, not PDF to SVG conversion
    }

}
