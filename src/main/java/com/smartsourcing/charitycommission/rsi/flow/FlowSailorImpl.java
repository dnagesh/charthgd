package uk.gov.ccew.rsi.flow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Service;
import uk.gov.ccew.rsi.exception.ActiveFlowException;
import uk.gov.ccew.rsi.exception.FlowException;
import uk.gov.ccew.rsi.flow.model.UserStep;

import java.util.*;
import java.util.function.Predicate;

import static uk.gov.ccew.rsi.flow.util.FormDataUtil.cleanFormData;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowSailorImpl implements FlowSailor {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final FormService formService;
    private final RepositoryService repositoryService;

    private static final String RSI_FLOW = "rsi_initial_flow";


    public void startOrResume(String businessKey) {
        if (businessKey == null || businessKey.isBlank()) {
            throw new FlowException("Business key cannot be null or empty");
        }


        var activeInstance = getActiveInstance(businessKey);

        if (activeInstance != null) {
            log.info("Resuming existing process instance for businessKey {}", businessKey);
            return;
        }

        try {
            runtimeService.startProcessInstanceByKey(RSI_FLOW, businessKey);
            log.info("Started new process instance for businessKey {}", businessKey);
        } catch (Exception e) {
            log.error("Failed to start process instance for businessKey: {}", businessKey, e);
            throw new ActiveFlowException("Failed to start workflow process", e);
        }
    }



    /**
     * Orchestrates progression through the RSI initial flow using Camunda services.
     * <p>
     * This implementation provides operations to start or resume process instances,
     * query the current user-facing task (step), complete tasks, move forward to the next step,
     * and jump backwards to a prior task ( null}. * and jump backwards to a prior task (by task definition key) within the same process instance.
     * @throws IllegalArgumentException if the business key is invalid.
     * @throws ActiveFlowException      if the process instance cannot be created or retrieved.
     * @throws FlowException            if the process has no active task.
     */


    @Override
    public Optional<UserStep> getCurrentStep(String businessKey) {
        if (isNullOrBlank.test(businessKey)){
            throw new IllegalArgumentException("Business key cannot be null or empty");
        }

        // Ensure process exists, is this the best?
        startOrResume(businessKey);

        var activeInstance = getActiveInstance(businessKey);
        if (activeInstance == null) {
            log.error("Active instance not found after startOrResume for businessKey: {}", businessKey);
            throw new ActiveFlowException("Failed to create or retrieve process instance for businessKey: " + businessKey);
        }

        Task task = getActiveTask(businessKey);
        if (task == null) {
            log.error("No active task found for businessKey: {} and instanceID: {}", businessKey, activeInstance.getId());
            throw new FlowException("Process instance exists but has no active task");
        }
        return Optional.of(toUserStep(task));
    }


    /**
     * Completes the current active task with the supplied form data.
     * <p>
     * Validates the existence of the process instance and active task, cleans the form data
     * and completes the task.
     * </p>
     *
     * @param businessKey A non-null, non-blank external identifier for the process instance.
     * @param formData    A map of form field values (maybe {@code null}); will be sanitised prior to submission.
     * @throws IllegalArgumentException if the business key is invalid.
     * @throws ActiveFlowException      if the process instance cannot be retrieved.
     * @throws FlowException            if there is no active task or completion fails.
     */

    @Override
    public void completeCurrent(String businessKey, Map<String, String> formData) {


        if (isNullOrBlank.test(businessKey)) {
            throw new IllegalArgumentException("Business key can not be null or empty");
        }


        var activeInstance = getActiveInstance(businessKey);
        if (activeInstance == null) {
            throw new ActiveFlowException("Failed to retrieve process instance for businessKey: " + businessKey);
        }

        Task task = getActiveTask(businessKey);
        if (task == null) {
            throw new FlowException("No active task to complete");
        }

        try {
            taskService.complete(task.getId(), cleanFormData(formData));
        } catch (Exception e) {
            log.error("Failed to complete task {} for businessKey: {}", task.getId(), businessKey, e);
            throw new FlowException("Failed to complete task", e);
        }
    }


    /**
     * Completes the current task and returns the next {@code UserStep} task, if any.
     * <p>
     * After completing the active task with the provided form data, the method queries
     * for the new active task and maps it to {@code UserStep}. If the process finishes
     * and no task is available, {@link Optional#empty()} is returned. The main difference with {@code completeCurrent}
     * is that this method completes the task, move the cursor forward and provides the next userStep to be rendered.
     * </p>
     *
     * @param businessKey A non-null, non-blank external identifier for the process instance.
     * @param formData    A map of form field values (maybe {@code null}); will be sanitised prior to submission.
     * @return An {@link Optional} containing the next {@code UserStep} if present; empty if the process has no new active task.
     * @throws FlowException       if the business key is invalid, no active task exists, or completion fails.
     * @throws ActiveFlowException if the process instance cannot be retrieved.
     */

    @Override
    public Optional<UserStep> next(String businessKey, Map<String, String> formData) {
        if (isNullOrBlank.test(businessKey)) {
            throw new FlowException("Business key cannot be null or empty");
        }

        var activeInstance = getActiveInstance(businessKey);
        if (activeInstance == null) {
            throw new ActiveFlowException("Failed to retrieve process instance for businessKey: " + businessKey);
        }

        Task task = getActiveTask(businessKey);
        if (task == null) {
            throw new FlowException("No active task found to move next");
        }

        try {
            taskService.complete(task.getId(), cleanFormData(formData));
            Task nextTask = getActiveTask(businessKey);
            return nextTask == null ? Optional.empty() : Optional.of(toUserStep(nextTask));
        } catch (Exception e) {
            log.error("Failed to complete task {} for businessKey: {}", task.getId(), businessKey, e);
            throw new FlowException("Failed to move to next step", e);
        }
    }

    /**
     * Jumps back from the current active task to a specified target task definition key.
     * <p>
     * If the target task does not exist in the process model or equals the current task,
     * the method returns the current step without modification. Otherwise, it performs
     * a process instance modification to cancel the current activity instance and start
     * before the target activity, then returns the newly active {@code UserStep}.
     * </p>
     *
     * @param businessKey       Identification for the Process instance.
     * @param targetTaskDefKey  The BPMN task definition key to jump back to.
     * @return An {@link Optional} containing the {@code UserStep} after the jump; empty if no task is active.
     * @throws FlowException if there is no active task.
     */

    @Override
    public Optional<UserStep> back(String businessKey, String targetTaskDefKey) {

        Task current = getActiveTask(businessKey);
        if (current == null) throw new FlowException("No active task");

        if (!taskExist(current.getProcessDefinitionId(), targetTaskDefKey)) {
            log.error("No back page found with that Task definition key: {} resolving to stay in the same page", targetTaskDefKey);
            return Optional.of(toUserStep(current));
        }


        if (current.getTaskDefinitionKey().equals(targetTaskDefKey)) {
            return Optional.of(toUserStep(current));
        }

        String processInstanceId = current.getProcessInstanceId();
        String currentAiId = resolveActivityInstanceId(processInstanceId, current.getTaskDefinitionKey(), current.getExecutionId());

        runtimeService.createProcessInstanceModification(processInstanceId)
                .setAnnotation("Jump -> " + targetTaskDefKey)
                .cancelActivityInstance(currentAiId)
                .startBeforeActivity(targetTaskDefKey)
                .execute();

       return Optional.ofNullable(getActiveTask(businessKey))
               .map(this::toUserStep);
    }




    /**
     * Helper method
     * Checks if a BPMN task with the given definition key exists in the process model.
     *
     * @param processID        the process definition ID
     * @param targetTaskDefKey the BPMN task definition key
     * @return true if the element exists, false otherwise
     */

    private boolean taskExist(String processID, String targetTaskDefKey) {
        var model = repositoryService.getBpmnModelInstance(processID);
        return model.getModelElementById(targetTaskDefKey) != null;
    }



    /**
     * Helper method
     * Finds the activity instance ID matching the given activity and (optional) execution.
     *
     * @param processInstanceId the process instance ID
     * @param activityId        the BPMN activity ID
     * @param executionId       the specific execution ID, or null for any
     * @return the activity instance ID, or null if not found
     */

    private String resolveActivityInstanceId(String processInstanceId, String activityId, String executionId) {
        var root = runtimeService.getActivityInstance(processInstanceId);
        if (root == null) return null;

        var stack = new ArrayDeque<ActivityInstance>();
        stack.push(root);
        while (!stack.isEmpty()) {
            var ai = stack.pop();
            if (activityId.equals(ai.getActivityId())) {
                var execIds = Arrays.asList(ai.getExecutionIds());
                if (executionId == null || execIds.contains(executionId)) {
                    return ai.getId();
                }
            }
            for (var child : ai.getChildActivityInstances()) stack.push(child);
        }
        return null;
    }




    /**
     * Helper method
     * Returns the active process instance for the given business key.
     *
     * @param businessKey the business key
     * @return the active instance, or null if none exists
     */

    private ProcessInstance getActiveInstance(String businessKey) {
        ProcessInstanceQuery query = runtimeService
                .createProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey)
                .active();

        List<ProcessInstance> instances = query.list();
        return instances.isEmpty() ? null : instances.getFirst();
    }



    /**
     * Helper method
     * Returns the active task for the process associated with the business key.
     *
     * @param businessKey the business key
     * @return the active task, or null if none exists
     */

    private Task getActiveTask(String businessKey) {
        return taskService.createTaskQuery()
                .processInstanceBusinessKey(businessKey)
                .initializeFormKeys()
                .active()
                .singleResult();
    }

    private UserStep toUserStep(Task task) {
        String formKey = formService.getTaskFormKey(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        var formData = runtimeService.getVariables(task.getProcessInstanceId());

        return new UserStep(task.getId(), task.getTaskDefinitionKey(), task.getProcessInstanceId(), formKey, formData);
    }


    private static final Predicate<String> isNullOrBlank = str -> Optional.ofNullable(str).map(String::isBlank).orElse(true);



}
