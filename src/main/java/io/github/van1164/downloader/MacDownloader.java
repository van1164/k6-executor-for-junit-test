package io.github.van1164.downloader;

import static io.github.van1164.util.K6Constants.ZIP_FILE_NAME;
import static io.github.van1164.util.FileUtil.unzip;

public class MacDownloader extends AbstractK6DownloaderByOS {

    public MacDownloader(String k6Url, String k6BinaryPath){
        this.k6Url = k6Url;
        this.k6BinaryPath = k6BinaryPath;
    }

    @Override
    public void k6DownloadAndExtract() {
        downloadFile(k6Url,ZIP_FILE_NAME);
        unzip(ZIP_FILE_NAME, ".");
    }


}
