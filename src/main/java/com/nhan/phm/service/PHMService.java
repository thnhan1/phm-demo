package com.nhan.phm.service;
//import ca.pfv.spmf.algorithms.frequentpatterns.hui_miner.AlgoPHM;
import com.nhan.phm.service.api.AlgoPHM;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class PHMService {

  public PhmResult runPhm(MultipartFile inputFile, int minUtility, int minPeriodicity, int maxPeriodicity,
      int minAveragePeriodicity, int maxAveragePeriodicity, Integer minimumLength,
      Integer maximumLength) throws IOException {

    File uploadedFile = saveFile(inputFile);
    String input = uploadedFile.getAbsolutePath();

    File outputFile = createTempFile("output.txt");
    String output = outputFile.getAbsolutePath();

    AlgoPHM algorithm = new AlgoPHM();

    if (minimumLength != null) {
      algorithm.setMinimumLength(minimumLength);
    } else {
      algorithm.setMinimumLength(1);
    }
    if (maximumLength != null) {
      algorithm.setMaximumLength(maximumLength);
    } else {
      algorithm.setMaximumLength(Integer.MAX_VALUE);
    }

    algorithm.runAlgorithm(input, output, minUtility, minPeriodicity, maxPeriodicity, minAveragePeriodicity, maxAveragePeriodicity);
    // algorithm.printStats();

    String result = readFile(output);

    uploadedFile.delete();
//    outputFile.delete();

    return new PhmResult(result, algorithm.totalExecutionTime, algorithm.maximumMemoryUsage,
        algorithm.phuiCount, output);
  }


  private File saveFile(MultipartFile file) throws IOException {
    if (file.isEmpty()) {
      throw new IllegalArgumentException("Please select a file to upload.");
    }

    Path uploadDir = Paths.get("uploads");
    if (!Files.exists(uploadDir)) {
      Files.createDirectories(uploadDir);
    }

    String uniqueFileName = generateUniqueFileName(file.getOriginalFilename());
    Path filePath = uploadDir.resolve(uniqueFileName);
    Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

    return filePath.toFile();
  }

  private String generateUniqueFileName(String originalFilename) {
    String timestamp = String.valueOf(System.currentTimeMillis());
    String extension = "";
    int dotIndex = originalFilename.lastIndexOf('.');
    if (dotIndex > 0) {
      extension = originalFilename.substring(dotIndex);
    }
    return timestamp + extension;
  }

  private File createTempFile(String filename) throws IOException {
    Path tempDir = Files.createTempDirectory("spmf_temp");
    String extension ="";
    String nameWithoutExt = filename;
    int dotIndex = filename.lastIndexOf('.');
    if (dotIndex > 0) {
      nameWithoutExt = filename.substring(0, dotIndex);
      extension = filename.substring(dotIndex);
    }
    return Files.createTempFile(tempDir, nameWithoutExt+"_",extension).toFile();
  }

  private String readFile(String filePath) throws IOException {
    StringBuilder content = new StringBuilder();
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line).append("\n");
      }
    }
    return content.toString();
  }

  public static class PhmResult {
    private String output;
    private double runningTime;
    private double memoryUsage;
    private int itemsetCount;
    private String outputFilePath;

    public String getOutput() {
      return output;
    }

    public PhmResult() {
    }

    public PhmResult(String output, double runningTime, double memoryUsage, int itemsetCount,
        String outputFilePath) {
      this.output = output;
      this.runningTime = runningTime;
      this.memoryUsage = memoryUsage;
      this.itemsetCount = itemsetCount;
      this.outputFilePath = outputFilePath;
    }

    public void setOutput(String output) {
      this.output = output;
    }

    public double getRunningTime() {
      return runningTime;
    }

    public void setRunningTime(double runningTime) {
      this.runningTime = runningTime;
    }

    public double getMemoryUsage() {
      return memoryUsage;
    }

    public void setMemoryUsage(double memoryUsage) {
      this.memoryUsage = memoryUsage;
    }

    public int getItemsetCount() {
      return itemsetCount;
    }

    public void setItemsetCount(int itemsetCount) {
      this.itemsetCount = itemsetCount;
    }

    public String getOutputFilePath() {
      return outputFilePath;
    }

    public void setOutputFilePath(String outputFilePath) {
      this.outputFilePath = outputFilePath;
    }
  }
}
