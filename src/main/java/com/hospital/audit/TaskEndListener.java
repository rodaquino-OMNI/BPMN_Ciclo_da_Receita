package com.hospital.audit;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Execution Listener for Task End Events
 *
 * Logs audit trail when service tasks complete execution in the Revenue Cycle processes.
 * Captures task outcomes, duration, modified variables, and compliance metrics.
 *
 * @see com.hospital.audit.TaskStartListener
 */
public class TaskEndListener implements ExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskEndListener.class);

    @Override
    public void notify(DelegateExecution execution) throws Exception {
        try {
            // Capture audit metadata
            Map<String, Object> auditData = new HashMap<>();
            auditData.put("taskId", execution.getCurrentActivityId());
            auditData.put("taskName", execution.getCurrentActivityName());
            auditData.put("processInstanceId", execution.getProcessInstanceId());
            auditData.put("executionId", execution.getId());
            auditData.put("businessKey", execution.getProcessBusinessKey());
            auditData.put("timestamp", LocalDateTime.now());
            auditData.put("eventType", "TASK_END");

            // Calculate task duration
            Long startTime = (Long) execution.getVariable("taskStartTime_" + execution.getCurrentActivityId());
            if (startTime != null) {
                long duration = System.currentTimeMillis() - startTime;
                auditData.put("durationMs", duration);

                // Log SLA warnings if task exceeded thresholds
                checkSLACompliance(execution.getCurrentActivityId(), duration);

                // Clean up temporary variable
                execution.removeVariable("taskStartTime_" + execution.getCurrentActivityId());
            }

            // Capture task outcome and modified variables
            auditData.put("outcome", captureTaskOutcome(execution));
            auditData.put("modifiedVariables", captureModifiedVariables(execution));

            // Log audit entry
            LOGGER.info("Task Completed: {} [{}] - Process: {} - Duration: {}ms",
                execution.getCurrentActivityName(),
                execution.getCurrentActivityId(),
                execution.getProcessInstanceId(),
                auditData.get("durationMs"));

            // TODO: Persist to audit database
            // auditRepository.save(auditData);

        } catch (Exception e) {
            LOGGER.error("Error in TaskEndListener for task {}: {}",
                execution.getCurrentActivityId(), e.getMessage(), e);
            // Don't throw exception to avoid blocking process execution
        }
    }

    /**
     * Checks SLA compliance for critical tasks
     */
    private void checkSLACompliance(String taskId, long durationMs) {
        // ANS critical task thresholds (configurable)
        Map<String, Long> slaThresholds = new HashMap<>();
        slaThresholds.put("ServiceTask_SolicitarAutorizacao", 4 * 60 * 60 * 1000L); // 4 hours
        slaThresholds.put("Task_Submit_Webservice", 30 * 60 * 1000L); // 30 minutes
        slaThresholds.put("Task_Submit_Appeal", 24 * 60 * 60 * 1000L); // 24 hours

        Long threshold = slaThresholds.get(taskId);
        if (threshold != null && durationMs > threshold) {
            LOGGER.warn("SLA VIOLATION: Task {} exceeded threshold {}ms - Actual: {}ms",
                taskId, threshold, durationMs);
            // TODO: Trigger SLA violation alert
        }
    }

    /**
     * Captures task outcome based on output variables
     */
    private String captureTaskOutcome(DelegateExecution execution) {
        // Check for common outcome indicators
        Object result = execution.getVariable("result");
        Object status = execution.getVariable("status");
        Object success = execution.getVariable("success");

        if (Boolean.TRUE.equals(success)) return "SUCCESS";
        if (Boolean.FALSE.equals(success)) return "FAILURE";
        if (result != null) return result.toString();
        if (status != null) return status.toString();

        return "COMPLETED";
    }

    /**
     * Captures variables modified during task execution
     */
    private Map<String, Object> captureModifiedVariables(DelegateExecution execution) {
        Map<String, Object> modifiedVars = new HashMap<>();

        // Output variables based on task type
        addIfExists(modifiedVars, execution, "autorizacaoNecessaria");
        addIfExists(modifiedVars, execution, "autorizacaoConcedida");
        addIfExists(modifiedVars, execution, "triagemId");
        addIfExists(modifiedVars, execution, "atendimentoId");
        addIfExists(modifiedVars, execution, "protocolNumber");
        addIfExists(modifiedVars, execution, "batchStatus");
        addIfExists(modifiedVars, execution, "appealResult");
        addIfExists(modifiedVars, execution, "allocationResult");

        return modifiedVars;
    }

    private void addIfExists(Map<String, Object> map, DelegateExecution execution, String varName) {
        Object value = execution.getVariable(varName);
        if (value != null) {
            map.put(varName, value);
        }
    }
}
