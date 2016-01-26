package netlink;

import netlink.swig.SWIGTYPE_p_nl_sock;
import netlink.swig.capi;

import java.io.IOException;

public class NetlinkSocket {

    final private SWIGTYPE_p_nl_sock ptr;

    public NetlinkSocket(SWIGTYPE_p_nl_sock ptr) {
        this.ptr = ptr;
        if (ptr == null) {
            throw new NullPointerException("nl_sock");
        }
    }

    public NetlinkSocket() throws IOException {
        ptr = capi.nl_socket_alloc();
        if (ptr == null) {
            throw new IOException("cannot allocate nl_sock");
        }
    }

    public void connect() throws IOException {
        int ret = capi.nl_connect(ptr, 0);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        capi.nl_socket_free(ptr);
    }

    public SWIGTYPE_p_nl_sock getPtr() {
        return ptr;
    }

}
