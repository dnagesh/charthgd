package com.smartsourcing.charitycommission.rsi.controller.web;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import com.smartsourcing.charitycommission.rsi.model.entity.Submission;
import com.smartsourcing.charitycommission.rsi.repository.SubmissionRepository;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TestController {

    private final SubmissionRepository repository;
    private final ObjectMapper objectMapper;
    private Long formID;


    /**
     * {
     *   "contactByEmail" : "",
     *   "contact" : "phone",
     *   "addressLine1" : "angel@gmail.com",
     *   "action" : "save-and-quit",
     *   "contactByPhone" : "Contact",
     *   "whereDoYouLive" : "northern-ireland",
     *   "page1.1-date-day" : "09",
     *   "page1.1-date-month" : "10",
     *   "page1.1-date-year" : "2025",
     *   "page1.1-radio" : "no",
     *   "page1.1-radio2" : "no",
     *   "page1.1-checkbox" : "yes",
     *   "page1.1-checkbox2" : "yes",
     *   "page1.1-input-name" : "A",
     *   "page1.1-input-surname" : "S",
     *   "page1.1-textarea-description" : "Lore ipsum"
     * }
     * **/

    @GetMapping("/")
    public String home(Model model){
        Submission submission = new Submission();
        submission.setId(1L);
        Map<String,Object> dataToAdd = new HashMap<>();
        dataToAdd.put("contact-2", "yes");
        dataToAdd.put("contact-by-phone", "Contact");
        dataToAdd.put("whereDoYouLive-3", "yes");
        dataToAdd.put("address-line-1", "sample@charitycommission.gov.uk");
        model.addAttribute("data",dataToAdd);
        submission.setInputData(dataToAdd);
        return "home";
    }

    @GetMapping("/sample2")
    public String sample2(Model model){
        Map<String, Object> data = new HashMap<>();

        if (formID != null) {
            Submission submission = repository.findById(formID).orElse(null);
            if(submission != null && submission.getInputData() != null){
                data = submission.getInputData();
            }
        }
        model.addAttribute("data", data);
        return "sample2";
    }

    @GetMapping("/confirmation")
    public String confirmation(Model model) {
        if (formID != null) {
            Submission submission = repository.findById(formID).orElse(null);
            if (submission != null) {
                Map<String, Object> data = submission.getInputData();

                try {
                    String jsonData = objectMapper.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(data);
                    model.addAttribute("jsonData", jsonData);
                } catch (Exception e) {
                    log.error("Error converting to JSON", e);
                    model.addAttribute("jsonData", data.toString());
                }

                model.addAttribute("ID", formID);
                model.addAttribute("data", data);
            }
        }
        return "confirmation";
    }

    @PostMapping("/submit")
    public String submitForm(@RequestParam Map<String,String> formData, HttpServletRequest request) {
        
        String referer = request.getHeader("Referer");
        log.info("Submitted from: {}", referer);
        
        Submission submission;
        
        if (formID != null) {

            submission = repository.findById(formID).orElse(new Submission());
            Map<String, Object> existingData = submission.getInputData();
            
            if (existingData == null) {
                existingData = new HashMap<>();
            }
            

            existingData.putAll(formData);
            submission.setInputData(existingData);
            
            log.info("Appending data to submission {}: {}", formID, formData);
        } else {

            submission = new Submission();
            submission.setInputData(new HashMap<>(formData));
            log.info("Creating new submission: {}", formData);
        }
        

        formID = repository.save(submission).getId();
        log.info("Saved submission with ID: {}", formID);
        

        if (referer != null && referer.contains("/sample2")) {
            return "redirect:/confirmation";
        } else {
            return "redirect:/sample2";
        }
    }
}
