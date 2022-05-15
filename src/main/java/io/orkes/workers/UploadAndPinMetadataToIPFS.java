package io.orkes.workers;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Component
public class UploadAndPinMetadataToIPFS implements Worker {

    @Value("${storage.key}")
    private String storageKey;

    @Override
    public String getTaskDefName() {
        return "upload_and_pin_metadata_to_IPFS";
    }

    @Override
    public TaskResult execute(Task task) {

        TaskResult result = new TaskResult(task);

        try {
            String ipfsUrl = "ipfs://";

            Gson gson = new Gson();

            String imageIpfsUrl = (String) task.getInputData().get("imageIpfsUrl");
            JsonObject metadata = gson.toJsonTree(task.getInputData().get("metadata")).getAsJsonObject();

            metadata.addProperty("image", imageIpfsUrl);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.nft.storage/upload"))
                    .header("Authorization", "Bearer " + storageKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(metadata.toString()))
                    .build();

            HttpResponse<String> response = HttpClient.newBuilder()
                    .build()
                    .send(request, BodyHandlers.ofString());

            JsonObject responseOb = gson.fromJson(response.body(), JsonObject.class);
            if (!responseOb.get("ok").getAsBoolean()) {
                result.setStatus(TaskResult.Status.FAILED);
                return result;
            }
            String cid = responseOb.get("value").getAsJsonObject().get("cid").getAsString();

            ipfsUrl += cid;

            result.log("Storage Key length :" + storageKey.length());
            result.addOutputData("metadataIpfsUrl", ipfsUrl);
            result.setStatus(TaskResult.Status.COMPLETED);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
