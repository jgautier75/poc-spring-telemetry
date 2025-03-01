package com.acme.jga.utils.string;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class StringUtils {

    public static String nvl(String input) {
        return input != null ? input : "";
    }
}
