package io.github.van1164.executor;

import io.github.van1164.result.HttpReq;
import io.github.van1164.result.K6Result;
import io.github.van1164.scirptBuilder.Check;
import io.github.van1164.scirptBuilder.K6ScriptBuilder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static io.github.van1164.util.K6RegexFinder.countByResult;
import static io.github.van1164.util.K6RegexFinder.countHttpReq;

@SuperBuilder
@Getter
public class K6ExecutorWithScriptBuilder extends K6Executor {
    @NonNull
    K6ScriptBuilder sb;

    @Override
    public K6Result runTest() throws IOException {
        k6SetUp();
        String[] command = createCommand();
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        try (OutputStream outputStream = process.getOutputStream()) {
            outputStream.write(sb.build().getBytes());
            outputStream.flush();
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder outputString = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            outputString.append(line).append("\n");
        }
        reader.close();

        String result = outputString.toString();

        return resultToK6Result(result);
    }


    @Override
    protected String[] createCommand() {
        System.out.println(k6BinaryPath);
        List<String> commandList = new ArrayList<>(List.of(k6BinaryPath, "run","-"));
        return commandList.toArray(new String[0]);
    }

    @Override
    protected K6Result resultToK6Result(String result) {
        List<String> failedCheckList = new ArrayList<>();
        HashMap<String, Integer> countHashMap = new HashMap<>();
        HttpReq httpReq = countHttpReq(result);
        boolean allChecksPass = isAllChecksPass(result, failedCheckList);
        computeCounter(result, countHashMap);
        return new K6Result(result, allChecksPass, failedCheckList, httpReq, countHashMap);
    }

    private void computeCounter(String result, HashMap<String, Integer> countHashMap) {
        for (Check check : sb.getChecks()) {
            countHashMap.put(check.getVariable(), countByResult(result, check.getVariable()));
        }
    }

    private boolean isAllChecksPass(String result, List<String> failedCheckList) {
        boolean allChecksPass = true;
        for (Check check : sb.getChecks()) {
            String findArgs = "✓ " + check.getVariable();
            if (!result.contains(findArgs)) {
                allChecksPass = false;
                failedCheckList.add(check.getVariable());
            }
        }
        return allChecksPass;
    }

}
