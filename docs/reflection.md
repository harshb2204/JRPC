# Java Reflection — Complete Guide

## What is Reflection?

Normally in Java, you write code that knows exactly what it's working with at compile time:

```java
BookingDetailServiceImpl service = new BookingDetailServiceImpl();
service.getBookingDetailsByUserId(42);
```

The compiler knows the class. It knows the method. It checks everything. If you make a typo,
it tells you immediately.

**Reflection is the opposite.** It lets you inspect and interact with classes, methods,
and fields at **runtime** — when you don't know (or don't want to hardcode) what class
or method you're dealing with at compile time.

```java
// with reflection — you don't know the class or method at compile time
Class<?> clazz = Class.forName("org.harsh.rpcbooking.booking.service.BookingDetailServiceImpl");
Method method = clazz.getMethod("getBookingDetailsByUserId", int.class);
Object result = method.invoke(serviceInstance, 42);
```

Both lines do the same thing. But the second one can work with ANY class and ANY method,
as long as you know the names as strings at runtime.

---

## Why Does Reflection Exist?

### The core problem it solves

Imagine you're writing a framework (like Spring, or this RPC framework). You don't know,
at the time you write the framework, what classes the users will create. You can't write:

```java
// framework code — impossible to write this
BookingDetailServiceImpl service = ...; // what class is this? we don't know yet
service.someMethod();                   // what method? we don't know yet
```

The framework is written once. The user's classes are written later, independently.
Reflection is the bridge — it lets framework code operate on user code it has never seen.

### Real examples of where reflection is used

| Framework / Tool | What it does with reflection |
|---|---|
| **Spring** | Finds classes with `@Component`, creates instances, injects `@Autowired` fields |
| **JUnit** | Finds methods with `@Test`, calls them without knowing they exist at compile time |
| **Jackson / Gson** | Reads field names and values from any object to convert to JSON |
| **Hibernate / JPA** | Maps Java fields to database columns |
| **This RPC framework** | Finds `@MarkAsRpc` methods, stores them, calls them when a remote request arrives |

---

## The Four Things Reflection Can Do

### 1. Inspect classes

```java
Class<?> clazz = Class.forName("com.harsh.common.rpc.booking.BookingDetailService");

clazz.getName()           // "com.harsh.common.rpc.booking.BookingDetailService"
clazz.getSimpleName()     // "BookingDetailService"
clazz.getPackage()        // package com.harsh.common.rpc.booking
clazz.getInterfaces()     // [RemoteService.class]
clazz.isInterface()       // true
clazz.getDeclaredMethods() // all methods declared in this class
```

### 2. Inspect methods

```java
Method method = clazz.getMethod("getBookingDetailsByUserId", int.class);

method.getName()           // "getBookingDetailsByUserId"
method.getParameterCount() // 1
method.getParameterTypes() // [int.class]
method.getReturnType()     // String.class
method.isAnnotationPresent(MarkAsRpc.class) // true or false
```

### 3. Inspect fields

```java
Field field = clazz.getDeclaredField("bookingDetailService");

field.getName()   // "bookingDetailService"
field.getType()   // BookingDetailService.class
field.get(object) // the current value of this field on the given object
field.set(object, value) // set the field's value (even if private)
```

### 4. Create instances and call methods dynamically

```java
// create instance
Object instance = clazz.getDeclaredConstructor().newInstance();

// call method
Method method = clazz.getMethod("getBookingDetailsByUserId", int.class);
Object result = method.invoke(instance, 42);
// result is "Booking Service is called, and the user id is: 42"
```

---

## The `Class<?>` Object — The Entry Point

Every type in Java has a corresponding `Class` object. It's the metadata representation
of that type. You can get it three ways:

```java
// Way 1: from a class name string (used in this project)
Class<?> c = Class.forName("com.harsh.common.rpc.booking.BookingDetailService");

// Way 2: from a type literal (compile time)
Class<?> c = BookingDetailService.class;

// Way 3: from an existing instance
BookingDetailServiceImpl impl = new BookingDetailServiceImpl();
Class<?> c = impl.getClass();
```

All three give you the same `Class` object. The first one is what reflection is really
about — you only know the class name as a string, not as a compile-time type.

---

## `getMethod` vs `getDeclaredMethod` — Important Difference

