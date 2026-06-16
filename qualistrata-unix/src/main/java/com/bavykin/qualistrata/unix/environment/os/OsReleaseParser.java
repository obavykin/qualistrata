package com.bavykin.qualistrata.unix.environment.os;

import com.bavykin.qualistrata.core.contracts.unix.dto.OsRelease;
import com.bavykin.qualistrata.unix.session.UnixSessionException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parser for {@code /etc/os-release} content.
 *
 * <p>The parser supports the standard {@code KEY=VALUE} format used by Linux
 * distributions. Values may be quoted or unquoted. Unknown fields are preserved
 * in the raw map so consuming projects can inspect additional OS metadata when
 * needed.</p>
 */
public class OsReleaseParser {

    /**
     * Creates an OS release parser.
     */
    public OsReleaseParser() {
        // Parser has no configurable state.
    }

    /**
     * Parses {@code /etc/os-release} content.
     *
     * @param content raw file content.
     * @return parsed OS release.
     */
    public OsRelease parse(String content) {
        if (content == null || content.isBlank()) {
            throw new UnixSessionException("OS release content must not be blank.");
        }

        Map<String, String> values = new LinkedHashMap<>();

        for (String line : content.split("\\R")) {
            parseLine(line, values);
        }

        return new OsRelease(
                values.get("ID"),
                values.get("NAME"),
                values.get("PRETTY_NAME"),
                values.get("VERSION"),
                values.get("VERSION_ID"),
                values
        );
    }

    /**
     * Parses one {@code KEY=VALUE} line.
     *
     * @param line raw line.
     * @param values parsed values.
     */
    private void parseLine(
            String line,
            Map<String, String> values
    ) {
        if (line == null) {
            return;
        }

        String trimmedLine = line.trim();

        if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
            return;
        }

        int separatorIndex = trimmedLine.indexOf('=');

        if (separatorIndex <= 0) {
            return;
        }

        String key = trimmedLine.substring(0, separatorIndex).trim();
        String value = trimmedLine.substring(separatorIndex + 1).trim();

        if (!key.isEmpty()) {
            values.put(key, unquote(value));
        }
    }

    /**
     * Removes surrounding single or double quotes from a value.
     *
     * @param value raw value.
     * @return unquoted value.
     */
    private String unquote(String value) {
        if (value.length() < 2) {
            return value;
        }

        boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
        boolean singleQuoted = value.startsWith("'") && value.endsWith("'");

        if (doubleQuoted || singleQuoted) {
            return value.substring(1, value.length() - 1);
        }

        return value;
    }
}