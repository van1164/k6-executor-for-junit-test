package io.github.van1164.result;

import java.util.List;

public class K6Result {

    private String resultBody = null;
    private Boolean allChecksPass = false;
    private List<String> failedCheckList;
    private HttpReq httpReq;

    public K6Result() {
    }

    public K6Result( String resultBody, Boolean allChecksPass, List<String> failedCheckList, HttpReq httpReq) {
        this.resultBody = resultBody;
        this.allChecksPass = allChecksPass;
        this.failedCheckList = failedCheckList;
        this.httpReq = httpReq;
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

    public Integer getSuccessRequest(){
        return httpReq.success;
    }

    public Integer getFailRequest(){
        return httpReq.fail;
    }

    public Integer getTotalRequest(){
        return httpReq.total;
    }

    public Boolean httRequestFound(){
        return httpReq.found;
    }
}
