package com.unicomai.wanwu.api.common;

import java.io.Serializable;

public class ServiceDescriptor implements Serializable {

    private static final long serialVersionUID = 1L;

    private String serviceCode;
    private String serviceName;
    private String module;
    private String status;
    private String rpcFramework;
    private String javaBaseline;
    private String version;

    public static ServiceDescriptor of(String serviceCode, String serviceName, String module) {
        ServiceDescriptor descriptor = new ServiceDescriptor();
        descriptor.setServiceCode(serviceCode);
        descriptor.setServiceName(serviceName);
        descriptor.setModule(module);
        descriptor.setStatus("READY");
        descriptor.setRpcFramework("Apache Dubbo");
        descriptor.setJavaBaseline("Java 8");
        descriptor.setVersion("0.1.0-SNAPSHOT");
        return descriptor;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRpcFramework() {
        return rpcFramework;
    }

    public void setRpcFramework(String rpcFramework) {
        this.rpcFramework = rpcFramework;
    }

    public String getJavaBaseline() {
        return javaBaseline;
    }

    public void setJavaBaseline(String javaBaseline) {
        this.javaBaseline = javaBaseline;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
