package io.github.van1164.downloader;


import static io.github.van1164.util.Constant.K6_VERSION;

public class K6Downloader {

    private K6DownloaderByOS k6DownloaderByOS;

    public K6Downloader(String k6BinaryPath) throws Exception {
        String os = System.getProperty("os.name").toLowerCase();
        String k6Url = String.format("https://github.com/grafana/k6/releases/download/%1$s/k6-%1$s-",K6_VERSION);
        if (os.contains("win")) {
            k6Url += "windows-amd64.zip";
            k6DownloaderByOS = new WindowsDownloader(k6Url,k6BinaryPath);
        } else if (os.contains("mac") || os.contains("darwin")) {
            k6Url += "macos-amd64.tar.gz";
            k6DownloaderByOS = new MacDownloader(k6Url,k6BinaryPath);
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            k6Url += "linux-amd64.tar.gz";
            k6DownloaderByOS = new LinuxDownloader(k6Url,k6BinaryPath);
        } else {
            throw new Exception("Unsupported OS: " + os);
        }
    }

    public void downloadK6Binary() throws Exception {
        k6DownloaderByOS.k6DownloadAndExtract();
    }
}
