package io.orkes.workers;

import java.math.BigInteger;
import java.util.List;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.nftmarket.NFTMarket;
import org.web3j.nftmarket.NFTMarket.ListingCreatedEventResponse;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.springframework.beans.factory.annotation.Value;

@Component
public class createERC1155MarketplaceListing implements Worker {

    @Value("${address.key}")
    private String addressKey;

    @Override
    public String getTaskDefName() {
        return "create_ERC1155_marketplace_listing";
    }

    @Override
    public TaskResult execute(Task task) {
        TaskResult result = new TaskResult(task);
        try {

            String marketplaceAddress = (String) task.getInputData().get("marketplaceAddress");
            String tokenAddress = (String) task.getInputData().get("tokenAddress");
            String tokenId = (String) task.getInputData().get("tokenId");
            String ownerAddress = (String) task.getInputData().get("ownerAddress");
            String price = (String) task.getInputData().get("price");
            String quantity = (String) task.getInputData().get("quantity");

            String listingId = "";

            Web3j web3j = Web3j.build(new HttpService("https://rpc-mumbai.matic.today"));
            TransactionManager transactionManager = new RawTransactionManager(web3j, Credentials.create(addressKey),
                    80001);

            NFTMarket contract = NFTMarket.load(marketplaceAddress, web3j, transactionManager,
                    new DefaultGasProvider());

            TransactionReceipt transactionReceipt = contract
                    .createERC1155Listing(tokenAddress, new BigInteger(tokenId), ownerAddress, new BigInteger(price),
                            new BigInteger(quantity))
                    .send();

            if (!transactionReceipt.isStatusOK()) {
                result.setStatus(TaskResult.Status.FAILED);
                return result;
            }

            List<ListingCreatedEventResponse> eventValues = contract.getListingCreatedEvents(transactionReceipt);
            listingId = eventValues.get(0).listingId.toString();

            web3j.shutdown();

            result.addOutputData("listingId", listingId);
            result.setStatus(TaskResult.Status.COMPLETED);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
