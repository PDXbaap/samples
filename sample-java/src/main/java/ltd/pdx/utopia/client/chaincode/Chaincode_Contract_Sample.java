package ltd.pdx.utopia.client.chaincode;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Chaincode_Contract_Sample extends ChaincodeBase {

    public static void main(String[] args) {
      new Chaincode_Contract_Sample().start(args);
    }
    @Override
    public Response init(ChaincodeStub chaincodeStub) {
        return ResponseUtils.newSuccessResponse();
    }

    @Override
    public Response invoke(ChaincodeStub chaincodeStub) {
        String response = "";
        try {
            final String function = chaincodeStub.getFunction();
            final List<String> params = chaincodeStub.getParameters();

            switch (function) {
                case "put":
                    chaincodeStub.putStringState(params.get(0), params.get(1));
                    break;
                case "get":
                    String result = chaincodeStub.getStringState(params.get(0));
                    response = result;
                    break;
                case "getHis":
                    StringBuilder resultHis = new StringBuilder();
                    QueryResultsIterator<KeyModification> historyForKey = chaincodeStub.getHistoryForKey(params.get(0));
                    historyForKey.forEach(e -> {
                        resultHis.append(String.format("key : %s /value : %s /isDelete : %s", params.get(0), e.getStringValue(), e.isDeleted()));
                    });
                    historyForKey.close();
                    response = resultHis.toString();
                    break;
                case "getRange":
                    StringBuilder resultRange = new StringBuilder();
                    QueryResultsIterator<KeyValue> stateByRange = chaincodeStub.getStateByRange(params.get(0), params.get(1));
                    stateByRange.forEach(e -> {
                        resultRange.append(String.format("key : %s /value : %s", e.getKey(), e.getStringValue()));
                    });
                    stateByRange.close();
                    response = resultRange.toString();
                    break;
                case "del":
                    chaincodeStub.delState(params.get(0));
                    break;

                default:
                    break;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseUtils.newErrorResponse(exception);
        }
        return ResponseUtils.newSuccessResponse(response.getBytes(StandardCharsets.UTF_8));
    }
}
