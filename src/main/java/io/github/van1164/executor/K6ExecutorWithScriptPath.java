package io.github.van1164.executor;

import io.github.van1164.downloader.*;
import io.github.van1164.result.HttpReq;
import io.github.van1164.result.K6Result;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static io.github.van1164.util.K6Constants.K6_BINARY_PATH;
import static io.github.van1164.util.K6Constants.K6_VERSION;
import static io.github.van1164.util.K6RegexFinder.countByResult;
import static io.github.van1164.util.K6RegexFinder.countHttpReq;

@Getter
@SuperBuilder
public class K6ExecutorWithScriptPath extends K6Executor {
    @NonNull
    private String scriptPath;

    @Builder.Default
    private List<String> checkList = List.of();

    @Builder.Default
    private List<String> counterList = List.of();

    @Builder.Default
    private HashMap<String, String> args = new HashMap<>();


//    /**
//     * K6 Executor Constructor
//     *
//     * @param scriptPath  k6 javascript script file path
//     *                    <br> ex) "/to/script/path/test.js"
//     *                    <br>
//     * @param checkList   k6 check list
//     *                    <br> If your script file contains the following,
//     *                    <code>
//     *                    <br>   check(res, {
//     *                    <br>        'is status 200': (r) => r.status === 200,
//     *                    <br>        'response time {@literal <} 500ms': (r) => r.timings.duration {@literal <} 50000,
//     *                    <br>        });
//     *                    </code>
//     *                    <br> you can configure the checklist as follows.
//     *                    <br> {@code List<String> checkList = {"is status 200", "response time {@literal <} 500ms"}; }
//     * @param counterList k6 counter list
//     *                    <br> ex "success_check"
//     *                    <code>
//     *                    <br> const successCheck = new Counter('success_check');
//     *                    </code>
//     */
//
//    public K6ExecutorWithScriptPath(String scriptPath, String k6BinaryPath, List<String> checkList, List<String> counterList, HashMap<String, String> args) {
//        if (scriptPath == null) {
//            throw new RuntimeException("scriptPath is should not null");
//        }
//        this.scriptPath = scriptPath;
//        this.checkList = checkList;
//        this.counterList = counterList;
//        this.args = args;
//    }
//
//    public K6ExecutorWithScriptPath(String scriptPath, List<String> checkList, List<String> counterList) {
//        if (scriptPath == null) {
//            throw new RuntimeException("scriptPath is should not null");
//        }
//        this.scriptPath = scriptPath;
//        this.checkList = checkList;
//        this.counterList = counterList;
//
//        k6SetUp();
//    }


    /**
     * K6 Executor Run test {@literal &} Check for Check List
     *
     * @return {@link K6Result} Returns k6Result
     */

    public K6Result runTest() throws IOException {
        k6SetUp();
        String[] command = createCommand();
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
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
        List<String> commandList = new ArrayList<>(List.of(k6BinaryPath, "run", scriptPath));

        if (!args.isEmpty()) {
            for (String key : args.keySet()) {
                commandList.add("--env");
                commandList.add(key + "=" + args.get(key));
            }
        }
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
        for (String countName : this.counterList) {
            countHashMap.put(countName, countByResult(result, countName));
        }
    }

    private boolean isAllChecksPass(String result, List<String> failedCheckList) {
        boolean allChecksPass = true;
        for (String check : checkList) {
            String findArgs = "✓ " + check;
            if (!result.contains(findArgs)) {
                allChecksPass = false;
                failedCheckList.add(check);
            }
        }
        return allChecksPass;
    }


}