package net.furizon.zebra_proxy.utils;


import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Utils {
    public static <T extends Comparable<? super T>> @NotNull List<T> asSortedList(@NotNull Collection<T> c) {
        List<T> list = new ArrayList<T>(c);
        Collections.sort(list);
        return list;
    }
    public static <T> @NotNull List<T> asSortedList(@NotNull Collection<T> c, @NotNull Comparator<T> comparing) {
        List<T> list = new ArrayList<T>(c);
        list.sort(comparing);
        return list;
    }
}
