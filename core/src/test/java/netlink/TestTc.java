package netlink;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;

public class TestTc {

    static {
        System.loadLibrary("nl4j");
    }

    @Test
    public void test() throws IOException {
        NetlinkSocket sock = new NetlinkSocket();
        sock.connect();

        HTBTcQdisc qdisc = new HTBTcQdisc();
        int ifindex = (int) NetlinkUtils.getIfindexByName("eth2");
        qdisc.asTcObject().setIfindex(ifindex).setParent(0xFFFFFFFF).setHandle(0x12 << 16);
        qdisc.delete(sock);
        qdisc.add(sock, 0x400 | 0x100);

        HTBTcClass clazz = new HTBTcClass();
        clazz.asTcObject().setIfindex(ifindex).setParent(0x12 << 16).setHandle(0x123);
        clazz.setPrio(0).setRate(1024*1024*50/8);
        clazz.add(sock, 0x400 | 0x100);

        U32TcFilter filter = new U32TcFilter();
        filter.asTcObject().setIfindex(ifindex).setParent(0x12 << 16).setHandle(0x234);
        filter.setClass(0x123).addKeySrcAddr(new InetSocketAddress("1.2.3.4", 1234)).addKeyDestAddr(new InetSocketAddress("4.3.2.1", 4321)).setPriority(1234);

        filter.delete(sock, 0);
        filter.add(sock, 0x400);
    }
}
