package com.harsh.rpc.client;

import com.harsh.rpc.config.AutoRemoteInjection;
import com.harsh.rpc.config.MarkAsRpc;
import com.harsh.rpc.config.RemoteService;
import com.harsh.rpc.model.MessagePayload;
import com.harsh.rpc.model.MessageType;
import com.harsh.rpc.model.RpcMethodDescriptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

// Main Task: processRequest
public abstract class RemoteClientTemplate implements RemoteClient, SmartInitializingSingleton, ApplicationContextAware {

    private String clientId;

    private String[] scanPackages;

    private ApplicationContext applicationContext;

    // Key = full class name of the RPC implementation // Map (key: method Id, value = RpcMethodDescriptor)
    private Map<String, Map<String, RpcMethodDescriptor>> methodsHashMap = new HashMap<>();

    private Map<String, Method> classMethodsMap = new HashMap<>();

    private Map<String, CompletableFuture<MessagePayload.RpcResponse>> requestMap = new ConcurrentHashMap<>();

    @Override
    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String[] getScanPackages() {
        return scanPackages;
    }

    public void setScanPackages(String[] scanPackages) {
        this.scanPackages = scanPackages;
    }

    @Override
    public void sendRequest(Object message, String requestId, CompletableFuture<MessagePayload.RpcResponse> future) {
        requestMap.put(requestId, future);
    }

    public void completeRequest(MessagePayload.RpcResponse rpcResponse) {
        String requestId = rpcResponse.getRequestId();
        CompletableFuture<MessagePayload.RpcResponse> future = requestMap.get(requestId);
        future.complete(rpcResponse);
    }

    // Process the RPC request from the consumer
    public void processRequest(MessagePayload messagePayload) {
        MessagePayload.RpcRequest rpcRequest = (MessagePayload.RpcRequest) messagePayload.getPayload();

        // Rpc interface full class name
        String requestClassName = rpcRequest.getRequestClassName();

        String methodName = rpcRequest.getRequestMethodSimpleName();
        String[] paramsTypes = rpcRequest.getParamsTypes();
        Object[] params = rpcRequest.getParams();
        String returnValueType = rpcRequest.getReturnValueType();

        // Generate Method Id unique
        String methodId = RpcMethodDescriptor.generateMethodId(methodName, paramsTypes.length, paramsTypes, returnValueType);

        // Find out the request class
        // In the RPC request call, the message carries the RPC interface (BookingDetailService)
        // Using the interface to find any implementation (Find BookingDetailServiceImpl)
        Class<?> requestClass = null;
        try {
            requestClass = Class.forName(requestClassName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // We need to use this Class object to find out the implementation.
        Map<String, ?> beansOfType = applicationContext.getBeansOfType(requestClass);

        if (beansOfType.isEmpty()) {
            throw new RuntimeException("Class of implementation is not found");
        }

        // Find two implementations
        if (beansOfType.size() > 1) {
            throw new RuntimeException("Multiple implementations of request class found");
        }

        Object requestedClassBean = beansOfType.values().iterator().next();

        String fullRequestClassBeanName = requestedClassBean.getClass().getName();

        // Key: Method ID
        Map<String, RpcMethodDescriptor> methodDescriptorMap = methodsHashMap.get(fullRequestClassBeanName);

        RpcMethodDescriptor rpcMethodDescriptor = methodDescriptorMap.get(methodId);

        if (rpcMethodDescriptor == null) {
            throw new RuntimeException("No RPC method supported");
        }

        if (rpcMethodDescriptor.getMethodName().equals(rpcRequest.getRequestMethodSimpleName())
                && rpcMethodDescriptor.getNumOfParams() == paramsTypes.length) {
            Method method = classMethodsMap.get(methodId);

            try {
                Object result = method.invoke(requestedClassBean, params);

                MessagePayload responseMessage = new MessagePayload.RequestMessageBuilder()
                        .setRequestId(rpcRequest.getRequestId())
                        .setMessageType(MessageType.RESPONSE)
                        .setReturnValue(result).build();

                sendResponse(responseMessage);

            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void didCatchResponse(MessagePayload.RpcResponse response) {
        requestMap.remove(response.getRequestId());
    }

    // RPC method scanning process
    @Override
    public void afterSingletonsInstantiated() {
        System.out.println("afterSingletonsInstantiated started");

        List<Class<?>> classList = new ArrayList<>();
        for (String scanPackage : scanPackages) {
            scanPackage = scanPackage.replaceAll("\\.", "/");
            ClassPathScanningCandidateComponentProvider scanProvider = new ClassPathScanningCandidateComponentProvider(false);
            scanProvider.addIncludeFilter(new AssignableTypeFilter(Object.class));
            Set<BeanDefinition> candidateComponents = scanProvider.findCandidateComponents(scanPackage);

            for (BeanDefinition beanDefinition : candidateComponents) {
                try {
                    Class<?> aClass = Class.forName(beanDefinition.getBeanClassName());

                    if (RemoteService.class.isAssignableFrom(aClass)) {
                        classList.add(aClass);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        for (Class<?> clazz : classList) {
            Method[] declaredMethods = clazz.getDeclaredMethods();

            for (Method declaredMethod : declaredMethods) {
                if (declaredMethod.isAnnotationPresent(MarkAsRpc.class)) {
                    RpcMethodDescriptor md = RpcMethodDescriptor.build(declaredMethod);

                    methodsHashMap.computeIfAbsent(clazz.getName(), k -> new HashMap<>()).put(md.getMethodId(), md);

                    classMethodsMap.put(md.getMethodId(), declaredMethod);
                }
            }
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
