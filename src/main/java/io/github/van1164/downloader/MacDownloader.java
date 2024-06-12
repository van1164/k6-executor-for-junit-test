package io.github.van1164.downloader;

import static io.github.van1164.util.Constant.TAR_FILE_NAME;
import static io.github.van1164.util.FileUtil.unzip;

public class MacDownloader extends AbstractK6DownloaderByOS {

    public MacDownloader(String k6Url, String k6BinaryPath){
        this.k6Url = k6Url;
        this.k6BinaryPath = k6BinaryPath;
    }

    @Override
    public void k6DownloadAndExtract() throws Exception {
        downloadFile(k6Url,TAR_FILE_NAME);
        unzip(TAR_FILE_NAME, ".",k6BinaryPath);
    }


}