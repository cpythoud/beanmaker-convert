package org.beanmaker.converters.lib;

import org.beanmaker.v2.util.Dates;

import rodeo.password.pgencheck.CharacterGroups;
import rodeo.password.pgencheck.PasswordMaker;

public class FileCode {

    private static final PasswordMaker TAIL_CHARS_GENERATOR =
            PasswordMaker.factory()
                    .addCharGroup(CharacterGroups.UPPER_CASE)
                    .addCharGroup(CharacterGroups.LOWER_CASE)
                    .setLength(16)
                    .create();

    private final String code = Dates.getMeaningfulTimeStamp() + "-" + TAIL_CHARS_GENERATOR.create();
    
    public String getCode() {
        return code;
    }
    
    public String jsonResponse() {
        return "{ \"status\": \"ok\" , \"code\": \"" + code + "\" }";
    }

}
