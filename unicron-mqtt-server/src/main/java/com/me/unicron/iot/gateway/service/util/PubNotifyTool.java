package com.me.unicron.iot.gateway.service.util;

import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.me.epower.entity.Operator;
import com.me.unicron.HMacMD5;
import com.me.unicron.QEncodeUtil;

public class PubNotifyTool {

    private static final Logger logger = LoggerFactory.getLogger(PubNotifyTool.class);

    public static final String DIDI_OPERATOR_ID = "101437000";//运营商XX公司编号

    public static final String DIDI_AES_SECRET = "EeA7JzJzrf4bbOAF";//运营商XX公司分配加密密钥

    public static final String DIDI_AESLV = "GzNHw7klZcGGAT7F";//运营商XX公司分配初始化向量

    public static final String DIDI_OPERETOR_SECRET = "Sfasa4xTrA1l0ZfF";//运营商XX公司分配token密钥

    public static final String DIDI_SIG_SECRET = "gZFpcUfiWQ9FhwTF";//运营商XX公司分配签名密钥

    public static String genSig(Operator operator, String enString, String timeStamp) {
        return HMacMD5.getHmacMd5Str(operator.getSigSecret(), operator.getDdOperatorId() + enString + timeStamp + "0001");
    }

    public static String genDDDecodeData(String operatorId, String jsonData) {
        return QEncodeUtil.decrypt(jsonData, getAESSecret(operatorId), getAESLV(operatorId));
    }

    public static String genDDEncodeData(String operatorId, String jsonData) {
        return QEncodeUtil.encrypt(jsonData, getAESSecret(operatorId), getAESLV(operatorId));
    }

    public static String genDDSig(String operatorId, String enString, String timeStamp) {
        return HMacMD5.getHmacMd5Str(getSigSecret(operatorId), DIDI_OPERATOR_ID + enString + timeStamp + "0001");
    }

    public static String dealEscapeData(String data) {
        data = StringEscapeUtils.unescapeJava(data);
        if (data.startsWith("\"")) {
            data = data.replaceFirst("\"", "");
            data = data.substring(0, data.length() - 1);
        }
        return data;
    }

    public static boolean checkSig(Map<String, String> map, String req_operatorID) {
        try {
            String Data = map.get("Data");
            String TimeStamp = map.get("TimeStamp");
            String Seq = map.get("Seq");
            String Sig = map.get("Sig");
            String ddSig = HMacMD5.getHmacMd5Str(getSigSecret(req_operatorID), req_operatorID + Data + TimeStamp + Seq);
            if (Sig.equals(ddSig))
                return true;
        } catch (Exception e) {
            logger.error("checkSig error", e);
        }
        return false;
    }

    public static String getAESSecret(String operatorId) {

        return DIDI_AES_SECRET;
    }

    public static String getAESLV(String operatorId) {
        return DIDI_AESLV;
    }

    public static String getOperatorSecret(String operatorId) {
        return DIDI_OPERETOR_SECRET;
    }

    public static String getSigSecret(String operatorId) {
        return DIDI_SIG_SECRET;
    }

    public static String getDdOperator(String operatorId) {
        return DIDI_OPERATOR_ID;
    }
}
