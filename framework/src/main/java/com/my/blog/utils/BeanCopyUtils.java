package com.my.blog.utils;

import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BeanCopyUtils {
    private BeanCopyUtils() {}

    public static <V> V copyBean(Object source, Class<V> clazz) {
        if (source == null) {
            return null;
        }
        try {
            V result = clazz.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Bean copy failed: " + clazz.getName(), e);
        }
    }

    public static <O, V> List<V> copyBeanList(List<O> list, Class<V> clazz) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(o -> copyBean(o, clazz))
                .collect(Collectors.toList());
    }
}
