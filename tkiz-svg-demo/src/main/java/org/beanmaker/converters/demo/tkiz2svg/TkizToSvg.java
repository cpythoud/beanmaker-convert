package org.beanmaker.converters.demo.tkiz2svg;

import org.beanmaker.converters.lib.ConverterService;
import org.beanmaker.converters.lib.FileCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Path;

@Path( "/tkiz2svg")
public class TkizToSvg extends ConverterService {

    final Logger logger = LoggerFactory.getLogger(TkizToSvg.class);

    public TkizToSvg(String workDir, String resultDir) {
        super(workDir, resultDir);
    }

    @Override
    protected void forkConversion(FileCode code) {
        logger.info("Converting... (not yet)");
    }

}
