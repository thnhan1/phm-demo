package com.nhan.phm.controller;

import com.nhan.phm.service.PHMService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class PHMController {

  private final PHMService phmService;

  public PHMController(PHMService phmService) {
    this.phmService = phmService;
  }

  @GetMapping
  public String indexPage() {
    return "index";
  }

  @GetMapping("/algorithm")
  public String algorithmPage() {
    return "algorithm";
  }

  @GetMapping("/dataset")
  public String datasetPage() {
    return "dataset";
  }

  @PostMapping("/run")
  public String runPhm(@RequestParam("inputFile") MultipartFile inputFile,
      @RequestParam("minUtility") int minUtility, @RequestParam("minPer") int minPeriodicity,
      @RequestParam("maxPer") int maxPeriodicity, @RequestParam("minAvg") int minAveragePeriodicity,
      @RequestParam("maxAvg") int maxAveragePeriodicity,
      @RequestParam(value = "minItem", required = false) Integer minimumLength,
      @RequestParam(value = "maxItem", required = false) Integer maximumLength, Model model) {
    try {
      PHMService.PhmResult result = phmService.runPhm(inputFile, minUtility, minPeriodicity,
          maxPeriodicity, minAveragePeriodicity, maxAveragePeriodicity, minimumLength,
          maximumLength);

      model.addAttribute("result", result.getOutput());
      model.addAttribute("runningTime", String.format("%.2f", result.getRunningTime()));
      model.addAttribute("memoryUsage", String.format("%.2f", result.getMemoryUsage()));
      model.addAttribute("itemsetCount", result.getItemsetCount());
      model.addAttribute("outputFilePath", result.getOutputFilePath());

      return "result";

    } catch (IOException e) {
      model.addAttribute("error", e.getMessage());
      return "index";
    }
  }

  @GetMapping("/downloadResult")
  public void downloadResult(HttpServletResponse response,
      @ModelAttribute("outputFilePath") String filePath) throws IOException {
    File file = new File(filePath);

    response.setContentType("text/plain");
    response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
    response.setContentLength((int) file.length());

    try (FileInputStream inputStream = new FileInputStream(file)) {
      IOUtils.copy(inputStream, response.getOutputStream());
    }
  }

  @GetMapping("/error")
  public String handleError(HttpServletRequest request, Model model) {
    Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    if (status != null) {
      int statusCode = Integer.parseInt(status.toString());
      if (statusCode == HttpStatus.NOT_FOUND.value()) {
        model.addAttribute("errorMessage", "Page not Found.");
      } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()){
        model.addAttribute("errorMessage", "Internal Server Error.");
      }
    }
    return "error";
  }

  public String getErrorPath() {
    return "/error";
  }

}
