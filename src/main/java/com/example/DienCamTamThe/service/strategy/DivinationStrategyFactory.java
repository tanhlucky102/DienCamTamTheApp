package com.example.DienCamTamThe.service.strategy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DivinationStrategyFactory {

    private final Map<Integer, DivinationStrategy> strategies = new HashMap<>();

    @Autowired
    public DivinationStrategyFactory(List<DivinationStrategy> strategyList) {
        for (DivinationStrategy strategy : strategyList) {
            strategies.put(strategy.getSectionNumber(), strategy);
        }
    }

    public DivinationStrategy getStrategy(int sectionNumber) {
        return strategies.get(sectionNumber);
    }
}
