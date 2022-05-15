package io.orkes.workers;

import java.math.BigInteger;

import com.netflix.conductor.client.worker.Worker;
import com.netflix.conductor.common.metadata.tasks.Task;
import com.netflix.conductor.common.metadata.tasks.TaskResult;
import org.springframework.stereotype.Component;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tokenerc1155.TokenERC1155;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.springframework.beans.factory.annotation.Value;

@Component
public class mintERC1155 implements Worker {

    @Value("${address.key}")
    private String addressKey;

    @Override
    public String getTaskDefName() {
        return "mint_ERC1155";
    }

    @Override
    public TaskResult execute(Task task) {
        TaskResult result = new TaskResult(task);
        try {

            String tokenAddress = (String) task.getInputData().get("tokenAddress");
            String ownerAddress = (String) task.getInputData().get("ownerAddress");
            String tokenId = (String) task.getInputData().get("tokenId");
            String quantity = (String) task.getInputData().get("quantity");
            String tokenUri = (String) task.getInputData().get("tokenUri");

            String txHash = "";

            Web3j web3j = Web3j.build(new HttpService("https://rpc-mumbai.matic.today"));
            TransactionManager transactionManager = new RawTransactionManager(web3j, Credentials.create(addressKey),
                    80001);

            TokenERC1155 contract = TokenERC1155.load(tokenAddress, web3j, transactionManager,
                    new DefaultGasProvider());

            TransactionReceipt transactionReceipt = contract
                    .mint(ownerAddress, new BigInteger(tokenId), new BigInteger(quantity), "".getBytes(), tokenUri)
                    .send();

            if (!transactionReceipt.isStatusOK()) {
                result.setStatus(TaskResult.Status.FAILED);
                return result;
            }

            txHash = transactionReceipt.getTransactionHash();

            web3j.shutdown();

            result.addOutputData("txHash", txHash);
            result.setStatus(TaskResult.Status.COMPLETED);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
