package com.chamika.research.smartprediction.ui.accuracy;

public class TestResult {
    private boolean success;
    private int entriesTested;
    private long runningTime;
    private float accuracy;
    private int topClasses;
    private String message;

    private TestResult(boolean success, int entriesTested, long runningTime, float accuracy, int topClasses, String message) {
        this.success = success;
        this.entriesTested = entriesTested;
        this.runningTime = runningTime;
        this.accuracy = accuracy;
        this.topClasses = topClasses;
        this.message = message;
    }

    public static TestResult success(int entriesTested, long runningTime, float accuracy, int topClasses) {
        return new TestResult(true, entriesTested, runningTime, accuracy, topClasses, null);
    }

    public static TestResult error(String message) {
        return new TestResult(false, 0, 0, 0f, 0, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public int getEntriesTested() {
        return entriesTested;
    }

    public long getRunningTime() {
        return runningTime;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public int getTopClasses() {
        return topClasses;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "TestResult{" +
                "success=" + success +
                ", entriesTested=" + entriesTested +
                ", runningTime=" + runningTime +
                ", accuracy=" + accuracy +
                ", topClasses=" + topClasses +
                ", message='" + message + '\'' +
                '}';
    }
}
