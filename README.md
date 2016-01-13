Coflow Framework
===

Coflow scheduling in Java.

Dependency
---

* Java 8
* Scala 2.10
* Akka 2.3
* Javassist
* Netty 4 (For running example)

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

Example
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
