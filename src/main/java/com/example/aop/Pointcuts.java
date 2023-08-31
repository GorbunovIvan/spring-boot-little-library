package com.example.aop;

import org.aspectj.lang.annotation.Pointcut;

public class Pointcuts {

    @Pointcut("execution(public String com.example.controller.*Controller.*(..))")
    public void allEndpoints() {}

    @Pointcut("execution(* com.example.service.*Service.*(..))")
    public void allServicesMethods() {}

    @Pointcut("execution(* com.example.security.*.*(..))")
    public void allSecurityMethods() {}
}
