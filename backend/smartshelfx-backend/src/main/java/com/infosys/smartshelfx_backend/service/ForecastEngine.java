package com.infosys.smartshelfx_backend.service;

import com.infosys.smartshelfx_backend.controller.ForecastController.TransactionData;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForecastEngine {

    /**
     * Naive weighted moving average over last up to 7 observations.
     */
    public double predictNext(List<TransactionData> history) {
        if (history == null || history.isEmpty()) return 0.0;

        int n = Math.min(history.size(), 7);
        double sum = 0.0, weightSum = 0.0;
        for (int i = 0; i < n; i++) {
            double weight = (i + 1);
            double val = history.get(history.size() - 1 - i).sold != null ? history.get(history.size() - 1 - i).sold : 0.0;
            sum += val * weight;
            weightSum += weight;
        }
        return weightSum > 0 ? sum / weightSum : 0.0;
    }
}
