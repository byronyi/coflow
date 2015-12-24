package coflow.example;

import coflow.CoflowClient$;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetAddress;

public class Client {

    public static void main(String[] args) throws Exception {
        String host = InetAddress.getLocalHost().getHostAddress();
        int port = 8080;
        if (args.length > 1) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new ClientHandler());
                }
            });

            byte[] junk = new byte[1024 * 1024];

            for (int z = 0; z < 10; z++) {

                ChannelFuture f = b.connect(host, port).sync();
                Channel channel = f.channel();

                CoflowClient$.MODULE$.register(channel.localAddress(),
                    channel.remoteAddress(), "test" + z);

                long start = System.currentTimeMillis();

                for (int i = 0; i < 100; i++) {
                    ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
                    byteBuf.writeBytes(junk);
                    channel.writeAndFlush(byteBuf).sync();
                }

                long end = System.currentTimeMillis();
                channel.close().sync();

                System.out.println("Wrote 100MB in " + (end - start) / 1000. + "s");
            }
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
