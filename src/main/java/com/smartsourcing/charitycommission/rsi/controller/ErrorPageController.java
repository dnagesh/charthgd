package com.smartsourcing.charitycommission.rsi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorPageController {

    @GetMapping("/charity-not-found")
    public String charityNotFound() {
        return "errors/charity-not-found";
    }
}
