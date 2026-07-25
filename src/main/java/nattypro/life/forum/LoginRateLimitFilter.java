package nattypro.life.forum;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class LoginRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public LoginRateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isLoginAttempt(request)) {
            String ip = resolveClientIp(request);
            if (!rateLimitService.isAllowed(ip)) {
                response.setStatus(429);
                response.getWriter().write("Too many login attempts. Please wait a few minutes and try again.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginAttempt(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && "/login".equals(request.getRequestURI());
    }

    private String resolveClientIp(HttpServletRequest request) {
        String cfIp = request.getHeader("CF-Connecting-IP");
        return (cfIp != null && !cfIp.isBlank()) ? cfIp : request.getRemoteAddr();
    }
}