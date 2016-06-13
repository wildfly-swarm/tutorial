package org.javaee7.wildfly.samples.everest.order;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author arungupta
 */
@Entity
@Table(name = "CART_ORDER")
@NamedQueries({
    @NamedQuery(name = "Order.findAll", query = "SELECT u FROM Order u"),
    @NamedQuery(name = "Order.findById", query = "SELECT u FROM Order u where u.orderId= :id")
})
@XmlRootElement
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column
    int orderId;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @Embedded
    List<OrderItem> orderItems;

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public List<OrderItem> getOrderItems() {
        if (null == orderItems) {
            orderItems = new ArrayList<>();
        }
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }
}
