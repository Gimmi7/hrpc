package com.shareit.live.hrpc.util;

public class ClassNameUtil {
    /**
     * translate className to beanName,
     * for example: com.example.UserService -> userService
     */
    public static String beanName(String className) {
        String[] arr = className.split("\\.");
        String beanName = arr[arr.length - 1];
        return Character.toLowerCase(beanName.charAt(0)) + beanName.substring(1);
    }
}
