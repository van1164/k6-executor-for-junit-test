package io.github.van1164;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

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

    public K6Executor(String scriptPath, String[] checkList) {
        this.scriptPath = scriptPath;
        this.k6BinaryPath = "k6";

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            this.k6BinaryPath += ".exe";
        }

        this.checkList = checkList;
    }

    private void downloadK6Binary() throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        String k6Url = "https://github.com/grafana/k6/releases/download/v0.51.0/k6-v0.51.0-";

        if (os.contains("win")) {
            k6Url += "windows-amd64.zip";
        } else if (os.contains("mac") || os.contains("darwin")) {
            k6Url += "macos-amd64.tar.gz";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            k6Url += "linux-amd64.tar.gz";
        } else {
            throw new Exception("Unsupported OS: " + os);
        }


        URL url = new URI(k6Url).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
        FileOutputStream out = new FileOutputStream("k6_download");
        byte[] buffer = new byte[1024];
        int count;

        while ((count = in.read(buffer, 0, 1024)) != -1) {
            out.write(buffer, 0, count);
        }

        in.close();
        out.close();


        if (k6Url.endsWith(".zip")) {
            unzip("k6_download", ".");
        } else {
            untar("k6_download", ".");
        }
    }

    private void unzip(String filePath, String destDir) throws Exception {
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(filePath))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                File outputFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    if (!outputFile.exists()) {
                        outputFile.mkdirs();
                    }
                } else {
                    try (FileOutputStream fos = new FileOutputStream(outputFile);
                         BufferedOutputStream bos = new BufferedOutputStream(fos, 1024)) {
                        byte[] buffer = new byte[1024];
                        int count;
                        while ((count = zipIn.read(buffer)) != -1) {
                            bos.write(buffer, 0, count);
                        }
                    }
                }
                zipIn.closeEntry();
            }
        }

        if (k6BinaryPath.endsWith(".exe")) {
            File sourceFile = new File(destDir + "/k6-v0.51.0-windows-amd64/k6.exe");
            File destFile = new File(destDir + "/k6.exe");
            if (!destFile.exists() && sourceFile.exists()) {
                Files.move(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private void untar(String filePath, String destDir) throws Exception {
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(
                new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(filePath))))) {
            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                File outputFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    if (!outputFile.exists()) {
                        outputFile.mkdirs();
                    }
                } else {
                    try (FileOutputStream fos = new FileOutputStream(outputFile);
                         BufferedOutputStream dest = new BufferedOutputStream(fos, 1024)) {
                        int count;
                        byte[] data = new byte[1024];
                        while ((count = tarIn.read(data, 0, 1024)) != -1) {
                            dest.write(data, 0, count);
                        }
                    }
                }
            }
        }
    }

    /**
     *  K6 Executor Run test {@literal &} Check for Check List
     *
     * @return Returns true if all checklists are satisfied
     */

    public boolean runTest() throws Exception {

        if (!new File(k6BinaryPath).exists()) {
            downloadK6Binary();
        }

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