package com.mr486.msrisque.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mr486.msrisque.exception.RemoteServiceException;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class FeignConfigurationTest {

    private final FeignConfiguration configuration = new FeignConfiguration();

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(configuration, "user", "user");
        ReflectionTestUtils.setField(configuration, "password", "pass");
    }

    // Construit une réponse Feign factice avec le statut et le corps fournis.
    private Response reponse(int statut, String corps) {
        Request requete = Request.create(Request.HttpMethod.GET, "http://localhost/x",
                Collections.emptyMap(), Request.Body.empty(), new RequestTemplate());
        return Response.builder()
                .status(statut)
                .reason("reason")
                .request(requete)
                .headers(Collections.emptyMap())
                .body(corps, StandardCharsets.UTF_8)
                .build();
    }

    @Test
    void interceptor_ajouteLAuthentificationBasic() {
        RequestInterceptor interceptor = configuration.resourceBAuthInterceptor();
        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        String attendu = "Basic " + Base64.getEncoder()
                .encodeToString("user:pass".getBytes(StandardCharsets.UTF_8));
        assertThat(template.headers().get("Authorization")).contains(attendu);
    }

    @Test
    void errorDecoder_decodeLeCorpsDErreur() {
        ErrorDecoder decoder = configuration.errorDecoder(new ObjectMapper());

        Exception ex = decoder.decode("GET", reponse(404, "{\"status\":404,\"message\":\"boom\"}"));

        assertThat(ex).isInstanceOf(RemoteServiceException.class);
        RemoteServiceException rse = (RemoteServiceException) ex;
        assertThat(rse.getMessage()).isEqualTo("boom");
        assertThat(rse.getHttpStatus()).isEqualTo(404);
    }

    @Test
    void errorDecoder_replieSurUnMessageParDefautQuandCorpsInvalide() {
        ErrorDecoder decoder = configuration.errorDecoder(new ObjectMapper());

        Exception ex = decoder.decode("GET", reponse(503, "pas du json"));

        assertThat(ex).isInstanceOf(RemoteServiceException.class);
        assertThat(ex.getMessage()).contains("Le service ne répond pas");
        assertThat(((RemoteServiceException) ex).getHttpStatus()).isEqualTo(503);
    }
}
