package netlink;

import netlink.swig.capi;

public class NetlinkUtils {
    static public long getIfindexByName(String name) {
        return capi.if_nametoindex(name);
    }
}
