/*************************************************************************
 * Copyright (C) 2016-2019 PDX Technologies, Inc. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *************************************************************************/

package ltd.pdx.utopia.driver.example.chaincodedriver;

import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * 开发一个简单的合约实例
 */
public class MyCc extends ChaincodeBase {

	private static Logger logger = LoggerFactory.getLogger(MyCc.class);

	@Override
	public Response init(ChaincodeStub stub) {
		logger.info("init............................");
		return ResponseUtils.newSuccessResponse();
	}

	@Override
	public Response invoke(ChaincodeStub stub) {
		String payload = "";
		try {
			final String function = stub.getFunction();
			final List<String> params = stub.getParameters();

			logger.info("invoke method: {}, txId:{}", function, stub.getTxId());
			switch (function) {
				case "getHis":
					StringBuilder result = new StringBuilder();
					QueryResultsIterator<KeyModification> historyForKey = stub.getHistoryForKey(params.get(0));
					historyForKey.forEach(e -> {
						logger.info("tid : {} /key : {} /value : {} /isDelete : {}", e.getTxId(), params.get(0), e.getStringValue(), e.isDeleted());
						result.append(String.format("key : %s /value : %s /isDelete : %s", params.get(0), e.getStringValue(), e.isDeleted()));
					});

					historyForKey.close();
					payload = result.toString();
					break;
				case "get":
					String state = stub.getStringState(params.get(0));
					logger.info("get key : {} result : {}", params.get(0), state);
					payload = state;
					break;
				case "put":
					logger.info("start put:{}", stub.getTxId());
					stub.putStringState(params.get(0), params.get(1));
					logger.info("put key : {} value : {}", params.get(0), params.get(1));
					break;
				case "del":
					stub.delState(params.get(0));
					logger.info("delete key : {}", params.get(0));
					break;
				case "getRange":
					logger.info("function:{}", function);
					StringBuilder resultRange = new StringBuilder();
					QueryResultsIterator<KeyValue> stateByRange = stub.getStateByRange(params.get(0), params.get(1));
					stateByRange.forEach(e -> {
						logger.info("key : {} /value : {}", e.getKey(), e.getStringValue());
						resultRange.append(String.format("key : %s /value : %s", e.getKey(), e.getStringValue()));
					});

					stateByRange.close();
					payload = resultRange.toString();
					break;
				default:
					break;
			}
		} catch (Exception e) {
			logger.error("error", e);
			return ResponseUtils.newErrorResponse(e);
		}

		return ResponseUtils.newSuccessResponse(payload.getBytes());
	}

	public static void main(String[] args) {
//		args = new String[]{"-a", "127.0.0.1:7045", "-i", "1ceb7edecea8d481aa315b9a51b65c4def9b3dc6:testcc:1.0.3", "-c", "739"};
//		Arrays.stream(args).forEach(System.out::println);
		logger.info("cc started!");
		MyCc myCc = new MyCc();
		myCc.start(args);
	}
}
