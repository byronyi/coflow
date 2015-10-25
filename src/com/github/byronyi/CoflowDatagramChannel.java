package com.github.byronyi;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.util.Set;

public class CoflowDatagramChannel extends DatagramChannel {

    final private DatagramChannel datagramChannel;

    protected CoflowDatagramChannel(CoflowSelectorProvider provider) throws IOException {
        super(provider);
        datagramChannel = provider.selectorProvider.openDatagramChannel();
    }

    protected CoflowDatagramChannel(CoflowSelectorProvider provider,
                                    ProtocolFamily family) throws IOException {
        super(provider);
        datagramChannel = provider.selectorProvider.openDatagramChannel(family);
    }

    @Override
    public DatagramChannel bind(SocketAddress local) throws IOException {
        return datagramChannel.bind(local);
    }

    @Override
    public <T> DatagramChannel setOption(SocketOption<T> name, T value) throws IOException {
        return datagramChannel.setOption(name, value);
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return datagramChannel.getOption(name);
    }

    @Override
    public Set<SocketOption<?>> supportedOptions() {
        return datagramChannel.supportedOptions();
    }

    @Override
    public DatagramSocket socket() {
        return datagramChannel.socket();
    }

    @Override
    public boolean isConnected() {
        return datagramChannel.isConnected();
    }

    @Override
    public DatagramChannel connect(SocketAddress remote) throws IOException {
        return datagramChannel.connect(remote);
    }

    @Override
    public DatagramChannel disconnect() throws IOException {
        return datagramChannel.disconnect();
    }

    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        return datagramChannel.getRemoteAddress();
    }

    @Override
    public SocketAddress receive(ByteBuffer dst) throws IOException {
        return datagramChannel.receive(dst);
    }

    @Override
    public int send(ByteBuffer src, SocketAddress target) throws IOException {
        return datagramChannel.send(src, target);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return datagramChannel.read(dst);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        return datagramChannel.read(dsts, offset, length);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return datagramChannel.write(src);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        return datagramChannel.write(srcs, offset, length);
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        return datagramChannel.getLocalAddress();
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
        datagramChannel.close();
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
        datagramChannel.configureBlocking(block);
    }

    @Override
    public MembershipKey join(InetAddress group, NetworkInterface interf) throws IOException {
        return datagramChannel.join(group, interf);
    }

    @Override
    public MembershipKey join(InetAddress group, NetworkInterface interf, InetAddress source) throws IOException {
        return datagramChannel.join(group, interf, source);
    }
}
