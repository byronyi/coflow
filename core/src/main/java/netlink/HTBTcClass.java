package netlink;

import netlink.swig.capi;

import java.io.IOException;

public class HTBTcClass extends TcClass {

    public HTBTcClass() throws IOException {
        super();
        asTcObject().setKind("htb");
    }

    public long getRate() {
        return capi.rtnl_htb_get_rate(ptr);
    }

    public HTBTcClass setRate(long rate) throws IOException {
        int ret = capi.rtnl_htb_set_rate(ptr, rate);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
        return this;
    }

    public long getCeil() {
        return capi.rtnl_htb_get_ceil(ptr);
    }

    public HTBTcClass setCeil(long ceil) throws IOException {
        int ret = capi.rtnl_htb_set_ceil(ptr, ceil);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
        return this;
    }

    public long getRbuffer() {
        return capi.rtnl_htb_get_rbuffer(ptr);
    }

    public HTBTcClass setRbuffer(long rbuffer) throws IOException {
        int ret = capi.rtnl_htb_set_rbuffer(ptr, rbuffer);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
        return this;
    }

    public long getCbuffer() {
        return capi.rtnl_htb_get_cbuffer(ptr);
    }

    public HTBTcClass setCbuffer(long cbuffer) throws IOException {
        int ret = capi.rtnl_htb_set_cbuffer(ptr, cbuffer);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
        return this;
    }

    public long getPrio() {
        return capi.rtnl_htb_get_prio(ptr);
    }

    public HTBTcClass setPrio(long prio) throws IOException {
        int ret = capi.rtnl_htb_set_prio(ptr, prio);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
        return this;
    }
}

