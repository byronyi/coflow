package netlink;

import netlink.swig.SWIGTYPE_p_rtnl_class;
import netlink.swig.capi;

import java.io.IOException;

public class TcClass {

    final protected SWIGTYPE_p_rtnl_class ptr;

    public TcClass() throws IOException {
        ptr = capi.rtnl_class_alloc();
        if (ptr == null) {
            throw new IOException("cannot allocate rtnl_class");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        capi.rtnl_class_put(ptr);
    }

    public void add(NetlinkSocket sock, int flags) throws IOException {
        int ret = capi.rtnl_class_add(sock.getPtr(), ptr, flags);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
    }

    public void delete(NetlinkSocket sock) throws IOException {
        int ret = capi.rtnl_class_delete(sock.getPtr(), ptr);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
    }

    public TcObject asTcObject() {
        return new TcObject(capi.class2tc(ptr));
    }
}
