package com.shareit.live.hrpc.config;

import org.springframework.beans.factory.BeanFactory;

public class BeanFactoryHolder {
    private static BeanFactory beanFactory;

    public static BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public static void setBeanFactory(BeanFactory beanFactory) {
        BeanFactoryHolder.beanFactory = beanFactory;
    }
}
