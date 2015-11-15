package com.github.byronyi;

import sun.nio.ch.SelChImpl;
import sun.nio.ch.SelectionKeyImpl;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * All methods are delegated except for {@link #accept()}, which returns
 * a decorated {@link CoflowSocketChannel}.
 */
class CoflowServerSocketChannel
    extends ServerSocketChannel implements SelChImpl {

    final private ServerSocketChannel serverSocketChannel;

    protected CoflowServerSocketChannel(CoflowSelectorProvider provider)
        throws IOException {
        super(provider);
        serverSocketChannel =
            provider.getDefaultProvider().openServerSocketChannel();
    }

    private SelChImpl getServerSocketChannel() {
        return (SelChImpl) serverSocketChannel;
    }

    @Override
    public ServerSocketChannel bind(SocketAddress local, int backlog)
        throws IOException {
        serverSocketChannel.bind(local, backlog);
        return this;
    }

    @Override
    public <T> ServerSocketChannel setOption(SocketOption<T> name, T value)
        throws IOException {
        return serverSocketChannel.setOption(name, value);
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return serverSocketChannel.getOption(name);
    }

    @Override
    public Set<SocketOption<?>> supportedOptions() {
        return serverSocketChannel.supportedOptions();
    }

    @Override
    public ServerSocket socket() {
        return serverSocketChannel.socket();
    }

    @Override
    public SocketChannel accept() throws IOException {
        SocketChannel socketChannel = serverSocketChannel.accept();
        if (socketChannel == null) {
            return null;
        }
        return new CoflowSocketChannel(this.provider(), socketChannel);
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        return serverSocketChannel.getLocalAddress();
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
        serverSocketChannel.close();
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
        serverSocketChannel.configureBlocking(block);
    }

    @Override
    public FileDescriptor getFD() {
        return getServerSocketChannel().getFD();
    }

    @Override
    public int getFDVal() {
        return getServerSocketChannel().getFDVal();
    }

    @Override
    public boolean translateAndUpdateReadyOps(int i, SelectionKeyImpl key) {
        return getServerSocketChannel().translateAndUpdateReadyOps(i, key);
    }

    @Override
    public boolean translateAndSetReadyOps(int i, SelectionKeyImpl key) {
        return getServerSocketChannel().translateAndSetReadyOps(i, key);
    }

    @Override
    public void translateAndSetInterestOps(int i, SelectionKeyImpl key) {
        getServerSocketChannel().translateAndSetInterestOps(i, key);
    }

    @Override
    public void kill() throws IOException {
        getServerSocketChannel().kill();
    }
}
