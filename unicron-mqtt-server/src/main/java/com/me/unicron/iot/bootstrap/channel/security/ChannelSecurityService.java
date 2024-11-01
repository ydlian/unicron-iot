package com.me.unicron.iot.bootstrap.channel.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.me.epower.entity.OperatorKey;
import com.me.epower.repositories.OperatorKeyRepository;
import com.me.unicron.iot.bean.ClientConnectionInfo;
import com.me.unicron.iot.constant.OrangeConst;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ChannelSecurityService {
	@Autowired
	OperatorKeyRepository operatorKeyRepository;
	
	//MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAhT8MC0Q4ohT8PmBWxwOI+IUj57WJktvq1Yz24Q0V67BuvzncebrpMRrxKnjKOF+81mMwjIAqoj4UfGALNxlCzQIDAQABAkEAg7cfIMP/d7lm4AI7xd8otwJly9cYd6QNL6x5H17gHGqOpz1B3uOfHFpFGvEAtEmA7980EmyzxqbhIro8ieOgAQIhAL/RVPCh4Ub2XMdOJmih4VifRRgYcClO/d2vapKha3uBAiEAsdSexKiZV17e6Jdhwx+3RhU6Z6sg/QM6fjUcCp6HnU0CIHzLpmWADF7sveP1VkvQthnAVWWd0Ksvdz8Zd066soEBAiAIRUM9EjpPffIQDzpUwNzdPY+B0sut+MB3rjCnrcfQ4QIgE51YC84ojXzznRAuskqw8xb72VisAARI99NPOi8ARfM=
	//测试用，请勿删除，实际上线不使用以下密钥
	private final String  zhichongCompanyId =  "911101083397675346"; 
	private final String  zhonghengCompanyId = "913300002539163407"; 
	private final static Map<String,String> _hardPassMap= new HashMap<String,String>();	
	private final String  zhichongClientPublicKey="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCKhbwz2L/N3i7nX/XcbPYije5g0tW0iFh++rvR140kGhu0rMOnwwP0wZiIflleGkWN49JhpSyE118qffX7qtuQOTMWMcLHFDVDYkLa/CiqAKfmKgF+L/3dzUzmMhPD893bIm+h/uXQHAYdmcQXYZ9h4pNF15PnpYXmbAbSF13N9wIDAQAB";
	private final String  zhichongServerPrivateKey="MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAIXHzDg6jkSHJFEX/oYT7Di4dVFJQ/bQEoiCqGY5XtlsdHIzXLDeiQug43TlwSFOd/jUgy3AqPMIZJWSjcIwR76RCh0YpWVD7EUqefJdJ66IILNl2GCDJGKsJhM3daVwYsuDeyJRp34bgDMVbeG8r5L3pKCoi7ESyHxAGAUNt4L9AgMBAAECgYBVqkEdaHGddCRPQVwcozlKgOOu74lzzH1xqKkI/Ie+FnLihf0+zOFY7ud3/X+UQlkUn5B4hbwqFB/En5ni1WO6sm96hvJNAKM4rKxCArN3+0Lxg4JXaFRmTVnRVmZYLSAKeUNpnEdNvTmnoZlFiI66Yuh6NyGVvMUAb5dwnbfUSQJBAOkd6Zh2nBfUOid+xIjQ5oAHInOklJ4aEBZ/ZGhGd191KQUVYB3/OnpyOjUZL3n/IPYBot0Z1Uh+0dwBECN4af8CQQCS6Z77tM6YfiCn3P299FtiD0Otsr9r/VZqwQHLdxK3VvmbxtG8FpeD+P6aUdjED2g5U3ZvJnq/FVS3azkoH7sDAkEAlzEAVRLwUAAkK4NrYjTimjyGWqxue0/6CS1caMjzGSU6koJ3kVz8h3fYSqHoCd6veP48q9vaWadu4pqJ5gdt7wJAVeYRVhz2uwTsNRoqptlsYR6JtNMRx6N91rd5RC0gHWDWW3XZhf8zoi+BNlvqwWA80V/ynLTEClv/h6tC5SoAXwJBAKUWtXdPaFZYvjEXgOwcWqHlu3BEXEB+MTMgv8U/nWklhgHyTiGJR965uIOSNE54lY5I9OJi7R7477NhvybPjvo=";
	private final String  zhichongServerPublicKey="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCVE5KIgHz4i00e7u38T7XUlo73RjIV4X8L99WXeRi7+11CfFELnzg6gSJ+GgmRwuOHPdaFdgjym8MURHKMvbPuhVhsB6seZkcBYWmwRXidJqLkeqrWMoPp+LUeDKapw1Enaa+R1l1FueEF+3LNX8CHIUCGXjQN+RffD5wnRLHfewIDAQAB";
	
	private final String  ZhonghengClientPublicKey="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDQV0jsJBLVXxlDvhCSqgBzb34XZqOjOgMm41gLXx9gpIIPP2lC8XlN0Ljp1bUADVqRSHarkCJI/R1ZPfWQDIo72IDuLcTcMb75pZq3PYNXEVQaSQvXVPDxGOQT1FoQIjfSxRimEgSQ/yjZTa6IAFcRizXSMHILVbhYRmlECl4nfwIDAQAB";
	private final String  ZhonghengServerPrivateKey="MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAIXHzDg6jkSHJFEX/oYT7Di4dVFJQ/bQEoiCqGY5XtlsdHIzXLDeiQug43TlwSFOd/jUgy3AqPMIZJWSjcIwR76RCh0YpWVD7EUqefJdJ66IILNl2GCDJGKsJhM3daVwYsuDeyJRp34bgDMVbeG8r5L3pKCoi7ESyHxAGAUNt4L9AgMBAAECgYBVqkEdaHGddCRPQVwcozlKgOOu74lzzH1xqKkI/Ie+FnLihf0+zOFY7ud3/X+UQlkUn5B4hbwqFB/En5ni1WO6sm96hvJNAKM4rKxCArN3+0Lxg4JXaFRmTVnRVmZYLSAKeUNpnEdNvTmnoZlFiI66Yuh6NyGVvMUAb5dwnbfUSQJBAOkd6Zh2nBfUOid+xIjQ5oAHInOklJ4aEBZ/ZGhGd191KQUVYB3/OnpyOjUZL3n/IPYBot0Z1Uh+0dwBECN4af8CQQCS6Z77tM6YfiCn3P299FtiD0Otsr9r/VZqwQHLdxK3VvmbxtG8FpeD+P6aUdjED2g5U3ZvJnq/FVS3azkoH7sDAkEAlzEAVRLwUAAkK4NrYjTimjyGWqxue0/6CS1caMjzGSU6koJ3kVz8h3fYSqHoCd6veP48q9vaWadu4pqJ5gdt7wJAVeYRVhz2uwTsNRoqptlsYR6JtNMRx6N91rd5RC0gHWDWW3XZhf8zoi+BNlvqwWA80V/ynLTEClv/h6tC5SoAXwJBAKUWtXdPaFZYvjEXgOwcWqHlu3BEXEB+MTMgv8U/nWklhgHyTiGJR965uIOSNE54lY5I9OJi7R7477NhvybPjvo=";
	private final String  ZhonghengServerPublicKey="MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCFx8w4Oo5EhyRRF/6GE+w4uHVRSUP20BKIgqhmOV7ZbHRyM1yw3okLoON05cEhTnf41IMtwKjzCGSVko3CMEe+kQodGKVlQ+xFKnnyXSeuiCCzZdhggyRirCYTN3WlcGLLg3siUad+G4AzFW3hvK+S96SgqIuxEsh8QBgFDbeC/QIDAQAB";
	
	static{
		_hardPassMap.put("913300002539163407", "ZH2018053000001");
		_hardPassMap.put("911101083397675346", "C6S1171020ETZZKSSY");
	}
		
	private OperatorKey getLatestKey(List<OperatorKey> Keys){
		if(Keys==null || Keys.isEmpty()){
			return null;
		}
		OperatorKey key=Keys.get(0);
		int minVer=0;
		for(OperatorKey onKey:Keys){
			if(onKey.getKeyversion()!=null && onKey.getKeyversion()>minVer){
				key=onKey;
			}
		}
		if(StringUtils.isBlank(key.getPublickey())){
			return null;
		}
		return key;
		
	}
	
	public ClientConnectionInfo getSecurityKeyPair(String username){
		ClientConnectionInfo info=new ClientConnectionInfo();
		List<OperatorKey> clientKeys = operatorKeyRepository
				.findOperatorKeyByOperatorid(username);
		
		
		List<OperatorKey> orangeKeys = operatorKeyRepository
				.findOperatorKeyByOperatorid(OrangeConst.ORANGE_COMPANY_ID);
		
		OperatorKey orangeRASKey=getLatestKey(orangeKeys);
		OperatorKey clientRSAKey=getLatestKey(clientKeys);
		
		String clientPublicKey=null;
		String serverPublicKey=null;
		String serverPrivateKey=null;
		if (clientKeys == null || clientRSAKey==null) {
			log.info("Client RSA keys is null!Default RSA key active.");
			//return newClient;
		}else{
			clientPublicKey=clientRSAKey.getPublickey();
			log.info("Client RSA keys(DB):clientPublicKey={}",clientPublicKey);
		}
		
		if (orangeKeys == null || orangeRASKey==null) {
			log.info("Orange RSA keys is null!PASSWORD CHECK SKIPPED!");
		}else{
			serverPublicKey=orangeRASKey.getPublickey();
			serverPrivateKey=orangeRASKey.getPrivatekey();
			log.info("Orange RSA keys(DB):serverPublicKey={}",serverPublicKey);
		}
		
		if(StringUtils.isBlank(clientPublicKey)){
			if(username.equals(zhonghengCompanyId)){
				clientPublicKey=ZhonghengClientPublicKey;
			}else if(username.equals(zhichongCompanyId)){
				clientPublicKey=zhichongClientPublicKey;
			}else{
				
			}
		}
		info.setServerPrivateKey(serverPrivateKey);
		info.setServerPublicKey(serverPublicKey);
		info.setClientPublicKey(clientPublicKey);
		
		return info;
		
	}
}
