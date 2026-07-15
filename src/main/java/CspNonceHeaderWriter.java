package nattypro.life.forum;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.security.web.header.HeaderWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CspNonceHeaderWriter implements HeaderWriter {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void writeHeaders(HttpServletRequest request, HttpServletResponse response) {
        byte[] nonceBytes = new byte[16];
        secureRandom.nextBytes(nonceBytes);
        String nonce = Base64.getUrlEncoder().withoutPadding().encodeToString(nonceBytes);
        request.setAttribute("cspNonce", nonce);

        String csp = "default-src 'self'; " +
            "script-src 'self' 'nonce-" + nonce + "' static.cloudflareinsights.com cdn.jsdelivr.net cdnjs.cloudflare.com blob: https://challenges.cloudflare.com; " +
            "style-src 'self' 'unsafe-inline' fonts.googleapis.com cdnjs.cloudflare.com cdn.jsdelivr.net; " +
            "font-src fonts.gstatic.com cdnjs.cloudflare.com cdn.jsdelivr.net; " +
           "img-src 'self' i.ytimg.com data: blob: images.nattypro.life nattypro-images.s3.us-east-2.amazonaws.com cdn.jsdelivr.net; " +
            "frame-src https://www.youtube.com https://challenges.cloudflare.com; " +
            "connect-src 'self' https://www.youtube.com";

        response.setHeader("Content-Security-Policy", csp);
    }
}