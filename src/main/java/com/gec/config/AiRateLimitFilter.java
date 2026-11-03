package com.gec.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 接口限流：每个客户端 IP 在固定窗口内最多发起 {@link #MAX_REQUESTS_PER_WINDOW} 次调用，
 * 超出返回 429。公开部署后防止接口被刷——每次调用都会真实消耗大模型 token。
 * <p>
 * 限流窗口是进程内存态（滑动窗口实现，基于每 IP 的时间戳队列），
 * 重启清零；单实例演示场景足够，多实例部署可替换为 Redis/Zookeeper 方案。
 */
@Component
public class AiRateLimitFilter extends OncePerRequestFilter {

    /** 限流窗口：60 秒 */
    private static final long WINDOW_MILLIS = 60_000L;
    /** 每 IP 每窗口最大请求数（覆盖正常连聊速度，足够面试官演示用） */
    private static final int MAX_REQUESTS_PER_WINDOW = 20;

    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 只限 AI 相关端点，静态页/健康检查不受影响
        String path = request.getRequestURI();
        return !path.startsWith("/ai/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String ip = clientIp(request);
        long now = System.currentTimeMillis();
        Deque<Long> timestamps = hits.computeIfAbsent(ip, k -> new ArrayDeque<>());

        boolean allowed;
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() > WINDOW_MILLIS) {
                timestamps.pollFirst(); // 移除窗口外的旧记录
            }
            if (timestamps.size() >= MAX_REQUESTS_PER_WINDOW) {
                allowed = false;
            } else {
                timestamps.addLast(now);
                allowed = true;
            }
        }

        if (!allowed) {
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"error\":\"请求过于频繁，请稍后再试（每 IP 每分钟最多 "
                            + MAX_REQUESTS_PER_WINDOW + " 次）\"}");
            return;
        }

        // 表过大时清理过期 IP，防止内存无界增长
        if (hits.size() > 10_000) {
            hits.entrySet().removeIf(e -> {
                synchronized (e.getValue()) {
                    return e.getValue().isEmpty() || now - e.getValue().peekLast() > WINDOW_MILLIS;
                }
            });
        }

        chain.doFilter(request, response);
    }

    /** 反向代理（Render/Koyeb 等）透传真实 IP 的场景，X-Forwarded-For 取最左地址 */
    private String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
