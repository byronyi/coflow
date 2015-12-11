package coflow.example;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client {

    public static void main(String[] args) throws Exception {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
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

            ChannelFuture f = b.connect(host, port).sync();
            Channel channel = f.channel();

            for (int i = 0; i < 1024; i++) {
                ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
                for (int j = 0; j < 16*1024; j++) {
                    byteBuf.writeInt(0);
                }
                channel.writeAndFlush(byteBuf).sync().await();
            }
            channel.close().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
