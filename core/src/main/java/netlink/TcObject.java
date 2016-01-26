package netlink;

import netlink.swig.SWIGTYPE_p_rtnl_tc;
import netlink.swig.capi;

import java.io.IOException;

public class TcObject {

    final SWIGTYPE_p_rtnl_tc ptr;

    protected TcObject(SWIGTYPE_p_rtnl_tc ptr) {
        this.ptr = ptr;
    }

    public String getKind() {
        return capi.rtnl_tc_get_kind(ptr);
    }

    public TcObject setKind(String kind) throws IOException {
        int ret = capi.rtnl_tc_set_kind(ptr, kind);
        if (ret < 0) {
            throw new IOException(capi.nl_geterror(ret));
        }
        return this;
    }

    public int getIfindex() {
        return capi.rtnl_tc_get_ifindex(ptr);
    }

    public TcObject setIfindex(int ifindex) {
        capi.rtnl_tc_set_ifindex(ptr, ifindex);
        return this;
    }

    public long getMtu() {
        return capi.rtnl_tc_get_mtu(ptr);
    }

    public TcObject setMtu(long mtu) {
        capi.rtnl_tc_set_mtu(ptr, mtu);
        return this;
    }

    public long getMpu() {
        return capi.rtnl_tc_get_mpu(ptr);
    }

    public TcObject setMpu(long mpu) {
        capi.rtnl_tc_set_mpu(ptr, mpu);
        return this;
    }

    public long getOverhead() {
        return capi.rtnl_tc_get_overhead(ptr);
    }

    public TcObject setOverhead(long overhead) {
        capi.rtnl_tc_set_overhead(ptr, overhead);
        return this;
    }

    public long getLinkType() {
        return capi.rtnl_tc_get_linktype(ptr);
    }

    public TcObject setLinkType(long linkType) {
        capi.rtnl_tc_set_linktype(ptr, linkType);
        return this;
    }

    public long getHandle() {
        return capi.rtnl_tc_get_handle(ptr);
    }

    public TcObject setHandle(long handle) {
        capi.rtnl_tc_set_handle(ptr, handle);
        return this;
    }

    public long getParent() {
        return capi.rtnl_tc_get_parent(ptr);
    }

    public TcObject setParent(long parent) {
        capi.rtnl_tc_set_parent(ptr, parent);
        return this;
    }

}
