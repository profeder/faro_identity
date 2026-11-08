package it.faro.identity.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    @Value("${faro.brand.logo-url:}")
    private String logoUrl;

    @Value("${faro.brand.android-download-url:}")
    private String androidDownloadUrl;

    @GetMapping("/login")
    public String loginPage(Model model) {
        model.addAttribute("logoUrl", logoUrl);
        model.addAttribute("androidDownloadUrl", androidDownloadUrl);
        return "login";
    }
}
