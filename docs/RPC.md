
## What is RPC framework?
 - Remote Procedure Call is a way for programs to communicate allowing you to call a method on another server as if it were a local method. You don't need to care about the network details, the RPC framework handles packaging the call, sending the request and decoding the response.

For example:
You write `userService.getUserById()` — but that method actually runs on another server.
Still, you call it just like any local method.
That’s the power of an RPC framework — it hides the complexity of remote communication and makes distributed systems feel local.

##  Popular RPC Frameworks

| Language | Framework | Description |
| :--- | :--- | :--- |
| Java | Dubbo | Open-source high-performance RPC by Alibaba |
| Java | gRPC | Google’s multi-language RPC over HTTP/2 |
| Java | Motan | Open-source RPC from Sina Weibo |
| JavaScript | tRPC | TypeScript-friendly RPC for frontend developers |
| Python | Thrift | Cross-language RPC framework |

##  RPC vs. RESTful API — What's the Difference?

| Comparison | RPC | REST API |
| :--- | :--- | :--- |
| Call Style | Method call | HTTP URL-based call |
| Performance | Faster (supports binary protocols) | Slower (text-based) |
| Flexibility | High (but requires contract) | More open and standardized |
| Use Case | Internal service calls, low-latency | External APIs, frontend/backend communication |
