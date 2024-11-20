package org.beanmaker.converters.demo.tkiz2svg;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.texngine.DefaultTeXngine;
import org.texngine.TeXCommand;

public class SameThreadTeXngine extends DefaultTeXngine {

    private final Logger logger = LoggerFactory.getLogger(SameThreadTeXngine.class);

    public SameThreadTeXngine() {
        super(1);  // ! Won't be used. File creation done in current thread as it should be very lightweight
    }

    @Override
    public void execute(TeXCommand texCommand) {
        logger.info("Executing command in current thread: {}", texCommand);
        texCommand.run();
    }

}
