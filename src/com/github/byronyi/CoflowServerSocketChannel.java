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

class CoflowServerSocketChannel extends ServerSocketChannel implements SelChImpl {

    final private ServerSocketChannel serverSocketChannel;

    protected CoflowServerSocketChannel(CoflowSelectorProvider provider) throws IOException {
        super(provider);
        serverSocketChannel = provider.selectorProvider.openServerSocketChannel();
    }

    @Override
    public ServerSocketChannel bind(SocketAddress local, int backlog) throws IOException {
        return serverSocketChannel.bind(local, backlog);
    }

    @Override
    public <T> ServerSocketChannel setOption(SocketOption<T> name, T value) throws IOException {
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
        return serverSocketChannel.accept();
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
        return ((SelChImpl) serverSocketChannel).getFD();
    }

    @Override
    public int getFDVal() {
        return ((SelChImpl) serverSocketChannel).getFDVal();
    }

    @Override
    public boolean translateAndUpdateReadyOps(int i, SelectionKeyImpl selectionKey) {
        return ((SelChImpl) serverSocketChannel).translateAndUpdateReadyOps(i, selectionKey);
    }

    @Override
    public boolean translateAndSetReadyOps(int i, SelectionKeyImpl selectionKey) {
        return ((SelChImpl) serverSocketChannel).translateAndSetReadyOps(i, selectionKey);
    }

    @Override
    public void translateAndSetInterestOps(int i, SelectionKeyImpl selectionKey) {
        ((SelChImpl) serverSocketChannel).translateAndSetInterestOps(i, selectionKey);
    }

    @Override
    public void kill() throws IOException {
        ((SelChImpl) serverSocketChannel).kill();
    }
}
