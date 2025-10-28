package com.aredondocharro.ClothingStore.security.filter;

import com.aredondocharro.ClothingStore.security.port.AuthPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestControllerForAccessTokenFilter {
    @GetMapping("/test/me")
    String me(@AuthenticationPrincipal AuthPrincipal principal) {
        return principal == null ? "null" : principal.userId();
    }
}
