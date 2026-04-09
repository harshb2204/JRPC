## Architecture


![](/docs/diagrams/architecture.png)
- Lets imagine user which is called the consumer initiates a remote method call to booking using netty for network transport and custom encoder and decoders for message
  serialization.
- Client triggers a remote call. Client user which is the RPC consumer and and client booking which is the RPC producer implements and serves remote methods. The method in the architecture is marked as RPC method so other services can call it.
- Server which is the central message router and hosts active netty channels for all clients. So when you start your client user there is a netty client which is regitered with the server forming a channel. So they generate a netty channel that can communicate with the server. So every client after it gets started a netty client is started and registered with the server. The server maintains all the channel information.
- The message encoder converts the java object to byte array before sending out. The message decoder converts the byte array to java object after receiving it.The reason we need to have this serialization and deserialization is because in the netty network they use only byte streams.
- So what they transport is the byte array itself. The RPC request is a Java object which contains the metadata about the method call like the method name, parameters, etc.
- Server analyzes the information and forwards this request to the correct service and when the booking gets this request and processes it
- User client triggers a remote call, so the framework captures this call through a dynamic proxy and inside they are sending a rpc req.



### The Pipeline

![](/docs/diagrams/pipeline.png)
- The pipeline is a doubly linked list.
- It has addLast and addFirst methods which accept ChannelHandler and there are ChannelInboundHandler and ChannelOutboundHandler
- The pipeline belongs to the server. When netty client sends the message to the server it goes through the pipeline.
- The message is processed by the handlers in the pipeline.
- Inbound handlers are for incoming data events and outbound handlers are for outgoing data events.


### The Encoders and Decoders

![](/docs/diagrams/needofprotocol.png)
![](/docs/diagrams/encoding.png)


