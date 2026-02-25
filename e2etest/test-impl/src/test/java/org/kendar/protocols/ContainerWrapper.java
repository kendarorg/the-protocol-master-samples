package org.kendar.protocols;

import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

import java.util.Optional;
import java.util.function.Consumer;

public class ContainerWrapper {
    public ComposeContainer container;
    public DockerComposeContainer dockerComposeContainer;

    public ContainerWrapper(ComposeContainer container) {
        this.container = container;
    }
    public ContainerWrapper(DockerComposeContainer dockerComposeContainer) {
        this.dockerComposeContainer = dockerComposeContainer;
    }

    public void stop() {
        if(container!=null) container.stop();
        else dockerComposeContainer.stop();
    }

    public void withLogConsumer(String key, Consumer<OutputFrame> consumer) {
        if(container!=null) container.withLogConsumer(key,consumer);
        else dockerComposeContainer.withLogConsumer(key,consumer);
    }

    public void start() {
        if(container!=null) container.start();
        else dockerComposeContainer.start();
    }

    public Optional<ContainerState> getContainerByServiceName(String s) {
        if(container!=null) return container.getContainerByServiceName(s);
        return dockerComposeContainer.getContainerByServiceName(s);
    }

    public String getServiceHost(String tpmHost, int i) {
        if(container!=null) return container.getServiceHost(tpmHost,i);
        return dockerComposeContainer.getServiceHost(tpmHost,i);
    }

    public int getServicePort(String tpmHost, int i) {
        if(container!=null) return container.getServicePort(tpmHost,i);
        return dockerComposeContainer.getServicePort(tpmHost,i);
    }

    public void withExposedService(String host, int ports, WaitStrategy waitStrategy) {
        if(container!=null) container.withExposedService(host,ports,waitStrategy);
        else dockerComposeContainer.withExposedService(host,ports,waitStrategy);
    }
}
