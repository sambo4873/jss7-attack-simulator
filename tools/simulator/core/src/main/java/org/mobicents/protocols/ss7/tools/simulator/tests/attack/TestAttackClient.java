package org.mobicents.protocols.ss7.tools.simulator.tests.attack;

import org.apache.log4j.Level;
import org.mobicents.protocols.ss7.map.api.*;
import org.mobicents.protocols.ss7.map.api.datacoding.NationalLanguageIdentifier;
import org.mobicents.protocols.ss7.map.api.dialog.*;
import org.mobicents.protocols.ss7.map.api.errors.MAPErrorMessage;
import org.mobicents.protocols.ss7.map.api.primitives.*;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPDialogMobility;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPServiceMobility;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPServiceMobilityListener;
import org.mobicents.protocols.ss7.map.api.service.mobility.authentication.*;
import org.mobicents.protocols.ss7.map.api.service.mobility.faultRecovery.ForwardCheckSSIndicationRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.faultRecovery.ResetRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.faultRecovery.RestoreDataRequest;
import org.mobicents.protocols.ss7.map.api.service.mobility.faultRecovery.RestoreDataResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.imei.*;
import org.mobicents.protocols.ss7.map.api.service.mobility.locationManagement.*;
import org.mobicents.protocols.ss7.map.api.service.mobility.oam.ActivateTraceModeRequest_Mobility;
import org.mobicents.protocols.ss7.map.api.service.mobility.oam.ActivateTraceModeResponse_Mobility;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.*;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.*;
import org.mobicents.protocols.ss7.map.api.service.oam.*;
import org.mobicents.protocols.ss7.map.api.service.sms.*;
import org.mobicents.protocols.ss7.map.api.service.supplementary.SSCode;
import org.mobicents.protocols.ss7.map.api.smstpdu.*;
import org.mobicents.protocols.ss7.map.smstpdu.*;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;
import org.mobicents.protocols.ss7.tcap.api.MessageType;
import org.mobicents.protocols.ss7.tcap.asn.ApplicationContextName;
import org.mobicents.protocols.ss7.tcap.asn.comp.Invoke;
import org.mobicents.protocols.ss7.tcap.asn.comp.Problem;
import org.mobicents.protocols.ss7.tcap.asn.comp.ReturnResult;
import org.mobicents.protocols.ss7.tcap.asn.comp.ReturnResultLast;
import org.mobicents.protocols.ss7.tools.simulator.common.AddressNatureType;
import org.mobicents.protocols.ss7.tools.simulator.common.TesterBase;
import org.mobicents.protocols.ss7.tools.simulator.level3.MapMan;
import org.mobicents.protocols.ss7.tools.simulator.level3.MapProtocolVersion;
import org.mobicents.protocols.ss7.tools.simulator.level3.NumberingPlanMapType;
import org.mobicents.protocols.ss7.tools.simulator.management.TesterHost;
import org.mobicents.protocols.ss7.tools.simulator.tests.sms.*;

import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * @author Kristoffer Jensen
 */
