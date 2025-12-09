package com.hospital.performance;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import java.time.Duration;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Performance tests for Revenue Cycle using Gatling.
 * Tests load, throughput, and response times under various scenarios.
 */
public class RevenueCyclePerformanceTest extends Simulation {

    // Test configuration
    private static final String BASE_URL = System.getProperty("base.url", "http://localhost:8080");
    private static final int USERS_PER_SECOND = Integer.parseInt(System.getProperty("users.per.second", "10"));
    private static final int TEST_DURATION_MINUTES = Integer.parseInt(System.getProperty("test.duration", "5"));

    // HTTP protocol configuration
    HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json")
        .userAgentHeader("Gatling-PerformanceTest");

    // Feeder for test data
    Iterator<Map<String, Object>> patientFeeder = Stream.generate((Supplier<Map<String, Object>>) () -> {
        String cpf = String.format("%011d", (long) (Math.random() * 100000000000L));
        return Map.of(
            "patientCPF", cpf,
            "patientName", "Patient_" + cpf,
            "phone", "11987654321",
            "email", "patient" + cpf + "@test.com"
        );
    }).iterator();

    // Scenario 1: Patient Registration
    ScenarioBuilder patientRegistrationScenario = scenario("Patient Registration")
        .feed(patientFeeder)
        .exec(
            http("Create Patient")
                .post("/api/patients")
                .body(StringBody("""
                    {
                        "cpf": "#{patientCPF}",
                        "name": "#{patientName}",
                        "phone": "#{phone}",
                        "email": "#{email}"
                    }
                    """))
                .check(status().is(201))
                .check(jsonPath("$.patientId").saveAs("patientId"))
        )
        .pause(Duration.ofSeconds(1))
        .exec(
            http("Get Patient")
                .get("/api/patients/#{patientId}")
                .check(status().is(200))
        );

    // Scenario 2: Appointment Scheduling
    ScenarioBuilder appointmentSchedulingScenario = scenario("Appointment Scheduling")
        .feed(patientFeeder)
        .exec(
            http("Check Availability")
                .get("/api/appointments/availability")
                .queryParam("serviceType", "CONSULTATION")
                .queryParam("date", "2025-12-15")
                .check(status().is(200))
                .check(jsonPath("$.availableSlots[0]").saveAs("availableSlot"))
        )
        .pause(Duration.ofMillis(500))
        .exec(
            http("Book Appointment")
                .post("/api/appointments")
                .body(StringBody("""
                    {
                        "patientCPF": "#{patientCPF}",
                        "serviceType": "CONSULTATION",
                        "dateTime": "#{availableSlot}"
                    }
                    """))
                .check(status().is(201))
                .check(jsonPath("$.appointmentId").saveAs("appointmentId"))
        )
        .pause(Duration.ofSeconds(1))
        .exec(
            http("Get Appointment")
                .get("/api/appointments/#{appointmentId}")
                .check(status().is(200))
        );

    // Scenario 3: Authorization Request
    ScenarioBuilder authorizationScenario = scenario("Authorization Request")
        .feed(patientFeeder)
        .exec(
            http("Request Authorization")
                .post("/api/authorizations")
                .body(StringBody("""
                    {
                        "patientCPF": "#{patientCPF}",
                        "insuranceId": "UNIMED",
                        "procedureCodes": ["31001192"],
                        "cidCodes": ["K80.2"]
                    }
                    """))
                .check(status().is(201))
                .check(jsonPath("$.authorizationNumber").saveAs("authNumber"))
        )
        .pause(Duration.ofSeconds(2))
        .exec(
            http("Check Authorization Status")
                .get("/api/authorizations/#{authNumber}/status")
                .check(status().is(200))
                .check(jsonPath("$.status").in("PENDING", "APPROVED", "DENIED"))
        );

    // Scenario 4: Billing Submission
    ScenarioBuilder billingSubmissionScenario = scenario("Billing Submission")
        .feed(patientFeeder)
        .exec(
            http("Calculate Bill")
                .post("/api/billing/calculate")
                .body(StringBody("""
                    {
                        "accountId": "ACC_#{patientCPF}",
                        "procedureCodes": ["10101012"],
                        "materials": []
                    }
                    """))
                .check(status().is(200))
                .check(jsonPath("$.totalAmount").saveAs("totalAmount"))
        )
        .pause(Duration.ofMillis(500))
        .exec(
            http("Generate TISS Guide")
                .post("/api/billing/tiss")
                .body(StringBody("""
                    {
                        "accountId": "ACC_#{patientCPF}",
                        "insuranceId": "UNIMED"
                    }
                    """))
                .check(status().is(201))
                .check(jsonPath("$.tissGuideNumber").saveAs("tissGuide"))
        )
        .pause(Duration.ofSeconds(1))
        .exec(
            http("Submit to Insurance")
                .post("/api/billing/submit")
                .body(StringBody("""
                    {
                        "tissGuideNumber": "#{tissGuide}"
                    }
                    """))
                .check(status().is(200))
                .check(jsonPath("$.protocol").exists())
        );

