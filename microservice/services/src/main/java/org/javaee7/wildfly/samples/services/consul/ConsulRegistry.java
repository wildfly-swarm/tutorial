package org.javaee7.wildfly.samples.services.consul;

import java.net.URL;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.NewService;
import com.ecwid.consul.v1.agent.model.Service;
import org.javaee7.wildfly.samples.services.ConsulServices;
import org.javaee7.wildfly.samples.services.registration.ServiceRegistry;

/**
 * @author Heiko Braun
 * @since 10/05/16
 */

@ConsulServices
@ApplicationScoped
public class ConsulRegistry implements ServiceRegistry {

    @Override
    public void registerService(String name, String uri) {
        try {

            URL url = new URL(uri);

            ConsulClient client = getConsulClient();

            NewService newService = new NewService();
            newService.setId(serviceId(name, url.getHost(), url.getPort()));
            newService.setName(name);
            newService.setAddress(url.getHost());
            newService.setPort(url.getPort());

            NewService.Check serviceCheck = new NewService.Check();
            serviceCheck.setHttp(uri);
            serviceCheck.setInterval("30s");
            newService.setCheck(serviceCheck);

            client.agentServiceRegister(newService);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ConsulClient getConsulClient() {
        String consulHost = System.getProperty("consul.host", "192.168.99.100"); // DOCKER
        ConsulClient client = new ConsulClient(consulHost);
        return client;
    }


    private String serviceId(String name, String address, int port) {
        return name + ":" + address + ":" + port;
    }

    @Override
    public void unregisterService(String name, String uri) {

    }

    @Override
    public String discoverServiceURI(String name) {

        ConsulClient client = getConsulClient();
        Map<String, Service> agentServices = client.getAgentServices().getValue();

        Service match = null;

        for (Map.Entry<String, Service> entry : agentServices.entrySet()) {
            if(entry.getValue().getService().equals(name)) {
                match = entry.getValue();
                break;
            }
        }

        if(null==match)
            throw new RuntimeException("Service '"+name+"' cannot be found!");


        try {
            URL url = new URL("http://"+match.getAddress()+":"+match.getPort());
            return url.toExternalForm();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
