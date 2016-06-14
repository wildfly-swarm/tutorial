package org.javaee7.wildfly.samples.everest.catalog;

import java.io.Serializable;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.netflix.hystrix.HystrixInvokableInfo;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.ribbon.ClientOptions;
import com.netflix.ribbon.Ribbon;
import com.netflix.ribbon.http.HttpRequestTemplate;
import com.netflix.ribbon.http.HttpResourceGroup;
import com.netflix.ribbon.hystrix.FallbackHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.javaee7.wildfly.samples.everest.checkout.OrderItem;
import org.javaee7.wildfly.samples.services.discovery.ServiceDiscovery;
import rx.Observable;
import rx.observables.BlockingObservable;

/**
 * @author arungupta
 */
@Named
@SessionScoped
public class CatalogBean implements Serializable {
    @Inject CatalogItem catalogItem;
    
    @Inject ServiceDiscovery services;

    String status;
    
    public void addCatalog() {
        Response response = services.getCatalogService().request().post(Entity.xml(catalogItem));

        Response.StatusType statusInfo = response.getStatusInfo();
        
        if (statusInfo.getStatusCode() == Response.Status.CREATED.getStatusCode())
            status = "User added successfully";
        else
            status = statusInfo.getReasonPhrase();
    }
    
    public List<CatalogItem> getAllItems() {

        List<CatalogItem> result = Collections.EMPTY_LIST;

        try {
            // the request context thread locals
            HystrixRequestContext context = HystrixRequestContext.initializeContext();

            try {
                HttpResourceGroup httpResourceGroup = Ribbon.createHttpResourceGroup(
                        "catalog", // the name of the service in the registry
                        ClientOptions.create().withMaxAutoRetriesNextServer(3)
                );

                HttpRequestTemplate<ByteBuf> template = httpResourceGroup.newTemplateBuilder("loadCatalog", ByteBuf.class)
                        .withMethod("GET")
                        .withUriTemplate("/catalog/resources/catalog")
                        .withFallbackProvider(new FallbackHandler() {
                            @Override
                            public Observable getFallback(HystrixInvokableInfo hystrixInvokableInfo, Map map) {
                                System.out.println("<< Serving fallback result list >>");
                                return Observable.just(cachedResults);
                            }
                        })
                        .build();

                BlockingObservable<ByteBuf> obs = template.requestBuilder()
                        .withHeader("Content-Type", "text/xml")
                        .build()
                        .observe().toBlocking();

                ByteBuf responseBuffer = obs.last().copy().retain();
                if(responseBuffer.capacity()>0) {
                    String payload = responseBuffer.toString(Charset.forName("UTF-8"));
                    cachedResults = responseBuffer;

                    JAXBContext jc = JAXBContext.newInstance(CatalogItem.class, Catalog.class);
                    Unmarshaller u = jc.createUnmarshaller();
                    Catalog catalog = (Catalog) u.unmarshal(new StreamSource(new StringReader(payload)));


                    result = catalog.getCatalogItems();
                }

            } finally {
                context.shutdown();
            }

        } catch (Exception e) {
            e.printStackTrace();
            status = e.getLocalizedMessage();
        }

        return result;
    }

    public String catalogServiceEndpoint() {
        String itemId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("itemId");

        String response = services.getCatalogService().path(itemId).request().get(String.class);
        
        JsonObject jsonObject = Json.createReader(new StringReader(response)).readObject();
        catalogItem.setId(jsonObject.getInt("id"));
        catalogItem.setName(jsonObject.getString("name"));
        catalogItem.setDescription(jsonObject.getString("description"));
        
        return response;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private ByteBuf cachedResults = Unpooled.buffer();
}
