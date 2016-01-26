package netlink;

import netlink.swig.SWIGTYPE_p_rtnl_cls;
import netlink.swig.capi;

import java.io.IOException;

public class TcFilter implements Tc {

    final protected SWIGTYPE_p_rtnl_cls ptr;

    public TcFilter() throws IOException {
        ptr = capi.rtnl_cls_alloc();
        if (ptr == null) {
            throw new IOException("cannot allocate rtnl_cls");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        capi.rtnl_cls_put(ptr);
    }

    public TcFilter setPriority(int priority) {
        capi.rtnl_cls_set_prio(ptr, priority);
        return this;
    }

    public void add(NetlinkSocket sock, int flags) throws IOException {
        int ret = capi.rtnl_cls_add(sock.getPtr(), ptr, flags);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
    }

    public void delete(NetlinkSocket sock, int flags) throws IOException {
        int ret = capi.rtnl_cls_delete(sock.getPtr(), ptr, flags);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
    }

    @Override
    public TcObject asTcObject() {
        return new TcObject(capi.cls2tc(ptr));
    }
}

