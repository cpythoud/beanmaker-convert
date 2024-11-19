package org.beanmaker.converters.lib;

import org.beanmaker.v2.util.Dates;

import org.json.JSONObject;

import rodeo.password.pgencheck.CharacterGroups;
import rodeo.password.pgencheck.PasswordMaker;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

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

    public static FileCode fromJson(String json) {
        JSONObject inputObj = new JSONObject(json);
        if (!inputObj.has("reference"))
            throw new WebApplicationException("No reference", Response.Status.BAD_REQUEST);

        return new FileCode(inputObj.getString("reference"));
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
