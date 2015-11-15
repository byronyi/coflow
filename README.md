Coflow Client for Java NIO
===

Implemented token bucket rate limiting upon ``java.nio.SocketChannel``.

Currently bucket size and rate limit are hard-coded. Change them in
``core/src/main/java/com/github/byronyi/CoflowSocketChannel`` as you wish.

Build
---
Require [Gradle](http://gradle.org/) to build.
```bash
$ git clone https://github.com/byronyi/coflow
$ cd coflow
$ gradle runExample
```
Which runs a simple echo server.

Open another terminal, and use ``nc`` to test it out.
```bash
$ cat (some big files) | nc localhost 8080
```

Return to the last ``gradle runExample`` terminal to see the output.

``Non-comformant traffic`` will print if there is negative number of tokens.

Successful write occurs when there are positive number of tokens, printing
``Wrote xxx bytes``, indicating how many bytes are written in the last
``SocketChannel.write`` call.
