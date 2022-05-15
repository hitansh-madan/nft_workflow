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
import org.web3j.tokenerc721.TokenERC721;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.springframework.beans.factory.annotation.Value;

@Component
public class changeERC721MarketplaceApproval implements Worker {

    @Value("${address.key}")
    private String addressKey;

    @Override
    public String getTaskDefName() {
        return "change_ERC721_marketplace_approval";
    }

    @Override
    public TaskResult execute(Task task) {
        TaskResult result = new TaskResult(task);
        try {

            String tokenAddress = (String) task.getInputData().get("tokenAddress");
            String operator = (String) task.getInputData().get("operator");
            String tokenId = (String) task.getInputData().get("tokenId");

            Web3j web3j = Web3j.build(new HttpService("https://rpc-mumbai.matic.today"));
            TransactionManager transactionManager = new RawTransactionManager(web3j, Credentials.create(addressKey),
                    80001);

            TokenERC721 contract = TokenERC721.load(tokenAddress, web3j, transactionManager,
                    new DefaultGasProvider());

            TransactionReceipt transactionReceipt = contract.approveByContractOwner(operator, new BigInteger(tokenId))
                    .send();

            if (transactionReceipt.isStatusOK())
                result.setStatus(TaskResult.Status.COMPLETED);
            else
                result.setStatus(TaskResult.Status.FAILED);

            web3j.shutdown();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
