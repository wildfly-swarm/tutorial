# ShoppingCart - MSA

## Self-contained (all services running a=on a single WildFly instance)

This is the most simple case and our starting point for further steps into using WildFly Swarm.

### Build the top level modules first:

```
mvn clean install
```

### Make sure you have Consul running:

The progrium consul image lends itself well to launch consul. Simply pull the image:

```
docker pull progrium/consul
```

And start it:
```
docker run -d -p 8400:8400 -p 8500:8500 -p 8600:53/udp -h node1 progrium/consul -server -bootstrap -ui-dir /ui
```

You can verify if it runs successfully by navigating to the consul web UI:

http://192.168.99.100:8500

### Start WildFly with a concrete address

You need an actual network address for consul to be able to poke into WildFly:

```
./bin/standalone.sh -b 192.168.178.54
```

### Deploy all services:

```
#!/bin/sh

for i in catalog user order everest; do \
  cd $i; \
  mvn wildfly:deploy; \
  cd ..; \
done
```

## Accessing the everest web interface

http://192.168.178.54:8080/everest-web
