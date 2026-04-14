package com.harsh.rpc.config;

import java.lang.annotation.*;

@Documented // show up in javadoc when generating docs
@Retention(RetentionPolicy.RUNTIME) // controls how long the annotation survives
@Target(ElementType.METHOD) // restricts where this annotation can be placed
public @interface MarkAsRpc {
}

// readable via reflection at runtime