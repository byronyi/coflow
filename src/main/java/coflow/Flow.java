package coflow;

import java.net.SocketAddress;

public class Flow {

    protected final SocketAddress source;
    protected final SocketAddress destination;
    protected final long startTime;

    protected final static long MAX_BUCKET_SIZE = 8192; // in Bytes
    protected volatile long bytesWritten = 0;
    protected volatile int rateLimitMbps = 1000; // in Mbps

    private long bucketSize = MAX_BUCKET_SIZE;
    private long lastUpdate;

    public Flow(SocketAddress source, SocketAddress destination) {
        this.source = source;
        this.destination = destination;
        this.startTime = System.nanoTime();
        this.lastUpdate = this.startTime;
        System.out.println("New flow: " + source + " -> " + destination);
    }

    /**
     * @return If this socket has remaining tokens for outbound traffic.
     *
     * Also refill the number of tokens, to the cap of {@link #MAX_BUCKET_SIZE}
     */
    public boolean canProceed() {
        long durationNanos = System.nanoTime() - lastUpdate;
        bucketSize += ((double) durationNanos * rateLimitMbps) / 8192.;
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
        long lifetime = System.nanoTime() - startTime;
        System.out.println("Average rate: " +
            (bytesWritten * 8192. / lifetime) + " Mbps");
    }
}
