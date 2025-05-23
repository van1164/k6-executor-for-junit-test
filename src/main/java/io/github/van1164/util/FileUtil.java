package io.github.van1164.util;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileUtil {

    public static void unzip(String filePath, String destDir) {
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(filePath))) {
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                File outputFile = new File(destDir, "."+entry.getName());
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
        } catch (IOException e) {
            throw new RuntimeException("Failed to unzip file: " + filePath, e);
        }
        deleteFile(filePath);
    }

    public static void untar(String filePath, String destDir) {
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(
                new GzipCompressorInputStream(new BufferedInputStream(new FileInputStream(filePath))))) {
            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                File outputFile = new File(destDir, "."+entry.getName());
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
        } catch (IOException e) {
            throw new RuntimeException("Failed to untar file: " + filePath, e);
        }
        deleteFile(filePath);
    }

    private static void deleteFile(String filePath) {
        Path path = Paths.get(filePath);
        try {
            Files.delete(path);
        } catch (NoSuchFileException e) {
            throw new RuntimeException("k6 file not found: " + filePath);
        } catch (Exception e) {
            throw new RuntimeException("k6 directory already exist: " + filePath);
        }
    }

    private static void deleteDir(String dirPath){
        Path path = Paths.get(dirPath);
        try {
            Files.delete(path);
        } catch (NoSuchFileException e) {
            throw new RuntimeException("k6 file not found: " + dirPath);
        } catch (Exception e) {
            throw new RuntimeException("k6 directory already exist: " + dirPath);
        }
    }
}
