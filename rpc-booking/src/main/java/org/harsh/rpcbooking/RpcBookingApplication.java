package org.harsh.rpcbooking;

import com.harsh.rpc.config.EnableRpcClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableRpcClient
@SpringBootApplication
// When spring application starts it scans your annotation and it can scan the import
// RpcClient is instantiated
public class RpcBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(RpcBookingApplication.class, args);
    }

}
// WHen this spring application starts, he wants this application to be a RPC client
// Need to import rpc client in this module
