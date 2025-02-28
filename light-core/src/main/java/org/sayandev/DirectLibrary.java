package org.sayandev;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;

public class DirectLibrary {
    private final String uri;
    private final File outputFile;
    private final String checksum;
    private final String checksumType;

    public DirectLibrary(String uri, File outputFile, String checksum, String checksumType) {
        this.uri = uri;
        this.outputFile = outputFile;
        this.checksum = checksum;
        this.checksumType = checksumType;
    }

    public void download() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(uri).openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            // download file to downloads directory
            outputFile.getParentFile().mkdirs();
            OutputStream outputStream = Files.newOutputStream(outputFile.toPath());
            connection.getInputStream().transferTo(outputStream);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public String getURI() {
        return uri;
    }

    public File getOutputFile() {
        return outputFile;
    }
}
