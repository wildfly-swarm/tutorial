package org.javaee7.wildfly.samples.everest;

import java.net.URL;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ClassLoaderAsset;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.topology.TopologyArchive;
import org.wildfly.swarm.undertow.WARArchive;

/**
 * @author Heiko Braun
 * @since 01/06/16
 */
public class Main {

    static final String[] webResources = {"cart", "catalog", "catalog-item", "checkout", "confirm", "index", "user", "user-status"};

    public static void main(String[] args) throws Exception {

        URL stageConfig = Main.class.getClassLoader().getResource("project-stages.yml");

        Swarm swarm = new Swarm()
                .withStageConfig(stageConfig);

        WARArchive deployment = ShrinkWrap.create(WARArchive.class );
        deployment.addPackages(true, Main.class.getPackage());

        ClassLoader classLoader = Main.class.getClassLoader();

        for(String s : webResources) {
            String fileName = s + ".xhtml";
            deployment.addAsWebResource( new ClassLoaderAsset(fileName, classLoader), fileName);
        }

        deployment.addAsWebInfResource(
                new ClassLoaderAsset("WEB-INF/web.xml", classLoader), "web.xml");


        deployment.addAllDependencies();

        // advertise service
        deployment.as(TopologyArchive.class).advertise(
                swarm.stageConfig()
                        .resolve("service.web.service-name")
                        .getValue()
        );

        swarm.start().deploy(deployment);
    }
}
