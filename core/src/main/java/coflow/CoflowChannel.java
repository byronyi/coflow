package coflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SocketChannel;

final public class CoflowChannel {

    final private Logger logger = LoggerFactory.getLogger(CoflowChannel.class);

    final private SocketChannel javaChannel;

    final private Flow flow;

    final protected static long MAX_BUCKET_SIZE = 1024 * 1024; // in Bytes
    protected volatile long bytesWritten = 0;
    protected volatile int rateLimitMbps = 1000; // in Mbps
    protected volatile boolean canProceed = true;

    private long bucketSize = MAX_BUCKET_SIZE;
    private long lastUpdate;

    public CoflowChannel(SocketChannel channel) throws IOException {

        InetSocketAddress src = (InetSocketAddress) channel.getLocalAddress();
        InetSocketAddress dst = (InetSocketAddress) channel.getRemoteAddress();

        javaChannel = channel;
        flow = new Flow(src.getAddress().getHostAddress(), src.getPort(), dst.getAddress().getHostAddress(), dst.getPort());
        lastUpdate = System.nanoTime();

        CoflowClient$.MODULE$.open(this);
    }

    public static void register(SocketAddress source, SocketAddress destination, String coflowId) {
        InetSocketAddress src = (InetSocketAddress) source;
        InetSocketAddress dst = (InetSocketAddress) destination;
        CoflowClient$.MODULE$.register(src, dst, coflowId);
    }

    /**
     * @return Whether this socket has remaining tokens for outbound traffic.
     * <p>
     * Also refill the number of tokens, to the cap of {@link #MAX_BUCKET_SIZE}
     */
    public boolean canProceed() {
        if (!canProceed) {
            return false;
        }
        long durationNanos = System.nanoTime() - lastUpdate;
        bucketSize += (durationNanos * rateLimitMbps) >> 13;
        if (bucketSize > MAX_BUCKET_SIZE) {
            bucketSize = MAX_BUCKET_SIZE;
        }
        lastUpdate = System.nanoTime();

        return bucketSize > 0;
    }

    public void write(int size) {
        bucketSize -= size;
        bytesWritten += size;
    }

    public void write(long size) {
        bucketSize -= size;
        bytesWritten += size;
    }

    public void close() {
        CoflowClient$.MODULE$.close(this);
    }

    protected SocketChannel javaChannel() {
        return javaChannel;
    }

    protected Flow flow() {
        return flow;
    }

    protected long getBytesSent() {
        return bytesWritten;
    }

    protected void setPriority(int priority) {
        try {
            javaChannel.setOption(StandardSocketOptions.IP_TOS, priority);
            logger.trace("{} priority is set to {}", flow, priority);
        } catch (IOException e) {
            logger.warn("{} illegal priority {} ignored", flow, priority);
        }
    }

    protected void setRateMbps(int rate) {
        if (rate >= 0) {
            rateLimitMbps = rate;
            logger.trace("{} rate is set to {} Mbps", flow, rate);
        } else {
            logger.warn("{} illegal rate {} is ignored", flow, rate);
        }
    }

    protected void start() {
        canProceed = true;
    }

    protected void pause() {
        canProceed = false;
    }
}
