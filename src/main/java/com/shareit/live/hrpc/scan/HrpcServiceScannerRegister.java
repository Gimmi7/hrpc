package com.shareit.live.hrpc.scan;

import com.shareit.live.hrpc.annotation.HrpcService;
import com.shareit.live.hrpc.annotation.HrpcServiceScan;
import com.shareit.live.hrpc.binding.HrpcServiceProxy;
import com.shareit.live.hrpc.config.BeanFactoryHolder;
import com.shareit.live.hrpc.util.ClassNameUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.*;

@Slf4j
public class HrpcServiceScannerRegister implements BeanFactoryAware, ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private BeanFactory beanFactory;

    private ResourceLoader resourceLoader;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
        BeanFactoryHolder.setBeanFactory(beanFactory);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        ClassPathBeanDefinitionScanner scanner = new ClassPathHrpcServiceScanner(registry);
        scanner.setResourceLoader(this.resourceLoader);
        scanner.addIncludeFilter(new AnnotationTypeFilter(HrpcService.class));
        Set<BeanDefinition> beanDefinitions = this.scanPackages(importingClassMetadata, scanner);
        this.registerBeans(beanDefinitions);
    }

    /**
     * scan basePackages
     */
    private Set<BeanDefinition> scanPackages(AnnotationMetadata annotationMetadata, ClassPathBeanDefinitionScanner scanner) {
        List<String> packages = new ArrayList<>();
        Map<String, Object> annotationAttributes = annotationMetadata.getAnnotationAttributes(HrpcServiceScan.class.getCanonicalName());
        if (annotationAttributes != null) {
            String[] basePackages = (String[]) annotationAttributes.get("basePackages");
            if (basePackages.length > 0) {
                packages.addAll(Arrays.asList(basePackages));
            }
        }
        Set<BeanDefinition> beanDefinitions = new HashSet<>();
        for (String pack : packages) {
            beanDefinitions.addAll(scanner.findCandidateComponents(pack));
        }
        return beanDefinitions;
    }


    /**
     * register proxy to spring bean container
     */
    private void registerBeans(Set<BeanDefinition> beanDefinitions) {
        for (BeanDefinition definition : beanDefinitions) {
            String className = definition.getBeanClassName();
            if (StringUtils.isEmpty(className)) {
                continue;
            }

            try {
                //create proxy class
                Class<?> target = Class.forName(className);
                Object invoker = new Object();
                InvocationHandler invocationHandler = new HrpcServiceProxy<>(target, invoker, beanFactory);
                Object proxy = Proxy.newProxyInstance(HrpcService.class.getClassLoader(), new Class[]{target}, invocationHandler);

                //register proxy
                String beanName = ClassNameUtil.beanName(className);
                ((DefaultListableBeanFactory) beanFactory).registerSingleton(beanName, proxy);

            } catch (ClassNotFoundException e) {
                log.warn("hrpc register bean: class not found:", e);
            }
        }
    }

    private static class ClassPathHrpcServiceScanner extends ClassPathBeanDefinitionScanner {
        public ClassPathHrpcServiceScanner(BeanDefinitionRegistry registry) {
            super(registry, false);
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
        }
    }
}
