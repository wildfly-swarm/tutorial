package org.javaee7.wildfly.samples.everest.checkout;

import java.io.StringReader;
import java.net.URI;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import org.apache.http.util.EntityUtils;
import org.javaee7.wildfly.samples.services.discovery.ServiceDiscovery;

/**
 * @author Heiko Braun
 * @since 31/05/16
 */
public class OrderCommand extends HystrixCommand<OrderCommand.Result> {

    private final ServiceDiscovery services;

    private final Entity<String> order;

    public OrderCommand(ServiceDiscovery services, Entity<String> orderJson) {
        super(HystrixCommandGroupKey.Factory.asKey(OrderCommand.class.getName()));
        this.services = services;
        this.order = orderJson;
    }

    @Override
    protected Result run() throws Exception {

        // client cannot be be used in multi-threaded env's
        WebTarget target = ClientBuilder
                .newClient()
                .target(
                        UriBuilder.fromUri(services.getOrderServiceURI())
                                .path("/order/resources/order")
                                .build()
                );

        Response response = target.request()
                    .header("Host", "order") // l5d proxy
                    .post(order);

        Response.StatusType statusInfo = response.getStatusInfo();

        if (statusInfo.getFamily() == Response.Status.Family.SUCCESSFUL) {
            JsonObject jsonResponse = Json.createReader(new StringReader(response.readEntity(String.class))).readObject();
            return new Result("Order successful, order number: " + jsonResponse.get("orderId"), true);
        } else {
            throw new RuntimeException(statusInfo.getReasonPhrase());
        }

    }


    @Override
    protected Result getFallback() {
        return new Result("Order processing failed. Please try again ...", false);
    }

    public class Result {
        String status;
        boolean successful;

        public Result(String status, boolean successful) {
            this.status = status;
            this.successful = successful;
        }

        public String getStatus() {
            return status;
        }

        public boolean isSuccessful() {
            return successful;
        }
    }
}
