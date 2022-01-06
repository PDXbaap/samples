package ltd.pdx.utopia.client.solidity;

import javafx.util.Pair;
import ltd.pdx.utopia.common.Constants;
import ltd.pdx.utopia.driver.UtopiaSolidityDriver;

import ltd.pdx.utopia.driver.core.CallTransaction;
import ltd.pdx.utopia.driver.exception.BlockchainDriverException;
import ltd.pdx.utopia.driver.util.DriverUtil;
import ltd.pdx.utopia.eckey.util.ECKeyUtil;
import ltd.pdx.utopia.eckey.util.EncryptUtil;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;

import java.util.*;

/**
 * solidity合约操作实例
 * <p>
 * {@link #deploySol()}  部署solidity合约
 * <p>
 * {@link #deploySolAttachJWT()} JWT授权方式部署solidity合约
 * <p>
 * {@link #transfer()} 主链转账交易，非合约转账
 * <p>
 * {@link #balance()} 主链账余额查询
 * <p>
 * {@link #transferToken()} ERC20token的转账
 * <p>
 * {@link #balanceToken()} ERC20账余额查询
 */
public class SolidityClient {

    private UtopiaSolidityDriver driver;
    private String privateKey;
    private String address;

    @Before
    public void before() {

        privateKey = "1964cacf69a9dc3274f19ec94b95e12e76ee23cbee3546d45d2ccc2042f6ea94";
        address = ECKeyUtil.getAddressByPriKey(privateKey);

        // 非代理模式
        driver = initDriver();

        // 代理模式
        //driver = initDriverWithProxy();

    }

    /**
     * 非代理配置-指定真实节点IP
     *
     * @return
     */
    public UtopiaSolidityDriver initDriver() {
        UtopiaSolidityDriver driver = new UtopiaSolidityDriver();
        Properties props = new Properties();
        props.setProperty(Constants.BAAP_SENDER_PRIVKEY, privateKey);
        props.setProperty(Constants.BAAP_BLOCKCHAIN_RPC, "http://192.168.3.45:8545");
        props.setProperty(Constants.BAAP_ENGINE_ID, "777");
        props.setProperty(Constants.BAAP_BLOCKCHAIN_GAS_PRICE, "45000000001");
        props.setProperty(Constants.BAAP_BLOCKCHAIN_GAS_LIMIT, "2044109");
        driver.init(props);
        return driver;
    }