| Method | What it returns |
|---|---|
| `getMethod(name, params)` | Public methods including inherited ones |
| `getDeclaredMethod(name, params)` | All methods (including private) declared in THIS class only, not inherited |
| `getMethods()` | All public methods including inherited |
| `getDeclaredMethods()` | All methods declared in THIS class only (any visibility) |

In this project, `RemoteClientTemplate` uses `getDeclaredMethods()` to scan the
implementation class for `@MarkAsRpc`:

```java
Method[] declaredMethods = clazz.getDeclaredMethods();
```

This gets all methods declared in that class — including private ones, but NOT inherited ones.

---

## `method.invoke()` — Calling a Method Dynamically

This is the single most important reflection operation in this project.

```java
Object result = method.invoke(targetObject, arg1, arg2, ...);
```

| Part | What it means |
|---|---|
| `method` | The `Method` object (retrieved earlier via reflection) |
| `targetObject` | The instance to call the method on (like `this` in normal code) |
| `arg1, arg2...` | The arguments to pass (varargs) |
| `result` | The return value, typed as `Object` |

### Normal call vs reflection call — side by side

```java
// Normal (compile time)
BookingDetailServiceImpl impl = new BookingDetailServiceImpl();
String result = impl.getBookingDetailsByUserId(42);

// Reflection (runtime)
Object impl = applicationContext.getBean(BookingDetailService.class);
Method method = impl.getClass().getDeclaredMethod("getBookingDetailsByUserId", int.class);
Object result = method.invoke(impl, 42);
```

Both do exactly the same thing. The reflection version just doesn't need to know the class
or method name at compile time.

### What can go wrong with `invoke()`

| Exception | When it happens |
|---|---|
| `IllegalAccessException` | Calling a private method without `setAccessible(true)` |
| `InvocationTargetException` | The called method itself threw an exception — wrapped here |
| `IllegalArgumentException` | Wrong number or type of arguments |
| `NullPointerException` | `targetObject` is null for a non-static method |

In `RemoteClientTemplate`:

```java
try {
    Object result = method.invoke(requestedClassBean, params);
} catch (IllegalAccessException | InvocationTargetException e) {
    throw new RuntimeException(e);
}
```

Both checked exceptions are caught and wrapped in a `RuntimeException`.

---

## `isAnnotationPresent()` — Reading Annotations at Runtime

This is the other critical reflection operation in this project.

```java
if (declaredMethod.isAnnotationPresent(MarkAsRpc.class)) {
    // this method has @MarkAsRpc on it
}
```

This only works because `@MarkAsRpc` has `@Retention(RetentionPolicy.RUNTIME)`.
Without that, this would always return `false`.

You can also read the annotation's values:
```java
AutoRemoteInjection annotation = field.getAnnotation(AutoRemoteInjection.class);
String clientId = annotation.requestClientId(); // "harsh-booking"
Class<?> fallback = annotation.fallbackClass();  // MyUserBookingDetailService.class
```

---

## `isAssignableFrom()` — Checking Type Relationships

Used in `RemoteClientTemplate` to filter classes:

```java
if (RemoteService.class.isAssignableFrom(aClass)) {
    classList.add(aClass);
}
```

Read it as: "can `aClass` be assigned to a variable of type `RemoteService`?"

```java
RemoteService.class.isAssignableFrom(BookingDetailService.class)  // true
RemoteService.class.isAssignableFrom(String.class)                // false
RemoteService.class.isAssignableFrom(RemoteService.class)         // true (itself)
```

It checks the full inheritance chain — not just direct parents.
This is the reflection equivalent of `instanceof`, but for `Class` objects:

```java
// instance check (you have an object)
object instanceof RemoteService

// class check (you have a Class object, not an instance)
RemoteService.class.isAssignableFrom(someClass)
```

---

## How This Project Uses Reflection — The Full Picture

Here is every place reflection is used in this codebase and exactly why:

### 1. Scanning for RPC methods (`RemoteClientTemplate.afterSingletonsInstantiated`)

```java
// Step 1: load class by name
Class<?> aClass = Class.forName(beanDefinition.getBeanClassName());

// Step 2: check if it's a RemoteService
if (RemoteService.class.isAssignableFrom(aClass)) {

    // Step 3: get all methods it declares
    Method[] declaredMethods = clazz.getDeclaredMethods();

    for (Method declaredMethod : declaredMethods) {

        // Step 4: check if method has @MarkAsRpc
        if (declaredMethod.isAnnotationPresent(MarkAsRpc.class)) {

            // Step 5: store the method metadata and the Method object itself
            RpcMethodDescriptor md = RpcMethodDescriptor.build(declaredMethod);
            methodsHashMap.put(clazz.getName(), md);
            classMethodsMap.put(md.getMethodId(), declaredMethod);
        }
    }
}
```

