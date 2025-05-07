package io.github.van1164.executor;

import io.github.van1164.downloader.K6Downloader;
import io.github.van1164.result.CounterStats;
import io.github.van1164.result.DataStats;
import io.github.van1164.result.DurationStats;
import io.github.van1164.result.K6Result;
import io.github.van1164.scirptBuilder.K6ScriptBuilder;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.van1164.util.Helpers.toBytes;
import static io.github.van1164.util.Helpers.toMillis;
import static io.github.van1164.util.K6Constants.K6_BINARY_PATH;
import static io.github.van1164.util.K6Constants.K6_VERSION;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public abstract class K6Executor {
    @Builder.Default
    protected String k6BinaryPath = K6_BINARY_PATH;

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
                this.k6BinaryPath = "." + downloadedPath + fileSeparator + this.k6BinaryPath;
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

    /* 2) 상세 파싱 함수들 */
    DurationStats parseDuration(String line) {
        // 예: "avg=41.91ms min=41.91ms med=41.91ms max=41.91ms p(90)=41.91ms p(95)=41.91ms"
        Pattern p = Pattern.compile("(avg|min|med|max|p\\(90\\)|p\\(95\\))=([0-9.]+[a-zµ]+)");
        double avg=-1,min=-1,med=-1,max=-1,p90=-1,p95=-1;
        Matcher m = p.matcher(line);
        while (m.find()) {
            switch (m.group(1)) {
                case "avg"    -> avg = toMillis(m.group(2));
                case "min"    -> min = toMillis(m.group(2));
                case "med"    -> med = toMillis(m.group(2));
                case "max"    -> max = toMillis(m.group(2));
                case "p(90)"  -> p90 = toMillis(m.group(2));
                case "p(95)"  -> p95 = toMillis(m.group(2));
            }
        }
        return new DurationStats(avg,min,med,max,p90,p95);
    }

    CounterStats parseCounter(String line) {
        // 예: "1     18.59/s"  or "0 out of 1"
        Matcher m1 = Pattern.compile("^(\\d+)\\s+([0-9.]+)/s").matcher(line.trim());
        if (m1.find())
            return new CounterStats(Long.parseLong(m1.group(1)), Double.parseDouble(m1.group(2)));

        Matcher m2 = Pattern.compile("^(\\d+) out of (\\d+)").matcher(line.trim());
        if (m2.find())
            return new CounterStats(Long.parseLong(m2.group(1)), 0.0);

        return new CounterStats(0,0.0);
    }

    DataStats parseData(String line) {
        // 예: "89 B  1.7 kB/s"
        Matcher m = Pattern.compile("([0-9.]+)\\s*(B|kB|MB)\\s+([0-9.]+)\\s*(B|kB|MB)/s").matcher(line.trim());
        if (m.find()) {
            long bytes = toBytes(m.group(1), m.group(2));
            double rate = toBytes(m.group(3), m.group(4));
            return new DataStats(bytes, rate);
        }
        return new DataStats(0,0.0);
    }

    private String getDownLoadPath(String path) {
        return "k6-" + K6_VERSION + "-" + path;
    }


    /* ───────────────────── template method ───────────────────── */
    public final K6Result runTest() throws IOException {
        k6SetUp();                                            // 1. ensure binary
        Process process = new ProcessBuilder(createCommand()) // 2. start process
                .redirectErrorStream(true)
                .start();
        afterProcessStart(process);                           // 3. optional stdin

        StringBuilder out = new StringBuilder();              // 4. capture output
        try (BufferedReader br =
                     new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) out.append(line).append('\n');
        }
        return resultToK6Result(out.toString());              // 5. parse result
    }

    /* ───────────── hooks for subclasses ───────────── */
    protected abstract String[] createCommand();

    protected abstract K6Result resultToK6Result(String raw);

    protected void afterProcessStart(Process p) throws IOException {/* default NOP */}


    public static ScriptPathExecutor.ScriptPathExecutorBuilder<?, ?>
    withScriptPath(String scriptPath) {
        return ScriptPathExecutor.builder().scriptPath(scriptPath);
    }

    /* builder‑based script */
    public static ScriptBuilderExecutor.ScriptBuilderExecutorBuilder<?, ?>
    withScript(K6ScriptBuilder sb) {
        return ScriptBuilderExecutor.builder().sb(sb);
    }
}
