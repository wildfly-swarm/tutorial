package org.javaee7.wildfly.samples.everest.uzer;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.topology.TopologyArchive;
import org.wildfly.swarm.topology.consul.ConsulTopologyFraction;

/**
 * @author Heiko Braun
 * @since 17/05/16
 */
public class Main {
    public static void main(String[] args) throws Exception {

        Swarm swarm = new Swarm();

        swarm.fraction(new ConsulTopologyFraction(
                System.getProperty("swarm.consul.url", "http://localhost:8500/")
        ));

        swarm.start();

        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class);
        archive.addPackage(Main.class.getPackage());
        archive.addAsWebInfResource(new ClassLoaderAsset("META-INF/persistence.xml", Main.class.getClassLoader()), "classes/META-INF/persistence.xml");
        archive.addAllDependencies();

        // advertise service
        archive.as(TopologyArchive.class).advertise("user");

        swarm.deploy(archive);

    }
}
