package com.harsh.rpc.model;

import java.lang.reflect.Field;
import java.util.Objects;

/*


Just like RpcMethodDescriptor holds metadata about a Method, 
the RemoteServiceFieldHolder holds metadata about a Field (a variable inside a class).*/
public class RemoteServiceFieldHolder {
    /*
     * This is a Java Reflection Field object. (Just like a Method object represents
     * a method, a Field object represents a variable.
     * It lets the framework interact with the variable dynamically).
     */

    private Field remoteServiceField;
    private String requestClientId;
    private String fieldClassName;
    private String alias;
    private Class<?> fallbackClass;

    public RemoteServiceFieldHolder(Field remoteServiceField, String requestClientId) {
        this.remoteServiceField = remoteServiceField;
        this.requestClientId = requestClientId;
        this.fieldClassName = remoteServiceField.getType().getName();
        this.alias = remoteServiceField.getName();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fieldClassName);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof RemoteServiceFieldHolder holder)) {
            return false;
        }

        return this.fieldClassName.equals(holder.fieldClassName);
    }

    public Class<?> getFallbackClass() {
        return fallbackClass;
    }

    public void setFallbackClass(Class<?> fallbackClass) {
        this.fallbackClass = fallbackClass;
    }

    public Field getRemoteServiceField() {
        return remoteServiceField;
    }

    public void setRemoteServiceField(Field remoteServiceField) {
        this.remoteServiceField = remoteServiceField;
    }

    public String getRequestClientId() {
        return requestClientId;
    }

    public void setRequestClientId(String requestClientId) {
        this.requestClientId = requestClientId;
    }

    public String getFieldClassName() {
        return fieldClassName;
    }

    public void setFieldClassName(String fieldClassName) {
        this.fieldClassName = fieldClassName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
