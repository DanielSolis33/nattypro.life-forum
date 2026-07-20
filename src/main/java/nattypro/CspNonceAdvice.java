package nattypro.life.forum;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class CspNonceAdvice {

    @ModelAttribute("cspNonce")
    public String cspNonce(HttpServletRequest request) {
        Object nonce = request.getAttribute("cspNonce");
        return nonce != null ? nonce.toString() : "";
    }
}