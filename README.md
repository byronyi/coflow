Coflow Client for Java NIO
===

Token bucket filter (TBF) traffic shaping by instrumentation on ``sun.nio.ch.SocketChannelImpl``.

Dependency
---

Netty
Javassist

Build
---

Require [Gradle](http://gradle.org/) to build.

```bash
$ git clone https://github.com/byronyi/coflow
$ cd coflow
$ gradle jar
```

Find the built library ``(*.jar)`` in ``build/libs``.

Run Example
---

Run a simple echo server without rate limit.

```bash
$ gradle runServer
```

In another terminal, run a client with 1Gpbs rate limit.

```bash
$ gradle runClient
```

Pay attension to the output.

TODO
---

Currently the rate limit is hard coded in ``src/main/java/coflow/Flow.java``. Modify it so it can receive command line options.
