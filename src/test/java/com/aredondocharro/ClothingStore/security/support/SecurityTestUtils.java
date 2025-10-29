package com.aredondocharro.ClothingStore.security.support;

import com.aredondocharro.ClothingStore.security.port.AuthPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Arrays;
import java.util.List;

public final class SecurityTestUtils {
    private SecurityTestUtils() {}

    public static RequestPostProcessor authPrincipal(String userId, String... roles) {
        List<String> roleNames = Arrays.stream(roles)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .toList();

        var principal = AuthPrincipal.of(userId, roleNames);

        var auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                roleNames.stream().map(SimpleGrantedAuthority::new).toList()
        );
        return SecurityMockMvcRequestPostProcessors.authentication(auth);
    }
}
