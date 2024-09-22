package com.example.cinema_client.constants;

import java.util.regex.Pattern;

/**
 * @author tritcse00526x
 */
public class ValidateRandomString {
    private static Pattern UUID_V4_REGEX = Pattern.compile("^[0-9(a-f|A-F)]{8}-[0-9(a-f|A-F)]{4}-4[0-9(a-f|A-F)]{3}-[89ab][0-9(a-f|A-F)]{3}-[0-9(a-f|A-F)]{12}$");

    private static boolean isValidUUID(String uuid) {
        return UUID_V4_REGEX.matcher(uuid).matches();
    }
    public static boolean validateUUIDV4(String uuid) {
        return isValidUUID(uuid);
    }
}
