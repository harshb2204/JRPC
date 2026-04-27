# Custom RPC Framework

A high-performance, custom Remote Procedure Call (RPC) framework built from scratch. It allows microservices to communicate with each other making remote network calls look and behave exactly like local Java method calls.

##  Architecture

![](/docs/diagrams/architecture.png)

The system consists of three main components:
1. **RPC Consumer (e.g., User Service)**: The client that wants to call a method.
2. **RPC Producer (e.g., Booking Service)**: The client that owns and executes the method.
3. **Relay Server (Message Router)**: A central Netty TCP server that maintains active channels with all clients and routes messages between them. It executes no business logic.

### The Request Lifecycle (End-to-End)
When the User Service calls `bookingService.getBookingDetails(42)`:
1. **Interception**: A Dynamic Proxy intercepts the local method call.
2. **Serialization**: The framework packages the method details (Name, Parameters, Types) into an `RpcRequest` object and serializes it into a byte array.
3. **Transport (Outbound)**: The Netty client sends the bytes over a TCP connection to the Relay Server.
4. **Routing**: The Relay Server decodes the message, looks up the active channel for the Booking Service, and forwards the bytes.
5. **Execution (Inbound)**: The Booking Service decodes the bytes back into an `RpcRequest`. Using **Java Reflection**, it maps the requested Method ID to the actual Java method and runs it.
6. **Response**: The result is serialized into an `RpcResponse` and routed back through the Relay Server to the waiting User Service thread.

---

##  Core Technologies
*   **Networking**: Netty (Asynchronous, event-driven network application framework).
*   **Serialization**: Kryo (High-speed binary serialization) and JSON (Fastjson/Jackson).
*   **Dependency Injection**: Spring Boot.
*   **Language Features**: Java Reflection, Dynamic Proxies, CompletableFuture.

---

##  Key Technical Concepts & Interview Talking Points

### 1. How do we make a remote call look local? (Dynamic Proxies)
*   **Concept**: We use `java.lang.reflect.Proxy` combined with Spring's `FactoryBean`. 
*   **How it works**: The consumer only has an interface (e.g., `BookingService`). Spring injects a **Proxy** object instead of a real implementation. When the consumer calls a method on this proxy, the `InvocationHandler` intercepts the call, packages the method name and arguments, and sends them over the network.

### 2. The Netty Pipeline & Handling TCP (Encoders/Decoders)

![](/docs/diagrams/needofprotocol.png)
![](/docs/diagrams/pipeline.png)
![](/docs/diagrams/encoding.png)

*   **Concept**: TCP is a stream-based protocol, meaning messages can be fragmented or bundled together. Netty only understands `ByteBuf` (raw bytes).
*   **The Pipeline**: A doubly-linked list of handlers. Data goes through Inbound Handlers when receiving, and Outbound Handlers when sending.
*   **Solving Fragmentation (Length-Prefix Framing)**: Our custom Encoders/Decoders prepend a 4-byte integer (the body length) to every message. The Decoder (`ByteToMessageDecoder`) checks if enough bytes have arrived before attempting to reconstruct the Java object.

### 3. How does the server know which method to run? (Java Reflection)
*   **Concept**: Over the network, the producer just receives strings (Method name, types). It must convert this into a real method execution.
*   **The Solution**: We generate a unique **Method ID** (e.g., `getBooking.1.int.String`) to handle method overloading (`RpcMethodDescriptor`). The framework caches these during startup. When a request arrives, it uses the Method ID to fetch the `java.lang.reflect.Method` object and dynamically runs it using `method.invoke(object, args)`.

### 4. How do we wait for the response? (Async to Sync Bridging)
*   **Concept**: Netty is completely asynchronous. When the proxy sends the network request, the network I/O happens on a separate thread, but the consumer's thread needs a synchronous return value.
*   **The Solution**: We use a `ConcurrentHashMap<String, CompletableFuture>` mapping Request IDs to Futures. 
    *   The caller thread creates a `CompletableFuture`, stores it in the map, and calls `future.get(5, SECONDS)` (blocking itself).
    *   When the Netty I/O thread receives the `RESPONSE`, it looks up the Request ID in the map, completes the future with the result, and wakes up the caller thread.

### 5. Managing Spring Integration (Auto Scanning)
*   **Concept**: We don't want developers writing boilerplate setup code.
*   **The Solution**: We use custom annotations (`@EnableRpcClient`, `@AutoRemoteInjection`). Behind the scenes, we use Spring's `ImportBeanDefinitionRegistrar` and `ClassPathBeanDefinitionScanner` to dynamically scan for these annotations, generate proxies, and register them in the Spring Application Context before the app even fully starts.

---

##  Fault Tolerance & Reliability

1. **Heartbeats (Keep-Alive)**
   *   **Problem**: Dead TCP connections might look open to the OS but actually be dropped by a firewall or router.
   *   **Solution**: We use Netty's `IdleStateHandler`. If the client is idle (no writes) for 5 seconds, it sends a `HEART_BEAT` message. If the server receives nothing (no reads) for a set period, it assumes the client crashed and cleans up its channel.
2. **Fallbacks (Graceful Degradation)**
   *   If the remote server is down or a timeout occurs (the `CompletableFuture.get()` throws a TimeoutException), the framework automatically routes the call to a local **Fallback Class** to return default data instead of crashing the application.

---


