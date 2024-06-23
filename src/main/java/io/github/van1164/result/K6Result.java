package io.github.van1164.result;

import java.util.HashMap;
import java.util.List;

public class K6Result {

    private String resultBody = null;
    private Boolean allChecksPass = false;
    private List<String> failedCheckList;
    private HttpReq httpReq;
    private HashMap<String,Integer> countHashMap;

    public K6Result() {
    }

    public K6Result(String resultBody, Boolean allChecksPass, List<String> failedCheckList, HttpReq httpReq, HashMap<String,Integer> countHashMap) {
        this.resultBody = resultBody;
        this.allChecksPass = allChecksPass;
        this.failedCheckList = failedCheckList;
        this.httpReq = httpReq;
        this.countHashMap = countHashMap;
    }


    /**
     * Returns whether all checks have passed.
     *
     * @return true if all checks have passed, otherwise false.
     */
    public Boolean isAllPassed() {
        return allChecksPass;
    }

    /**
     * Prints the result body of running k6 to the standard output.
     */
    public void printResult() {
        System.out.print(resultBody);
    }

    /**
     * Returns the list of failed checks.
     *
     * @return a List of Strings representing the failed checks.
     */
    public List<String> getFailedCheckList() {
        return failedCheckList;
    }

    /**
     * Returns the number of successful HTTP requests.
     *
     * @return an Integer representing the count of successful HTTP requests.
     */
    public Integer getSuccessRequest() {
        return httpReq.success;
    }

    /**
     * Returns the number of failed HTTP requests.
     *
     * @return an Integer representing the count of failed HTTP requests.
     */
    public Integer getFailRequest() {
        return httpReq.fail;
    }

    /**
     * Returns the total number of HTTP requests.
     *
     * @return an Integer representing the total number of HTTP requests.
     */
    public Integer getTotalRequest() {
        return httpReq.total;
    }

    /**
     * Indicates whether any HTTP request was found.
     *
     * @return true if any HTTP request was found, otherwise false.
     */
    public Boolean httpRequestFound() {
        return httpReq.found;
    }

    public Integer getCount(String countName){
        return this.countHashMap.get(countName);
    }

}
