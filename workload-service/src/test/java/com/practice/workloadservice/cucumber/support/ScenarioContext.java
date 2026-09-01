package com.practice.workloadservice.cucumber.support;

import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.ResultActions;

@Component
public class ScenarioContext {

    private ResultActions lastResult;

    public ResultActions getLastResult() {
        return lastResult;
    }

    public void setLastResult(ResultActions lastResult) {
        this.lastResult = lastResult;
    }
}
