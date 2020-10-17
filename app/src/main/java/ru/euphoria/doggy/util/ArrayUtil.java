package ru.euphoria.doggy.util;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.reactivex.functions.Predicate;

/**
 * Created by admin on 24.03.18.
 */

public class ArrayUtil {
    public static int hash(int... values) {
        return Arrays.hashCode(values);
    }

    public static <E> E last(List<E> source) {
        return source.get(source.size() - 1);
    }

    public static boolean isEmpty(Collection source) {
        return source == null || source.isEmpty();
    }

    public static boolean isEmpty(int[] source) {
        return source == null || source.length == 0;
    }

    public static boolean isEmpty(Object[] source) {
        return source == null || source.length == 0;
    }

    public static boolean isNotEmpty(Collection source) {
        return !isEmpty(source);
    }

    public static <E> ArrayList<E> limit(List<E> list, int limit) {
        int size = Math.min(list.size(), limit);

        ArrayList<E> copy = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            copy.add(list.get(i));
        }
        return copy;
    }

    public static int max(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }

        int max = list.get(0);
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i) > max) {
                max = list.get(i);
            }
        }
        return max;
    }

    public static int frequency(char[] array, char value) {
        int result = 0;
        for (char c : array) {
            if (c == value) {
                result++;
            }
        }
        return result;
    }

    public static int[] toInts(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static int[] toInts(Set<Integer> set) {
        int[] array = new int[set.size()];
        int i = 0;

        for (Integer value : set) {
            array[i++] = value;
        }
        return array;
    }

    public static int[] toInts(String source, char separator) {
        String[] split = source.split(String.valueOf(separator));
        int[] values = new int[split.length];

        for (int i = 0; i < split.length; i++) {
            values[i] = Integer.parseInt(split[i]);
        }
        return values;
    }

    @SafeVarargs
    public static <E> E firstNotEmpty(E... values) {
        for (E value : values) {
            if (value != null) {
                if (value instanceof String) {
                    String s = (String) value;
                    if (!TextUtils.isEmpty(s)) {
                        return value;
                    }
                }
                return value;
            }
        }
        return null;
    }

    public static <T> void filter(List<T> list, Predicate<? super T> predicate) {
        Iterator<T> iterator = list.iterator();
        try {
            while (iterator.hasNext()) {
                if (!predicate.test(iterator.next())) {
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Integer> asList(int... values) {
        ArrayList<Integer> list = new ArrayList<>(values.length);
        for (int value : values) {
            list.add(value);
        }
        return list;
    }

    public static String join(String[] array, String separator) {
        return TextUtils.join(separator, array);
    }

    public static String join(int[] array, char separator) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(array[0]);

        for (int i = 1; i < array.length; i++) {
            buffer.append(separator);
            buffer.append(array[i]);
        }
        return buffer.toString();
    }
}
