package com.github.byronyi;

import sun.nio.ch.SelChImpl;
import sun.nio.ch.SelectionKeyImpl;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

class CoflowSocketChannel extends SocketChannel implements SelChImpl {

    final private SocketChannel socketChannel;

    protected CoflowSocketChannel(CoflowSelectorProvider provider) throws IOException {
        super(provider);
        socketChannel = provider.selectorProvider.openSocketChannel();
    }

    protected CoflowSocketChannel(SelectorProvider provider,
                                  SocketChannel socketChannel) {
        super(provider);
        this.socketChannel = socketChannel;
    }

    @Override
    public SocketChannel bind(SocketAddress local) throws IOException {
        socketChannel.bind(local);
        return this;
    }

    @Override
    public <T> SocketChannel setOption(SocketOption<T> name, T value) throws IOException {
        return socketChannel.setOption(name, value);
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return socketChannel.getOption(name);
    }

    @Override
    public Set<SocketOption<?>> supportedOptions() {
        return socketChannel.supportedOptions();
    }

    @Override
    public SocketChannel shutdownInput() throws IOException {
        socketChannel.shutdownInput();
        return this;
    }

    @Override
    public SocketChannel shutdownOutput() throws IOException {
        socketChannel.shutdownOutput();
        return this;
    }

    @Override
    public Socket socket() {
        return socketChannel.socket();
    }

    @Override
    public boolean isConnected() {
        return socketChannel.isConnected();
    }

    @Override
    public boolean isConnectionPending() {
        return socketChannel.isConnectionPending();
    }

    @Override
    public boolean connect(SocketAddress remote) throws IOException {
        return socketChannel.connect(remote);
    }

    @Override
    public boolean finishConnect() throws IOException {
        return socketChannel.finishConnect();
    }

    @Override
    public SocketAddress getRemoteAddress() throws IOException {
        return socketChannel.getRemoteAddress();
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return socketChannel.read(dst);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        return socketChannel.read(dsts, offset, length);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        int bytesWritten = socketChannel.write(src);
        System.out.println("Wrote " + bytesWritten + " bytes");
        return bytesWritten;
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        long bytesWritten = socketChannel.write(srcs, offset, length);
        System.out.println("Wrote " + bytesWritten + " bytes");
        return bytesWritten;
    }

    @Override
    public SocketAddress getLocalAddress() throws IOException {
        return socketChannel.getLocalAddress();
    }

    @Override
    protected void implCloseSelectableChannel() throws IOException {
        socketChannel.close();
    }

    @Override
    protected void implConfigureBlocking(boolean block) throws IOException {
        socketChannel.configureBlocking(block);
    }

    @Override
    public FileDescriptor getFD() {
        return ((SelChImpl) socketChannel).getFD();
    }

    @Override
    public int getFDVal() {
        return ((SelChImpl) socketChannel).getFDVal();
    }

    @Override
    public boolean translateAndUpdateReadyOps(int i, SelectionKeyImpl selectionKey) {
        return ((SelChImpl) socketChannel).translateAndUpdateReadyOps(i, selectionKey);
    }

    @Override
    public boolean translateAndSetReadyOps(int i, SelectionKeyImpl selectionKey) {
        return ((SelChImpl) socketChannel).translateAndSetReadyOps(i, selectionKey);
    }

    @Override
    public void translateAndSetInterestOps(int i, SelectionKeyImpl selectionKey) {
        ((SelChImpl) socketChannel).translateAndSetInterestOps(i, selectionKey);
    }

    @Override
    public void kill() throws IOException {
        ((SelChImpl) socketChannel).kill();
    }
}
