package org.javaee7.wildfly.samples.everest.checkout;

import java.io.Serializable;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonObject;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.ribbon.ClientOptions;
import com.netflix.ribbon.Ribbon;
import com.netflix.ribbon.http.HttpRequestTemplate;
import com.netflix.ribbon.http.HttpResourceGroup;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.javaee7.wildfly.samples.everest.cart.Cart;
import org.javaee7.wildfly.samples.everest.cart.CartItem;
import org.javaee7.wildfly.samples.services.discovery.ServiceDiscovery;
import rx.Observable;
import rx.observables.BlockingObservable;

/**
 * @author arungupta
 */
@Named
@SessionScoped
public class OrderBean implements Serializable {

    @Inject
    Order order;

    @Inject
    Cart cart;

    String status;

    @Inject ServiceDiscovery services;

    public void saveOrder() {

        List<CartItem> cartItems = cart.getItems();
        cartItems.stream().map((cartItem) -> {
            OrderItem orderItem = new OrderItem();
            orderItem.itemId = cartItem.getItemId();
            orderItem.itemCount = cartItem.getItemCount();
            return orderItem;
        }).forEach((orderItem) -> {
            order.getOrderItems().add(orderItem);
        });

        try {
            // the request context thread locals
            HystrixRequestContext context = HystrixRequestContext.initializeContext();

            try {
                HttpResourceGroup httpResourceGroup = Ribbon.createHttpResourceGroup(
                        "order", // the name of the service in the registry
                        ClientOptions.create().withMaxAutoRetriesNextServer(3)
                );

                HttpRequestTemplate<ByteBuf> template = httpResourceGroup.newTemplateBuilder("submitOrder", ByteBuf.class)
                        .withMethod("POST")
                        .withUriTemplate("/order/resources/order")
                        /*.withFallbackProvider(new RecommendationServiceFallbackHandler())*/
                        .build();

                BlockingObservable<ByteBuf> obs = template.requestBuilder()
                        .withHeader("Content-Type", "application/json")
                        .withContent(Observable.just(Unpooled.wrappedBuffer(order.asJsonString().getBytes())))
                        .build()
                        .observe().toBlocking();

                String response = new String(obs.last().toString(Charset.forName("UTF-8")));
                JsonObject jsonResponse = Json.createReader(new StringReader(response)).readObject();

                status = "Order successful, order number: " + jsonResponse.get("orderId");

                cart.clearCart();

            } finally {
                context.shutdown();
            }

        } catch (Exception e) {
            e.printStackTrace();
            status = e.getLocalizedMessage();
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
