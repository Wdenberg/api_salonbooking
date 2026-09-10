package com.company.salonbooking.infrastructure.messaging;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter;

/**
 * Applies CorrelationIdConsumerInterceptor around every @RabbitListener-backed bean.
 * Using a BeanPostProcessor keeps every consumer class (AppointmentNotificationConsumer,
 * AppointmentReminderConsumer, ReportGenerationConsumer) untouched — none of them needed
 * to import or reference the interceptor directly.
 */
@Configuration
public class RabbitMqInterceptorConfig {

    @Bean
    public BeanPostProcessor rabbitListenerCorrelationIdProxyProcessor(CorrelationIdConsumerInterceptor interceptor) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean.getClass().getPackageName().startsWith("com.company.salonbooking")
                        && bean.getClass().isAnnotationPresent(org.springframework.stereotype.Component.class)
                        && hasRabbitListenerMethod(bean.getClass())) {
                    ProxyFactory factory = new ProxyFactory(bean);
                    factory.addAdvice(interceptor);
                    return factory.getProxy();
                }
                return bean;
            }

            private boolean hasRabbitListenerMethod(Class<?> type) {
                for (var method : type.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(org.springframework.amqp.rabbit.annotation.RabbitListener.class)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}