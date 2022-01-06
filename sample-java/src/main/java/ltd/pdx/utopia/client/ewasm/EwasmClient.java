package ltd.pdx.utopia.client.ewasm;

import ltd.pdx.utopia.common.Constants;
import ltd.pdx.utopia.driver.UtopiaEwasmDriver;
import ltd.pdx.utopia.driver.exception.BlockchainDriverException;
import ltd.pdx.utopia.driver.util.DriverUtil;
import ltd.pdx.utopia.eckey.util.ECKeyUtil;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *  ewasm 合约调用实例
 *  合约开发请参考 https://github.com/PDXbaap/ewasm-rust-demo/blob/master/README.md
 *
 *  {@link #deployEwasm()}  部署ewasm合约
 *
 *  {@link #runWriteMethod()}   调用合约的写方法
 *
 *  {@link #runReadMethod()}    调用合约的读方法
 *
 *
 */
public class EwasmClient {


    private String privateKey;
    private String address;
    private UtopiaEwasmDriver driver;

    @Before
    public void before() {

        privateKey = "a2f1a32e5234f64a6624210b871c22909034f24a52166369c2619681390433aa";
        address = ECKeyUtil.getAddressByPriKey(privateKey);

        // 非代理模式
        driver = initDriver();

        // 代理模式
        //driver = initDriverWithProxy();
    }

    /**
     * init driver
     * @return
     */
    public UtopiaEwasmDriver initDriver() {
        UtopiaEwasmDriver driver = new UtopiaEwasmDriver();
        Properties props = new Properties();
        props.setProperty(Constants.BAAP_SENDER_PRIVKEY, privateKey);
        props.setProperty(Constants.BAAP_BLOCKCHAIN_RPC, "http://10.0.0.138:34321");
        props.setProperty(Constants.BAAP_ENGINE_ID, "739");
        driver.init(props);
        return driver;
    }

    /**
     * init driver WithProxy
     * @return
     * @throws BlockchainDriverException
     */
    public UtopiaEwasmDriver initDriverWithProxy() throws BlockchainDriverException {
        UtopiaEwasmDriver driver = new UtopiaEwasmDriver();
        Properties props = new Properties();
        props.setProperty(Constants.BAAP_SENDER_PRIVKEY, privateKey);
        props.setProperty(Constants.BAAP_BLOCKCHAIN_RPC, "http://utopia-chain-739:8545");
        props.setProperty(Constants.BAAP_BLOCKCHAIN_WS_RPC, "ws://127.0.0.1:4321");
        props.setProperty(Constants.BAAP_ENGINE_ID, "739");
        props.setProperty(Constants.BAAP_PROXY_SERVER, "http://10.0.0.8:9999");
        props.setProperty(Constants.BAAP_X_PDX_PROXY_JWT, "eyJhbGciOiJFUzI1NiJ9.eyJpYXQiOjE1ODMzMDE3NDIsIkZSRUUiOiJUUlVFIn0.pcDDkoQ0a6a2gfZ2uPon0tQZ4kJvb-cQrtgQKhj-9-MSS-Asx8PdQsoSgZNjk_a3ijk9r4VIZsm3OMnIrG7AlQ");

        driver.init(props);
        return driver;
    }

    @Test
    public void deployEwasm() throws Exception {
        byte[] payload = Files.readAllBytes(new File("/YourPath/Sample.wasm").toPath());
        String txId = driver.deploy(payload);
        System.out.println(txId);
    }

    /**
     * 写入数据
     * @throws Exception
     */
    @Test
    public void runWriteMethod() throws Exception {
        long nonce = getNonce();
        String contractAddress = "0x1614ef0bfe4c3cf88c8a643c02c4dc8019d87bef";
        byte[] payload = "put:pdx,pdxsss".getBytes();
        String txId = driver.exec(contractAddress, payload, nonce, Constants.BAAP_DEFAULT_GAS_PRICE, Constants.BAAP_DEFAULT_GAS_LIMIT, 0);
        System.out.println(txId);
    }

    /**
     * 读取数据
     * @throws Exception
     */
    @Test
    public void runReadMethod() throws Exception {
        String data = "0x" + Hex.toHexString("get:pdx".getBytes());
        Map<String, String> params = new HashMap<>();
        params.put("to", "0x1614ef0bfe4c3cf88c8a643c02c4dc8019d87bef");
        params.put("data", data);
        String result = driver.rpcCall(1, "eth_call", params, "latest");
        System.out.println(result);
    }


    private long getNonce() throws Exception {
        String from = driver.getProperties().getProperty(Constants.BAAP_SENDER_PRIVKEY);
        String address = ECKeyUtil.getAddressByPriKey(from);
        String nonceStr = driver.rpcCall(1, "eth_getTransactionCount", "0x" + address, "latest");
        long nonce = Long.parseLong((DriverUtil.parseResult(nonceStr).get("result")).substring(2), 16);
        return nonce;
    }
}
