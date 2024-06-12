package io.github.van1164.downloader;


import static io.github.van1164.util.Constant.K6_VERSION;
import static io.github.van1164.util.Constant.ZIP_FILE_NAME;
import static io.github.van1164.util.FileUtil.unzip;

public class WindowsDownloader extends AbstractK6DownloaderByOS{

    public WindowsDownloader(String k6Url, String k6BinaryPath){
        this.k6Url = k6Url;
        this.k6BinaryPath = k6BinaryPath;
    }

    @Override
    public void k6DownloadAndExtract() throws Exception {
        downloadFile(k6Url,ZIP_FILE_NAME);
        unzip(ZIP_FILE_NAME, ".",k6BinaryPath);
    }
}
