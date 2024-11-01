package com.me.unicron.iot.gateway.worker;

import org.springframework.stereotype.Component;

import com.me.epower.direct.entity.downward.AuthRequest_801;
import com.me.epower.direct.entity.downward.ChargeControlQuery_5;
import com.me.epower.direct.entity.downward.ChargeStatus_115;
import com.me.epower.direct.entity.downward.ChargeStopQuery_11;
import com.me.epower.direct.entity.downward.ChargeUnitQuery_13;
import com.me.epower.direct.entity.downward.CleanRequest_19;
import com.me.epower.direct.entity.downward.CleanRequest_21;
import com.me.epower.direct.entity.downward.DelayFeePolicyQuery_1307;
import com.me.epower.direct.entity.downward.FileVersionQuery_1107;
import com.me.epower.direct.entity.downward.FixPolicyQuery_1301;
import com.me.epower.direct.entity.downward.HistoryOrderQuery_403;
import com.me.epower.direct.entity.downward.Httpupdate_1101;
import com.me.epower.direct.entity.downward.LockRequest_23;
import com.me.epower.direct.entity.downward.LogRequest_409;
import com.me.epower.direct.entity.downward.PolicyQuery_1303;
import com.me.epower.direct.entity.downward.PowerStrategyDispatchQuery_17;
import com.me.epower.direct.entity.downward.PowerStrategyDispatch_15;
import com.me.epower.direct.entity.downward.RecentOrderQuery_205;
import com.me.epower.direct.entity.downward.ServiceFeeQuery_1305;
import com.me.epower.direct.entity.downward.StartChargeQuery_7;
import com.me.epower.direct.entity.downward.StationIntPara_1;
import com.me.epower.direct.entity.downward.StationStringPara_3;
import com.me.unicron.iot.bootstrap.ChannelService;

@Component
public interface ICommandWorkerService {

	boolean startCharge(ChannelService channelService, StartChargeQuery_7 startChargeQuery_7);

	boolean doRWStringPara(ChannelService channelService, StationStringPara_3 stationStringPara_3);

	boolean queryRecentOrder(ChannelService channelService, RecentOrderQuery_205 recentOrderQuery_205);

	boolean stopCharge(ChannelService channelService, ChargeControlQuery_5 chargeControlQuery_5);

	boolean queryHistoryOrder(ChannelService channelService, HistoryOrderQuery_403 historyOrderQuery_403);

	boolean queryChargeStatus(ChannelService channelService, ChargeStatus_115 chargeStatus_115);

	boolean lockControl(ChannelService channelService, LockRequest_23 lockRequest_23);

	boolean doRWIntPara(ChannelService channelService, StationIntPara_1 stationIntPara_1);

	boolean doStationControl(ChannelService channelService, ChargeControlQuery_5 chargeControlQuery_5);

	boolean doCleanIntPara(ChannelService channelService, CleanRequest_19 cleanRequest_19);

	boolean doCleanStringPara(ChannelService channelService, CleanRequest_21 cleanRequest_21);

	boolean queryLog(ChannelService channelService, LogRequest_409 logRequest_409);
	
	boolean queryLogBms(ChannelService channelService, LogRequest_409 logRequest_409);

	boolean doHttpupdate(ChannelService channelService, Httpupdate_1101 httpupdate_1101);

	boolean queryFileVersion(ChannelService channelService, FileVersionQuery_1107 fileVersionQuery_1107);

	boolean doRWFixedPowerTariffPolicy(ChannelService channelService, FixPolicyQuery_1301 fixPolicyQuery_1301);

	boolean doRWPowerTariffPolicy(ChannelService channelService, PolicyQuery_1303 policyQuery_1303);

	boolean doRWServiceFeePolicy(ChannelService channelService, ServiceFeeQuery_1305 serviceFeeQuery_1305);

	boolean doRWDelayFeePolicy(ChannelService channelService, DelayFeePolicyQuery_1307 delayFeePolicyQuery_1307);

	boolean authRequest(ChannelService channelService, AuthRequest_801 authRequest_801);

	boolean stopChargeByOrder(ChannelService channelService, ChargeStopQuery_11 chargeStopQuery_11);
	
	boolean queryChargeUnitInfo(ChannelService channelService, ChargeUnitQuery_13 chargeUnitQuery_13);
	
	boolean executeChargeUnitPowerDispatch(ChannelService channelService, PowerStrategyDispatch_15 powerStrategyDispatch_15);
	
	boolean queryPowerDispatchStategy(ChannelService channelService, PowerStrategyDispatchQuery_17 powerStrategyDispatchQuery_17);
	
}
