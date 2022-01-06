package ltd.pdx.utopia.client.dynamic_native;

import ltd.pdx.utopia.common.Constants;
import ltd.pdx.utopia.driver.UtopiaDriver;
import ltd.pdx.utopia.driver.UtopiaDynNativeDriver;
import ltd.pdx.utopia.driver.exception.BlockchainDriverException;
import ltd.pdx.utopia.eckey.util.ECKeyUtil;
import ltd.pdx.utopia.eckey.util.EncryptUtil;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DynNativeClient extends UtopiaDriver {

    /**
     * 16进制私钥
     */
    private String privateK;
    /**
     * 合约所有者地址
     */
    private String owner;
    /**
     * 部署动态库合约预编译合约地址
     */
    private static final String DEPLOY_ADDRESS = "0x6067b1C683c96EDEb4031cA8D75e2902D0dfB9dD";
    private UtopiaDynNativeDriver driver;

    /**
     * init driver
     *
     * @return
     */
    public UtopiaDynNativeDriver initDriver() {
        /*
        动态库合约driver
         */
        UtopiaDynNativeDriver driver = new UtopiaDynNativeDriver();
        Properties props = new Properties();
        /*
        设置私钥
         */
        props.setProperty(Constants.BAAP_SENDER_PRIVKEY, privateK);
        /*
        设置rpc地址
         */
        props.setProperty(Constants.BAAP_BLOCKCHAIN_RPC, "http://192.168.3.47:8545");
        /*
        设置链ID
         */
        props.setProperty(Constants.BAAP_ENGINE_ID, "773");
        /*
        初始化driver
         */
        driver.init(props);
        return driver;
    }

    @Before
    public void before() {
        /*
        初始化16进制私钥
         */
        privateK = "a2f1a32e5234f64a6624210b871c22909034f24a52166369c2619681390433aa";
        /*
        初始化动态库合约driver
         */
        driver = initDriver();
        /*
        初始化合约所有者
         */
        owner = ECKeyUtil.getAddressByPriKey(privateK);
    }

    /**
     * 部署动态库合约
     */
    @Test
    public void deploy() throws Exception {

        // 动态库本地path @发送方客户端
        String soPath = "/path-to-your/DynNative_sample.1.11.so";

        // 读取动态库文件二进制数据
        byte[] soData = Files.readAllBytes(new File(soPath).toPath());
        /*
        初始化交易payload
        soName              动态库名称 只有部署合约时传入
        lookUpClassName     动态库对外提供的类名称 部署、调用时都需要传入
        args                调用动态库合约参数 只有调用合约时传入
        soData              动态库文件二进制数据 只有部署合约时传入
         */
        DynNativeCallParam dynNativeCallParam = new DynNativeCallParam("sample", "DynNative", null, soData);

        // 获取rlp编码
        byte[] payload = dynNativeCallParam.getRlpEncoded();
        // DEPLOY_ADDRESS 0x6067b1C683c96EDEb4031cA8D75e2902D0dfB9dD部署动态库合约的预编译合约地址
        String txHash = driver.exec(DEPLOY_ADDRESS, payload);
        System.out.println("txHash: " + txHash);
    }

    /**
     * 动态库写入操作
     *
     * @throws Exception
     */
    @Test
    public void exec() throws Exception {
        /*
        动态库合约调用参数，第一项为合约方法名称，其余为合约方法调用参数列表
         */
        byte[][] args = new byte[][]{"put".getBytes(StandardCharsets.UTF_8), "key".getBytes(StandardCharsets.UTF_8), "value".getBytes(StandardCharsets.UTF_8)};
        /*
        动态库合约地址，规则为 ${合约拥有者地址}:${部署合约时设置的动态库名称(soName)} 拼接的字符串做sha3操作结果后20个字节
         */
        String contractAddress = EncryptUtil.keccak256ToAddress(owner + Constants.BAAP_CC_NAME_SEPARATOR + "sample");
        /*
        初始化交易payload
        soName              动态库名称 只有部署合约时传入
        lookUpClassName     动态库对外提供的类名称 部署、调用时都需要传入
        args                调用动态库合约参数 只有调用合约时传入
        soData              动态库文件二进制数据 只有部署合约时传入
         */
        DynNativeCallParam dynNativeCallParam = new DynNativeCallParam(null, "DynNative", args, null);
        /*
        获取rlp编码
         */
        byte[] payload = dynNativeCallParam.getRlpEncoded();
        /*
        执行交易 返回交易hash
         */
        String txHash = driver.exec(contractAddress, payload);
        System.out.println("txHash: " + txHash);
    }

    /**
     * 动态库合约查询操作
     *
     * @throws BlockchainDriverException
     */
    @Test
    public void query() throws BlockchainDriverException {
        /*
        动态库合约调用参数，第一项为合约方法名称，其余为合约方法调用参数列表
         */
        byte[][] args = new byte[][]{"get".getBytes(StandardCharsets.UTF_8), "key".getBytes(StandardCharsets.UTF_8)};
        /*
        动态库合约地址，规则为 ${合约拥有者地址}:${部署合约时设置的动态库名称(soName)} 拼接的字符串做sha3操作结果后20个字节
         */
        String contractAddress = EncryptUtil.keccak256ToAddress(owner + Constants.BAAP_CC_NAME_SEPARATOR + "sample");
        /*
        初始化交易payload
        soName              动态库名称 只有部署合约时传入
        lookUpClassName     动态库对外提供的类名称 部署、调用时都需要传入
        args                调用动态库合约参数 只有调用合约时传入
        soData              动态库文件二进制数据 只有部署合约时传入
         */
        DynNativeCallParam dynNativeCallParam = new DynNativeCallParam(null, "DynNative", args, null);
        /*
        获取rlp编码
         */
        byte[] payload = dynNativeCallParam.getRlpEncoded();
        /*
        payload转成16进制字符串
         */
        String data = "0x" + Hex.toHexString(payload);

        /*
        构建交易Map {to:"动态库合约地址", data: "16进制payload"}
         */
        Map<String, String> params = new HashMap<>();
        params.put("to", "0x" + contractAddress);
        params.put("data", data);
        /*
        执行JsonRpc method eth_call，返回执行结果
         */
        String result = driver.rpcCall(1, "eth_call", params, "latest");
        System.out.println("result: " + result);
    }
}
