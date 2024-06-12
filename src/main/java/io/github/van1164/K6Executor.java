package io.github.van1164;

import io.github.van1164.downloader.K6Downloader;
import io.github.van1164.result.HttpReq;
import io.github.van1164.result.K6Result;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.van1164.util.Constant.K6_BINARY_PATH;
import static io.github.van1164.util.K6RegexFinder.countHttpReq;

public class K6Executor {

    private final String scriptPath;
    private String k6BinaryPath;
    private final String[] checkList;


    /**
     * K6 Executor Constructor
     *
     * @param scriptPath k6 javascript script file path
     *                   <br> ex) "/to/script/path/test.js"
     *                   <br>
     * @param checkList  k6 check list
     *                   <br> If your script file contains the following,
     *                   <code>
     *                   <br>   check(res, {
     *                   <br>        'is status 200': (r) => r.status === 200,
     *                   <br>        'response time {@literal <} 500ms': (r) => r.timings.duration {@literal <} 50000,
     *                   <br>        });
     *                   </code>
     *                   <br> you can configure the checklist as follows.
     *                   <br> {@code String[] checkList = {"is status 200", "response time {@literal <} 500ms"}; }
     */

    public K6Executor(String scriptPath, String[] checkList) throws Exception {
        this.scriptPath = scriptPath;
        this.k6BinaryPath = K6_BINARY_PATH;
        this.checkList = checkList;

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            this.k6BinaryPath += ".exe";
        }

        K6Downloader k6Downloader = new K6Downloader(this.k6BinaryPath);
        if (!new File(k6BinaryPath).exists()) {
            k6Downloader.downloadK6Binary();
        }
    }


    /**
     * K6 Executor Run test {@literal &} Check for Check List
     *
     * @return {@link K6Result} Returns k6Result
     */

    public K6Result runTest() throws IOException, InterruptedException {
        String[] command = {k6BinaryPath, "run", scriptPath};
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

    private K6Result resultToK6Result(String result) {
        boolean allChecksPass = true;
        List<String> failedCheckList = new ArrayList<>();
        HttpReq httpReq = countHttpReq(result);
        for (String check : checkList) {
            String findArgs = "✓ " + check;
            if (!result.contains(findArgs)) {
                allChecksPass = false;
                failedCheckList.add(check);
            }
        }
        return new K6Result(result, allChecksPass,failedCheckList,httpReq);
    }



}