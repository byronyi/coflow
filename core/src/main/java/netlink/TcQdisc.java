package netlink;

import netlink.swig.SWIGTYPE_p_rtnl_qdisc;
import netlink.swig.capi;

import java.io.IOException;

public class TcQdisc implements Tc {

    final protected SWIGTYPE_p_rtnl_qdisc ptr;

    public TcQdisc() throws IOException {
        ptr = capi.rtnl_qdisc_alloc();
        if (ptr == null) {
            throw new IOException("cannot allocate rtnl_qdisc");
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        capi.rtnl_qdisc_put(ptr);
    }

    public void add(NetlinkSocket sock, int flags) throws IOException {
        int ret = capi.rtnl_qdisc_add(sock.getPtr(), ptr, flags);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
    }

    public void delete(NetlinkSocket sock) throws IOException {
        int ret = capi.rtnl_qdisc_delete(sock.getPtr(), ptr);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
    }

    @Override
    public TcObject asTcObject() {
        return new TcObject(capi.qdisc2tc(ptr));
    }
}

