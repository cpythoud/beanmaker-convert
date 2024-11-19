package org.beanmaker.converters.lib;

import org.beanmaker.v2.util.Dates;

import rodeo.password.pgencheck.CharacterGroups;
import rodeo.password.pgencheck.PasswordMaker;

public final class FileCode {

    private static final PasswordMaker TAIL_CHARS_GENERATOR =
            PasswordMaker.factory()
                    .addCharGroup(CharacterGroups.UPPER_CASE)
                    .addCharGroup(CharacterGroups.LOWER_CASE)
                    .setLength(16)
                    .create();

    private final String code;

    public static FileCode create() {
        return new FileCode(Dates.getMeaningfulTimeStamp() + "-" + TAIL_CHARS_GENERATOR.create());
    }

    public static FileCode from(String code) {
        return new FileCode(code);
    }

    private FileCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public String jsonResponse() {
        return "{ \"status\": \"ok\" , \"code\": \"" + code + "\" }";
    }

    @Override
    public String toString() {
        return getCode();
    }

}
