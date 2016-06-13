package org.javaee7.wildfly.samples.everest;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.javaee7.wildfly.samples.services.ConsulServices;
import org.javaee7.wildfly.samples.services.ConsulServices;
import org.javaee7.wildfly.samples.services.discovery.ServiceDiscovery;

/**
 * @author arungupta
 */
@ApplicationScoped
public class Resources {

    @Inject @ConsulServices
    ServiceDiscovery services;

    @Produces
    public ServiceDiscovery getService() {
        return services;
    }
}
