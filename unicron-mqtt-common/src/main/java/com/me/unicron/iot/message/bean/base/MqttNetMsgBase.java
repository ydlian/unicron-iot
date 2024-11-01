package com.me.unicron.iot.message.bean.base;

public abstract class  MqttNetMsgBase {

	public abstract int getLength(int dataLen);
    
    public abstract int getCodeLeftLength();
    
    public abstract int getBytesOffset();
    
    public abstract byte[] fillPayload2CmdNo(byte[] data,int totalLen,short cmd_no);
    
    public abstract byte[] getServerProtocolVersion();
    //isResponse 是否被动应答，被动应答需原样返回index报文序号,主动发起的序号统一编码
    protected boolean isResponse=false;
    public boolean isResponse() {
		return isResponse;
	}

	public void setResponse(boolean isResponse) {
		this.isResponse = isResponse;
	}

	//存储被动应答时客户端的拷贝
    private byte[] headCopyStart=new byte[2];
    
    private byte[] headCopyVersion=new byte[4];
    
    private byte[] headCopyIndex=new byte[4];

    private long indexVal;
    
	public long getIndexVal() {
		return indexVal;
	}

	public void setIndexVal(long indexVal) {
		this.indexVal = indexVal;
	}

	public byte[] getHeadCopyStart() {
		return headCopyStart;
	}

	public void setHeadCopyStart(byte[] headCopyStart) {
		this.headCopyStart = headCopyStart;
	}

	public byte[] getHeadCopyVersion() {
		return headCopyVersion;
	}

	public void setHeadCopyVersion(byte[] headCopyVersion) {
		this.headCopyVersion = headCopyVersion;
	}

	public byte[] getHeadCopyIndex() {
		return headCopyIndex;
	}

	public void setHeadCopyIndex(byte[] headCopyIndex) {
		this.headCopyIndex = headCopyIndex;
	}
    

	
    
}
