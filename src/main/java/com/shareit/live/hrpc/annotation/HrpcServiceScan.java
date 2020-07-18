package com.shareit.live.hrpc.annotation;

import com.shareit.live.hrpc.scan.HrpcServiceScannerRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({HrpcServiceScannerRegister.class})
public @interface HrpcServiceScan {
    /**
     * scan basePackages to find @HrpcService interface;
     * then register HrpcService to spring bean container;
     *
     * @author: wangcy
     */
    String[] basePackages() default {};
}
