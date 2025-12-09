package com.hospital.audit;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Execution Listener for Task Start Events
 *
 * Logs audit trail when service tasks start execution in the Revenue Cycle processes.
 * Captures task metadata, process variables, and timestamps for ANS compliance tracking.
 *
 * @see com.hospital.audit.TaskEndListener
 */
public class TaskStartListener implements ExecutionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskStartListener.class);

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
            auditData.put("eventType", "TASK_START");

            // Capture relevant process variables (configurable based on task)
            auditData.put("variables", captureRelevantVariables(execution));

            // Log audit entry
            LOGGER.info("Task Started: {} [{}] - Process: {}",
                execution.getCurrentActivityName(),
                execution.getCurrentActivityId(),
                execution.getProcessInstanceId());

            // TODO: Persist to audit database
            // auditRepository.save(auditData);

            // Store start time in execution for duration calculation
            execution.setVariable("taskStartTime_" + execution.getCurrentActivityId(),
                System.currentTimeMillis());

        } catch (Exception e) {
            LOGGER.error("Error in TaskStartListener for task {}: {}",
                execution.getCurrentActivityId(), e.getMessage(), e);
            // Don't throw exception to avoid blocking process execution
        }
    }

    /**
     * Captures relevant process variables based on task context
     */
    private Map<String, Object> captureRelevantVariables(DelegateExecution execution) {
        Map<String, Object> relevantVars = new HashMap<>();

        // Common variables across all processes
        addIfExists(relevantVars, execution, "pacienteId");
        addIfExists(relevantVars, execution, "convenio");
        addIfExists(relevantVars, execution, "atendimentoId");

        // Financial process variables
        addIfExists(relevantVars, execution, "accountId");
        addIfExists(relevantVars, execution, "batchNumber");
        addIfExists(relevantVars, execution, "totalValue");
        addIfExists(relevantVars, execution, "denialId");
        addIfExists(relevantVars, execution, "transactionId");

        return relevantVars;
    }

    private void addIfExists(Map<String, Object> map, DelegateExecution execution, String varName) {
        Object value = execution.getVariable(varName);
        if (value != null) {
            map.put(varName, value);
        }
    }
}
