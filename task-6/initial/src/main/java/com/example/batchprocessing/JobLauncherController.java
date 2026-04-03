package com.example.batchprocessing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class JobLauncherController {

    private static final Logger log = LoggerFactory.getLogger(JobLauncherController.class);

    private final JobLauncher jobLauncher;
    private final Job importProductJob;

    public JobLauncherController(JobLauncher jobLauncher, Job importProductJob) {
        this.jobLauncher = jobLauncher;
        this.importProductJob = importProductJob;
    }

    @PostMapping("/run-etl")
    public ResponseEntity<Map<String, String>> runEtlJob(HttpServletRequest request) {
        String traceId = MDC.get("traceId");
        String spanId = MDC.get("spanId");
        String uri = request.getRequestURI();

        log.info(">>> ETL Job launch requested. URI={}, traceId={}, spanId={}", uri, traceId, spanId);

        try {
            JobParameters params = new JobParametersBuilder()
                    .addLong("startedAt", System.currentTimeMillis())
                    .toJobParameters();

            var execution = jobLauncher.run(importProductJob, params);

            log.info("<<< ETL Job completed. status={}, traceId={}, spanId={}",
                    execution.getStatus(), traceId, spanId);

            return ResponseEntity.ok(Map.of(
                    "status", execution.getStatus().toString(),
                    "traceId", traceId != null ? traceId : "",
                    "spanId", spanId != null ? spanId : "",
                    "uri", uri
            ));
        } catch (Exception e) {
            log.error("ETL Job failed. traceId={}, spanId={}", traceId, spanId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "FAILED",
                    "error", e.getMessage(),
                    "traceId", traceId != null ? traceId : "",
                    "spanId", spanId != null ? spanId : ""
            ));
        }
    }
}
