package io.github.van1164;

import io.github.van1164.downloader.K6Downloader;

import java.io.*;

import static io.github.van1164.util.Constant.K6_BINARY_PATH;

public class K6Executor {

    private final String scriptPath;
    private String k6BinaryPath;

    private final String[] checkList;


    /**
     *  K6 Executor Constructor
     *
     * @param scriptPath k6 javascript script file path
     *                   <br> ex) "/to/script/path/test.js"
     *                   <br>
     * @param checkList  k6 check list
     *                  <br> If your script file contains the following,
     *                  <code>
     *                  <br>   check(res, {
     *                  <br>        'is status 200': (r) => r.status === 200,
     *                  <br>        'response time {@literal <} 500ms': (r) => r.timings.duration {@literal <} 50000,
     *                  <br>        });
     *                  </code>
     *                  <br> you can configure the checklist as follows.
     *                  <br> {@code String[] checkList = {"is status 200", "response time {@literal <} 500ms"}; }
     */

    public K6Executor(String scriptPath, String[] checkList) throws Exception {
        this.scriptPath = scriptPath;
        this.k6BinaryPath = K6_BINARY_PATH;

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            this.k6BinaryPath += ".exe";
        }

        this.checkList = checkList;

        K6Downloader k6Downloader = new K6Downloader(this.k6BinaryPath);

        if (!new File(k6BinaryPath).exists()) {
            k6Downloader.downloadK6Binary();
        }
    }


    /**
     *  K6 Executor Run test {@literal &} Check for Check List
     *
     * @return Returns true if all checklists are satisfied
     */

    public boolean runTest() throws IOException, InterruptedException {

        String[] command = {k6BinaryPath,"run" ,scriptPath};
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        String result = output.toString();

        boolean allChecksPass = true;

        allChecksPass = isAllChecksPass(result, allChecksPass);

        reader.close();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("K6 RunTime Error: " + exitCode);
        }

        return allChecksPass;
    }

    private boolean isAllChecksPass(String result, boolean allChecksPass) {
        for (String check : checkList) {
            String findArgs = "✓ "+check;
            if (!result.contains(findArgs)) {
                allChecksPass = false;
                assertError(check);
                break;
            }
        }
        return allChecksPass;
    }

    private void assertError(String error) {
        throw new AssertionError("The following assert Condition is not satisfied. :  " + error);
    }
}