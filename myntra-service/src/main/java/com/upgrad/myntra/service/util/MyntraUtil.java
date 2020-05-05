package com.upgrad.myntra.service.util;

import com.upgrad.myntra.service.exception.AuthorizationFailedException;

import java.util.regex.Pattern;

public class MyntraUtil {


    public static final String BASIC_TOKEN = "Basic ";
    public static final String BEARER_TOKEN = "Bearer ";
    public static final String COLON = ":";
    public static final Long EIGHT_HOURS_IN_MILLIS = 8 * 60 * 60 * 1000L;


    public static Boolean isInValid(String value) {
        return (value == null || value.isEmpty());
    }


    public static Boolean isInValidEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";

        Pattern pattern = Pattern.compile(regex);
        return !pattern.matcher(email).matches();
    }

    public static Boolean isInValidContactNumber(String contactNumber) {
        String regex = "\\d{10}";
        Pattern pattern = Pattern.compile(regex);
        return !pattern.matcher(contactNumber).matches();
    }


    public static Boolean isStrongPassword(String password) {
        // (?=.*[A-Z]) Match at least one Capital letter
        // (?=.*\d) at least one digit
        // (?=.*[#@$%&*!^]) at least one among the listed special characters
        // (.*) remaining can be any character
        // {8,} length at least 8
        String regex = "(?=^.{8,}$)(?=.*[A-Z])(?=.*\\d)(?=.*[#@$%&*!^])(.*)";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(password).matches();
    }


    public static String decodeBearerToken(String authorization) throws AuthorizationFailedException {
        try {
            String[] bearerToken = authorization.split(MyntraUtil.BEARER_TOKEN);
            if (bearerToken != null && bearerToken.length > 1) {
                String accessToken = bearerToken[1];
                return accessToken;
            } else {
                throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }
    }


    public static Boolean isInvalidPinCode(String pinCode) {
        // \b at the start and the end defines a word boundary so that it doesn't match words like text12, 9gag, 4chan et
        // \d{6}+ allows only 6 number of digits
        String regex = "(\\b\\d{6}+\\b)";
        Pattern pattern = Pattern.compile(regex);
        return !pattern.matcher(pinCode).matches();
    }
}
