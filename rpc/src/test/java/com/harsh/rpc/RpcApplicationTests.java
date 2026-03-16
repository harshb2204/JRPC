package com.harsh.rpc;

import com.harsh.rpc.server.RpcServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RpcApplicationTests {

	@Test
	void contextLoads() {
	}


    @Test
    public void run() throws InterruptedException {

        RpcServer rpcServer = new RpcServer();
        rpcServer.startServer();
    }
}
