Coflow Framework
===

Coflow scheduling in Java.

Dependency
---

Note that with the all-in-one jar, there is no need to specify the dependencies manually.

* Scala 2.10
* Akka 2.3.14 (with netty 3.8.0.Final and protobuf-java 2.5.0)
* Javassist 3.20
* Netty 4 (For running example)

This dependencies is compatible with Hadoop 2.7.1 and Spark 1.6.


Build
---

Require [Gradle](http://gradle.org/) to build.

```bash
$ git clone https://github.com/byronyi/coflow
$ cd coflow
$ gradle jar # for compile-time dependency
$ gradle shadowJar # for runtime linking and all its dependencies
```

Find the built library ``(*.jar)`` in ``core/build/libs``.

Usage
---

Just add ``-javaagent:/path/to/coflow-{version}-all.jar`` to your Java virtual machine start up options so you can use the library.

In the client, use ``coflow.CoflowChannel.register(localAddress, remoteAddress, coflowId)`` to register you coflow. Then the TCP stream of this local and remote address will be scheduled by the coflow framework!

See ``example/main/java/coflow/example/Client.java`` for detailed example.

How to Run Example
---

Open four terminals.

In the first terminal, start the coflow master.
```bash
java -cp core/build/libs/coflow-0.0.1-all.jar coflow.CoflowMaster
```

In the second terminal, start the coflow slave.
```bash
java -cp core/build/libs/coflow-0.0.1-all.jar coflow.CoflowSlave
```

In the third one, open a dummy echo server without coflow support.
```bash
gradle runServer
```

In the final termial, open the client to echo server with coflow support.
```bash
gradle runClient
```
