package net.furizon.zebra_proxy.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;

public class SecurityUtils {

    public static final int MAX_IP_LENGTH = 40;
    public static @NotNull String getRealIp(@NotNull HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0) {
            ip = request.getRemoteAddr();
        } else {
            //Efficiently cut the string at min(max_ip_len, indexOf(','))
            // This is efficient even if we receive a huge ass x-forwarded-for payload
            int len = ip.length();
            int cap = Math.min(len, MAX_IP_LENGTH);
            int i = ip.indexOf(',', 0, cap);
            if (i == -1) {
                if (cap != len) {
                    ip = ip.substring(0, cap);
                }
            } else {
                ip = ip.substring(0, i);
            }
        }
        return ip;
    }
}
