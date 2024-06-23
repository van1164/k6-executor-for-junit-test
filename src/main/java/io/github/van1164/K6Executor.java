package io.github.van1164;

import io.github.van1164.downloader.*;
import io.github.van1164.result.HttpReq;
import io.github.van1164.result.K6Result;
import lombok.Builder;
import lombok.Getter;

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

import static io.github.van1164.util.Constant.K6_BINARY_PATH;
import static io.github.van1164.util.Constant.K6_VERSION;
import static io.github.van1164.util.K6RegexFinder.countByResult;
import static io.github.van1164.util.K6RegexFinder.countHttpReq;

@Getter
@Builder
public class K6Executor {

    private final String scriptPath;
    private String k6BinaryPath = K6_BINARY_PATH;
    private List<String> checkList;
    private List<String> counterList;

    public static class K6ExecutorBuilder {
        public K6Executor build() {
            if (checkList == null){
                checkList = List.of();
            }

            if (counterList == null){
                counterList = List.of();
            }
                return new K6Executor(scriptPath, checkList, counterList);
        }
    }

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
     *                   <br> {@code List<String> checkList = {"is status 200", "response time {@literal <} 500ms"}; }
     */

    public K6Executor(String scriptPath, List<String> checkList, List<String> counterList) {
        if (scriptPath == null) {
            throw new RuntimeException("scriptPath is should not null");
        }
        this.scriptPath = scriptPath;
        this.checkList = checkList;
        this.counterList = counterList;

        k6SetUp();
    }

    private void k6SetUp() {
        String os = System.getProperty("os.name").toLowerCase();
        String addedK6Url;
        String downloadedPath;
        String fileSeparator = FileSystems.getDefault().getSeparator();

        if (os.contains("win")) {
            downloadedPath = getDownLoadPath("windows-amd64");
            addedK6Url = downloadedPath + ".zip";
            this.k6BinaryPath = downloadedPath + fileSeparator + this.k6BinaryPath + ".exe";
        } else if (os.contains("mac") || os.contains("darwin")) {
            String arch = System.getProperty("os.arch").toLowerCase();
            if (arch.contains("arm") || arch.contains("aarch")) {
                downloadedPath = getDownLoadPath("macos-arm64");
                addedK6Url = downloadedPath + ".zip";
                this.k6BinaryPath = downloadedPath + fileSeparator + this.k6BinaryPath;
            } else if (arch.contains("amd64") || arch.contains("x86_64")) {
                downloadedPath = getDownLoadPath("macos-amd64");
                addedK6Url = downloadedPath + ".zip";
                this.k6BinaryPath = downloadedPath + fileSeparator + this.k6BinaryPath;
            } else {
                throw new RuntimeException("Unsupported Arch: " + arch);
            }
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            downloadedPath = getDownLoadPath("linux-amd64");
            addedK6Url = downloadedPath + ".tar.gz";
            this.k6BinaryPath = downloadedPath + fileSeparator + this.k6BinaryPath;
        } else {
            throw new RuntimeException("Unsupported OS: " + os);
        }

        k6Download(downloadedPath, addedK6Url);
    }

    private void k6Download(String downloadedPath, String addedK6Url) {
        K6Downloader k6Downloader = new K6Downloader(downloadedPath, addedK6Url);
        if (!new File(k6BinaryPath).exists()) {
            k6Downloader.downloadK6Binary();
            givePermission();
        }
    }


    /**
     * K6 Executor Run test {@literal &} Check for Check List
     *
     * @return {@link K6Result} Returns k6Result
     */

    public K6Result runTest() throws IOException {
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

    private void givePermission() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("mac") || os.contains("darwin") || os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            try {
                Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxr-xr-x");
                Path k6Path = Paths.get(k6BinaryPath);
                Files.setPosixFilePermissions(k6Path, permissions);
                System.out.println("Permissions set successfully.");
            } catch (Exception e) {
                throw new RuntimeException("give permission failed");
            }
        }
    }

    private K6Result resultToK6Result(String result) {
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

    private String getDownLoadPath(String path) {
        return "k6-" + K6_VERSION + "-" + path;
    }


}