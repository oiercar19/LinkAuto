package com.linkauto.client;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
class WebClientApplicationTest {

    @Autowired
    private RestTemplate restTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void mainMethodRuns() {
        WebClientApplication.main(new String[] {});
    }
    
    @Test
    void restTemplateBeanIsLoaded() {
        assertThat(restTemplate).isNotNull();
    }
}