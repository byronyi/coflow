package netlink;

import netlink.swig.capi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetlinkUtils {

    static public long getIfindexByName(String name) {
        return capi.if_nametoindex(name);
    }

    static public int stringToClass(String classId) {
        Pattern p = Pattern.compile("([0-9]+):([0-9]+)");
        Matcher m = p.matcher(classId);
        if (!m.matches()) {
            throw new IllegalArgumentException("class_id has to be in major:minor format");
        }
        int major = Integer.parseInt(m.group(1));
        int minor = Integer.parseInt(m.group(2));
        if (major > 0xffff || minor > 0xffff) {
            throw new IllegalArgumentException("both major and minor in class_id must be smaller than 0xffff");
        }
        return (major << 16) | minor;
    }
}
