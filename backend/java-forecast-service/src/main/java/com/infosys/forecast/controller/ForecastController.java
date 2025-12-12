package com.infosys.forecast.controller;

import com.infosys.forecast.model.ForecastResult;
import com.infosys.forecast.model.TransactionData;
import com.infosys.forecast.service.ForecastEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/forecast")
public class ForecastController {

    @Autowired
    private ForecastEngine forecastEngine;

    @PostMapping("/predict")
    public List<ForecastResult> predict(@RequestBody List<TransactionData> data) {
        Map<Long, List<TransactionData>> grouped =
                data.stream().collect(Collectors.groupingBy(TransactionData::getProductId));

        List<ForecastResult> results = new ArrayList<>();

        for (Long productId : grouped.keySet()) {
            List<TransactionData> history = grouped.get(productId);
            double prediction = forecastEngine.predictNext(history);
            results.add(new ForecastResult(productId, prediction, java.time.LocalDate.now().plusDays(7).toString()));
        }

        return results;
    }
}