public class TestAttackClient extends TesterBase implements MAPDialogListener,
        MAPServiceSmsListener, MAPServiceMobilityListener {

    public static String SOURCE_NAME = "TestAttackClient";

    private final String name;

    private MapMan mapMan;

    private String currentRequestDef = "";

    private boolean isStarted = false;

    private static Charset isoCharset = Charset.forName("ISO-8859-1");

    private int countSriReq = 0;
    private int countSriResp = 0;
    private int countMtFsmReq = 0;
    private int countMtFsmReqNot = 0;
    private int countMtFsmResp = 0;
    private int countMoFsmReq = 0;
    private int countMoFsmResp = 0;
    private int countIscReq = 0;
    private int countRsmdsReq = 0;
    private int countRsmdsResp = 0;
    private int countAscReq = 0;
    private int countAscResp = 0;
    private int countErrRcvd = 0;
    private int countErrSent = 0;
    private int mesRef = 0;

    public TestAttackClient(String name) {
        super(SOURCE_NAME);
        this.name = name;
    }

    public void setTesterHost(TesterHost testerHost) {
        this.testerHost = testerHost;
    }

    public void setMapMan(MapMan val) {
        this.mapMan = val;
    }


    public AddressNatureType getAddressNature() {
        return new AddressNatureType(this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getAddressNature().getIndicator());
    }

    public String getAddressNature_Value() {
        return new AddressNatureType(this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getAddressNature().getIndicator()).toString();
    }

    public void setAddressNature(AddressNatureType val) {
        this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().setAddressNature(AddressNature.getInstance(val.intValue()));
        this.testerHost.markStore();
    }

    public NumberingPlanMapType getNumberingPlan() {
        return new NumberingPlanMapType(this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getNumberingPlan().getIndicator());
    }

    public String getNumberingPlan_Value() {
        return new NumberingPlanMapType(this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getNumberingPlan().getIndicator())
                .toString();
    }

    public void setNumberingPlan(NumberingPlanMapType val) {
        this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().setNumberingPlan(NumberingPlan.getInstance(val.intValue()));
        this.testerHost.markStore();
    }

    public String getServiceCenterAddress() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getServiceCenterAddress();
    }

    public void setServiceCenterAddress(String val) {
        this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().setServiceCenterAddress(val);
        this.testerHost.markStore();
    }

    public MapProtocolVersion getMapProtocolVersion() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getMapProtocolVersion();
    }

    public String getMapProtocolVersion_Value() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getMapProtocolVersion().toString();
    }

    public void setMapProtocolVersion(MapProtocolVersion val) {
        this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().setMapProtocolVersion(val);
        this.testerHost.markStore();
    }

    public SRIReaction getSRIReaction() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getSRIReaction();
    }

    public String getSRIReaction_Value() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getSRIReaction().toString();
    }

    public void setSRIReaction(SRIReaction val) {
        this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().setSRIReaction(val);
        this.testerHost.markStore();
    }

    public SRIInformServiceCenter getSRIInformServiceCenter() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getSRIInformServiceCenter();
    }

    public String getSRIInformServiceCenter_Value() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getSRIInformServiceCenter().toString();
    }

    public void setSRIInformServiceCenter(SRIInformServiceCenter val) {
        this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().setSRIInformServiceCenter(val);
        this.testerHost.markStore();
    }

    public boolean isSRIScAddressNotIncluded() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().isSRIScAddressNotIncluded();
    }

    public void setSRIScAddressNotIncluded(boolean val) {
        this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().setSRIScAddressNotIncluded(val);
        this.testerHost.markStore();
    }

    public MtFSMReaction getMtFSMReaction() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getMtFSMReaction();
    }

    public String getMtFSMReaction_Value() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getMtFSMReaction().toString();
    }

    public void setMtFSMReaction(MtFSMReaction val) {
        this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().setMtFSMReaction(val);
        this.testerHost.markStore();
    }

    public ReportSMDeliveryStatusReaction getReportSMDeliveryStatusReaction() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getReportSMDeliveryStatusReaction();
    }

    public String getReportSMDeliveryStatusReaction_Value() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getReportSMDeliveryStatusReaction().toString();
    }

    public void setReportSMDeliveryStatusReaction(ReportSMDeliveryStatusReaction val) {
        this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().setReportSMDeliveryStatusReaction(val);
        this.testerHost.markStore();
    }

    public String getSRIResponseImsi() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getSriResponseImsi();
    }

    public void setSRIResponseImsi(String val) {
        this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().setSriResponseImsi(val);
        this.testerHost.markStore();
    }

    public String getSRIResponseVlr() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getSriResponseVlr();
    }

    public void setSRIResponseVlr(String val) {
        this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().setSriResponseVlr(val);
        this.testerHost.markStore();
    }

    public int getSmscSsn() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getSmscSsn();
    }

    public void setSmscSsn(int val) {
        this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().setSmscSsn(val);
        this.testerHost.markStore();
    }

    public int getNationalLanguageCode() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getNationalLanguageCode();
    }

    public void setNationalLanguageCode(int val) {
        this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().setNationalLanguageCode(val);
        this.testerHost.markStore();
    }

    public TypeOfNumberType getTypeOfNumber() {
        return new TypeOfNumberType(this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getTypeOfNumber().getCode());
    }

    public String getTypeOfNumber_Value() {
        return new TypeOfNumberType(this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getTypeOfNumber().getCode()).toString();
    }

    public void setTypeOfNumber(TypeOfNumberType val) {
        this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().setTypeOfNumber(TypeOfNumber.getInstance(val.intValue()));
        this.testerHost.markStore();
    }

    public NumberingPlanIdentificationType getNumberingPlanIdentification() {
        return new NumberingPlanIdentificationType(this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getNumberingPlanIdentification()
                .getCode());
    }

    public String getNumberingPlanIdentification_Value() {
        return new NumberingPlanIdentificationType(this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getNumberingPlanIdentification()
                .getCode()).toString();
    }

    public void setNumberingPlanIdentification(NumberingPlanIdentificationType val) {
        this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData()
                .setNumberingPlanIdentification(NumberingPlanIdentification.getInstance(val.intValue()));
        this.testerHost.markStore();
    }

    public SmsCodingType getSmsCodingType() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getSmsCodingType();
    }

    public String getSmsCodingType_Value() {
        return this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getSmsCodingType().toString();
    }

    public void setSmsCodingType(SmsCodingType val) {
        this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().setSmsCodingType(val);
        this.testerHost.markStore();
    }


    public String getCurrentRequestDef() {
        return "LastDialog: " + currentRequestDef;
    }

    public boolean start() {
        this.activateMapServices();

        this.testerHost.sendNotif(SOURCE_NAME, "Attack Client has been started", "", Level.INFO);
        isStarted = true;

        return true;
    }

    private void activateMapServices() {
        MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();

        mapProvider.getMAPServiceMobility().acivate();
        mapProvider.getMAPServiceMobility().addMAPServiceListener(this);

        mapProvider.getMAPServiceSms().acivate();
        mapProvider.getMAPServiceSms().addMAPServiceListener(this);

        mapProvider.addMAPDialogListener(this);
    }

    private void deactivateMapServices() {
        MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();

        mapProvider.getMAPServiceMobility().deactivate();
        mapProvider.getMAPServiceMobility().removeMAPServiceListener(this);

        mapProvider.getMAPServiceSms().deactivate();
        mapProvider.getMAPServiceSms().removeMAPServiceListener(this);

        mapProvider.removeMAPDialogListener(this);
    }

    public void stop() {
        this.deactivateMapServices();

        isStarted = false;
        this.testerHost.sendNotif(SOURCE_NAME, "SRIForSM Client has been stopped", "", Level.INFO);
    }

    public String performMoForwardSM(String msg, String destIsdnNumber, String origIsdnNumber) {
        if (!isStarted)
            return "The tester is not started";
        if (msg == null || msg.equals(""))
            return "Msg is empty";
        if (destIsdnNumber == null || destIsdnNumber.equals(""))
            return "DestIsdnNumber is empty";
        if (origIsdnNumber == null || origIsdnNumber.equals(""))
            return "OrigIsdnNumber is empty";
        int maxMsgLen = this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getSmsCodingType().getSupportesMaxMessageLength(0);
        if (msg.length() > maxMsgLen)
            return "Simulator does not support message length for current encoding type more than " + maxMsgLen;

        currentRequestDef = "";

        return doMoForwardSM(msg, destIsdnNumber, origIsdnNumber, this.getServiceCenterAddress(), 0, 0, 0);
    }

    private String doMoForwardSM(String msg, String destIsdnNumber, String origIsdnNumber, String serviceCentreAddr, int msgRef, int segmCnt, int segmNum) {

        MAPProvider mapProvider = this.mapMan.getMAPStack().getMAPProvider();

        MAPApplicationContextVersion vers;
        MAPApplicationContextName acn = MAPApplicationContextName.shortMsgMORelayContext;
        switch (this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getMapProtocolVersion().intValue()) {
            case MapProtocolVersion.VAL_MAP_V1:
                vers = MAPApplicationContextVersion.version1;
                break;
            case MapProtocolVersion.VAL_MAP_V2:
                vers = MAPApplicationContextVersion.version2;
                break;
            default:
                vers = MAPApplicationContextVersion.version3;
                break;
        }
        MAPApplicationContext mapAppContext = MAPApplicationContext.getInstance(acn, vers);

        AddressString serviceCentreAddressDA = mapProvider.getMAPParameterFactory().createAddressString(
                this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getAddressNature(),
                this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getNumberingPlan(), serviceCentreAddr);
        SM_RP_DA da = mapProvider.getMAPParameterFactory().createSM_RP_DA(serviceCentreAddressDA);
        ISDNAddressString msisdn = mapProvider.getMAPParameterFactory().createISDNAddressString(
                this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getAddressNature(),
                this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getNumberingPlan(), origIsdnNumber);
        SM_RP_OA oa = mapProvider.getMAPParameterFactory().createSM_RP_OA_Msisdn(msisdn);

        try {
            AddressField destAddress = new AddressFieldImpl(this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getTypeOfNumber(),
                    this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getNumberingPlanIdentification(), destIsdnNumber);

            int dcsVal = 0;
            switch (this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getSmsCodingType().intValue()) {
                case SmsCodingType.VAL_GSM7:
                    dcsVal = 0;
                    break;
                case SmsCodingType.VAL_GSM8:
                    dcsVal = 4;
                    break;
                case SmsCodingType.VAL_UCS2:
                    dcsVal = 8;
                    break;
            }
            DataCodingScheme dcs = new DataCodingSchemeImpl(dcsVal);
            UserDataHeader udh = null;
            if (dcs.getCharacterSet() == CharacterSet.GSM8) {
                ApplicationPortAddressing16BitAddressImpl apa16 = new ApplicationPortAddressing16BitAddressImpl(16020, 0);
                udh = new UserDataHeaderImpl();
                udh.addInformationElement(apa16);
            }
            if (segmCnt > 1) {
                if (udh == null)
                    udh = new UserDataHeaderImpl();
                udh.addInformationElement(new ConcatenatedShortMessagesIdentifierImpl(false, msgRef, segmCnt, segmNum));
            }
            if (dcs.getCharacterSet() == CharacterSet.GSM7
                    && this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getNationalLanguageCode() > 0) {
                NationalLanguageIdentifier nli = NationalLanguageIdentifier.getInstance(this.testerHost.getConfigurationData()
                        .getTestSRIForSMClientConfigurationData().getNationalLanguageCode());
                if (nli != null) {
                    if (udh == null)
                        udh = new UserDataHeaderImpl();
                    udh.addInformationElement(new NationalLanguageLockingShiftIdentifierImpl(nli));
                    udh.addInformationElement(new NationalLanguageSingleShiftIdentifierImpl(nli));
                }
            }

            UserData userData = new UserDataImpl(msg, dcs, udh, isoCharset);
            ProtocolIdentifier pi = new ProtocolIdentifierImpl(0);
            ValidityPeriod validityPeriod = new ValidityPeriodImpl(169); // 3
            // days
            SmsSubmitTpdu tpdu = new SmsSubmitTpduImpl(false, false, false, ++mesRef, destAddress, pi, validityPeriod, userData);
            SmsSignalInfo si = mapProvider.getMAPParameterFactory().createSmsSignalInfo(tpdu, null);

            MAPDialogSms curDialog = mapProvider.getMAPServiceSms().createNewDialog(mapAppContext, this.mapMan.createOrigAddress(), null,
                    this.mapMan.createDestAddress(serviceCentreAddr, this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getSmscSsn()),
                    null);

            if (si.getData().length < 110 || vers == MAPApplicationContextVersion.version1) {
                if (this.testerHost.getConfigurationData().getTestSRIForSMClientConfigurationData().getMapProtocolVersion().intValue() <= 2)
                    curDialog.addForwardShortMessageRequest(da, oa, si, false);
                else
                    curDialog.addMoForwardShortMessageRequest(da, oa, si, null, null);
                curDialog.send();

                String mtData = createMoData(curDialog.getLocalDialogId(), destIsdnNumber, origIsdnNumber, serviceCentreAddr);
                currentRequestDef += "Sent moReq;";
                this.countMoFsmReq++;
                this.testerHost.sendNotif(SOURCE_NAME, "Sent: moReq: " + msg, mtData, Level.DEBUG);
            } else {
                ResendMessageData md = new ResendMessageData();
                md.da = da;
                md.oa = oa;
                md.si = si;
                md.destIsdnNumber = destIsdnNumber;
                md.origIsdnNumber = origIsdnNumber;
                md.serviceCentreAddr = serviceCentreAddr;
                md.msg = msg;
                curDialog.setUserObject(md);

                curDialog.send();
                currentRequestDef += "Sent emptTBegin;";
                this.testerHost.sendNotif(SOURCE_NAME, "Sent: emptTBegin", "", Level.DEBUG);
            }

            return "MoForwardShortMessageRequest has been sent";
        } catch (MAPException ex) {
            return "Exception when sending MoForwardShortMessageRequest: " + ex.toString();
        }
    }

    private String createMoData(long dialogId, String destIsdnNumber, String origIsdnNumber, String serviceCentreAddr) {
        StringBuilder sb = new StringBuilder();
        sb.append("dialogId=");
        sb.append(dialogId);
        sb.append(", destIsdnNumber=\"");
        sb.append(destIsdnNumber);
        sb.append(", origIsdnNumber=\"");
        sb.append(origIsdnNumber);
        sb.append("\", serviceCentreAddr=\"");
        sb.append(serviceCentreAddr);
        sb.append("\"");
        return sb.toString();
    }

    private class ResendMessageData {
        public SM_RP_DA da;
        public SM_RP_OA oa;
        public SmsSignalInfo si;
        public String msg;
        public String destIsdnNumber;
        public String origIsdnNumber;
        public String serviceCentreAddr;
    }

    @Override
    public void onDialogDelimiter(MAPDialog mapDialog) {

    }

    @Override
    public void onDialogRequest(MAPDialog mapDialog, AddressString destReference, AddressString origReference, MAPExtensionContainer extensionContainer) {

    }

    @Override
    public void onDialogRequestEricsson(MAPDialog mapDialog, AddressString destReference, AddressString origReference, IMSI eriImsi, AddressString eriVlrNo) {

    }

    @Override
    public void onDialogAccept(MAPDialog mapDialog, MAPExtensionContainer extensionContainer) {

    }

    @Override
    public void onDialogReject(MAPDialog mapDialog, MAPRefuseReason refuseReason, ApplicationContextName alternativeApplicationContext, MAPExtensionContainer extensionContainer) {

    }

    @Override
    public void onDialogUserAbort(MAPDialog mapDialog, MAPUserAbortChoice userReason, MAPExtensionContainer extensionContainer) {

    }

    @Override
    public void onDialogProviderAbort(MAPDialog mapDialog, MAPAbortProviderReason abortProviderReason, MAPAbortSource abortSource, MAPExtensionContainer extensionContainer) {

    }

    @Override
    public void onDialogClose(MAPDialog mapDialog) {

    }

    @Override
    public void onDialogNotice(MAPDialog mapDialog, MAPNoticeProblemDiagnostic noticeProblemDiagnostic) {

    }

    @Override
    public void onDialogRelease(MAPDialog mapDialog) {

    }

    @Override
    public void onDialogTimeout(MAPDialog mapDialog) {

    }

    @Override
    public void onForwardShortMessageRequest(ForwardShortMessageRequest forwSmInd) {

    }

    @Override
    public void onForwardShortMessageResponse(ForwardShortMessageResponse forwSmRespInd) {

    }

    @Override
    public void onMoForwardShortMessageRequest(MoForwardShortMessageRequest moForwSmInd) {

    }

    @Override
    public void onMoForwardShortMessageResponse(MoForwardShortMessageResponse moForwSmRespInd) {

    }

    @Override
    public void onMtForwardShortMessageRequest(MtForwardShortMessageRequest mtForwSmInd) {

    }

    @Override
    public void onMtForwardShortMessageResponse(MtForwardShortMessageResponse mtForwSmRespInd) {

    }

    @Override
    public void onSendRoutingInfoForSMRequest(SendRoutingInfoForSMRequest sendRoutingInfoForSMInd) {

    }

    @Override
    public void onSendRoutingInfoForSMResponse(SendRoutingInfoForSMResponse sendRoutingInfoForSMRespInd) {

    }

    @Override
    public void onReportSMDeliveryStatusRequest(ReportSMDeliveryStatusRequest reportSMDeliveryStatusInd) {

    }

    @Override
    public void onReportSMDeliveryStatusResponse(ReportSMDeliveryStatusResponse reportSMDeliveryStatusRespInd) {

    }

    @Override
    public void onInformServiceCentreRequest(InformServiceCentreRequest informServiceCentreInd) {

    }

    @Override
    public void onAlertServiceCentreRequest(AlertServiceCentreRequest alertServiceCentreInd) {

    }

    @Override
    public void onAlertServiceCentreResponse(AlertServiceCentreResponse alertServiceCentreInd) {

    }

    @Override
    public void onReadyForSMRequest(ReadyForSMRequest request) {

    }

    @Override
    public void onReadyForSMResponse(ReadyForSMResponse response) {

    }

    @Override
    public void onNoteSubscriberPresentRequest(NoteSubscriberPresentRequest request) {

    }

    @Override
    public void onUpdateLocationRequest(UpdateLocationRequest ind) {

    }

    @Override
    public void onUpdateLocationResponse(UpdateLocationResponse ind) {

    }

    @Override
    public void onCancelLocationRequest(CancelLocationRequest request) {

    }

    @Override
    public void onCancelLocationResponse(CancelLocationResponse response) {

    }

    @Override
    public void onSendIdentificationRequest(SendIdentificationRequest request) {

    }

    @Override
    public void onSendIdentificationResponse(SendIdentificationResponse response) {

    }

    @Override
    public void onUpdateGprsLocationRequest(UpdateGprsLocationRequest request) {

    }

    @Override
    public void onUpdateGprsLocationResponse(UpdateGprsLocationResponse response) {

    }

    @Override
    public void onPurgeMSRequest(PurgeMSRequest request) {

    }

    @Override
    public void onPurgeMSResponse(PurgeMSResponse response) {

    }

    @Override
    public void onSendAuthenticationInfoRequest(SendAuthenticationInfoRequest ind) {

    }

    @Override
    public void onSendAuthenticationInfoResponse(SendAuthenticationInfoResponse ind) {

    }

    @Override
    public void onAuthenticationFailureReportRequest(AuthenticationFailureReportRequest ind) {

    }

    @Override
    public void onAuthenticationFailureReportResponse(AuthenticationFailureReportResponse ind) {

    }

    @Override
    public void onResetRequest(ResetRequest ind) {

    }

    @Override
    public void onForwardCheckSSIndicationRequest(ForwardCheckSSIndicationRequest ind) {

    }

    @Override
    public void onRestoreDataRequest(RestoreDataRequest ind) {

    }

    @Override
    public void onRestoreDataResponse(RestoreDataResponse ind) {

    }

    @Override
    public void onAnyTimeInterrogationRequest(AnyTimeInterrogationRequest request) {

    }

    @Override
    public void onAnyTimeInterrogationResponse(AnyTimeInterrogationResponse response) {

    }

    @Override
    public void onProvideSubscriberInfoRequest(ProvideSubscriberInfoRequest request) {

    }

    @Override
    public void onProvideSubscriberInfoResponse(ProvideSubscriberInfoResponse response) {

    }

    @Override
    public void onInsertSubscriberDataRequest(InsertSubscriberDataRequest request) {

    }

    @Override
    public void onInsertSubscriberDataResponse(InsertSubscriberDataResponse request) {

    }

    @Override
    public void onDeleteSubscriberDataRequest(DeleteSubscriberDataRequest request) {

    }

    @Override
    public void onDeleteSubscriberDataResponse(DeleteSubscriberDataResponse request) {

    }

    @Override
    public void onCheckImeiRequest(CheckImeiRequest request) {

    }

    @Override
    public void onCheckImeiResponse(CheckImeiResponse response) {

    }

    @Override
    public void onActivateTraceModeRequest_Mobility(ActivateTraceModeRequest_Mobility ind) {

    }

    @Override
    public void onActivateTraceModeResponse_Mobility(ActivateTraceModeResponse_Mobility ind) {

    }
}
