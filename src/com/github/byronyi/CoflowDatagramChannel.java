package com.github.byronyi;

import sun.nio.ch.SelChImpl;
import sun.nio.ch.SelectionKeyImpl;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.util.Set;

class CoflowDatagramChannel extends DatagramChannel implements SelChImpl {

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
        datagramChannel.bind(local);
        return this;
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
        datagramChannel.connect(remote);
        return this;
    }

    @Override
    public DatagramChannel disconnect() throws IOException {
        datagramChannel.disconnect();
        return this;
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

    @Override
    public FileDescriptor getFD() {
        return ((SelChImpl) datagramChannel).getFD();
    }

    @Override
    public int getFDVal() {
        return ((SelChImpl) datagramChannel).getFDVal();
    }

    @Override
    public boolean translateAndUpdateReadyOps(int i, SelectionKeyImpl selectionKey) {
        return ((SelChImpl) datagramChannel).translateAndUpdateReadyOps(i, selectionKey);
    }

    @Override
    public boolean translateAndSetReadyOps(int i, SelectionKeyImpl selectionKey) {
        return ((SelChImpl) datagramChannel).translateAndSetReadyOps(i, selectionKey);
    }

    @Override
    public void translateAndSetInterestOps(int i, SelectionKeyImpl selectionKey) {
        ((SelChImpl) datagramChannel).translateAndSetInterestOps(i, selectionKey);
    }

    @Override
    public void kill() throws IOException {
        ((SelChImpl) datagramChannel).kill();
    }
}
