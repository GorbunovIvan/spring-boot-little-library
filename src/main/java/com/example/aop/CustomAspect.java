package com.example.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class CustomAspect {

//    @Around("Pointcuts.allEndpoints()") - for some reason the autowiring becomes broken...
    public Object aroundEndpointAdvice(ProceedingJoinPoint joinPoint) {
        return defaultHandlingJoinPoint(joinPoint, "controller", "endpoint");
    }

    @Around("Pointcuts.allServicesMethods()")
    public Object aroundServiceMethodAdvice(ProceedingJoinPoint joinPoint) {
        return defaultHandlingJoinPoint(joinPoint, "service");
    }

    @Around("Pointcuts.allSecurityMethods()")
    public Object aroundSecurityMethodAdvice(ProceedingJoinPoint joinPoint) {
        return defaultHandlingJoinPoint(joinPoint, "security-class");
    }

    private Object defaultHandlingJoinPoint(JoinPoint joinPoint, String classLayer) {
        return defaultHandlingJoinPoint(joinPoint, classLayer, "method");
    }

    private Object defaultHandlingJoinPoint(JoinPoint joinPoint, String classLayer, String methodCategory) {

        var signature = (MethodSignature) joinPoint.getSignature();

        String message = String.format("%s '%s', %s '%s'",
                classLayer,
                signature.getMethod().getDeclaringClass().getName(),
                methodCategory,
                signature.getName()
        );

        Object result = null;

        log.info(message + " is invoked");

        if (joinPoint instanceof ProceedingJoinPoint proceedingJoinPoint) {
            try {
                result = proceedingJoinPoint.proceed();
                log.info(message + " is completed");
            } catch (RuntimeException e) {
                log.error(message + " has thrown an error");
                throw e;
            } catch (Throwable e) {
                log.error(message + " has thrown an error");
                throw new RuntimeException(e);
            }
        }

        return result;
    }
}
