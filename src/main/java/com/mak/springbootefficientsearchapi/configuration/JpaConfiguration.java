package com.mak.springbootefficientsearchapi.configuration;

import net.kaczmarzyk.spring.data.jpa.nativeimage.SpecificationArgumentResolverHintRegistrar;
import net.kaczmarzyk.spring.data.jpa.web.SpecificationArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableTransactionManagement
// Doc: https://github.com/tkaczmarzyk/specification-arg-resolver/blob/master/README_native_image.md
@ImportRuntimeHints({SpecificationArgumentResolverHintRegistrar.class,
        ProjectSpecificationArgumentResolverProxyHintRegistrar.class}) //support for reflection //support for dynamic proxy

public class JpaConfiguration implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new SpecificationArgumentResolver());
    }

}

