%module capi
%{
#include <netlink/netlink.h>
#include <netlink/socket.h>
#include <netlink/errno.h>

#include <netlink/route/tc.h>
#include <netlink/route/qdisc.h>
#include <netlink/route/class.h>
#include <netlink/route/classifier.h>
#include <netlink/route/cls/u32.h>
#include <netlink/route/qdisc/htb.h>
%}

%include <stdint.i>
%include <cpointer.i>

/* <net/if.h> */

extern unsigned int if_nametoindex(const char *ifname);

/* <netlink/errno.h> */

extern const char *nl_geterror(int);

/* <netlink/netlink.h> */

extern int nl_connect(struct nl_sock *, int);
extern void nl_close(struct nl_sock *);

/* <netlink/socket.h> */

extern struct nl_sock *nl_socket_alloc(void);
extern void nl_socket_free(struct nl_sock *);

/* <netlink/route/tc.h> */

%inline %{
        uint32_t tc_str2handle(const char *name)
        {
                uint32_t result;

                if (rtnl_tc_str2handle(name, &result) < 0)
                        return 0;

                return result;
        }
%};
extern void rtnl_tc_set_ifindex(struct rtnl_tc *, int);
extern int rtnl_tc_get_ifindex(struct rtnl_tc *);
extern void rtnl_tc_set_mtu(struct rtnl_tc *, uint32_t);
extern uint32_t rtnl_tc_get_mtu(struct rtnl_tc *);
extern void rtnl_tc_set_mpu(struct rtnl_tc *, uint32_t);
extern uint32_t rtnl_tc_get_mpu(struct rtnl_tc *);
extern void rtnl_tc_set_overhead(struct rtnl_tc *, uint32_t);
extern uint32_t rtnl_tc_get_overhead(struct rtnl_tc *);
extern void rtnl_tc_set_linktype(struct rtnl_tc *, uint32_t);
extern uint32_t rtnl_tc_get_linktype(struct rtnl_tc *);
extern void rtnl_tc_set_handle(struct rtnl_tc *, uint32_t);
extern uint32_t rtnl_tc_get_handle(struct rtnl_tc *);
extern void rtnl_tc_set_parent(struct rtnl_tc *, uint32_t);
extern uint32_t rtnl_tc_get_parent(struct rtnl_tc *);
extern int rtnl_tc_set_kind(struct rtnl_tc *, const char *);
extern char *rtnl_tc_get_kind(struct rtnl_tc *);

/* <netlink/route/qdisc.h> */

%inline %{
        struct rtnl_tc *qdisc2tc(struct rtnl_qdisc *qdisc)
        {
                return TC_CAST(qdisc);
        }

        struct rtnl_tc *class2tc(struct rtnl_class *cl)
        {
                return TC_CAST(cl);
        }

        struct rtnl_tc *cls2tc(struct rtnl_cls *cls)
        {
                return TC_CAST(cls);
        }
%};

extern struct rtnl_qdisc *rtnl_qdisc_alloc(void);
extern void rtnl_qdisc_put(struct rtnl_qdisc *);

extern int rtnl_qdisc_add(struct nl_sock *, struct rtnl_qdisc *, int);
extern int rtnl_qdisc_update(struct nl_sock *, struct rtnl_qdisc *, struct rtnl_qdisc *, int);
extern int rtnl_qdisc_delete(struct nl_sock *, struct rtnl_qdisc *);

/* <netlink/route/class.h> */

extern struct rtnl_class *rtnl_class_alloc(void);
extern void rtnl_class_put(struct rtnl_class *);
extern int rtnl_class_add(struct nl_sock *, struct rtnl_class *, int);
extern int rtnl_class_delete(struct nl_sock *, struct rtnl_class *);

/* <netlink/route/classifier.h> */

extern struct rtnl_cls *rtnl_cls_alloc(void);
extern void rtnl_cls_put(struct rtnl_cls *);

extern int rtnl_cls_add(struct nl_sock *, struct rtnl_cls *, int);
extern int rtnl_cls_delete(struct nl_sock *, struct rtnl_cls *, int);

extern void rtnl_cls_set_prio(struct rtnl_cls *, uint16_t);
extern uint16_t rtnl_cls_get_prio(struct rtnl_cls *);

extern void rtnl_cls_set_protocol(struct rtnl_cls *, uint16_t);
extern uint16_t rtnl_cls_get_protocol(struct rtnl_cls *);

/* <netlink/route/cls/u32.h> */

extern void rtnl_u32_set_handle(struct rtnl_cls *, int, int, int);
extern int rtnl_u32_set_classid(struct rtnl_cls *, uint32_t);
extern int rtnl_u32_set_divisor(struct rtnl_cls *, uint32_t);
extern int rtnl_u32_set_link(struct rtnl_cls *, uint32_t);
extern int rtnl_u32_set_hashtable(struct rtnl_cls *, uint32_t);
extern int rtnl_u32_set_hashmask(struct rtnl_cls *, uint32_t, uint32_t);
extern int rtnl_u32_set_cls_terminal(struct rtnl_cls *);
extern int rtnl_u32_set_flags(struct rtnl_cls *, int);

extern int rtnl_u32_add_key_uint8(struct rtnl_cls *, uint8_t, uint8_t, int, int);
extern int rtnl_u32_add_key_uint16(struct rtnl_cls *, uint16_t, uint16_t, int, int);
extern int rtnl_u32_add_key_uint32(struct rtnl_cls *, uint32_t, uint32_t, int, int);

/* <netlink/route/qdisc/htb.h> */

extern uint32_t rtnl_htb_get_rate2quantum(struct rtnl_qdisc *);
extern int rtnl_htb_set_rate2quantum(struct rtnl_qdisc *, uint32_t);
extern uint32_t rtnl_htb_get_defcls(struct rtnl_qdisc *);
extern int rtnl_htb_set_defcls(struct rtnl_qdisc *, uint32_t);
extern uint32_t rtnl_htb_get_prio(struct rtnl_class *);
extern int rtnl_htb_set_prio(struct rtnl_class *, uint32_t);
extern uint32_t rtnl_htb_get_rate(struct rtnl_class *);
extern int rtnl_htb_set_rate(struct rtnl_class *, uint32_t);
extern uint32_t rtnl_htb_get_ceil(struct rtnl_class *);
extern int rtnl_htb_set_ceil(struct rtnl_class *, uint32_t);
extern uint32_t rtnl_htb_get_rbuffer(struct rtnl_class *);
extern int rtnl_htb_set_rbuffer(struct rtnl_class *, uint32_t);
extern uint32_t rtnl_htb_get_cbuffer(struct rtnl_class *);
extern int rtnl_htb_set_cbuffer(struct rtnl_class *, uint32_t);
extern uint32_t rtnl_htb_get_quantum(struct rtnl_class *);
extern int rtnl_htb_set_quantum(struct rtnl_class *, uint32_t);
extern int rtnl_htb_get_level(struct rtnl_class *);
