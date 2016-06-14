/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.javaee7.wildfly.samples.everest.utils;

import org.javaee7.wildfly.samples.everest.utils.exception.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * @author Ryan McGuinness [rmcguinness@walmartlabs.com]
 * @author Heiko Braun
 *
 */
@Startup
@Singleton
public class WildFlyUtil {

    private static final Logger log = LoggerFactory.getLogger(WildFlyUtil.class);

    private static final String JBOSS_BIND_ADDRESS = "jboss.bind.address";

    private static final String SWARM_BIND_ADDRESS = "swarm.bind.address";

    private static final String JBOSS_SOCKET_BINDING_PORT_OFFSET = "jboss.socket.binding.port-offset";

    private static final String SWARM_PORT_OFFSET = "swarm.port.offset";

    private String hostName = "localhost";
    private int hostPort = 8080;
    private int hostSecurePort = 8443;

    @PostConstruct
    void init() throws InitializationException {
        if(!resolveFromSystemProps()) // system props first, JMX as fallback
            resolveFromJMX();

        log.info("[INFO] Host and port resolved to: " + hostName + " : " + hostPort + "/" + hostSecurePort);
    }

    private boolean resolveFromSystemProps() {
        String bindAddress = System.getProperty(JBOSS_BIND_ADDRESS)!=null ?
                System.getProperty(JBOSS_BIND_ADDRESS) : System.getProperty(SWARM_BIND_ADDRESS);

        String portOffset = System.getProperty(JBOSS_SOCKET_BINDING_PORT_OFFSET)!=null ?
                       System.getProperty(JBOSS_SOCKET_BINDING_PORT_OFFSET) : System.getProperty(SWARM_PORT_OFFSET, "0");

        if(bindAddress!=null) {

            Integer offset = Integer.valueOf(portOffset);
            hostName = bindAddress;
            hostPort = hostPort + offset;
            hostSecurePort = hostSecurePort + offset;
        }

        return bindAddress!=null;
    }

    private void resolveFromJMX() {
        try {
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

            ObjectName http = new ObjectName("jboss.as:socket-binding-group=standard-sockets,socket-binding=http");
            hostName = (String) mBeanServer.getAttribute(http,"boundAddress");
            hostPort = (Integer) mBeanServer.getAttribute(http,"boundPort");

            ObjectName ws = new ObjectName("jboss.ws", "service", "ServerConfig");
            hostSecurePort = (int) mBeanServer.getAttribute(ws, "WebServiceSecurePort");
        } catch (Exception e) {
            throw new InitializationException(e);
        }
    }

    public String getHostName() {
        return hostName;
    }

    public int getHostPort() {
        return hostPort;
    }

    public int getSecurePort() {
        return hostSecurePort;
    }
}