**Why reflection here?** The framework doesn't know at compile time that
`BookingDetailServiceImpl` exists. The user creates it independently. Reflection
lets the framework discover it at startup by scanning the classpath.

---

### 2. Dispatching the RPC call (`RemoteClientTemplate.processRequest`)

```java
// Step 1: load the interface class from the name that arrived in the network message
Class<?> requestClass = Class.forName(requestClassName);
// requestClassName = "com.harsh.common.rpc.booking.BookingDetailService"

// Step 2: find the Spring bean that implements it
Map<String, ?> beansOfType = applicationContext.getBeansOfType(requestClass);
Object requestedClassBean = beansOfType.values().iterator().next();
// → BookingDetailServiceImpl instance

// Step 3: look up the pre-stored Method object using the method ID
Method method = classMethodsMap.get(methodId);

// Step 4: call it with the params that arrived from the network
Object result = method.invoke(requestedClassBean, params);
// → "Booking Service is called, and the user id is: 42"
```

**Why reflection here?** The relay server forwarded a message containing
`"com.harsh.common.rpc.booking.BookingDetailService"` and
`"getBookingDetailsByUserId"` as strings. The only way to turn strings
into actual method calls is through reflection.

---

### 3. Building method metadata (`RpcMethodDescriptor.build`)

```java
Method method = ...; // the @MarkAsRpc method

method.getName()                // "getBookingDetailsByUserId"
method.getParameterCount()      // 1
method.getParameterTypes()      // [int.class]
method.getReturnType()          // String.class
method.getReturnType().getSimpleName() // "String"
```

**Why reflection here?** To extract method metadata to build the unique method ID
and store it. Done once at startup, reused on every RPC call.

---

### 4. Building the RPC proxy (`RemoteServiceFactoryBean.getObject`)

```java
Proxy.newProxyInstance(
    Thread.currentThread().getContextClassLoader(),
    new Class[]{ rpcInterfaceClass },
    new InvocationHandler() {
        public Object invoke(Object proxy, Method method, Object[] args) {
            // method.getName() → "getBookingDetailsByUserId"
            // method.getParameterTypes() → [int.class]
            // method.getReturnType() → String.class
            // args → [42]
        }
    }
);
```

**Why reflection here?** `Proxy.newProxyInstance` is pure reflection. It creates
a fake implementation of an interface at runtime. The `InvocationHandler` receives
every method call as a `Method` object plus its arguments — the definition of reflection.

---

## The Performance Question

Reflection is slower than direct calls. `method.invoke()` is roughly 10-50x slower
than a direct method call in isolation.

**But in this project, it doesn't matter.** Why?

1. **Scanning happens once** — `afterSingletonsInstantiated` runs once at startup.
   The cost is paid once, not per request.

2. **The `Method` object is cached** — the expensive part of reflection is finding
   the method (`getDeclaredMethod`). This project stores the `Method` object in
   `classMethodsMap` after finding it. On every request, it just does `map.get(methodId)`
   to retrieve it — O(1) lookup, no re-scanning.

3. **The network is the bottleneck** — an RPC call involves TCP, serialization, and
   at least two network round-trips. That takes milliseconds. `method.invoke()` takes
   microseconds. The reflection cost is invisible next to the network cost.

---

## Summary

| Question | Answer |
|---|---|
| What is reflection? | Inspecting and calling Java code using runtime strings instead of compile-time types |
| Why does it exist? | Frameworks need to operate on user code they've never seen |
| Entry point | `Class.forName(name)` or `SomeClass.class` or `object.getClass()` |
| Key operations | `getDeclaredMethods()`, `isAnnotationPresent()`, `method.invoke()`, `isAssignableFrom()` |
| Key requirement | `@Retention(RUNTIME)` on any annotation you want to read via reflection |
| Where used in this project | Scanning, dispatching RPC calls, building method IDs, creating proxies |
| Performance | Slower than direct calls, but acceptable because scanning is cached and network dominates |
