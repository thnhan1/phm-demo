package com.nhan.phm.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;

@org.springframework.web.bind.annotation.ControllerAdvice
public class ControllerAdvice {
  @ExceptionHandler(Exception.class)
  public String handleException(Exception e, Model model) {
    model.addAttribute("errorMessage", e.getMessage());
    return "error";
  }

}
