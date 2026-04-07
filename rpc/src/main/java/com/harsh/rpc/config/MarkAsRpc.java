package com.harsh.rpc.config;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface MarkAsRpc {
}
