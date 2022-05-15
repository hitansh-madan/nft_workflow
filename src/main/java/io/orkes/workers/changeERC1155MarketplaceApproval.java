package io.orkes.workers;

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
public class changeERC1155MarketplaceApproval implements Worker {

    @Value("${address.key}")
    private String addressKey;

    @Override
    public String getTaskDefName() {
        return "change_ERC1155_marketplace_approval";
    }

    @Override
    public TaskResult execute(Task task) {
        TaskResult result = new TaskResult(task);
        try {

            String tokenAddress = (String) task.getInputData().get("tokenAddress");
            String ownerAddress = (String) task.getInputData().get("ownerAddress");
            String operator = (String) task.getInputData().get("operator");
            Boolean approved = (Boolean) task.getInputData().get("approved");

            Web3j web3j = Web3j.build(new HttpService("https://rpc-mumbai.matic.today"));
            TransactionManager transactionManager = new RawTransactionManager(web3j, Credentials.create(addressKey),
                    80001);

            TokenERC1155 contract = TokenERC1155.load(tokenAddress, web3j, transactionManager,
                    new DefaultGasProvider());

            TransactionReceipt transactionReceipt = contract
                    .setApprovalForAllByContractOwner(ownerAddress, operator, approved)
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
