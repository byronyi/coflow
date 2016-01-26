package netlink;

import netlink.swig.capi;

import java.io.IOException;

public class HTBTcQdisc extends TcQdisc {

    public HTBTcQdisc() throws IOException {
        super();
        asTcObject().setKind("htb");
    }

    public long getRateToQuantum() {
        return capi.rtnl_htb_get_rate2quantum(ptr);
    }

    public HTBTcQdisc setRateToQuantum(int rateToQuantum) throws IOException {
        int ret = capi.rtnl_htb_set_rate2quantum(ptr, rateToQuantum);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
        return this;
    }

    public long getDefaultClass() {
        return capi.rtnl_htb_get_defcls(ptr);
    }

    public HTBTcQdisc setDefaultClass(int defaultClass) throws IOException {
        int ret = capi.rtnl_htb_set_defcls(ptr, defaultClass);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
        return this;
    }
}

