package io.orkes.workers;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpResponse;
import javax.imageio.ImageIO;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class UploadAndPinImageToIPFS implements Worker {

    @Value("${storage.key}")
    private String storageKey;

    @Override
    public String getTaskDefName() {
        return "upload_and_pin_image_to_IPFS";
    }

    @Override
    public TaskResult execute(Task task) {
        TaskResult result = new TaskResult(task);
        try {
            String imageUrl = (String) task.getInputData().get("imageUrl");
            String imageIpfsUrl = "ipfs://";

            URL url = new URL(imageUrl);

            BufferedImage image = ImageIO.read(url);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            String mimeType = url.openConnection().getContentType();
            if (mimeType.startsWith("image/"))
                ImageIO.write(image, mimeType.substring("image/".length()), bos);
            else {
                result.setStatus(TaskResult.Status.FAILED);
                return result;
            }

            byte[] data = bos.toByteArray();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://api.nft.storage/upload"))
                    .header("Authorization", "Bearer " + storageKey)
                    .header("Content-Type", mimeType)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(data))
                    .build();

            HttpResponse<String> response = HttpClient.newBuilder()
                    .build()
                    .send(request, BodyHandlers.ofString());

            Gson gson = new Gson();
            JsonObject responseOb = gson.fromJson(response.body(), JsonObject.class);
            if (!responseOb.get("ok").getAsBoolean()) {
                result.setStatus(TaskResult.Status.FAILED);
                return result;
            }
            String cid = responseOb.get("value").getAsJsonObject().get("cid").getAsString();

            imageIpfsUrl += cid;

            result.log("Storage Key length :" + storageKey.length());
            result.addOutputData("imageIpfsUrl", imageIpfsUrl);
            result.setStatus(TaskResult.Status.COMPLETED);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
