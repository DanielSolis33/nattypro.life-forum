package nattypro.life.forum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
          CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // Admin — must hold ROLE_ADMIN. (Was permitAll: a real hole.)
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Static assets — open so guest pages render fully
                .requestMatchers("/favicon.ico", "/css/**", "/js/**",
                                 "/images/**", "/static/**").permitAll()

                // GUEST read-only browsing: home, category pages, single post, search.
                // GET only — POST /create, /delete/*, /post/*/comment fall through to authenticated().
                // "/post/*" matches /post/123 but NOT /post/123/edit, so the edit form stays gated.
                .requestMatchers(HttpMethod.GET, "/", "/post/*", "/search").permitAll()

                // Auth / account / public info pages
                .requestMatchers(
                     "/error","/register", "/register/age", "/register/rules",
                    "/join",
                    "/login", "/forgot-password", "/reset-password", "/confirm-email",
                    "/h2-console/**",
                    "/privacy-policy", "/terms-of-service",
                    "/community-guidelines", "/sponsors"
                ).permitAll()

                // Everything else — incl. /ws/** (chat socket) and /profile/** — needs login.
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", false)   // false = send guest back to the page they tried to reach
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login")
                .permitAll()
            )
           .csrf(csrf -> csrf
    .ignoringRequestMatchers("/h2-console/**", "/ws/**")
)

            .headers(headers -> {
                headers.contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' static.cloudflareinsights.com cdn.jsdelivr.net cdnjs.cloudflare.com blob:; " +
                        "style-src 'self' 'unsafe-inline' fonts.googleapis.com cdnjs.cloudflare.com cdn.jsdelivr.net; " +
                        "font-src fonts.gstatic.com cdnjs.cloudflare.com cdn.jsdelivr.net; " +
                        "img-src 'self' i.ytimg.com data: blob: nattypro-images.s3.us-east-2.amazonaws.com cdn.jsdelivr.net; " +
                        "frame-src https://www.youtube.com; " +
                        "connect-src 'self' https://www.youtube.com")
                );
                headers.frameOptions(frame -> frame.sameOrigin());
            });
        return http.build();
    }
}