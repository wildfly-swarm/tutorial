package org.javaee7.wildfly.samples.everest.catalog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Heiko Braun
 * @since 07/06/16
 */
@Named
@SessionScoped
@XmlRootElement(name = "collection")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class Catalog implements Serializable {

    @XmlElement(name = "catalogItem")
    List<CatalogItem> items;

    public List<CatalogItem> getCatalogItems() {
        if (null == items) {
            items = new ArrayList<>();
        }
        return items;
    }

    public void setCatalogItems(List<CatalogItem> items) {
        this.items = items;
    }
}
