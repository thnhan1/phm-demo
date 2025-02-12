package com.nhan.phm.service.api;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class MemoryLogger {
    private static final MemoryLogger instance = new MemoryLogger();
    private double maxMemory = 0.0F;
    private boolean recordingMode = false;
    private File outputFile = null;
    private BufferedWriter writer = null;

    public MemoryLogger() {
    }

    public static MemoryLogger getInstance() {
        return instance;
    }

    public double getMaxMemory() {
        return this.maxMemory;
    }

    public void reset() {
        this.maxMemory = 0.0F;
    }

    public double checkMemory() {
        double currentMemory = (double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (double) 1024.0F / (double) 1024.0F;
        if (currentMemory > this.maxMemory) {
            this.maxMemory = currentMemory;
        }

        if (this.recordingMode) {
            try {
                this.writer.write(currentMemory + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return currentMemory;
    }

    public void startRecordingMode(String fileName) {
        this.recordingMode = true;
        this.outputFile = new File(fileName);

        try {
            this.writer = new BufferedWriter(new FileWriter(this.outputFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopRecordingMode() {
        if (this.recordingMode) {
            try {
                this.writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            this.recordingMode = false;
        }

    }
}