    /**
     * 代理配置-负载模式
     *
     * @return
     */
    public UtopiaSolidityDriver initDriverWithProxy() {
        UtopiaSolidityDriver driver = new UtopiaSolidityDriver();
        Properties props = new Properties();
        props.setProperty(Constants.BAAP_SENDER_PRIVKEY, "d29ce71545474451d8292838d4a0680a8444e6e4c14da018b4a08345fb2bbb84");
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
     * deploySol
     *
     * @throws BlockchainDriverException
     */
    @Test
    public void deploySol() throws BlockchainDriverException {
        // Solidity合约的ByteCode
        byte[] payload = Hex.decode("608060405234801561001057600080fd5b50336000806101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff16021790555060405180608001604052806008815260200160018152602001600181526020016000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815250600260008201518160000155602082015181600101556040820151816002015560608201518160030160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055509050506111b9806101256000396000f3fe6080604052600436106100555760003560e01c806312b583491461005a5780631eaf76de14610085578063262c643b1461011c5780632a1beec8146101885780636084b0ce146101c35780638ea0bcea14610232575b600080fd5b34801561006657600080fd5b5061006f6102f4565b6040518082815260200191505060405180910390f35b34801561009157600080fd5b506100be600480360360208110156100a857600080fd5b810190808035906020019092919050505061031e565b6040518083815260200180602001828103825283818151815260200191508051906020019060200280838360005b838110156101075780820151818401526020810190506100ec565b50505050905001935050505060405180910390f35b34801561012857600080fd5b50610131610459565b604051808581526020018481526020018381526020018273ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200194505050505060405180910390f35b34801561019457600080fd5b506101c1600480360360208110156101ab57600080fd5b81019080803590602001909291905050506104a4565b005b3480156101cf57600080fd5b50610230600480360360808110156101e657600080fd5b81019080803590602001909291908035906020019092919080359060200190929190803573ffffffffffffffffffffffffffffffffffffffff169060200190929190505050610ab2565b005b6102f26004803603604081101561024857600080fd5b81019080803590602001909291908035906020019064010000000081111561026f57600080fd5b82018360208201111561028157600080fd5b803590602001918460208302840111640100000000831117156102a357600080fd5b919080806020026020016040519081016040528093929190818152602001838360200280828437600081840152601f19601f820116905080830192505050505050509192919290505050610c47565b005b60008030905060008173ffffffffffffffffffffffffffffffffffffffff16319050809250505090565b6000606061032a61103a565b600160008581526020019081526020016000206040518060600160405290816000820160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020016001820180548060200260200160405190810160405280929190818152602001828054801561042357602002820191906000526020600020905b8160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190600101908083116103d9575b50505050508152602001600282015481525050905080604001519250806020015191508281602001518090509250925050915091565b6000806000806002600001546002600101546002800154600260030160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16935093509350935090919293565b6000600160008381526020019081526020016000206002015411610530576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260078152602001807f4e6f207461736b0000000000000000000000000000000000000000000000000081525060200191505060405180910390fd5b3373ffffffffffffffffffffffffffffffffffffffff166001600083815260200190815260200160002060000160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614610607576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252601d8152602001807f596f7520617265206e6f7420746865207061796572206f66207468697300000081525060200191505060405180910390fd5b6000610641600160008481526020019081526020016000206002015460026001015460028001546002600101546002600001540101610f06565b90506000600260030160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1690508073ffffffffffffffffffffffffffffffffffffffff166108fc839081150290604051600060405180830381858888f193505050501580156106b3573d6000803e3d6000fd5b50600080600180600087815260200190815260200160002060010180549050111561089557610710600160008781526020019081526020016000206002015460026000015460028001546002600101546002600001540101610f06565b91506000848360016000898152602001908152602001600020600201540303905061075881600180600160008b81526020019081526020016000206001018054905003610f06565b915060008090505b60016000888152602001908152602001600020600101805490508163ffffffff16101561088e576000600160008981526020019081526020016000206001018263ffffffff16815481106107b057fe5b9060005260206000200160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff16905060008263ffffffff161415610838578073ffffffffffffffffffffffffffffffffffffffff166108fc869081150290604051600060405180830381858888f19350505050158015610832573d6000803e3d6000fd5b50610880565b8073ffffffffffffffffffffffffffffffffffffffff166108fc859081150290604051600060405180830381858888f1935050505015801561087e573d6000803e3d6000fd5b505b508080600101915050610760565b5050610967565b6108c7600160008781526020019081526020016000206002015460026000015460026001015460026000015401610f06565b91506000600160008781526020019081526020016000206001016000815481106108ed57fe5b9060005260206000200160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1690508073ffffffffffffffffffffffffffffffffffffffff166108fc849081150290604051600060405180830381858888f19350505050158015610960573d6000803e3d6000fd5b5060009150505b60016000868152602001908152602001600020600080820160006101000a81549073ffffffffffffffffffffffffffffffffffffffff02191690556001820160006109b29190611071565b600282016000905550503373ffffffffffffffffffffffffffffffffffffffff167fde0c80e80bb520f986d8ce5c11e2c618438b4ff19fb3f0d105aff9c88b21bb0f8660016000898152602001908152602001600020600101858560405180858152602001806020018481526020018381526020018281038252858181548152602001915080548015610a9a57602002820191906000526020600020905b8160009054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019060010190808311610a50575b50509550505050505060405180910390a25050505050565b3373ffffffffffffffffffffffffffffffffffffffff166000809054906101000a900473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1614610b74576040517f08c379a000000000000000000000000000000000000000000000000000000000815260040180806020018281038252601d8152602001807f596f7520617265206e6f7420746865206f776e6572206f66207468697300000081525060200191505060405180910390fd5b60405180608001604052808581526020018381526020018481526020018273ffffffffffffffffffffffffffffffffffffffff16815250600260008201518160000155602082015181600101556040820151816002015560608201518160030160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055509050507f5400d48d40ab53228b938d99e5e297c4522918c514d4d05f7c75dfb121f78de260405160405180910390a150505050565b6000815111610cbe576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260168152602001807f746172676574732e6c656e677468206973207a65726f0000000000000000000081525060200191505060405180910390fd5b60003411610d34576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260118152602001807f7061792076616c7565206973207a65726f00000000000000000000000000000081525060200191505060405180910390fd5b6000602060ff1611610dae576040517f08c379a00000000000000000000000000000000000000000000000000000000081526004018080602001828103825260098152602001807f4e6f207461736b4964000000000000000000000000000000000000000000000081525060200191505060405180910390fd5b60405180606001604052803373ffffffffffffffffffffffffffffffffffffffff168152602001828152602001348152506001600084815260200190815260200160002060008201518160000160006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff1602179055506020820151816001019080519060200190610e55929190611092565b50604082015181600201559050503373ffffffffffffffffffffffffffffffffffffffff167f3c71680647bebb33a1d4f5e18df0e617be565adc171b89b529234887b6c3d1068383346040518084815260200180602001838152602001828103825284818151815260200191508051906020019060200280838360005b83811015610eed578082015181840152602081019050610ed2565b5050505090500194505050505060405180910390a25050565b6000806000610f158686610fe7565b91509150838110610f2557600080fd5b60008480610f2f57fe5b868809905082811115610f43576001820391505b808303925060008560000386169050808681610f5b57fe5b049550808481610f6757fe5b0493506001818260000381610f7857fe5b04018302840193506000600190508087026002038102905080870260020381029050808702600203810290508087026002038102905080870260020381029050808702600203810290508087026002038102905080870260020381029050808502955050505050509392505050565b60008060007fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8061101457fe5b84860990508385029250828103915082811015611032576001820391505b509250929050565b6040518060600160405280600073ffffffffffffffffffffffffffffffffffffffff16815260200160608152602001600081525090565b508054600082559060005260206000209081019061108f919061111c565b50565b82805482825590600052602060002090810192821561110b579160200282015b8281111561110a5782518260006101000a81548173ffffffffffffffffffffffffffffffffffffffff021916908373ffffffffffffffffffffffffffffffffffffffff160217905550916020019190600101906110b2565b5b5090506111189190611141565b5090565b61113e91905b8082111561113a576000816000905550600101611122565b5090565b90565b61118191905b8082111561117d57600081816101000a81549073ffffffffffffffffffffffffffffffffffffffff021916905550600101611147565b5090565b9056fea265627a7a72315820a345359b0467832c64772d903a7b2476e6994f92e7d3bd39acb9b44e8ba807e564736f6c63430005110032");
        String txId = driver.deploy(payload);
        System.out.println("txId:" + txId);
    }

    /**
     * deploySolAttachJWT
     *
     * @throws BlockchainDriverException
     */
    @Test
    public void deploySolAttachJWT() throws BlockchainDriverException {
        Map<String, byte[]> meta = new HashMap<>();
        meta.put("jwt", "eyJhbGciOiJFUzI1NiIsInR5cCI6IkpXVCJ9.eyJhayI6IjAyOTc3NjRjMzMwM2EwYmJiMjEwOGNjMDlhY2I2MzJiMWNjMjcyMDk0OTdiMjgwM2Y1ZTgwMjFkMTIwMzAzMGIyYSIsImwiOjYwMDAwMDAwMCwibiI6ImVlZmZmZWZyZXJlZGZmZHN1dWYycnJmZHNtZmxqbGpyciIsInIiOiJkIiwicyI6MTIzNDc4LCJzayI6IjAyNTk1ZDU1MzY5NzMwNWM3NjcwZGZkOTI2MjhlNWZmNjgwODAzMzUyNjVlZGY4MDRhZWE0ZTZlOGRmNTExMjQ2NCJ9.PP6-9tCUeXLNKWhJTwKsmycsnkO6qH5dkQYZIHiUhVW75zxLfu_t9l8Yed_seWdEzjCguB-7sub3sKKbIENb-w".getBytes());
        meta.put("name", "youmi".getBytes());
        meta.put("version", "v1.0.0".getBytes());
        meta.put("desc", "umi".getBytes());

        long nonce = Long.parseLong(getNonce(), 16);
        byte[] payload = Hex.decode("6060604052341561000f57600080fd5b60405160208061130e83398101604052808051906020019091905050601260ff16600a0a633b9aca0002600181905550601260ff16600a0a633b9aca00026000808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055508073ffffffffffffffffffffffffffffffffffffffff1660007fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef601260ff16600a0a633b9aca00026040518082815260200191505060405180910390a350611212806100fc6000396000f3006060604052600436106100ba576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff16806306fdde03146100bf578063095ea7b31461014d57806318160ddd146101a757806323b872dd146101d05780632ff2e9dc14610249578063313ce5671461027257806366188463146102a157806370a08231146102fb57806395d89b4114610348578063a9059cbb146103d6578063d73dd62314610430578063dd62ed3e1461048a575b600080fd5b34156100ca57600080fd5b6100d26104f6565b6040518080602001828103825283818151815260200191508051906020019080838360005b838110156101125780820151818401526020810190506100f7565b50505050905090810190601f16801561013f5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b341561015857600080fd5b61018d600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190803590602001909190505061052f565b604051808215151515815260200191505060405180910390f35b34156101b257600080fd5b6101ba610621565b6040518082815260200191505060405180910390f35b34156101db57600080fd5b61022f600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190803573ffffffffffffffffffffffffffffffffffffffff1690602001909190803590602001909190505061062b565b604051808215151515815260200191505060405180910390f35b341561025457600080fd5b61025c6109e5565b6040518082815260200191505060405180910390f35b341561027d57600080fd5b6102856109f6565b604051808260ff1660ff16815260200191505060405180910390f35b34156102ac57600080fd5b6102e1600480803573ffffffffffffffffffffffffffffffffffffffff169060200190919080359060200190919050506109fb565b604051808215151515815260200191505060405180910390f35b341561030657600080fd5b610332600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050610c8c565b6040518082815260200191505060405180910390f35b341561035357600080fd5b61035b610cd4565b6040518080602001828103825283818151815260200191508051906020019080838360005b8381101561039b578082015181840152602081019050610380565b50505050905090810190601f1680156103c85780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34156103e157600080fd5b610416600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091908035906020019091905050610d0d565b604051808215151515815260200191505060405180910390f35b341561043b57600080fd5b610470600480803573ffffffffffffffffffffffffffffffffffffffff16906020019091908035906020019091905050610f2c565b604051808215151515815260200191505060405180910390f35b341561049557600080fd5b6104e0600480803573ffffffffffffffffffffffffffffffffffffffff1690602001909190803573ffffffffffffffffffffffffffffffffffffffff16906020019091905050611128565b6040518082815260200191505060405180910390f35b6040805190810160405280600581526020017f594f554d4900000000000000000000000000000000000000000000000000000081525081565b600081600260003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055508273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925846040518082815260200191505060405180910390a36001905092915050565b6000600154905090565b60008073ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff161415151561066857600080fd5b6000808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020019081526020016000205482111515156106b557600080fd5b600260008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054821115151561074057600080fd5b610791826000808773ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020546111af90919063ffffffff16565b6000808673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002081905550610824826000808673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020546111c890919063ffffffff16565b6000808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055506108f582600260008773ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020546111af90919063ffffffff16565b600260008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055508273ffffffffffffffffffffffffffffffffffffffff168473ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef846040518082815260200191505060405180910390a3600190509392505050565b601260ff16600a0a633b9aca000281565b601281565b600080600260003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054905080831115610b0c576000600260003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002081905550610ba0565b610b1f83826111af90919063ffffffff16565b600260003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055505b8373ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925600260003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008873ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020546040518082815260200191505060405180910390a3600191505092915050565b60008060008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020549050919050565b6040805190810160405280600381526020017f554d49000000000000000000000000000000000000000000000000000000000081525081565b60008073ffffffffffffffffffffffffffffffffffffffff168373ffffffffffffffffffffffffffffffffffffffff1614151515610d4a57600080fd5b6000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020548211151515610d9757600080fd5b610de8826000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020546111af90919063ffffffff16565b6000803373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002081905550610e7b826000808673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020546111c890919063ffffffff16565b6000808573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055508273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167fddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef846040518082815260200191505060405180910390a36001905092915050565b6000610fbd82600260003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008673ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020546111c890919063ffffffff16565b600260003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008573ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020819055508273ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff167f8c5be1e5ebec7d5bd14f71427d1e84f3dd0314c0f7b2291e5b200ac8c7c3b925600260003373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008773ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff168152602001908152602001600020546040518082815260200191505060405180910390a36001905092915050565b6000600260008473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002060008373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200190815260200160002054905092915050565b60008282111515156111bd57fe5b818303905092915050565b60008082840190508381101515156111dc57fe5b80915050929150505600a165627a7a7230582038d642f675c3544154329d8117031673341831e48f20ff9881ad67ee8d6160130029000000000000000000000000a768c134e9342d0811c555466d04ff0809b07c97");

        String txId = driver.exec(null, payload, meta, nonce, Constants.BAAP_DEFAULT_GAS_PRICE, Constants.BAAP_DEFAULT_GAS_LIMIT, 0);
        System.out.println(txId);
    }

    /**
     * @throws Exception
     */
    @Test
    public void transfer() throws Exception {
        //targer addr(note: no '0x')
        String to = "0xf69360a50b10f1980a859e28cd3d4392956bf921";

        Long nonce = Long.parseLong(getNonce(), 16);
        Long gasprice = Long.parseLong("04a817c800", 16);
        Long gaslimit = Long.parseLong("47e7c4", 16);
        Long value = Long.parseLong("9000000000000000000", 10);

        String txId = driver.exec(to, null, nonce, gasprice, gaslimit, value);

        System.out.println("|||" + txId);
    }

    @Test
    public void balance() throws Exception {
        String from = driver.getProperties().getProperty(Constants.BAAP_SENDER_PRIVKEY);
        String address = ECKeyUtil.getAddressByPriKey(from);

        //use rpc interface ： eth_getBalance
        String result = driver.rpcCall(1, "eth_getBalance", "0x" + address, "latest");
        System.out.println(result);
    }

    @Test
    public void transferToken() throws Exception {
        //ERC20 Token （transfer）
        String transfer = "{\"constant\": false,\"inputs\": [{\"name\": \"_to\",\"type\": \"address\"}, {\"name\": \"_value\",\"type\": \"uint256\"}],\"name\": \"transfer\",\"outputs\": [{\"name\": \"\",\"type\": \"bool\"}],\"payable\": false,\"stateMutability\": \"nonpayable\",\"type\": \"function\"}";

        //contract addr
        String contract = "4ed79bef59580bbae789002324fdc24de81be62f";
        //target addr
        String to = "aa38e566b4b023c1ad9ccec527cb4bb0b97b7bb4";

        Long nonce = Long.parseLong(getNonce(), 16);
        Long gasprice = Long.parseLong("05a817c800", 16);
        Long gaslimit = Long.parseLong("47e7c4", 16);
        Long value = Long.parseLong("0de0b6b3a7640000", 16);

        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(transfer);
        byte[] callData = function.encode(Hex.decode(to), value);

        String txId = driver.exec(contract, callData, nonce, gasprice, gaslimit, 0);

        System.out.println(txId);
    }


    @Test
    public void set() throws BlockchainDriverException {
        String abiStr = "{\"constant\": false,\"inputs\": [{\"internalType\": \"bytes\",\"name\": \"key\",\"type\": \"bytes\"}, {\"internalType\": \"bytes\",\"name\": \"value\",\"type\": \"bytes\"}],\"name\": \"put\",\"outputs\": [],\"payable\": false,\"stateMutability\": \"nonpayable\",\"type\": \"function\"}";
        String contract = "0x8C56F7029629fd965D77A625557Ff9e6EF4b3110";
        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(abiStr);
        byte[] callData = function.encode("key", "value");
        Long nonce = null;
        try {
            nonce = Long.parseLong(getNonce(), 16);
        } catch (BlockchainDriverException e) {
            e.printStackTrace();
        }
        Long gasprice = Long.parseLong("05a817c800", 16);
        Long gaslimit = Long.parseLong("47e7c4", 16);
        Long value = Long.parseLong("0de0b6b3a7640000", 16);
        String txId = driver.exec(contract, callData, nonce, gasprice, gaslimit, value);
        System.out.println(txId);
    }


    @Test
    public void deposit() throws BlockchainDriverException {
        String abiStr = "{\"constant\": false,\"inputs\": [{\"internalType\": \"bytes\",\"name\": \"taskId\",\"type\": \"bytes\"},{\"internalType\": \"address[]\",\"name\": \"targets\",\"type\": \"address[]\"}],\"name\": \"deposit\",\"outputs\": [],\"payable\": true,\"stateMutability\": \"payable\",\"type\": \"function\"}";
        String contract = "0xcb912c7a2bb04944c15ac53bd2b8b5ab323f96cb";
        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(abiStr);


        List params = new ArrayList() {
            {
                add("0x58dfe602278d3f82ebce7355624279b8a5d4c14a");
                add("0xeda3cceff74dcdb14a04a995d51e7fa06e807a1a");
            }
        };
        byte[] callData = function.encode("hzzzz", params);
        Long nonce = null;
        try {
            nonce = Long.parseLong(getNonce(), 16);
        } catch (BlockchainDriverException e) {
            e.printStackTrace();
        }
        Long gasprice = Long.parseLong("05a817c800", 16);
        Long gaslimit = Long.parseLong("47e7c4", 16);
        Long value = Long.parseLong("0de0b6b3a7640000", 16);


        String txId = driver.exec(contract, callData, nonce, gasprice, gaslimit, value);

        System.out.println(txId);

    }


    @Test
    public void queryPayInfo() throws Exception {
        String balanceOf = "{\"constant\": true,\"inputs\": [{\"internalType\": \"bytes\",\"name\": \"taskId\",\"type\": \"bytes\"}],\"name\": \"queryPayInfo\",\"outputs\": [{\"internalType\": \"uint256\",\"name\": \"totalAmount\",\"type\": \"uint256\"},{\"internalType\": \"address[]\",\"name\": \"targets\",\"type\": \"address[]\"}],\"payable\": false,\"stateMutability\": \"view\",\"type\": \"function\"}";
        String contract = "0x8C56F7029629fd965D77A625557Ff9e6EF4b3110"; //contract addr

        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(balanceOf);
        byte[] callData = function.encode("hzzzz"); //get call data

        Map<String, Object> params = new HashMap<>();
        params.put("to", contract);
        params.put("data", "0x" + Hex.toHexString(callData));

        //use rpc
        String result = driver.rpcCall(1, "eth_call", params, "latest");
        System.out.println(result);
        String ret = DriverUtil.parseResult(result).get("result").substring(1);
        Object[] obj = function.decode(ret.getBytes());
        System.out.println(obj);

//        ObjectMapper objectMapper = new ObjectMapper();
//        MapType javaType = objectMapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
//        Map<String, Object> ret = objectMapper.readValue(result, javaType);
//        List<TypeReference<?>> outputParameters = new ArrayList<TypeReference<?>>() {{
//            add(new TypeReference<Uint256>(true) {
//            });
//            add(new TypeReference<DynamicArray<Address>>(true) {
//            });
//        }};
//        List decodeData = decodeMsg((String) ret.get("result"), outputParameters);
//        System.out.println("data:" + decodeData);

    }

    @Test
    public void query() throws Exception {
        String abiStr = "{\"constant\": true,\"inputs\": [{\"internalType\": \"bytes\",\"name\": \"key\",\"type\": \"bytes\"}],\"name\": \"get\",\"outputs\": [{\"internalType\": \"bytes\",\"name\": \"value\",\"type\": \"bytes\"}],\"payable\": false,\"stateMutability\": \"view\",\"type\": \"function\"}";
        String contractAddr = "0x8C56F7029629fd965D77A625557Ff9e6EF4b3110"; // contract addr

        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(abiStr);
        byte[] callData = function.encode("key"); //get call data

        Map<String, Object> params = new HashMap<>();
        params.put("to", contractAddr);
        params.put("data", "0x" + Hex.toHexString(callData));
        // use rpc
        String result = driver.rpcCall(1, "eth_call", params, "latest");
        System.out.println(result);
    }

    @Test
    public void getProportion() throws BlockchainDriverException {
        String abiStr = "{\"constant\": true,\"inputs\": [],\"name\": \"getProportion\",\"outputs\": [{\"internalType\": \"uint256\",\"name\": \"\",\"type\": \"uint256\"},{\"internalType\": \"uint256\",\"name\": \"\",\"type\": \"uint256\"},{\"internalType\": \"uint256\",\"name\": \"\",\"type\": \"uint256\"},{\"internalType\": \"address\",\"name\": \"\",\"type\": \"address\"}],\"payable\": false,\"stateMutability\": \"view\",\"type\": \"function\"}";
        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(abiStr);
        byte[] callData = function.encode();
        Map<String, Object> params = new HashMap<>();
        params.put("to", "0x8C56F7029629fd965D77A625557Ff9e6EF4b3110");
        params.put("data", "0x" + Hex.toHexString(callData));
        String result = driver.rpcCall(1, "eth_call", params, "latest");
        String ret = DriverUtil.parseResult(result).get("result").substring(2);

        List<TypeReference<?>> outputParameters = new ArrayList<TypeReference<?>>() {{
            add(new TypeReference<Uint256>(true) {
            });
            add(new TypeReference<Uint256>(true) {
            });
            add(new TypeReference<Uint256>(true) {
            });
            add(new TypeReference<Address>() {
            });
        }};
        List obj1 = decodeMsg(ret, outputParameters);
        for (int i = 0; i < 4; i++) {
            Object o = obj1.get(i);

            if (i == 3) {
                System.out.println(o);
            } else {

                System.out.println(o);
            }
        }

        Object[] obj = function.decodeResult(ret.getBytes());

        for (int i = 0; i < 4; i++) {
            Object o = obj[i];
            if (i == 3) {
                System.out.println(new String((byte[]) o));
            } else {

                System.out.println(String.valueOf(o));
            }
        }


    }

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

    @Test
    public void getTotalBalance() throws BlockchainDriverException {
        String balanceOf = "{\"constant\": true,\"inputs\": [],\"name\": \"getTotalBalance\",\"outputs\": [{\"internalType\": \"uint256\",\"name\": \"\",\"type\": \"uint256\"}],\"payable\": false,\"stateMutability\": \"view\",\"type\": \"function\"},";
        String contract = "0x8C56F7029629fd965D77A625557Ff9e6EF4b3110"; //contract addr

        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(balanceOf);
        byte[] callData = function.encode(); //get call data

        Map<String, Object> params = new HashMap<>();
        params.put("to", contract);
        params.put("data", "0x" + Hex.toHexString(callData));

        //use rpc
        String result = driver.rpcCall(1, "eth_call", params, "latest");
        String re = DriverUtil.parseResult(result).get("result");
        System.out.println(Long.decode(re));
    }

    public String getNonce() throws BlockchainDriverException {
        String nonceStr = driver.rpcCall(1, "eth_getTransactionCount",  address, "latest");
        return DriverUtil.parseResult(nonceStr).get("result").substring(2);
    }

    @Test
    public void getTransactionReceipt() throws BlockchainDriverException {
        String result = driver.rpcCall(1, "eth_getTransactionReceipt", "0x" + "86c0de595bcac069c75c73129c246cd8216e2b4b78100afec1b15df3d64a61f2");
        System.out.println(result);
    }

    @Test
    public void contractAddr() {

        String addr = EncryptUtil.keccak256ToAddress("0x58dfe602278d3f82ebce7355624279b8a5d4c14a" + "1");
        System.out.println(addr);
    }

    @Test
    public void estimateDeplyGas() throws BlockchainDriverException {
        Map<String, Object> params = new HashMap<>();
        params.put("from", "0x" + address);
        params.put("data", "0x" + "608060405234801561001057600080fd5b50610150806100206000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c80632e64cec11461003b5780636057361d14610059575b600080fd5b610043610075565b60405161005091906100d9565b60405180910390f35b610073600480360381019061006e919061009d565b61007e565b005b60008054905090565b8060008190555050565b60008135905061009781610103565b92915050565b6000602082840312156100b3576100b26100fe565b5b60006100c184828501610088565b91505092915050565b6100d3816100f4565b82525050565b60006020820190506100ee60008301846100ca565b92915050565b6000819050919050565b600080fd5b61010c816100f4565b811461011757600080fd5b5056fea2646970667358221220cac7483d530561ce847ff094c2a59eaaa145ef1442b55b0f5de1ebe6a043c8c964736f6c63430008070033");

        // use rpc
        String result = driver.rpcCall(1, "eth_estimateGas", params);
        String re = DriverUtil.parseResult(result).get("result");

        System.out.println(re);

    }

    @Test
    public void estimateExecGas() throws BlockchainDriverException {
        String abiStr = "{\"constant\": false,\"inputs\": [{\"internalType\": \"bytes32\",\"name\": \"taskId\",\"type\": \"bytes32\"},{\"internalType\": \"address[]\",\"name\": \"targets\",\"type\": \"address[]\"}],\"name\": \"deposit\",\"outputs\": [],\"payable\": true,\"stateMutability\": \"payable\",\"type\": \"function\"}";
        CallTransaction.Function function = CallTransaction.Function.fromJsonInterface(abiStr);
        List<String> params = new ArrayList() {
            {
                add("0x58dfe602278d3f82ebce7355624279b8a5d4c14a");
                add("0xeda3cceff74dcdb14a04a995d51e7fa06e807a1a");
            }
        };
        // 如果调用的智能合约的方法没有参数则不传即可
        byte[] callData = function.encode("hz", params);

        Map<String, Object> param = new HashMap<>();
        param.put("from", "0x" + address);
        param.put("data", "0x" + Hex.toHexString(callData));
        param.put("to", "0x5D5F040Ae032E7B03f809e9f0Bb2579acc2dc621");
        // 如果智能合约的方法非payable则value参数不传即可。
        param.put("value", "0x" + Hex.toHexString("3".getBytes()));

        // use rpc
        String result = driver.rpcCall(1, "eth_estimateGas", param);
        String re = DriverUtil.parseResult(result).get("result");
        System.out.println(re);
    }
}
