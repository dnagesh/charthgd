package com.smartsourcing.charitycommission.rsi.controller;

import com.smartsourcing.charitycommission.rsi.model.NavigationRequest;
import com.smartsourcing.charitycommission.rsi.model.NavigationResponse;
import com.smartsourcing.charitycommission.rsi.service.NavigationServiceNew;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for handling navigation requests
 */
@Slf4j
@Controller
@RequestMapping("/navigate")
@RequiredArgsConstructor
public class NavigationControllerNew {

    private final NavigationServiceNew navigationService;

    /**
     * Start navigation for a section
     */
    @GetMapping("/start")
    public String startNavigation(
            @RequestParam(defaultValue = "initialPages") String section,
            Model model,
            RedirectAttributes redirectAttributes) {

        log.info("Start navigation request for section: {}", section);

        NavigationResponse response = navigationService.startSection(section);

        if (response.isSuccess()) {
            return "redirect:/navigate/page/" + response.getNextPageId();
        } else {
            redirectAttributes.addFlashAttribute("error", response.getMessage());
            return "redirect:/navigate/error";
        }
    }

    /**
     * Display a specific page
     */
    @GetMapping("/page/{pageId:.+}")
    public String showPage(@PathVariable String pageId, Model model) {
        log.info("Showing page: {}", pageId);

        try {
            NavigationResponse response = navigationService.getCurrentState();

            model.addAttribute("pageId", pageId);
            model.addAttribute("response", response);
            model.addAttribute("conditions", response.getConditions());
            model.addAttribute("canGoBack", response.isCanGoBack());
            model.addAttribute("isEndPage", response.isEndPage());
            model.addAttribute("flowPath", response.getFlowPath());

            // NEW: Return template path based on page ID
            // Example: "initial/P1.0" → "forms/initial/P1.0"
            String templatePath = "forms/" + pageId;
            log.debug("Resolving template: {}", templatePath);

            return templatePath;

        } catch (Exception e) {
            log.error("Error showing page: {}", pageId, e);
            model.addAttribute("error", "Failed to load page: " + e.getMessage());
            return "navigation/error";
        }
    }

    /**
     * Handle next navigation
     */
    @PostMapping("/next")
    public String navigateNext(
            @RequestParam String currentPageId,
            @RequestParam(required = false) String userResponse,
            RedirectAttributes redirectAttributes) {

        log.info("Next navigation from page: {}, response: {}", currentPageId, userResponse);

        // Convert empty string to null for pages without conditions
        String response = (userResponse != null && userResponse.trim().isEmpty()) ? null : userResponse;

        NavigationRequest request = NavigationRequest.builder()
                .action("next")
                .currentPageId(currentPageId)
                .userResponse(response)
                .build();

        NavigationResponse navigationResponse = navigationService.processNavigation(request);

        if (navigationResponse.isSuccess()) {
            return "redirect:/navigate/page/" + navigationResponse.getNextPageId();
        } else {
            redirectAttributes.addFlashAttribute("error", navigationResponse.getMessage());
            return "redirect:/navigate/page/" + currentPageId;
        }
    }

    /**
     * Handle previous navigation
     */
    @PostMapping("/prev")
    public String navigatePrevious(
            @RequestParam String currentPageId,
            RedirectAttributes redirectAttributes) {

        log.info("Previous navigation from page: {}", currentPageId);

        NavigationRequest request = NavigationRequest.builder()
                .action("prev")
                .currentPageId(currentPageId)
                .build();

        NavigationResponse response = navigationService.processNavigation(request);

        if (response.isSuccess()) {
            return "redirect:/navigate/page/" + response.getNextPageId();
        } else {
            redirectAttributes.addFlashAttribute("error", response.getMessage());
            return "redirect:/navigate/page/" + currentPageId;
        }
    }

    /**
     * Reset navigation
     */
    @GetMapping("/reset")
    public String reset() {
        log.info("Resetting navigation");
        navigationService.reset();
        return "redirect:/navigate/start";
    }

    /**
     * Error page
     */
    @GetMapping("/error")
    public String errorPage(Model model) {
        return "navigation/error";
    }

    /**
     * Home/landing page
     */
    @GetMapping
    public String home() {
        return "navigation/home";
    }
}