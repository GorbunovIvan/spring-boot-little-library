package com.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.PrintWriter;
import java.io.StringWriter;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public String handleGlobalException(Exception e, Model model) {

        StringWriter writer = new StringWriter();
        e.printStackTrace(new PrintWriter(writer));
        var stackTrace = writer.toString();

        if (!e.getMessage().equals("No static resource favicon.ico.")) {
            log.error(stackTrace);
        }

        model.addAttribute("message", e.getMessage());
        model.addAttribute("details", stackTrace);

        return "error";
    }
}
