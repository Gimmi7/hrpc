package com.shareit.live.hrpc.annotation;

import com.shareit.live.hrpc.enums.SerializeType;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that a interface is a HrpcService,
 * default serverName is empty;
 *
 * @author wangcy
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface HrpcService {
    //@required
    String name();

    String serverFetchBeanName() default "";

    SerializeType serialization() default SerializeType.PROTOSTUFF;
}
