package io.github.van1164.executor;

import io.github.van1164.downloader.K6Downloader;
import io.github.van1164.result.HttpReq;
import io.github.van1164.result.K6Result;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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

@SuperBuilder
abstract class K6Executor {
    @Builder.Default
    protected String k6BinaryPath = K6_BINARY_PATH;

//
//    K6Executor() {
//        this.k6BinaryPath  = K6_BINARY_PATH;
//        System.out.println(k6BinaryPath);
//
//    }
//    K6Executor(String k6BinaryPath) {
//        if (k6BinaryPath == null) {
//            this.k6BinaryPath  = K6_BINARY_PATH;
//        }
//        else{
//            this.k6BinaryPath = k6BinaryPath;
//            System.out.println(k6BinaryPath);
//        }
//    }

    protected void k6SetUp() {
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


    private String getDownLoadPath(String path) {
        return "k6-" + K6_VERSION + "-" + path;
    }


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
    // Declare resultToK6Result as an abstract method
    protected abstract K6Result resultToK6Result(String result);


    protected abstract String[] createCommand();


}
