package io.github.van1164.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {

    public static void unzip(String filePath, String destDir, String k6BinaryPath) throws Exception {
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

    private static void untar(String filePath, String destDir) throws Exception {
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
}
