package coflow;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

final public class CoflowChannel {

    final static private ConcurrentMap<Object, String> keyToCoflowId = new ConcurrentHashMap<>();

    final private Flow flow;
    final protected AtomicLong bytesWritten = new AtomicLong(0L);

    static public void register(SocketAddress source, SocketAddress destination, String coflowId) {
        InetSocketAddress src = (InetSocketAddress) source;
        InetSocketAddress dst = (InetSocketAddress) destination;

        Flow flow = new Flow(src.getAddress().getHostAddress(), src.getPort(), dst.getAddress().getHostAddress(), dst.getPort());

        CoflowClient$.MODULE$.register(flow, coflowId);
    }

    static public void register0(Object key, String coflowId) {
        keyToCoflowId.put(key, coflowId);
    }

    static public void register1(Object key, SocketAddress source, SocketAddress destination) {
        String coflowId = keyToCoflowId.remove(key);
        if (coflowId != null) {
            register(source, destination, coflowId);
        }
    }

    public CoflowChannel(SocketChannel channel) throws IOException {

        InetSocketAddress src = (InetSocketAddress) channel.getLocalAddress();
        InetSocketAddress dst = (InetSocketAddress) channel.getRemoteAddress();

        flow = new Flow(src.getAddress().getHostAddress(), src.getPort(), dst.getAddress().getHostAddress(), dst.getPort());

        CoflowClient$.MODULE$.open(this);
    }

    public void write(int size) {
        write((long) size);
    }

    public void write(long size) {
        bytesWritten.addAndGet(size);
    }

    public void close() {
        CoflowClient$.MODULE$.close(this);
    }

    protected Flow flow() {
        return flow;
    }

    protected long getBytesSent() {
        return bytesWritten.get();
    }
}
