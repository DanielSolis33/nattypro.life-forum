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
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/favicon.ico", "/css/**", "/js/**",
                                 "/images/**", "/static/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/", "/post/*", "/search").permitAll()
                .requestMatchers(
                    "/error", "/register", "/register/age", "/register/rules",
                    "/join",
                    "/login", "/forgot-password", "/reset-password", "/confirm-email",
                    "/h2-console/**",
                    "/privacy-policy", "/terms-of-service",
                    "/community-guidelines", "/sponsors"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", false)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                .logoutSuccessUrl("/login")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**", "/ws/**")
            )
            headers.contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' static.cloudflareinsights.com cdn.jsdelivr.net cdnjs.cloudflare.com blob: https://challenges.cloudflare.com; " +
                        "style-src 'self' 'unsafe-inline' fonts.googleapis.com cdnjs.cloudflare.com cdn.jsdelivr.net; " +
                        "font-src fonts.gstatic.com cdnjs.cloudflare.com cdn.jsdelivr.net; " +
                        "img-src 'self' i.ytimg.com data: blob: nattypro-images.s3.us-east-2.amazonaws.com cdn.jsdelivr.net; " +
                        "frame-src https://www.youtube.com https://challenges.cloudflare.com; " +
                        "connect-src 'self' https://www.youtube.com")
                );
                headers.frameOptions(frame -> frame.sameOrigin());
            });
        return http.build();
    }
}