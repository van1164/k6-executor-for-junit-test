package io.github.van1164.result;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class K6Result {

    private String resultBody = null;
    private Boolean allChecksPass = false;

    private DurationStats httpReqDuration;
    private CounterStats httpReqFailed;
    private CounterStats httpReqs;
    private DurationStats iterationDuration;
    private CounterStats iterations;
    private DataStats dataReceived;
    private DataStats dataSent;
    private List<String> failedCheckList;
    private HashMap<String, Integer> countHashMap;

    public K6Result() {
    }

    public K6Result(
            String resultBody,
            Boolean allChecksPass,
            List<String> failedCheckList,
            HashMap<String, Integer> countHashMap,
            Map<String,String> metricLines,
            DurationStats httpReqDuration, CounterStats httpReqFailed,
            CounterStats httpReqs,     DurationStats iterationDuration,
            CounterStats iterations,   DataStats dataReceived,
            DataStats dataSent

    ) {
        this.resultBody = resultBody;
        this.allChecksPass = allChecksPass;
        this.failedCheckList = failedCheckList;
        this.countHashMap = countHashMap;
        this.httpReqDuration   = httpReqDuration;
        this.httpReqFailed     = httpReqFailed;
        this.httpReqs          = httpReqs;
        this.iterationDuration = iterationDuration;
        this.iterations        = iterations;
        this.dataReceived      = dataReceived;
        this.dataSent          = dataSent;
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



    public Integer getCount(String countName) {
        return this.countHashMap.get(countName);
    }

}
