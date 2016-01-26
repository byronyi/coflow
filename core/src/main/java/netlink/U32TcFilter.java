package netlink;

import netlink.swig.capi;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class U32TcFilter extends TcFilter {

    public U32TcFilter() throws IOException {
        super();
        asTcObject().setKind("u32");
    }

    public U32TcFilter setClass(long handle) throws IOException {
        int ret = capi.rtnl_u32_set_classid(ptr, handle);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
        return this;
    }

    public U32TcFilter addKeyUint8(short val, short mask, int offset, int offmask) throws IOException {
        int ret = capi.rtnl_u32_add_key_uint8(ptr, val, mask, offset, offmask);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
        return this;
    }

    public U32TcFilter addKeyUint16(int val, int mask, int offset, int offmask) throws IOException {
        int ret = capi.rtnl_u32_add_key_uint16(ptr, val, mask, offset, offmask);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
        return this;
    }

    public U32TcFilter addKeyUint32(long val, long mask, int offset, int offmask) throws IOException {
        int ret = capi.rtnl_u32_add_key_uint32(ptr, val, mask, offset, offmask);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
        return this;
    }

    public U32TcFilter addKeySrcAddr(InetSocketAddress src) throws IOException {
        int ip = ByteBuffer.wrap(src.getAddress().getAddress()).getInt();
        int port = src.getPort();
        addKeyUint32(ip, 0xffffffff, 12, 0);
        addKeyUint16(port, 0xffff, 0, 1);
        return this;
    }

    public U32TcFilter addKeyDestAddr(InetSocketAddress dest) throws IOException {
        int ip = ByteBuffer.wrap(dest.getAddress().getAddress()).getInt();
        int port = dest.getPort();
        addKeyUint32(ip, 0xffffffff, 16, 0);
        addKeyUint16(port, 0xffff, 2, 1);
        return this;
    }
}
