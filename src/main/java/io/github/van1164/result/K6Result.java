package io.github.van1164.result;

import java.util.List;

public class K6Result {
    private Integer exitCode = null;
    private String resultBody = null;
    private Boolean allChecksPass = false;
    private List<String> failedCheckList;

    public K6Result() {
    }

    public K6Result(Integer exitCode, String resultBody, Boolean allChecksPass, List<String> failedCheckList) {
        this.exitCode = exitCode;
        this.resultBody = resultBody;
        this.allChecksPass = allChecksPass;
        this.failedCheckList = failedCheckList;
    }

    public Boolean isAllPassed() {
        return allChecksPass;
    }

    public void printResult() {
        System.out.print(resultBody);
    }

    public List<String> getFailedCheckList() {
        return failedCheckList;
    }
}
