package com.acme.jga.utils.lambdas;

import java.util.List;
import java.util.stream.Stream;

public final class StreamUtil {

     public static <T> Stream<T> ofNullableArray(T[] array) {
        return (array != null) ? Stream.of(array) : Stream.empty();
    }

    public static <T> Stream<T> ofNullableList(List<T> lst) {
        return (lst != null) ? lst.stream() : Stream.empty();
    }

}
