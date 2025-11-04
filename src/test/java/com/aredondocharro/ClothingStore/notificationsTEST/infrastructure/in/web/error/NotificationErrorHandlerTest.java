package com.aredondocharro.ClothingStore.notificationsTEST.infrastructure.in.web.error;

import com.aredondocharro.ClothingStore.notification.domain.exception.EmailSendFailedException;
import com.aredondocharro.ClothingStore.notification.domain.port.in.SendEmailUseCase;
import com.aredondocharro.ClothingStore.notification.infrastructure.in.web.EmailController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.thymeleaf.exceptions.TemplateInputException;
import org.springframework.context.NoSuchMessageException;
import com.aredondocharro.ClothingStore.testconfig.TestSecurityConfig;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = EmailController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestSecurityConfig.class)
class NotificationErrorHandlerTest {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    SendEmailUseCase useCase;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private String validJson() {
        return """
        {
          "from": "me@example.com",
          "to": ["you@example.com"],
          "templateId": "verify-email",
          "model": { "verificationUrl": "http://x" },
          "locale": "es_ES"
        }
        """;
    }

    @Test
    @DisplayName("validation error -> 400 notification.validation_error con fieldErrors")
    void validation_error_400() throws Exception {
        String json = """
        {
          "to": [],
          "templateId": "",
          "model": { },
          "locale": "en"
        }
        """;

        MvcResult result = mvc.perform(post("/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("notification.validation_error"))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        JsonNode root = MAPPER.readTree(body);
        JsonNode fieldErrors = root.get("fieldErrors");

        assertNotNull(fieldErrors, "fieldErrors no debería ser null");
        assertTrue(fieldErrors.isArray(), "fieldErrors debería ser un array");
        assertTrue(fieldErrors.size() > 0, "fieldErrors debería contener al menos un error");
    }

    @Test
    @DisplayName("JSON malformado -> 400 notification.bad_request")
    void unreadable_json_400() throws Exception {
        mvc.perform(post("/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("notification.bad_request"));
    }

    @Test
    @DisplayName("TemplateInputException -> 400 notification.template_error")
    void template_error_400() throws Exception {
        doThrow(new TemplateInputException("bad template"))
                .when(useCase).send(eq("me@example.com"), eq(List.of("you@example.com")), eq("verify-email"), anyMap(), any());

        mvc.perform(post("/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("notification.template_error"));
    }

    @Test
    @DisplayName("NoSuchMessageException -> 400 notification.i18n_missing_message")
    void missing_subject_key_400() throws Exception {
        doThrow(new NoSuchMessageException("mail.reset.subject"))
                .when(useCase).send(eq("me@example.com"), eq(List.of("you@example.com")), eq("verify-email"), anyMap(), any());

        mvc.perform(post("/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("notification.i18n_missing_message"));
    }

    @Test
    @DisplayName("EmailSendFailedException -> 502 notification.email_send_failed")
    void messaging_exception_502() throws Exception {
        // No uses MessagingException (checked) en el use case si el método no la declara.
        // Simula el fallo SMTP con tu excepción de dominio (runtime).
        doThrow(new EmailSendFailedException("SMTP error", new RuntimeException("SMTP error")))
                .when(useCase).send(eq("me@example.com"), eq(List.of("you@example.com")), eq("verify-email"), anyMap(), any());

        mvc.perform(post("/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("notification.email_send_failed"));
    }

    @Test
    @DisplayName("IllegalArgumentException del use case -> 400 notification.bad_request")
    void illegal_argument_400() throws Exception {
        doThrow(new IllegalArgumentException("invalid model"))
                .when(useCase).send(eq("me@example.com"), eq(List.of("you@example.com")), eq("verify-email"), anyMap(), any());

        mvc.perform(post("/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("notification.bad_request"));
    }

    @Test
    @DisplayName("GET /email -> 405 (sin body JSON)")
    void method_not_allowed_405() throws Exception {
        mvc.perform(get("/email"))
                .andExpect(status().isMethodNotAllowed());
    }
}