    // Scenario 5: Complete Revenue Cycle
    ScenarioBuilder fullCycleScenario = scenario("Full Revenue Cycle")
        .feed(patientFeeder)
        // Registration
        .exec(
            http("1. Create Patient")
                .post("/api/patients")
                .body(StringBody("""
                    {
                        "cpf": "#{patientCPF}",
                        "name": "#{patientName}",
                        "insuranceId": "UNIMED"
                    }
                    """))
                .check(status().is(201))
                .check(jsonPath("$.patientId").saveAs("patientId"))
        )
        .pause(Duration.ofMillis(500))
        // Scheduling
        .exec(
            http("2. Book Appointment")
                .post("/api/appointments")
                .body(StringBody("""
                    {
                        "patientId": "#{patientId}",
                        "serviceType": "CONSULTATION"
                    }
                    """))
                .check(status().is(201))
                .check(jsonPath("$.appointmentId").saveAs("appointmentId"))
        )
        .pause(Duration.ofMillis(500))
        // Admission
        .exec(
            http("3. Create Admission")
                .post("/api/admissions")
                .body(StringBody("""
                    {
                        "patientId": "#{patientId}",
                        "admissionType": "OUTPATIENT"
                    }
                    """))
                .check(status().is(201))
                .check(jsonPath("$.admissionId").saveAs("admissionId"))
        )
        .pause(Duration.ofSeconds(1))
        // Discharge
        .exec(
            http("4. Process Discharge")
                .post("/api/admissions/#{admissionId}/discharge")
                .body(StringBody("""
                    {
                        "dischargeType": "REGULAR",
                        "cidCodes": ["Z00.0"]
                    }
                    """))
                .check(status().is(200))
        )
        .pause(Duration.ofMillis(500))
        // Billing
        .exec(
            http("5. Submit Billing")
                .post("/api/billing/submit")
                .body(StringBody("""
                    {
                        "admissionId": "#{admissionId}"
                    }
                    """))
                .check(status().is(200))
        );

    // Scenario 6: Concurrent Authorization Requests
    ScenarioBuilder concurrentAuthorizationScenario = scenario("Concurrent Authorizations")
        .feed(patientFeeder)
        .repeat(5).on(
            exec(
                http("Request Authorization")
                    .post("/api/authorizations")
                    .body(StringBody("""
                        {
                            "patientCPF": "#{patientCPF}",
                            "insuranceId": "UNIMED",
                            "procedureCodes": ["31001192"]
                        }
                        """))
                    .check(status().is(201))
            )
            .pause(Duration.ofMillis(100))
        );

    // Load profiles
    {
        // Smoke test: Verify basic functionality
        setUp(
            patientRegistrationScenario.injectOpen(
                rampUsers(5).during(Duration.ofSeconds(10))
            ).protocols(httpProtocol)
        ).assertions(
            global().responseTime().max().lt(2000),
            global().successfulRequests().percent().gt(95.0)
        );
    }

    {
        // Load test: Normal operating conditions
        setUp(
            patientRegistrationScenario.injectOpen(
                constantUsersPerSec(USERS_PER_SECOND).during(Duration.ofMinutes(TEST_DURATION_MINUTES))
            ),
            appointmentSchedulingScenario.injectOpen(
                constantUsersPerSec(USERS_PER_SECOND / 2).during(Duration.ofMinutes(TEST_DURATION_MINUTES))
            ),
            authorizationScenario.injectOpen(
                constantUsersPerSec(USERS_PER_SECOND / 4).during(Duration.ofMinutes(TEST_DURATION_MINUTES))
            )
        ).protocols(httpProtocol)
        .assertions(
            global().responseTime().percentile3().lt(3000), // 95th percentile < 3s
            global().responseTime().percentile4().lt(5000), // 99th percentile < 5s
            global().successfulRequests().percent().gt(99.0)
        );
    }

    {
        // Stress test: Push system to limits
        setUp(
            fullCycleScenario.injectOpen(
                incrementUsersPerSec(5)
                    .times(10)
                    .eachLevelLasting(Duration.ofSeconds(30))
                    .separatedByRampsLasting(Duration.ofSeconds(10))
                    .startingFrom(10)
            )
        ).protocols(httpProtocol)
        .assertions(
            global().responseTime().mean().lt(5000),
            global().failedRequests().percent().lt(5.0)
        );
    }

    {
        // Spike test: Sudden traffic increase
        setUp(
            appointmentSchedulingScenario.injectOpen(
                nothingFor(Duration.ofSeconds(10)),
                atOnceUsers(100),
                nothingFor(Duration.ofSeconds(30)),
                atOnceUsers(200)
            )
        ).protocols(httpProtocol)
        .assertions(
            global().responseTime().max().lt(10000),
            global().successfulRequests().percent().gt(90.0)
        );
    }

    {
        // Soak test: Extended duration for memory leaks
        setUp(
            patientRegistrationScenario.injectOpen(
                constantUsersPerSec(5).during(Duration.ofMinutes(30))
            ),
            appointmentSchedulingScenario.injectOpen(
                constantUsersPerSec(3).during(Duration.ofMinutes(30))
            )
        ).protocols(httpProtocol)
        .assertions(
            global().responseTime().mean().lt(2000),
            global().successfulRequests().percent().gt(99.5)
        );
    }

    {
        // Concurrent user test: Simulate peak hospital hours
        setUp(
            patientRegistrationScenario.injectOpen(
                rampConcurrentUsers(10).to(100).during(Duration.ofMinutes(2)),
                constantConcurrentUsers(100).during(Duration.ofMinutes(3))
            ),
            appointmentSchedulingScenario.injectOpen(
                rampConcurrentUsers(5).to(50).during(Duration.ofMinutes(2)),
                constantConcurrentUsers(50).during(Duration.ofMinutes(3))
            ),
            authorizationScenario.injectOpen(
                rampConcurrentUsers(2).to(20).during(Duration.ofMinutes(2)),
                constantConcurrentUsers(20).during(Duration.ofMinutes(3))
            )
        ).protocols(httpProtocol)
        .assertions(
            global().responseTime().percentile3().lt(4000),
            global().successfulRequests().percent().gt(98.0)
        );
    }
}
