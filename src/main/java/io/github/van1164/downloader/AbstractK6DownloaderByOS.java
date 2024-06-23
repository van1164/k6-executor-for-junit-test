package io.github.van1164.downloader;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

abstract public class AbstractK6DownloaderByOS implements K6DownloaderByOS{
    protected String k6Url;
    protected String k6BinaryPath;

    @Override
    public abstract void k6DownloadAndExtract();

    protected void downloadFile(String k6Url, String file) {
        try {
            URL url = new URI(k6Url).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
            FileOutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int count;

            while ((count = in.read(buffer, 0, 1024)) != -1) {
                out.write(buffer, 0, count);
            }

            in.close();
            out.close();
        } catch (Exception e){
            throw new RuntimeException("K6 Download Failed");
        }

    }
}
