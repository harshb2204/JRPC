package com.harsh.rpc.config;


import com.harsh.rpc.client.RpcClient;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(RpcClient.class)
public @interface EnableRpcClient {
}
