package com.uninote.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Service
public class CloudConvertService {

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${cloudconvert.api.key}")
    private String apiKey;

    public File convertToPdf(File inputFile) throws IOException, InterruptedException {
        // 1. Create Job
        String jobPayload = "{ \"tasks\": { " +
                "\"import-1\": { \"operation\": \"import/upload\" }, " +
                "\"convert-1\": { \"operation\": \"convert\", \"input\": \"import-1\", \"output_format\": \"pdf\" }, " +
                "\"export-1\": { \"operation\": \"export/url\", \"input\": \"convert-1\" } } }";

        RequestBody jobBody = RequestBody.create(
                jobPayload,
                MediaType.parse("application/json")
        );

        Request jobRequest = new Request.Builder()
                .url("https://api.cloudconvert.com/v2/jobs")
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(jobBody)
                .build();

        String uploadUrl;
        String jobId;

        try (Response jobResponse = client.newCall(jobRequest).execute()) {
            if (!jobResponse.isSuccessful()) {
                throw new IOException("Job creation failed: " + jobResponse);
            }

            JsonNode jobJson = objectMapper.readTree(jobResponse.body().string());
            JsonNode tasks = jobJson.at("/data/tasks");

            uploadUrl = null;
            for (JsonNode task : tasks) {
                if ("import/upload".equals(task.get("operation").asText())) {
                    uploadUrl = task.get("result").get("form").get("url").asText();
                }
            }

            jobId = jobJson.get("data").get("id").asText();

            if (uploadUrl == null) {
                throw new IllegalStateException("Upload URL not found in job response");
            }
        }

        // 2. Upload file
        RequestBody filePart = RequestBody.create(
                Files.readAllBytes(inputFile.toPath()),
                MediaType.parse("application/octet-stream")
        );

        RequestBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", inputFile.getName(), filePart)
                .build();

        Request uploadRequest = new Request.Builder()
                .url(uploadUrl)
                .post(multipartBody)
                .build();

        try (Response uploadResponse = client.newCall(uploadRequest).execute()) {
            if (!uploadResponse.isSuccessful()) {
                throw new IOException("File upload failed: " + uploadResponse);
            }
        }

        // 3. Poll for export URL
        String exportUrl = null;
        for (int i = 0; i < 20; i++) {
            Thread.sleep(3000);

            Request pollRequest = new Request.Builder()
                    .url("https://api.cloudconvert.com/v2/jobs/" + jobId)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .build();

            try (Response pollResponse = client.newCall(pollRequest).execute()) {
                JsonNode pollJson = objectMapper.readTree(pollResponse.body().string());
                for (JsonNode task : pollJson.at("/data/tasks")) {
                    if ("export/url".equals(task.get("operation").asText()) &&
                            "finished".equals(task.get("status").asText())) {
                        exportUrl = task.get("result").get("files").get(0).get("url").asText();
                        break;
                    }
                }

                if (exportUrl != null) break;
            }
        }

        if (exportUrl == null) {
            throw new RuntimeException("Timed out waiting for export URL");
        }

        // 4. Download the converted file
        Request downloadRequest = new Request.Builder()
                .url(exportUrl)
                .build();

        File outputFile = new File("/tmp", UUID.randomUUID() + ".pdf");

        try (Response downloadResponse = client.newCall(downloadRequest).execute();
             FileOutputStream fos = new FileOutputStream(outputFile)) {

            if (!downloadResponse.isSuccessful()) {
                throw new IOException("Failed to download converted PDF");
            }

            fos.write(downloadResponse.body().bytes());
        }

        return outputFile;
    }
}
