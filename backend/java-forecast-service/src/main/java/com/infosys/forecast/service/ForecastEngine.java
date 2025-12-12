package com.infosys.forecast.service;

import com.infosys.forecast.model.TransactionData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForecastEngine {

    /**
     * Simple linear regression-based forecast. For production, use Weka or DJL.
     * This demo computes a weighted moving average as a naive predictor.
     */
    public double predictNext(List<TransactionData> history) {
        if (history == null || history.isEmpty()) {
            return 0.0;
        }

        // Sort by date (assumes dates are in ascending order)
        // For this naive version, we just average the last N records with higher weight on recent
        double sum = 0.0;
        double weightSum = 0.0;
        int n = Math.min(history.size(), 7);

        for (int i = 0; i < n; i++) {
            double weight = (i + 1); // more recent = higher weight
            sum += history.get(history.size() - 1 - i).getSold() * weight;
            weightSum += weight;
        }

        return weightSum > 0 ? sum / weightSum : 0.0;
    }
}
