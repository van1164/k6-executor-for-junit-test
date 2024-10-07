package io.github.van1164.downloader;

import static io.github.van1164.util.K6Constants.TAR_FILE_NAME;
import static io.github.van1164.util.FileUtil.untar;

public class LinuxDownloader extends AbstractK6DownloaderByOS{
    public LinuxDownloader(String k6Url, String k6BinaryPath){
        this.k6Url = k6Url;
        this.k6BinaryPath = k6BinaryPath;
    }

    @Override
    public void k6DownloadAndExtract(){
        downloadFile(k6Url,TAR_FILE_NAME);
        untar(TAR_FILE_NAME, ".");
    }

}
