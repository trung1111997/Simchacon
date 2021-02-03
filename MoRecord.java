package com.viettel.smsfw.object;

import java.sql.Timestamp;

import com.viettel.cluster.agent.integration.Record;

/**
 * Thong tin ban ghi MO
 *
 * @author LucNV1
 * @version 1.0
 * @since 01-03-2015
 */
public class MoRecord implements Record {

    public static String MO_ID = "MO_ID";
    public static String MSISDN = "MSISDN";
    public static String CONTENT = "CONTENT";
    public static String CHANNEL = "CHANNEL";
    public static String RECEIVE_TIME = "RECEIVE_TIME";
    public static int STATUS;
    public static String APP_ID = "APP_ID";
    public static String ACTION_TYPE = "ACTION_TYPE";
    public static String AES_KEY = "AES_KEY";
    public static int REQUEST_ID;
    public static int RSC;

    //
    public static int PREPAID = 1;
    public static int POSTPAID = 0;
    //
    private Long id = Long.valueOf(-1);
    private String msisdn;
    private Long subId = Long.valueOf(-1);
    private String productCode;
    private Integer processCode;
    private Integer subType = -1;
    private String content;
    private String channel;
    private Integer actionType = -1;
    private Integer errCode = -1;
    private String errOcs;
    private Long fee = Long.valueOf(0);
    private String nodeName;
    private String clusterName;
    private Long regId= Long.valueOf(-1);
    private String appId;
    private String message;
    private String imsi;
    private String serial;
    private String partyCode = null;
    private String vasCode = null;
    private String description;
    private String aeskey;
    private int status;
    private int requestid;
  
    
    /**
     * @return the partyCode
     */
    public String getPartyCode() {
        return partyCode;
    }

    public int getRSC(){return RSC;}
    public void setRSC(int RSC){this.RSC = RSC;}
    public int getRequestId() { return requestid;}
    public void setRequestId(int requestId){this.requestid = requestId;}

    /**
     * @return the vasCode
     */
    public String getVasCode() {
        return vasCode;
    }

    /**
     * @param partyCode the partyCode to set
     */
    public void setPartyCode(String partyCode) {
        this.partyCode = partyCode;
    }

    /**
     * @param vasCode the vasCode to set
     */
    public void setVasCode(String vasCode) {
        this.vasCode = vasCode;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public Long getRegId() {
        return regId;
    }

    public void setRegId(Long regId) {
        this.regId = regId;
    }

    public int getSTATUS(){ return status;}

    public void setSTATUS(int status) { this.status = status;}


    @Override
    public String toString() {
        return id + "|" + msisdn + "|" + content;
    }

    public MoRecord() {
    }

    public MoRecord(MoRecord moRecord) {
        this.id = moRecord.id;
        this.msisdn = moRecord.msisdn;
        this.subId = moRecord.subId;
        this.productCode = moRecord.productCode;
        this.processCode = moRecord.processCode;
        this.subType = moRecord.subType;
        this.content = moRecord.content;
        this.channel = moRecord.channel;
        this.actionType = moRecord.actionType;
        this.errCode = moRecord.errCode;
        this.errOcs = moRecord.errOcs;
        this.fee = moRecord.fee;
        this.nodeName = moRecord.nodeName;
        this.clusterName = moRecord.clusterName;
        this.regId = moRecord.regId;
        this.appId = moRecord.appId;
        this.message = moRecord.message;
        this.imsi = moRecord.imsi;
        this.serial = moRecord.serial;
        this.description = moRecord.description;
        this.aeskey = moRecord.aeskey;
        this.RSC = moRecord.RSC;
    }
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getProcessCode(){ return processCode;}

    public void  setProcessCode(int processCode) { this.processCode = processCode; }

    public String getDescription(){ return description;}

    public void setDescription(String description){ this.description = description; }

    public Integer getSubType() {
        return subType;
    }

    public void setSubType(Integer subType) {
        this.subType = subType;
    }

    public Long getSubId() {
        return subId;
    }

    public void setSubId(Long subId) {
        this.subId = subId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public Integer getActionType() {
        return actionType;
    }

    public void setActionType(Integer actionType) {
        this.actionType = actionType;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getErrCode() {
        return errCode;
    }

    public void setErrCode(Integer errCode) {
        this.errCode = errCode;
    }

    public String getErrOcs() {
        return errOcs;
    }

    public void setErrOcs(String errOcs) {
        this.errOcs = errOcs;
    }

    public Long getFee() {
        return fee;
    }

    public void setFee(Long fee) {
        this.fee = fee;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(String msisdn) {
        this.msisdn = msisdn;
    }

    public long getID() {
        return this.id;
    }

    public String getAESKey(){ return aeskey;}
    public void setAESKey (String aeskey){ this.aeskey = aeskey;}
}
