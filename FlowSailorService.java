package uk.gov.ccew.rsi.flow.service;


import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.RuntimeServiceImpl;
import org.camunda.bpm.engine.impl.TaskServiceImpl;
import org.camunda.bpm.engine.task.Task;
import org.springframework.stereotype.Component;


import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.MapUtils.verbosePrint;


@Slf4j
@RequiredArgsConstructor
@Component
public class FlowSailorService {

    @Getter
    private final RuntimeService service;
    @Getter
    private final TaskService taskService;
    @Getter
    private String BASE_PAGE_PATH = "form/page";
    private String RSI_FLOW = "rsi_initial_flow";
    private final String SESSION_KEY = "BusinessKey";


    public void startRSI(String businessKey) {
        service.startProcessInstanceByKey(RSI_FLOW, businessKey);
    }

   public String getPageFromFlow(HttpSession session) {
        var userBusinessKey = (String) session.getAttribute(SESSION_KEY);
        String result;

        try {
            Task task = taskService.createTaskQuery()
                    .processInstanceBusinessKey(userBusinessKey)
                    .initializeFormKeys()
                    .active()
                    .singleResult();

            result = task.getFormKey();
            return result;
        } catch (ProcessEngineException e) {
            log.error("An error occurred taking the page from the task", e);
            return "redirect:/error";
        }
    }

    Task getTask(String businessKey) {

        return taskService.createTaskQuery().processInstanceBusinessKey(businessKey).initializeFormKeys().active().singleResult();
    }

    void completeTask(String businessKey) {
        Task task = getTask(businessKey);

        taskService.complete(task.getId());
    }

    public void completePage(HttpSession session, Map<String, String> formData) {
        var userBusinessKey = (String) session.getAttribute(SESSION_KEY);
        Task task = getTask(userBusinessKey);

        var cleanMap = cleanMapFromUnneededInput(formData);
        log.info("Result cleaned map:");
        verbosePrint(System.out,"Cleaner", cleanMap);

        taskService.complete(task.getId(), cleanMap);
    }

    public Map<String, Object> completeAndContinue(HttpSession session, Map<String, String> formData){
        var userBusinessKey = (String) session.getAttribute(SESSION_KEY);
        Task task = getTask(userBusinessKey);
        taskService.setVariables(task.getId(),cleanMapFromUnneededInput(formData));
        return cleanMapFromUnneededInput(formData);
    }

    private Map<String, Object> cleanMapFromUnneededInput(Map<String, String> formData) {
        return formData.entrySet().stream().filter(filterPredicate).collect(Collectors.toMap(entry -> transformToFlowEngineString.apply(entry.getKey()), Map.Entry::getValue));
    }


    Predicate<Map.Entry<String, String>> filterPredicate = entry -> entry.getKey().toLowerCase().contains("radiogroup") && (entry.getValue().equalsIgnoreCase("yes")
            || entry.getValue().equalsIgnoreCase("no")
            || entry.getValue().toLowerCase().contains("option"));

    Function<String, String> transformToFlowEngineString = key -> key.replace(".", "_").replace("-", "_");


    public static void main(String[] args) {
        TaskService tsv = new TaskServiceImpl();
        RuntimeService rtm = new RuntimeServiceImpl();
        FlowSailorService service1 = new FlowSailorService(rtm, tsv);


        Map<String, String> inputMap = Map.of("form.radiogroup1", "yes", "form.radiogroup2", "option1", "form.textfield", "hello", "form.radiogroup3", "no", "form.checkbox", "checked","P.9.2","P9.2-input");


        Map<String, Object> result = service1.cleanMapFromUnneededInput(inputMap);

        result.forEach((key, value) -> System.out.println(key + " " + value.toString()));
    }

}
