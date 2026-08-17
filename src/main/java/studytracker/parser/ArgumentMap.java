package studytracker.parser;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import studytracker.exception.ParseException;

/** Tokenises prefixed values such as {@code m/CS3227 d/90}. */
final class ArgumentMap {
    private static final Pattern PREFIX = Pattern.compile("(?:^|\\s)(m|t|d|on|n|from|to)/");

    private final Map<String, String> values;

    private ArgumentMap(Map<String, String> values) {
        this.values = Collections.unmodifiableMap(values);
    }

    static ArgumentMap parse(String input, Set<String> allowedPrefixes) throws ParseException {
        Map<String, String> values = new LinkedHashMap<>();
        Matcher matcher = PREFIX.matcher(input);
        int previousValueStart = -1;
        String previousPrefix = null;
        int firstPrefixStart = -1;

        while (matcher.find()) {
            if (firstPrefixStart < 0) {
                firstPrefixStart = matcher.start();
            }
            if (previousPrefix != null) {
                put(values, previousPrefix, input.substring(previousValueStart, matcher.start()).strip());
            }
            previousPrefix = matcher.group(1);
            if (!allowedPrefixes.contains(previousPrefix)) {
                throw new ParseException("Prefix '" + previousPrefix + "/' is not valid for this command.");
            }
            previousValueStart = matcher.end();
        }
        if (previousPrefix != null) {
            put(values, previousPrefix, input.substring(previousValueStart).strip());
        }
        String unprefixed = firstPrefixStart < 0 ? input.strip() : input.substring(0, firstPrefixStart).strip();
        if (!unprefixed.isEmpty()) {
            throw new ParseException("Unexpected text before the first prefixed argument: " + unprefixed);
        }
        return new ArgumentMap(values);
    }

    private static void put(Map<String, String> values, String prefix, String value) throws ParseException {
        if (values.containsKey(prefix)) {
            throw new ParseException("Prefix '" + prefix + "/' was supplied more than once.");
        }
        values.put(prefix, value);
    }

    Optional<String> optional(String prefix) {
        return Optional.ofNullable(values.get(prefix));
    }

    String required(String prefix, String label) throws ParseException {
        String value = values.get(prefix);
        if (value == null || value.isBlank()) {
            throw new ParseException(label + " is required. Use " + prefix + "/" + label.toUpperCase() + ".");
        }
        return value;
    }

    boolean isEmpty() {
        return values.isEmpty();
    }
}
