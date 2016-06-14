package org.javaee7.wildfly.samples.everest.order;

import java.net.URL;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.jaxrs.JAXRSArchive;
import org.wildfly.swarm.topology.TopologyArchive;

/**
 * @author Heiko Braun
 * @since 12/05/16
 */
public class Main {

    public static void main(String[] args) throws Exception {

        URL stageConfig = Main.class.getClassLoader().getResource("project-stages.yml");

        Swarm swarm = new Swarm()
                .withStageConfig(stageConfig);

        swarm.start();

        JAXRSArchive archive = ShrinkWrap.create(JAXRSArchive.class);
        archive.addPackage(Main.class.getPackage());
        archive.addAsWebInfResource(new ClassLoaderAsset("META-INF/persistence.xml", Main.class.getClassLoader()), "classes/META-INF/persistence.xml");
        archive.addAllDependencies();

        // advertise service
        archive.as(TopologyArchive.class).advertise(
                swarm.stageConfig()
                        .resolve("service.order.service-name")
                        .getValue()
        );

        swarm.deploy(archive);

    }
}
