package com.acme.jga.domain.model.utils;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class KeyValuePair {
    private final String key;
    private final String value;
}
