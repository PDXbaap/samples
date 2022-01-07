package ltd.pdx.utopia.client.dynamic_native;


import ltd.pdx.utopia.eckey.rlp.RLP;
import java.nio.charset.StandardCharsets;


public class DynNativeCallParam {
    private String soName;
    private String lookUpClassName;
    byte[][] args;
    private byte[] data;

    public DynNativeCallParam(String soName, String lookUpClassName, byte[][] args, byte[] data) {
        this.soName = soName;
        this.lookUpClassName = lookUpClassName;
        this.args = args;
        this.data = data;
    }

    public byte[] getRlpEncoded() {
        byte[] soNameBytes = RLP.encodeElement(this.soName == null ? null : this.soName.getBytes(StandardCharsets.UTF_8));
        byte[] lookUpClassNameBytes = RLP.encodeElement(this.lookUpClassName.getBytes(StandardCharsets.UTF_8));
        byte[] argsBytes = RLP.encode(this.args);
        byte[] dataBytes = RLP.encodeElement(this.data);
        return RLP.encodeList(soNameBytes, lookUpClassNameBytes, argsBytes, dataBytes);
    }
}
