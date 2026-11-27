package erp.notification.config;

import erp.notification.exception.ApiError;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.ObjectMapper;

@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
class NotificationSecurity {
    private static final String[] USER_ROLES = {"ADMIN", "HQ_MANAGER", "HQ_STAFF", "BRANCH_MANAGER", "BRANCH_STAFF"};

    @Bean
    SecurityFilterChain notificationChain(HttpSecurity http, ObjectMapper mapper,
            @Value("${NOTIFICATION_PUBLISHERS:}") Set<String> publishers) throws Exception {
        AuthenticationEntryPoint unauthorized = (req, res, ex) -> {
            res.setHeader("WWW-Authenticate", "Bearer");
            write(mapper, res, 401, "UNAUTHORIZED", "유효한 액세스 토큰이 필요합니다.");
        };
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**", "/swagger-ui.html",
                                "/swagger-ui/**", "/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/notifications/admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/notifications").hasAnyRole(USER_ROLES)
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/notifications/*/read").hasAnyRole(USER_ROLES)
                        .requestMatchers(HttpMethod.POST, "/internal/notifications/events")
                        .access((authentication, context) -> new AuthorizationDecision(
                                authentication.get() instanceof JwtAuthenticationToken token
                                        && publishers.contains(token.getName())
                                        && token.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_NOTIFICATION_PUBLISHER"))))
                        .anyRequest().denyAll())
                .exceptionHandling(e -> e.authenticationEntryPoint(unauthorized)
                        .accessDeniedHandler((req, res, ex) -> write(mapper, res, 403, "FORBIDDEN", "요청 권한이 없습니다.")))
                .oauth2ResourceServer(resource -> resource
                        .authenticationEntryPoint(unauthorized)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(roleConverter())))
                .build();
    }

    private JwtAuthenticationConverter roleConverter() {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName("role");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(roles);
        return converter;
    }

    private static void write(ObjectMapper mapper, HttpServletResponse res, int status, String code, String message)
            throws IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        mapper.writeValue(res.getOutputStream(), new ApiError(code, message));
    }
}
