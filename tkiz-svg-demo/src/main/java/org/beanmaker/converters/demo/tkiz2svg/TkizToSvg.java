package org.beanmaker.converters.demo.tkiz2svg;

import org.beanmaker.converters.lib.ConverterService;
import org.beanmaker.converters.lib.FileCode;
import org.beanmaker.converters.lib.SingleFileRetriever;

import org.beanmaker.v2.util.Files;
import org.beanmaker.v2.util.Processes;
import org.beanmaker.v2.util.SVG;
import org.beanmaker.v2.util.Strings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.texngine.ErrorProcessor;
import org.texngine.PostProcessor;
import org.texngine.TeXCommand;
import org.texngine.TeXngine;

import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;

import java.io.IOException;

import java.util.ArrayList;

@Path( "/tkiz2svg")
public class TkizToSvg extends ConverterService {

    private static final String LATEX_PROCESSOR = "lualatex";

    private final Logger logger = LoggerFactory.getLogger(TkizToSvg.class);
    private final TeXngine teXngine = new SameThreadTeXngine();
    private final TikzToSvgSuccessProcessor successProcessor = new TikzToSvgSuccessProcessor();
    private final TikzToSvgErrorProcessor errorProcessor = new TikzToSvgErrorProcessor();

    public TkizToSvg(String workDir, String resultDir, String errorDir) {
        super(workDir, resultDir, errorDir);
    }

    @Override
    protected void forkConversion(FileCode code) {
        fork(() -> {
            var workingDir = getWorkDir(code);
            String texFilename = SingleFileRetriever.getSingleFile(workingDir).fileName();
            var command = composeCommand(texFilename);
            logger.info("Executing TeX command: {}", command.printCommand());
            command.execute(
                    1,
                    getWorkDir(code),
                    null,
                    new Pdf2Svg(code, texFilename),
                    new LatexErrorProcessor(code)
            );
        });
    }

    private TeXCommand composeCommand(String texFilename) {
        return teXngine.getCommandFactory()
                .setCommandAndArguments(
                        LATEX_PROCESSOR,
                        TeXngine.NON_STOP_MODE_OPTION,
                        texFilename)
                .create();
    }

    private class LatexErrorProcessor implements ErrorProcessor {
        private final FileCode code;

        LatexErrorProcessor(FileCode code) {
            this.code = code;
        }

        @Override
        public void processCompilationErrors(String logFileContent) {
            errorProcessor.process(getWorkDir(code), getErrorDir(code), "");
        }
    }

    private class Pdf2Svg implements PostProcessor {
        private final FileCode code;
        private final String texFilename;

        Pdf2Svg(FileCode code, String texFilename) {
            this.code = code;
            this.texFilename = texFilename;
        }

        @Override
        public void doPostProcessing() {
            String pdfFilename = Files.replaceExtension(texFilename, ".tex", ".pdf");
            String svgFilename = Files.replaceExtension(texFilename, ".tex", ".svg");
            convert(pdfFilename, svgFilename);
            successProcessor.process(getWorkDir(code), getResultDir(code), pdfFilename);
        }

        private void convert(String pdfFilename, String svgFilename) {
            var workingDir = getWorkDir(code);
            var commandAndArguments = new ArrayList<String>();
            commandAndArguments.add("/usr/bin/pdf2svg");
            commandAndArguments.add(workingDir.resolve(pdfFilename).toAbsolutePath().toString());
            var svgFilePath = workingDir.resolve(svgFilename);
            commandAndArguments.add(svgFilePath.toAbsolutePath().toString());

            logger.info("Executing pdf2svg command: {}", Strings.concatWithSeparator(" ",  commandAndArguments));
            Processes.runExternalProcess(commandAndArguments);
            try {
                logger.info("Removing width and height from converted svg file: {}", svgFilePath.toAbsolutePath());
                SVG.removeWidthAndHeight(svgFilePath, svgFilePath);
            } catch (IOException e) {
                logger.error("Could not remove width and height from converted svg file", e);
                throw new WebApplicationException(e);
            }
        }
    }

}
