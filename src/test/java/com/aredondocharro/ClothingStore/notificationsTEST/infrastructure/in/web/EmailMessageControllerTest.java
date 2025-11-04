package com.aredondocharro.ClothingStore.notificationsTEST.infrastructure.in.web;

import com.aredondocharro.ClothingStore.notification.infrastructure.in.web.EmailController;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.aredondocharro.ClothingStore.testconfig.TestSecurityConfig;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EmailController.class)
@Import(TestSecurityConfig.class)
class EmailMessageControllerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean SendEmailUseCase useCase;

    @Test
    @DisplayName("returns 202 Accepted on valid request")
    void accepted_on_valid() throws Exception {
        String json = """
    {
      "from": "me@example.com",
      "to": ["you@example.com"],
      "templateId": "verify-emailMessage",
      "model": { "verificationUrl": "http://x", "emailMessage": "you@example.com" },
      "locale": "en"
    }
    """;

        mvc.perform(post("/email")
                        .with(csrf())
                        .with(user("tester").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isAccepted());

        // Nueva firma del UseCase: (from, to, templateId, model, locale)
        Mockito.verify(useCase).send(
                eq("me@example.com"),
                eq(List.of("you@example.com")),
                eq("verify-emailMessage"),
                anyMap(),
                any()
        );
    }


    @Test
    @DisplayName("returns 400 Bad Request when validation fails")
    void bad_request_on_invalid() throws Exception {
        String json = """
        {
          "to": [],
          "subject": "",
          "body": "",
          "html": false
        }
        """;

        mvc.perform(post("/email")
                        .with(user("tester").roles("USER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }
}
