package com.aredondocharro.ClothingStore.notificationsTEST.adapter.in.web;


import com.aredondocharro.ClothingStore.notification.adapter.port.in.web.EmailController;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmailController.class)
class EmailControllerTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    SendEmailUseCase useCase;

    @Test
    @DisplayName("returns 202 Accepted on valid request")
    void accepted_on_valid() throws Exception {
        String json = """
        {
          "from": "me@example.com",
          "to": ["you@example.com"],
          "subject": "Hello",
          "body": "Hi there",
          "html": false
        }
        """;

        mvc.perform(post("/email")
                        .with(csrf())
                        .with(user("tester").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isAccepted());

        Mockito.verify(useCase).send(
                eq("me@example.com"),
                eq(java.util.List.of("you@example.com")),
                eq("Hello"), eq("Hi there"), eq(false));
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