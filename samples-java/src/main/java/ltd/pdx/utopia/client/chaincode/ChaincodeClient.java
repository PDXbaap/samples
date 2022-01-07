package ltd.pdx.utopia.client.chaincode;

import ltd.pdx.utopia.common.Constants;
import ltd.pdx.utopia.driver.UtopiaChaincodeDriver;
import ltd.pdx.utopia.driver.api.Invocation;
import ltd.pdx.utopia.driver.bean.DeployInfo;
import ltd.pdx.utopia.driver.bean.DeployParams;
import ltd.pdx.utopia.driver.bean.NodeSk;
import ltd.pdx.utopia.driver.enums.ChaincodeType;
import ltd.pdx.utopia.driver.exception.BlockchainDriverException;
import ltd.pdx.utopia.driver.util.DriverUtil;
import ltd.pdx.utopia.eckey.aes.AES;
import ltd.pdx.utopia.eckey.crypto.ECKey;
import ltd.pdx.utopia.eckey.exception.EckeyException;
import ltd.pdx.utopia.eckey.keyStore.KeystoreFormat;
import ltd.pdx.utopia.eckey.util.ECKeyUtil;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import pdx.ltd.fileservice.FileServiceProperties;
import pdx.ltd.fileservice.FileUploadResponse;
import pdx.ltd.fileservice.impl.DFSFileStorageService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 合约调用实例
 * <p>
 * {@link #deployCC()} 以java/class文件形式部署一个合约
 * <p>
 * {@link #deploySelective()} 私密合约以java/class文件形式部署一个合约
 * <p>
 * {@link #deployCCAttachJWT()} 通过JWT的方式部署一个合约
 * <p>
 * {@link #deployJarCC()} 以jar包的方式部署一个合约
 * <p>
 * {@link #deploySelectiveJar()} 私密合约以jar文件形式部署一个合约
 * <p>
 * {@link #exec()} 执行{@link MyCc#invoke(ChaincodeStub)}的操作并调用put方法
 * <p>
 * {@link #execSelective()} 私密合约执行{@link MyCc#invoke(ChaincodeStub)}的操作并调用put方法
 * <p>
 * {@link #query()} 执行{@link MyCc#invoke(ChaincodeStub)}的操作并调用query方法
 * <p>
 * {@link #querySelective()} 私密合约执行{@link MyCc#invoke(ChaincodeStub)}的操作并调用query方法
 * <p>
 * {@link #del()} 执行{@link MyCc#invoke(ChaincodeStub)}的操作并调用del方法
 * <p>
 * {@link #delSelective()} 私密合约执行{@link MyCc#invoke(ChaincodeStub)}的操作并调用del方法
 * <p>
 * {@link #execAttachJWT()} 通过jwt授权方式执行{@link MyCc#invoke(ChaincodeStub)}的操作并调用put方法
 */
public class ChaincodeClient {

    private String owner;
    private String name;
    private String version;
    private String ccAddress;
    private String privateKey;
    private UtopiaChaincodeDriver driver;
    private String[] nodePubKeys = {"0312b6ed0443ebd8efc5afc33d0740e0189e292d667f4b323fe4635217889c6984",
            "02ba4c4a0918399c59caf305298b847505a93b80a98d421241bef446f58198698b"};

    @Before
    public void before() {
        // dev
        privateKey = "a2f1a32e5234f64a6624210b871c22909034f24a52166369c2619681390433aa";
        // local
        privateKey = "ffbb4235e03ca50dda2338befcc5b18fa81de3aaa0aba69dadad808e1fa5a21b";
        owner = ECKeyUtil.getAddressByPriKey(privateKey);
        name = "testcc03";
//        name = "testcc";
        version = "1.0.3";
        ccAddress = UtopiaChaincodeDriver.address(owner, name);
        ccAddress = "d6d11b18f2b814dcfcf5b36e39632eaf051a86ae";

        // 非代理模式
        driver = initDriver();
        System.out.println("ccAddress:" + ccAddress);

//        nodePubKeys = new String[]{"02595d553697305c7670dfd92628e5ff68080335265edf804aea4e6e8df5112464"};
        // 代理模式
        //driver = initDriverWithProxy();
    }

    /**
     * 非代理配置-指定真实节点IP
     *
     * @return UtopiaChaincodeDriver
     */
    public UtopiaChaincodeDriver initDriver() {
        driver = new UtopiaChaincodeDriver();
        Properties props = new Properties();
        props.setProperty(Constants.BAAP_SENDER_PRIVKEY, privateKey);
        props.setProperty(Constants.BAAP_BLOCKCHAIN_RPC, "http://10.0.0.114:30040");
//        props.setProperty(Constants.BAAP_BLOCKCHAIN_RPC, "http://10.0.0.206:30114");
//        props.setProperty(Constants.BAAP_BLOCKCHAIN_RPC, "http://10.0.0.207:30165");
//        props.setProperty(Constants.BAAP_BLOCKCHAIN_RPC, "http://192.168.3.117:8546");
        props.setProperty(Constants.BAAP_BLOCKCHAIN_WS_RPC, "ws://127.0.0.1:4321");
        props.setProperty(Constants.BAAP_ENGINE_ID, "1000004");
        driver.init(props);
        return driver;
    }

    /**
     * 代理配置-负载模式
     *
     * @return
     */
    public UtopiaChaincodeDriver initDriverWithProxy() {
        driver = new UtopiaChaincodeDriver();
        Properties props = new Properties();
        props.setProperty(Constants.BAAP_SENDER_PRIVKEY, privateKey);
        //格式：[ ] [protocol]//utopia-chain-[chainId]:[port]
        //protocol: http/ws协议
        //chainId: 代理的链ID
        //port: 8545/8546 8545对应http协议、8546对应ws协议，固定值，主要用来换取目标节点的真实端口
        props.setProperty(Constants.BAAP_BLOCKCHAIN_RPC, "http://utopia-chain-739:8545");
        props.setProperty(Constants.BAAP_BLOCKCHAIN_WS_RPC, "ws://127.0.0.1:4321");
        props.setProperty(Constants.BAAP_ENGINE_ID, "739");
        props.setProperty(Constants.BAAP_PROXY_SERVER, "http://10.0.0.8:9999");
        props.setProperty(Constants.BAAP_X_PDX_PROXY_JWT, "eyJhbGciOiJFUzI1NiJ9.eyJpYXQiOjE1ODMzMDE3NDIsIkZSRUUiOiJUUlVFIn0.pcDDkoQ0a6a2gfZ2uPon0tQZ4kJvb-cQrtgQKhj-9-MSS-Asx8PdQsoSgZNjk_a3ijk9r4VIZsm3OMnIrG7AlQ");
        driver.init(props);
        return driver;
    }

    /**
     * 代理配置-负载模式-节点地址
     *
     * @return
     * @throws BlockchainDriverException
     */
    public UtopiaChaincodeDriver initDriverWithProxyAddress() throws BlockchainDriverException {
        driver = new UtopiaChaincodeDriver();
        Properties props = new Properties();
        props.setProperty(Constants.BAAP_SENDER_PRIVKEY, "d29ce71545474451d8292838d4a0680a8444e6e4c14da018b4a08345fb2bbb84");

        //格式：[ ] [protocol]//utopia-addr-[node-addr]:[port]
        //protocol: http/ws协议
        //node-addr: 具体节点的地址(获取方式iaas控制台)
        //port: 具体节点的http/ws的对应的端口
        props.setProperty(Constants.BAAP_BLOCKCHAIN_RPC, "http://utopia-addr-d15257c65252b7b31b5315f27502724b8ecc4eb2:30038");
        props.setProperty(Constants.BAAP_BLOCKCHAIN_WS_RPC, "ws://127.0.0.1:4321");
        props.setProperty(Constants.BAAP_ENGINE_ID, "739");
        props.setProperty(Constants.BAAP_PROXY_SERVER, "http://10.0.0.8:9999");
        props.setProperty(Constants.BAAP_X_PDX_PROXY_JWT, "eyJhbGciOiJFUzI1NiJ9.eyJpYXQiOjE1ODMzMDE3NDIsIkZSRUUiOiJUUlVFIn0.pcDDkoQ0a6a2gfZ2uPon0tQZ4kJvb-cQrtgQKhj-9-MSS-Asx8PdQsoSgZNjk_a3ijk9r4VIZsm3OMnIrG7AlQ");
        driver.init(props);
        return driver;
    }

    /**
     * 部署Chaincode合约
     *
     * @throws IOException
     * @throws BlockchainDriverException
     */
    @Test
    public void deployCC() throws IOException, BlockchainDriverException {
        File ccFile = new File("/YourWorkSpace/driver/utopia-driver-example/src/main/java/ltd/pdx/utopia/driver/example/Chaincode_Java_Sample.java");
        String txId = driver.deploy(ChaincodeType.JAVA, name, version, ccFile);
        System.out.println(txId);
    }

    /**
     * 选择性部署Chaincode合约
     *
     * @throws IOException
     * @throws BlockchainDriverException
     */
    @Test
    public void deploySelective() throws Exception {
        DeployInfo deployInfo = new DeployInfo();
        deployInfo.setFileName("MyCc.java");
        String fileUrl = uploadFile(new File("/home/pdx/IdeaProjects/driver/utopia-driver-example/src/main/java/ltd/pdx/utopia/driver/example/chaincodedriver/MyCc.java"));
        selectiveExecDeal(deployInfo, fileUrl);
    }

    /**
     * 选择性部署Jar形式的Chaincode合约
     *
     * @throws IOException
     * @throws BlockchainDriverException
     */
    @Test
    public void deploySelectiveJar() throws Exception {
        DeployInfo deployInfo = new DeployInfo();
        deployInfo.setFileName("utopia-driver-example.jar");
        deployInfo.setClassName("ltd.pdx.utopia.driver.example.chaincode.MyCcJar");
        String fileUrl = uploadFile(new File("/home/pdx/IdeaProjects/utopia-driver-example/target/utopia-driver-example.jar"));
        selectiveExecDeal(deployInfo, fileUrl);
    }

    private void selectiveExecDeal(DeployInfo deployInfo, String fileUrl) throws Exception {
        // 用户自定义的加密秘钥
        String password = "4b3f00536097e8e8c17672f4888e26e9f48a2d7c40de30008fc254963cc6a51c";
        String urlToken = fileUrl.split("\\?")[1];
        String link = fileUrl.split("\\?")[0];
        AES aes = new AES();
        byte[] jwt;
        try {
            jwt = aes.encrypt(urlToken.getBytes(), Hex.decode(password));
        } catch (EckeyException e) {
            throw new Exception(e);
        }
        deployInfo.setJwt(jwt);

        System.out.println("fileUrl:" + fileUrl);
        try {
            //需要部署节点的公钥

            List<NodeSk> nodeSks = DriverUtil.getNodeSks(privateKey, password, nodePubKeys);
            System.out.println(nodeSks);
            deployInfo.setNodes(nodeSks);
            deployInfo.setConsensus("all");
            deployInfo.setChaincodeId(name + ":" + version);
            deployInfo.setLink(link);
            deployInfo.setAuthPublickey(ECKeyUtil.getPubkeyFromPriKey(privateKey));
            System.out.println(deployInfo);
            String txId = driver.deploySelective(deployInfo, "");
            System.out.println("txId:" + txId);
        } catch (EckeyException e) {
            throw new Exception(e);
        }
    }

    /**
     * 部署Chaincode合约 jwt联盟链建权
     *
     * @throws IOException
     * @throws BlockchainDriverException
     */
    @Test
    public void deployCCAttachJWT() throws IOException, BlockchainDriverException {
        String jwt = "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJhayI6IjAzOTBkNWQxMDQ4MjMzMDRlYjQ0Mjc2NTQ1Y2U0YjNiYmVkYmEyODE3MTYyOGExMjYyYjBmZjBiNThiNTllM2QyZiIsImwiOjUwMDAwMCwibiI6MSwiciI6ImQiLCJzIjo1NTU1LCJzayI6IjAyNTk1ZDU1MzY5NzMwNWM3NjcwZGZkOTI2MjhlNWZmNjgwODAzMzUyNjVlZGY4MDRhZWE0ZTZlOGRmNTExMjQ2NCJ9.agZbI5oK4OqJbkZoTslXjm8L-HB7kZ_NB2DRwZf3-r9I6QAGFV0ci2Og9pUpjjjryf4_jbrztwuhQNgIT5cxDA";
        String txId = driver.deploy(ChaincodeType.JAVA, name, version, new File("/Users/yzk/IdeaProjects/driver/utopia-driver-example/src/main/java/ltd/pdx/utopia/driver/example/MyCc.java"), jwt);
        System.out.println(txId);
    }

    @Test
    public void deployJarCC() throws Exception {
        // jar包上传服务器

        String filePath = "/home/pdx/IdeaProjects/utopia-driver-example/target/utopia-driver-example.jar";
        File file = new File(filePath);
        String fileUrl = uploadFile(file);

        Map<DeployParams, String> params = new HashMap<>();
        params.put(DeployParams.FILE_URL, fileUrl);
        params.put(DeployParams.CLASS_NAME, "ltd.pdx.utopia.driver.example.chaincode.MyCcJar");

        String txId = driver.deployJar(name, version, file.getName(), params);
        System.out.println(txId);
    }


    private static FileServiceProperties properties = new FileServiceProperties();
    private static DFSFileStorageService fileStorageService = new DFSFileStorageService();

    private String uploadFile(File file) throws FileNotFoundException {
        String fileUrl = "";

        final FileUploadResponse fileUploadResponse;
        FileInputStream fileInputStream = new FileInputStream(file);

        FileServiceProperties.FastDFSProperties ossProperties = new FileServiceProperties.FastDFSProperties();
        ossProperties.setTrackerServer("39.98.200.189:22122");
        ossProperties.setAccessUrl("http://39.98.200.189:8888");
        ossProperties.setAntiStealToken(true);
        ossProperties.setSecretKey("QJzfnT4rIeuoaSGE");

        properties.setFastDFS(ossProperties);
        fileStorageService.init(properties);
        try {
            fileUploadResponse = fileStorageService.uploadFile(file.getName(), fileInputStream);
            fileUrl = fileUploadResponse.getUrl();
            fileUrl = fileStorageService.generatePresignedUrl(fileUrl, new Date());
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return fileUrl;
    }

    @Test
    public void testUploadFile() throws FileNotFoundException {
        String s = uploadFile(new File("/home/pdx/邀请码.txt"));
        System.out.println(s);
    }

    /**
     * 写入key/value
     *
     * @throws BlockchainDriverException
     */
    @Test
    public void execSelective() throws BlockchainDriverException, EckeyException {
        // 用户自定义的加密秘钥
        String secretKey = "4b3f00536097e8e8c17672f4888e26e9f48a2d7c40de30008fc254963cc6a51c";

        List<byte[]> args = new ArrayList<>();
        args.add("pdxy".getBytes());
        args.add("h100".getBytes());

        Invocation tx = Invocation.builder()
                .fcn("put")
                .args(args)
                .build();

        String txId = driver.execSelective(ccAddress, tx, secretKey, nodePubKeys);
        System.out.println("txId:" + txId);
    }

    @Test
    public void batchExecSelective() throws BlockchainDriverException, EckeyException {
        // 用户自定义的加密秘钥
        String secretKey = "4b3f00536097e8e8c17672f4888e26e9f48a2d7c40de30008fc254963cc6a51c";

        AtomicLong nonceStr = new AtomicLong(3);
        int i = 0;
        while (i < 19) {
            List<byte[]> args = new ArrayList<>();
            String key = "pdx" + i;
            String value = "h" + i;
            args.add(key.getBytes());
            args.add(value.getBytes());

            Invocation tx = Invocation.builder()
                    .fcn("put")
                    .args(args)
                    .build();

            String txId = driver.execSelective(ccAddress, tx, secretKey, nodePubKeys, nonceStr.getAndIncrement());
            System.out.println("txId:" + txId + "--nonce:" + nonceStr.get());
            i++;
        }

    }

    /**
     * 写入key/value
     *
     * @throws BlockchainDriverException
     */
    @Test
    public void exec() throws BlockchainDriverException {
        List<byte[]> args = new ArrayList<>();
        args.add("pdx".getBytes());
        args.add("hello11".getBytes());

        Invocation tx = Invocation.builder()
                .fcn("put")
                .args(args)
                .build();
        String txId = driver.exec(ccAddress, tx);
        System.out.println(txId);
    }


    @Test
    public void query() throws BlockchainDriverException {
        byte[] result = driver.state(ccAddress, "get", "pdx");
        System.out.println(new String(result));
    }

    /**
     * 通过key获取value
     *
     * @throws BlockchainDriverException
     */
    @Test
    public void querySelective() throws BlockchainDriverException, EckeyException {
        String key = "pdxy";
        byte[] result = driver.stateSelective(ccAddress, "get",key);
        System.out.println("key:" + key + "--result:" + new String(result));
    }
    @Test
    public void batchQuerySelective() throws BlockchainDriverException, EckeyException {
        int i = 0;
        while (i < 100) {
            String key = "pdx" + i;
            byte[] result = driver.stateSelective(ccAddress, "get",key);
            System.out.println("key:" + key + "--result:" + new String(result));
            i++;
        }

    }

    @Test
    public void del() throws BlockchainDriverException {
        Invocation invocation = Invocation.builder()
                .fcn("del").args(new ArrayList<byte[]>() {{
                    add("pdx".getBytes());
                }}).build();

        String txId = driver.exec(ccAddress, invocation);
        System.out.println(txId);
    }

    @Test
    public void delSelective() throws BlockchainDriverException, EckeyException {
        String secretKey = "4b3f00536097e8e8c17672f4888e26e9f48a2d7c40de30008fc254963cc6a51c";
        Invocation invocation = Invocation.builder()
                .fcn("del").args(new ArrayList<byte[]>() {{
                    add("pdxy".getBytes());
                }}).build();

        String txId = driver.execSelective(ccAddress, invocation, secretKey, nodePubKeys);
        System.out.println("txId:" + txId);
    }

    /**
     * 写入key/value jwt联盟链建权
     *
     * @throws BlockchainDriverException
     */
    @Test
    public void execAttachJWT() throws BlockchainDriverException {
        List<byte[]> args = new ArrayList<>();
        args.add("pdx".getBytes());
        args.add("hello11".getBytes());

        Map<String, byte[]> meta = new HashMap<>();
        meta.put("jwt", "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJhayI6IjAzOTBkNWQxMDQ4MjMzMDRlYjQ0Mjc2NTQ1Y2U0YjNiYmVkYmEyODE3MTYyOGExMjYyYjBmZjBiNThiNTllM2QyZiIsImwiOjUwMDAwMCwibiI6MSwiciI6ImQiLCJzIjo1NTU1LCJzayI6IjAyNTk1ZDU1MzY5NzMwNWM3NjcwZGZkOTI2MjhlNWZmNjgwODAzMzUyNjVlZGY4MDRhZWE0ZTZlOGRmNTExMjQ2NCJ9.agZbI5oK4OqJbkZoTslXjm8L-HB7kZ_NB2DRwZf3-r9I6QAGFV0ci2Og9pUpjjjryf4_jbrztwuhQNgIT5cxDA".getBytes());

        Invocation tx = Invocation.builder()
                .fcn("put").args(args).meta(meta).build();
        String txId = driver.exec(ccAddress, tx);
        System.out.println(txId);
    }

    /**
     * 通过key删除对应的数据
     *
     * @throws BlockchainDriverException
     */
    @Test
    public void delAttachJWT() throws BlockchainDriverException {
        Map<String, byte[]> meta = new HashMap<>();
        meta.put("jwt", "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJhayI6IjAzOTBkNWQxMDQ4MjMzMDRlYjQ0Mjc2NTQ1Y2U0YjNiYmVkYmEyODE3MTYyOGExMjYyYjBmZjBiNThiNTllM2QyZiIsImwiOjUwMDAwMCwibiI6MSwiciI6ImQiLCJzIjo1NTU1LCJzayI6IjAyNTk1ZDU1MzY5NzMwNWM3NjcwZGZkOTI2MjhlNWZmNjgwODAzMzUyNjVlZGY4MDRhZWE0ZTZlOGRmNTExMjQ2NCJ9.agZbI5oK4OqJbkZoTslXjm8L-HB7kZ_NB2DRwZf3-r9I6QAGFV0ci2Og9pUpjjjryf4_jbrztwuhQNgIT5cxDA".getBytes());
        Invocation invocation = Invocation.builder()
                .fcn("del").args(new ArrayList<byte[]>() {{
                    add("pdx".getBytes());
                }}).meta(meta).build();

        String txId = driver.exec(ccAddress, invocation);
        System.out.println(txId);
    }

    /**
     * 通过key获取范围内的数据
     *
     * @throws BlockchainDriverException
     */
    @Test
    public void getRange() throws BlockchainDriverException {
        String keyStart = "pdx1";
        String keyEnd = "pdx4";
        byte[] result = driver.state(ccAddress, "getRange", keyStart, keyEnd);
        System.out.println(new String(result));
    }

    /**
     * 获取历史数据
     *
     * @throws BlockchainDriverException
     */
    @Test
    public void getHis() throws BlockchainDriverException {
        String key = "pdx";
        byte[] result = driver.state(ccAddress, "getHis", key);
        System.out.println(new String(result));
    }

    /**
     * 获取公钥、私钥和地址
     */
    @Test
    public void testKey() throws Exception {
        String privateKey = ECKeyUtil.getPrivateKey();
        privateKey = "a3ee9075d30ab550c8fa7972a09b0c5e7973e613c4f5a573e4b19808143965f6";
        System.out.println("423d35dc609e2eb9aa96435f663f2e49f862ff1fba241ad6d15a8840224e8311");
        String pubkey = ECKeyUtil.getPubkeyFromPriKey(privateKey);
        System.out.println(pubkey);
        String address = ECKeyUtil.getAddressFromPubkey(pubkey);
        System.out.println(address);

        KeystoreFormat decryptFormat = new KeystoreFormat();
        String keystore = "{\"address\":\"8000d109daef5c81799bc01d4d82b0589deedb33\",\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"6265372dabfb8c22fb69fdd4457d89bd422b8eb98376b975ecdce5770b636f34\",\"cipherparams\":{\"iv\":\"f834cd1a01f10beebce14730a9ba091b\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"984f74fc175a3e49864f774b002a7759ecc1afc3f587cca42237d6339660197d\"},\"mac\":\"02ca5e5d4ff9541940b93255a040ae979a050b9b66e7a6dcfa454658da7fcbce\"},\"id\":\"b0672270-1854-4401-a898-11cdda4d4e2d\",\"version\":3}";
        String password = "11111";
        ECKey key = decryptFormat.fromKeystore(keystore, password);
        privateKey = Hex.toHexString(key.getPrivKeyBytes());
        System.out.println("privateKey:" + privateKey);
        String publicKey = Hex.toHexString(key.getPubKeyPoint().getEncoded(true));
        address = Hex.toHexString(key.getAddress());
        System.out.println("publicKey:" + publicKey);
        System.out.println("address:" + address);
    }
}
