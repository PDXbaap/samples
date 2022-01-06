package ltd.pdx.utopia.client.event_listening;


import ltd.pdx.utopia.common.Constants;
import ltd.pdx.utopia.driver.UtopiaChaincodeDriver;
import ltd.pdx.utopia.driver.enums.SubscribeType;
import ltd.pdx.utopia.driver.webSocketClent.Listener;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.MapType;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import java.io.IOException;
import java.util.*;

/**
 * 消息订阅实例
 */
public class DriverEvent {
    public static void main(String[] args) throws Exception {

        UtopiaChaincodeDriver driver = new UtopiaChaincodeDriver();

        Properties props = new Properties();
        props.setProperty(Constants.BAAP_SENDER_PRIVKEY, "65035d9621f7be3bb6dc1f5a646e6ee2ef6bddf3f1ce57782d409c23857401a6");
        props.setProperty(Constants.BAAP_BLOCKCHAIN_RPC, "http://127.0.0.1:8545");
        props.setProperty(Constants.BAAP_BLOCKCHAIN_WS_RPC, "ws://192.168.5.8:6667");
        props.setProperty(Constants.BAAP_ENGINE_ID, Constants.BAAP_ENGINE_ID_DEFAULT);
        driver.init(props, new Listener() {
            @Override
            public void event(String clientAssignedId, String result) {
                System.out.println(clientAssignedId + "--->" + result);
                // 如果订阅的是logs 用此方法解析
//                parseLogData(result);
            }
        });

        subcribeLogs(driver);
//        driver.subscribe("1", SubscribeType.NEWPENDINGTRANSACTIONS.getName());
//        driver.subscribe("1", SubscribeType.NEWHEADS.getName());
//        Thread.sleep(10000);
//        driver.unsubscribe("1");

    }

    // 订阅事件
    static public void subcribeLogs(UtopiaChaincodeDriver driver) throws Exception {
        Object[] params = new Object[2];
        params[0] = (SubscribeType.LOGS.getName());
        Map<String, Object> map = new HashMap<String, Object>();
        // 订阅合约地址数组,topics 只能是同一个事件的，多个事件不能写在同一个topics里
        map.put("address", Arrays.asList("0xBd29e3A43E2cd714631eDb6D10f57CcaAaA2CAff", "0x50d99F3a62dEa582E0c2D5BA0A9785e368Cb407b"));
        // 订阅topic 数组   第一个元素为为合约事件签名（签名的参数顺序不可变）,
        // 由于solidity可以在事件参数上增加indexed属性，最多可以对三个参数增加这样的属性。所以topics最多为4个
        // 即第一个默认主题事件签名和三个indexed修饰的参数的值，且另外三个参数只能是此事件下的indexed参数作为topic
        // topics数组的顺序要和事件indexed参数顺序一致
        // 如果数组，包括字符串，字节数据做为索引参数，实际主题是对应值的Keccak-256哈希值。
        List paramsArr = Arrays.asList(new TypeReference<Uint>() {
        }, new TypeReference<Address>() {
        }, new TypeReference<Utf8String>() {
        });
        Event event = new Event("transfer", paramsArr);
        String functionEventSig = EventEncoder.encode(event);

        List paramsArr2 = Arrays.asList(new TypeReference<Utf8String>() {
        }, new TypeReference<Address>() {
        }, new TypeReference<Utf8String>() {
        });
        Event event2 = new Event("transfer", paramsArr2);
        String functionEventSig2 = EventEncoder.encode(event2);

        List<String> topics = Arrays.asList();
        map.put("topics", new ArrayList<String>() {{
            add(functionEventSig2);
        }});
        params[1] = map;
        driver.subscribe("1", params);
    }


    static public void parseLogData(String result) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        MapType javaType = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
        Map<String, Object> ret = objectMapper.readValue(result, javaType);
        List<TypeReference<?>> outputParameters = new ArrayList<TypeReference<?>>() {{
            add(new TypeReference<Utf8String>(true) {
            });
        }};
        List decodeData = decodeMsg((String) ret.get("data"), outputParameters);
        System.out.println("data:" + decodeData);
        List decodeTopics = (List) ret.get("topics");
        // topics 内元素可decodeMsg（）解码
        System.out.println("decodeTopics2:" + decodeTopics);

    }


    /*
     * 解码log 里的topics和data数据
     */
    static public List decodeMsg(String rawIput, List<TypeReference<?>> outputParameters) {
        Function function = new Function(null, new ArrayList<>(), outputParameters);
        List<Type> decode = FunctionReturnDecoder.decode(rawIput, function.getOutputParameters());
        // gid groupInfo
        List result = new ArrayList();
        for (Object obj : decode) {
            result.add(((Type) obj).getValue());
        }
        return result;
    }
}
