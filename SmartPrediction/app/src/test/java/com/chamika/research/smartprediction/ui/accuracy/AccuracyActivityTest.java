package com.chamika.research.smartprediction.ui.accuracy;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertTrue;

public class AccuracyActivityTest {

    @Test
    public void isTraining() {
        Random random = new Random();
        float testProbability = 0.1f;

        List<Boolean> results = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            results.add(AccuracyActivity.isTraining(random, testProbability));
        }

        int falseCount = 0;
        for (Boolean result : results) {
            if (!result) {
                falseCount++;
            }
        }

        assertTrue((falseCount > 0 && falseCount < 20));
    }
}