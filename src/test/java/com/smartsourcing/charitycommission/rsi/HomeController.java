package com.smartsourcing.charitycommission.rsi;

import com.smartsourcing.charitycommission.rsi.ui.User;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
@Controller
public class HomeController {

    @GetMapping("/form1")
    public String showForm(Model model) {
        model.addAttribute("user", new User());
        return "home";  // must match the template name
    }

    @PostMapping("/form1")
    public String submitForm(@Valid User user, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return "form1";
        }
        model.addAttribute("name", user.getName());
        return "result";
    }
}
