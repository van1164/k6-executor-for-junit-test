package io.github.van1164.downloader;


import static io.github.van1164.util.Constant.K6_VERSION;

public class K6Downloader {

    private K6DownloaderByOS k6DownloaderByOS;
    private String k6BinaryPath;

    public K6Downloader(String downloadedPath, String addedK6Url) throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        String k6Url = String.format("https://github.com/grafana/k6/releases/download/%s/%s",K6_VERSION,addedK6Url);
        if (os.contains("win")) {
            k6DownloaderByOS = new WindowsDownloader(k6Url,downloadedPath);
        } else if (os.contains("mac") || os.contains("darwin")) {
            k6DownloaderByOS = new MacDownloader(k6Url,downloadedPath);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            k6DownloaderByOS = new LinuxDownloader(k6Url,downloadedPath);
        } else {
            throw new Exception("Unsupported OS: " + os);
        }
    }

    public void downloadK6Binary() throws Exception {
        k6DownloaderByOS.k6DownloadAndExtract();
    }

}
