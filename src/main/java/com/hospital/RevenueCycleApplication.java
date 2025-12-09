package com.hospital;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot Application for Hospital Revenue Cycle Management
 *
 * This application orchestrates the complete revenue cycle using Camunda 7 BPMN:
 * - Patient registration and eligibility verification
 * - Clinical attendance and medical coding
 * - Billing and submission to insurance providers
 * - Payment collection and denial management
 * - Financial reconciliation and reporting
 *
 * @author Revenue Cycle Development Team
 * @version 1.0.0
 */
@SpringBootApplication
@EnableScheduling
public class RevenueCycleApplication {

    public static void main(String[] args) {
        SpringApplication.run(RevenueCycleApplication.class, args);
    }
}
