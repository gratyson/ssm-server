package com.gt.ssm.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WebController {

    private static final Logger log = LoggerFactory.getLogger(WebController.class);


    @GetMapping("/")
    public ModelAndView getApp(HttpServletRequest request) {

        // Do not delete - it appears that the call to getToken() is necessary for the response to include the CRSF token
        CsrfToken token = (CsrfToken)request.getAttribute("_csrf");

        return new ModelAndView("index.html");
    }
}
