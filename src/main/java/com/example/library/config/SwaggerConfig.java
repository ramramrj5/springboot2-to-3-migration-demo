package com.example.library.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.WebMvcRequestHandlerProvider;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Springfox is unmaintained since 2020 and does not work on Spring Boot 3.
 * On Boot 2.6+ it already needs two hacks to start at all:
 *   1. spring.mvc.pathmatch.matching-strategy=ant_path_matcher (application.yml)
 *   2. the BeanPostProcessor below, which strips Actuator's PathPattern-based
 *      handler mappings that Springfox cannot read (springfox issue #3462).
 * Both go away when the migrated branch swaps it for springdoc-openapi.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.OAS_30)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.library.web"))
                .paths(PathSelectors.any())
                .build();
    }

    @Bean
    public static BeanPostProcessor springfoxActuatorWorkaround() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof WebMvcRequestHandlerProvider) {
                    Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
                    field.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    List<RequestMappingInfoHandlerMapping> mappings =
                            (List<RequestMappingInfoHandlerMapping>) ReflectionUtils.getField(field, bean);
                    List<RequestMappingInfoHandlerMapping> filtered = mappings.stream()
                            .filter(m -> m.getPatternParser() == null)
                            .collect(Collectors.toList());
                    mappings.clear();
                    mappings.addAll(filtered);
                }
                return bean;
            }
        };
    }
}
