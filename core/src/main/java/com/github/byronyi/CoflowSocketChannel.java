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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SocketChannel with Token Bucket Rate-limiting algorithm support.
 */
class CoflowSocketChannel extends SocketChannel implements SelChImpl {

    /**
     * Vanilla socket channel. Most methods here are just its delegation,
     * except for {@link #write(ByteBuffer)} and {@link #write(ByteBuffer[], int, int)}.
     */
    final protected SocketChannel socketChannel;

    // TODO: support message size fragmentation
    final protected AtomicLong bytesWritten = new AtomicLong(0L);
    // TODO: open this attribute to external modification
    final protected AtomicInteger rateLimitBps = new AtomicInteger(1048576);
    // TODO: open this attribute to external modification
    final long MAX_BUCKET_SIZE = 65536;

    /**
     * Number of available tokens is initialized with MAX_BUCKET_SIZE.
     */
    volatile private long bucketSize = MAX_BUCKET_SIZE;
    volatile private long lastUpdate = System.currentTimeMillis();

    protected CoflowSocketChannel(CoflowSelectorProvider provider)
        throws IOException {
        super(provider);
        socketChannel = provider.getDefaultProvider().openSocketChannel();
    }

    protected CoflowSocketChannel(SelectorProvider provider,
                                  SocketChannel socketChannel) {
        super(provider);
        this.socketChannel = socketChannel;
    }

    private SelChImpl getSocketChannel() {
        return (SelChImpl) socketChannel;
    }

    /**
     * @return If this socket has remaining tokens for outbound traffic.
     *
     * Also refill the number of tokens, to the cap of {@link #MAX_BUCKET_SIZE}
     */
    private boolean isConforming() {

        long durationMillis = System.currentTimeMillis() - lastUpdate;
        bucketSize += (durationMillis * (long) rateLimitBps.get()) >> 13;
        if (bucketSize > MAX_BUCKET_SIZE) {
            bucketSize = MAX_BUCKET_SIZE;
        }
        lastUpdate = System.currentTimeMillis();

        System.out.println(bucketSize +
            " tokens left for outbound traffic");
        return bucketSize > 0;
    }

    @Override
    public SocketChannel bind(SocketAddress local) throws IOException {
        socketChannel.bind(local);
        return this;
    }

    @Override
    public <T> SocketChannel setOption(SocketOption<T> name, T value)
        throws IOException {
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
    public long read(ByteBuffer[] dsts, int offset, int length)
        throws IOException {
        return socketChannel.read(dsts, offset, length);
    }

    /**
     * @param src
     * @return number of bytes successfully written, or 0 if non is written
     * @throws IOException
     *
     * Subtract number of tokens from successfully written bytes,
     * possibly results in a bucket with negative number of tokens.
     *
     * Since it would be rather difficult to limit the traffic before
     * the actual write occurs, we choose to delay the next successful
     * write until the bucket has positive number of tokens.
     *
     * In the long run, this will (hopefully) yield an average
     * traffic rate that close to the rate limit.
     *
     */
    @Override
    synchronized public int write(ByteBuffer src) throws IOException {
        if (isConforming()) {
            int size = socketChannel.write(src);
            bucketSize -= size;
            bytesWritten.getAndAdd(size);
            System.out.println("Wrote " + size + " bytes");
            return size;
        } else {
            System.out.println("Non-comformant traffic");
            return 0;
        }
    }

    /**
     * @param srcs
     * @param offset
     * @param length
     * @return number of bytes successfully written, or 0 if non is written
     * @throws IOException
     *
     * See {@link #write(ByteBuffer)} for explanation.
     */
    @Override
    synchronized public long write(ByteBuffer[] srcs, int offset, int length)
        throws IOException {
        if (isConforming()) {
            long size = socketChannel.write(srcs, offset, length);
            bucketSize -= size;
            bytesWritten.getAndAdd(size);
            System.out.println("Wrote " + size + " bytes");
            return size;
        } else {
            System.out.println("Non-comformant traffic");
            return 0;
        }
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
        return (getSocketChannel()).getFD();
    }

    @Override
    public int getFDVal() {
        return getSocketChannel().getFDVal();
    }

    @Override
    public boolean translateAndUpdateReadyOps(int i, SelectionKeyImpl key) {
        return getSocketChannel().translateAndUpdateReadyOps(i, key);
    }

    @Override
    public boolean translateAndSetReadyOps(int i, SelectionKeyImpl key) {
        return getSocketChannel().translateAndSetReadyOps(i, key);
    }

    @Override
    public void translateAndSetInterestOps(int i, SelectionKeyImpl key) {
        getSocketChannel().translateAndSetInterestOps(i, key);
    }

    @Override
    public void kill() throws IOException {
        getSocketChannel().kill();
    }
}
