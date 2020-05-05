package com.upgrad.myntra.api.common;

import com.upgrad.myntra.service.exception.AuthorizationFailedException;

public class Utility {

    public static String getTokenFromAuthorizationField(String authorization) throws AuthorizationFailedException {

        //split and extract authorization base 64 code string from "authorization" field
        String[] base64EncodedString = authorization.split("Bearer ");

        //decode base64 string from a "authorization" field
        if (base64EncodedString.length != 2) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in");
        }

        return base64EncodedString[1];
    }
}
