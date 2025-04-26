package com.linkauto.restapi.performance;

import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.linkauto.restapi.model.User;
import com.linkauto.restapi.model.User.Gender;
import com.linkauto.restapi.service.AuthService;
import com.linkauto.restapi.service.LinkAutoService;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(JUnitPerfInterceptor.class)
public class AuthServicePerformanceTest {
    
    @Autowired
    private AuthService authService;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "testpass";
    private static String testToken;

    @JUnitPerfTestActiveConfig
    private final static JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(new HtmlReportGenerator(System.getProperty("user.dir") + "/target/reports/authPerf-report.html"))
            .build();
       

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 5000, warmUpMs = 2000)
    @JUnitPerfTestRequirement(
        executionsPerSec = 100, meanLatency = 10, maxLatency = 1500, minLatency = 10
    )    
    public void testRegisterPerformance() {
        User user = new User("testuser", "testName", "testProfilePicture", "testEmail", new ArrayList<>(), 123456L, Gender.MALE, "testLocation", "testpass", "testDescription", new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        authService.register(user);
    }

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 5000, warmUpMs = 2000)
    @JUnitPerfTestRequirement(
        executionsPerSec = 100, meanLatency = 10, maxLatency = 1500, minLatency = 10
    )    
    public void testLoginPerformance() {
        testToken = authService.login("testUsername", "testPassword");
    }


    @Test
    @JUnitPerfTest(threads = 10, durationMs = 5000, warmUpMs = 2000)
    @JUnitPerfTestRequirement(
        executionsPerSec = 100, meanLatency = 10, maxLatency = 1500, minLatency = 10
    )
    public void testLogoutPerformance() {
        authService.logout(testToken);
    }

}