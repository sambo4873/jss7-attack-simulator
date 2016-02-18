package org.mobicents.protocols.ss7.tools.simulator.tests.attack;

import org.mobicents.protocols.ss7.map.api.*;
import org.mobicents.protocols.ss7.map.api.dialog.*;
import org.mobicents.protocols.ss7.map.api.errors.MAPErrorMessage;
import org.mobicents.protocols.ss7.map.api.primitives.*;
import org.mobicents.protocols.ss7.map.api.service.mobility.MAPDialogMobility;
import org.mobicents.protocols.ss7.map.api.service.mobility.authentication.*;
import org.mobicents.protocols.ss7.map.api.service.mobility.imei.EquipmentStatus;
import org.mobicents.protocols.ss7.map.api.service.mobility.imei.RequestedEquipmentInfo;
import org.mobicents.protocols.ss7.map.api.service.mobility.imei.UESBIIu;
import org.mobicents.protocols.ss7.map.api.service.mobility.locationManagement.*;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.RequestedInfo;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.SubscriberInfo;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.*;
import org.mobicents.protocols.ss7.map.api.service.oam.*;
import org.mobicents.protocols.ss7.map.api.service.sms.*;
import org.mobicents.protocols.ss7.map.api.service.supplementary.SSCode;
import org.mobicents.protocols.ss7.sccp.parameter.SccpAddress;
import org.mobicents.protocols.ss7.tcap.api.MessageType;
import org.mobicents.protocols.ss7.tcap.asn.ApplicationContextName;
import org.mobicents.protocols.ss7.tcap.asn.comp.Invoke;
import org.mobicents.protocols.ss7.tcap.asn.comp.Problem;
import org.mobicents.protocols.ss7.tcap.asn.comp.ReturnResult;
import org.mobicents.protocols.ss7.tcap.asn.comp.ReturnResultLast;

import java.util.ArrayList;

/**
 * @author Kristoffer Jensen
 */
public class TestAttackServer implements MAPDialogListener, MAPDialogSms, MAPDialogMobility {

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
    public Long addUpdateLocationRequest(IMSI imsi, ISDNAddressString mscNumber, ISDNAddressString roamingNumber, ISDNAddressString vlrNumber, LMSI lmsi, MAPExtensionContainer extensionContainer, VLRCapability vlrCapability, boolean informPreviousNetworkEntity, boolean csLCSNotSupportedByUE, GSNAddress vGmlcAddress, ADDInfo addInfo, PagingArea pagingArea, boolean skipSubscriberDataUpdate, boolean restorationIndicator) throws MAPException {
        return null;
    }

    @Override
    public Long addUpdateLocationRequest(int customInvokeTimeout, IMSI imsi, ISDNAddressString mscNumber, ISDNAddressString roamingNumber, ISDNAddressString vlrNumber, LMSI lmsi, MAPExtensionContainer extensionContainer, VLRCapability vlrCapability, boolean informPreviousNetworkEntity, boolean csLCSNotSupportedByUE, GSNAddress vGmlcAddress, ADDInfo addInfo, PagingArea pagingArea, boolean skipSubscriberDataUpdate, boolean restorationIndicator) throws MAPException {
        return null;
    }

    @Override
    public void addUpdateLocationResponse(long invokeId, ISDNAddressString hlrNumber, MAPExtensionContainer extensionContainer, boolean addCapability, boolean pagingAreaCapability) throws MAPException {

    }

    @Override
    public Long addCancelLocationRequest(int customInvokeTimeout, IMSI imsi, IMSIWithLMSI imsiWithLmsi, CancellationType cancellationType, MAPExtensionContainer extensionContainer, TypeOfUpdate typeOfUpdate, boolean mtrfSupportedAndAuthorized, boolean mtrfSupportedAndNotAuthorized, ISDNAddressString newMSCNumber, ISDNAddressString newVLRNumber, LMSI newLmsi) throws MAPException {
        return null;
    }

    @Override
    public Long addCancelLocationRequest(IMSI imsi, IMSIWithLMSI imsiWithLmsi, CancellationType cancellationType, MAPExtensionContainer extensionContainer, TypeOfUpdate typeOfUpdate, boolean mtrfSupportedAndAuthorized, boolean mtrfSupportedAndNotAuthorized, ISDNAddressString newMSCNumber, ISDNAddressString newVLRNumber, LMSI newLmsi) throws MAPException {
        return null;
    }

    @Override
    public void addCancelLocationResponse(long invokeId, MAPExtensionContainer extensionContainer) throws MAPException {

    }

    @Override
    public Long addSendIdentificationRequest(int customInvokeTimeout, TMSI tmsi, Integer numberOfRequestedVectors, boolean segmentationProhibited, MAPExtensionContainer extensionContainer, ISDNAddressString mscNumber, LAIFixedLength previousLAI, Integer hopCounter, boolean mtRoamingForwardingSupported, ISDNAddressString newVLRNumber, LMSI lmsi) throws MAPException {
        return null;
    }

    @Override
    public Long addSendIdentificationRequest(TMSI tmsi, Integer numberOfRequestedVectors, boolean segmentationProhibited, MAPExtensionContainer extensionContainer, ISDNAddressString mscNumber, LAIFixedLength previousLAI, Integer hopCounter, boolean mtRoamingForwardingSupported, ISDNAddressString newVLRNumber, LMSI lmsi) throws MAPException {
        return null;
    }

    @Override
    public void addSendIdentificationResponse(long invokeId, IMSI imsi, AuthenticationSetList authenticationSetList, CurrentSecurityContext currentSecurityContext, MAPExtensionContainer extensionContainer) throws MAPException {

    }

    @Override
    public Long addUpdateGprsLocationRequest(int customInvokeTimeout, IMSI imsi, ISDNAddressString sgsnNumber, GSNAddress sgsnAddress, MAPExtensionContainer extensionContainer, SGSNCapability sgsnCapability, boolean informPreviousNetworkEntity, boolean psLCSNotSupportedByUE, GSNAddress vGmlcAddress, ADDInfo addInfo, EPSInfo epsInfo, boolean servingNodeTypeIndicator, boolean skipSubscriberDataUpdate, UsedRATType usedRATType, boolean gprsSubscriptionDataNotNeeded, boolean nodeTypeIndicator, boolean areaRestricted, boolean ueReachableIndicator, boolean epsSubscriptionDataNotNeeded, UESRVCCCapability uesrvccCapability) throws MAPException {
        return null;
    }

    @Override
    public Long addUpdateGprsLocationRequest(IMSI imsi, ISDNAddressString sgsnNumber, GSNAddress sgsnAddress, MAPExtensionContainer extensionContainer, SGSNCapability sgsnCapability, boolean informPreviousNetworkEntity, boolean psLCSNotSupportedByUE, GSNAddress vGmlcAddress, ADDInfo addInfo, EPSInfo epsInfo, boolean servingNodeTypeIndicator, boolean skipSubscriberDataUpdate, UsedRATType usedRATType, boolean gprsSubscriptionDataNotNeeded, boolean nodeTypeIndicator, boolean areaRestricted, boolean ueReachableIndicator, boolean epsSubscriptionDataNotNeeded, UESRVCCCapability uesrvccCapability) throws MAPException {
        return null;
    }

    @Override
    public void addUpdateGprsLocationResponse(long invokeId, ISDNAddressString hlrNumber, MAPExtensionContainer extensionContainer, boolean addCapability, boolean sgsnMmeSeparationSupported) throws MAPException {

    }

    @Override
    public Long addPurgeMSRequest(int customInvokeTimeout, IMSI imsi, ISDNAddressString vlrNumber, ISDNAddressString sgsnNumber, MAPExtensionContainer extensionContainer) throws MAPException {
        return null;
    }

    @Override
    public Long addPurgeMSRequest(IMSI imsi, ISDNAddressString vlrNumber, ISDNAddressString sgsnNumber, MAPExtensionContainer extensionContainer) throws MAPException {
        return null;
    }

    @Override
    public void addPurgeMSResponse(long invokeId, boolean freezeTMSI, boolean freezePTMSI, MAPExtensionContainer extensionContainer, boolean freezeMTMSI) throws MAPException {

    }

    @Override
    public Long addSendAuthenticationInfoRequest(IMSI imsi, int numberOfRequestedVectors, boolean segmentationProhibited, boolean immediateResponsePreferred, ReSynchronisationInfo reSynchronisationInfo, MAPExtensionContainer extensionContainer, RequestingNodeType requestingNodeType, PlmnId requestingPlmnId, Integer numberOfRequestedAdditionalVectors, boolean additionalVectorsAreForEPS) throws MAPException {
        return null;
    }

    @Override
    public Long addSendAuthenticationInfoRequest(int customInvokeTimeout, IMSI imsi, int numberOfRequestedVectors, boolean segmentationProhibited, boolean immediateResponsePreferred, ReSynchronisationInfo reSynchronisationInfo, MAPExtensionContainer extensionContainer, RequestingNodeType requestingNodeType, PlmnId requestingPlmnId, Integer numberOfRequestedAdditionalVectors, boolean additionalVectorsAreForEPS) throws MAPException {
        return null;
    }

    @Override
    public void addSendAuthenticationInfoResponse(long invokeId, AuthenticationSetList authenticationSetList, MAPExtensionContainer extensionContainer, EpsAuthenticationSetList epsAuthenticationSetList) throws MAPException {

    }

    @Override
    public Long addAuthenticationFailureReportRequest(IMSI imsi, FailureCause failureCause, MAPExtensionContainer extensionContainer, Boolean reAttempt, AccessType accessType, byte[] rand, ISDNAddressString vlrNumber, ISDNAddressString sgsnNumber) throws MAPException {
        return null;
    }

    @Override
    public Long addAuthenticationFailureReportRequest(int customInvokeTimeout, IMSI imsi, FailureCause failureCause, MAPExtensionContainer extensionContainer, Boolean reAttempt, AccessType accessType, byte[] rand, ISDNAddressString vlrNumber, ISDNAddressString sgsnNumber) throws MAPException {
        return null;
    }

    @Override
    public void addAuthenticationFailureReportResponse(long invokeId, MAPExtensionContainer extensionContainer) throws MAPException {

    }

    @Override
    public Long addResetRequest(NetworkResource networkResource, ISDNAddressString hlrNumber, ArrayList<IMSI> hlrList) throws MAPException {
        return null;
    }

    @Override
    public Long addResetRequest(int customInvokeTimeout, NetworkResource networkResource, ISDNAddressString hlrNumber, ArrayList<IMSI> hlrList) throws MAPException {
        return null;
    }

    @Override
    public Long addForwardCheckSSIndicationRequest() throws MAPException {
        return null;
    }

    @Override
    public Long addForwardCheckSSIndicationRequest(int customInvokeTimeout) throws MAPException {
        return null;
    }

    @Override
    public Long addRestoreDataRequest(IMSI imsi, LMSI lmsi, VLRCapability vlrCapability, MAPExtensionContainer extensionContainer, boolean restorationIndicator) throws MAPException {
        return null;
    }

    @Override
    public Long addRestoreDataRequest(int customInvokeTimeout, IMSI imsi, LMSI lmsi, VLRCapability vlrCapability, MAPExtensionContainer extensionContainer, boolean restorationIndicator) throws MAPException {
        return null;
    }

    @Override
    public void addRestoreDataResponse(long invokeId, ISDNAddressString hlrNumber, boolean msNotReachable, MAPExtensionContainer extensionContainer) throws MAPException {

    }

    @Override
    public long addAnyTimeInterrogationRequest(SubscriberIdentity subscriberIdentity, RequestedInfo requestedInfo, ISDNAddressString gsmSCFAddress, MAPExtensionContainer extensionContainer) throws MAPException {
        return 0;
    }

    @Override
    public long addAnyTimeInterrogationRequest(long customInvokeTimeout, SubscriberIdentity subscriberIdentity, RequestedInfo requestedInfo, ISDNAddressString gsmSCFAddress, MAPExtensionContainer extensionContainer) throws MAPException {
        return 0;
    }

    @Override
    public void addAnyTimeInterrogationResponse(long invokeId, SubscriberInfo subscriberInfo, MAPExtensionContainer extensionContainer) throws MAPException {

    }

    @Override
    public long addProvideSubscriberInfoRequest(IMSI imsi, LMSI lmsi, RequestedInfo requestedInfo, MAPExtensionContainer extensionContainer, EMLPPPriority callPriority) throws MAPException {
        return 0;
    }

    @Override
    public long addProvideSubscriberInfoRequest(long customInvokeTimeout, IMSI imsi, LMSI lmsi, RequestedInfo requestedInfo, MAPExtensionContainer extensionContainer, EMLPPPriority callPriority) throws MAPException {
        return 0;
    }

    @Override
    public void addProvideSubscriberInfoResponse(long invokeId, SubscriberInfo subscriberInfo, MAPExtensionContainer extensionContainer) throws MAPException {

    }

    @Override
    public Long addInsertSubscriberDataRequest(IMSI imsi, ISDNAddressString msisdn, Category category, SubscriberStatus subscriberStatus, ArrayList<ExtBearerServiceCode> bearerServiceList, ArrayList<ExtTeleserviceCode> teleserviceList, ArrayList<ExtSSInfo> provisionedSS, ODBData odbData, boolean roamingRestrictionDueToUnsupportedFeature, ArrayList<ZoneCode> regionalSubscriptionData, ArrayList<VoiceBroadcastData> vbsSubscriptionData, ArrayList<VoiceGroupCallData> vgcsSubscriptionData, VlrCamelSubscriptionInfo vlrCamelSubscriptionInfo) throws MAPException {
        return null;
    }

    @Override
    public Long addInsertSubscriberDataRequest(long customInvokeTimeout, IMSI imsi, ISDNAddressString msisdn, Category category, SubscriberStatus subscriberStatus, ArrayList<ExtBearerServiceCode> bearerServiceList, ArrayList<ExtTeleserviceCode> teleserviceList, ArrayList<ExtSSInfo> provisionedSS, ODBData odbData, boolean roamingRestrictionDueToUnsupportedFeature, ArrayList<ZoneCode> regionalSubscriptionData, ArrayList<VoiceBroadcastData> vbsSubscriptionData, ArrayList<VoiceGroupCallData> vgcsSubscriptionData, VlrCamelSubscriptionInfo vlrCamelSubscriptionInfo) throws MAPException {
        return null;
    }

    @Override
    public Long addInsertSubscriberDataRequest(IMSI imsi, ISDNAddressString msisdn, Category category, SubscriberStatus subscriberStatus, ArrayList<ExtBearerServiceCode> bearerServiceList, ArrayList<ExtTeleserviceCode> teleserviceList, ArrayList<ExtSSInfo> provisionedSS, ODBData odbData, boolean roamingRestrictionDueToUnsupportedFeature, ArrayList<ZoneCode> regionalSubscriptionData, ArrayList<VoiceBroadcastData> vbsSubscriptionData, ArrayList<VoiceGroupCallData> vgcsSubscriptionData, VlrCamelSubscriptionInfo vlrCamelSubscriptionInfo, MAPExtensionContainer extensionContainer, NAEAPreferredCI naeaPreferredCI, GPRSSubscriptionData gprsSubscriptionData, boolean roamingRestrictedInSgsnDueToUnsupportedFeature, NetworkAccessMode networkAccessMode, LSAInformation lsaInformation, boolean lmuIndicator, LCSInformation lcsInformation, Integer istAlertTimer, AgeIndicator superChargerSupportedInHLR, MCSSInfo mcSsInfo, CSAllocationRetentionPriority csAllocationRetentionPriority, SGSNCAMELSubscriptionInfo sgsnCamelSubscriptionInfo, ChargingCharacteristics chargingCharacteristics, AccessRestrictionData accessRestrictionData, Boolean icsIndicator, EPSSubscriptionData epsSubscriptionData, ArrayList<CSGSubscriptionData> csgSubscriptionDataList, boolean ueReachabilityRequestIndicator, ISDNAddressString sgsnNumber, DiameterIdentity mmeName, Long subscribedPeriodicRAUTAUtimer, boolean vplmnLIPAAllowed, Boolean mdtUserConsent, Long subscribedPeriodicLAUtimer) throws MAPException {
        return null;
    }

    @Override
    public Long addInsertSubscriberDataRequest(long customInvokeTimeout, IMSI imsi, ISDNAddressString msisdn, Category category, SubscriberStatus subscriberStatus, ArrayList<ExtBearerServiceCode> bearerServiceList, ArrayList<ExtTeleserviceCode> teleserviceList, ArrayList<ExtSSInfo> provisionedSS, ODBData odbData, boolean roamingRestrictionDueToUnsupportedFeature, ArrayList<ZoneCode> regionalSubscriptionData, ArrayList<VoiceBroadcastData> vbsSubscriptionData, ArrayList<VoiceGroupCallData> vgcsSubscriptionData, VlrCamelSubscriptionInfo vlrCamelSubscriptionInfo, MAPExtensionContainer extensionContainer, NAEAPreferredCI naeaPreferredCI, GPRSSubscriptionData gprsSubscriptionData, boolean roamingRestrictedInSgsnDueToUnsupportedFeature, NetworkAccessMode networkAccessMode, LSAInformation lsaInformation, boolean lmuIndicator, LCSInformation lcsInformation, Integer istAlertTimer, AgeIndicator superChargerSupportedInHLR, MCSSInfo mcSsInfo, CSAllocationRetentionPriority csAllocationRetentionPriority, SGSNCAMELSubscriptionInfo sgsnCamelSubscriptionInfo, ChargingCharacteristics chargingCharacteristics, AccessRestrictionData accessRestrictionData, Boolean icsIndicator, EPSSubscriptionData epsSubscriptionData, ArrayList<CSGSubscriptionData> csgSubscriptionDataList, boolean ueReachabilityRequestIndicator, ISDNAddressString sgsnNumber, DiameterIdentity mmeName, Long subscribedPeriodicRAUTAUtimer, boolean vplmnLIPAAllowed, Boolean mdtUserConsent, Long subscribedPeriodicLAUtimer) throws MAPException {
        return null;
    }

    @Override
    public void addInsertSubscriberDataResponse(long invokeId, ArrayList<ExtTeleserviceCode> teleserviceList, ArrayList<ExtBearerServiceCode> bearerServiceList, ArrayList<SSCode> ssList, ODBGeneralData odbGeneralData, RegionalSubscriptionResponse regionalSubscriptionResponse) throws MAPException {

    }

    @Override
    public void addInsertSubscriberDataResponse(long invokeId, ArrayList<ExtTeleserviceCode> teleserviceList, ArrayList<ExtBearerServiceCode> bearerServiceList, ArrayList<SSCode> ssList, ODBGeneralData odbGeneralData, RegionalSubscriptionResponse regionalSubscriptionResponse, SupportedCamelPhases supportedCamelPhases, MAPExtensionContainer extensionContainer, OfferedCamel4CSIs offeredCamel4CSIs, SupportedFeatures supportedFeatures) throws MAPException {

    }

    @Override
    public Long addDeleteSubscriberDataRequest(IMSI imsi, ArrayList<ExtBasicServiceCode> basicServiceList, ArrayList<SSCode> ssList, boolean roamingRestrictionDueToUnsupportedFeature, ZoneCode regionalSubscriptionIdentifier, boolean vbsGroupIndication, boolean vgcsGroupIndication, boolean camelSubscriptionInfoWithdraw, MAPExtensionContainer extensionContainer, GPRSSubscriptionDataWithdraw gprsSubscriptionDataWithdraw, boolean roamingRestrictedInSgsnDueToUnsuppportedFeature, LSAInformationWithdraw lsaInformationWithdraw, boolean gmlcListWithdraw, boolean istInformationWithdraw, SpecificCSIWithdraw specificCSIWithdraw, boolean chargingCharacteristicsWithdraw, boolean stnSrWithdraw, EPSSubscriptionDataWithdraw epsSubscriptionDataWithdraw, boolean apnOiReplacementWithdraw, boolean csgSubscriptionDeleted) throws MAPException {
        return null;
    }

    @Override
    public Long addDeleteSubscriberDataRequest(long customInvokeTimeout, IMSI imsi, ArrayList<ExtBasicServiceCode> basicServiceList, ArrayList<SSCode> ssList, boolean roamingRestrictionDueToUnsupportedFeature, ZoneCode regionalSubscriptionIdentifier, boolean vbsGroupIndication, boolean vgcsGroupIndication, boolean camelSubscriptionInfoWithdraw, MAPExtensionContainer extensionContainer, GPRSSubscriptionDataWithdraw gprsSubscriptionDataWithdraw, boolean roamingRestrictedInSgsnDueToUnsuppportedFeature, LSAInformationWithdraw lsaInformationWithdraw, boolean gmlcListWithdraw, boolean istInformationWithdraw, SpecificCSIWithdraw specificCSIWithdraw, boolean chargingCharacteristicsWithdraw, boolean stnSrWithdraw, EPSSubscriptionDataWithdraw epsSubscriptionDataWithdraw, boolean apnOiReplacementWithdraw, boolean csgSubscriptionDeleted) throws MAPException {
        return null;
    }

    @Override
    public void addDeleteSubscriberDataResponse(long invokeId, RegionalSubscriptionResponse regionalSubscriptionResponse, MAPExtensionContainer extensionContainer) throws MAPException {

    }

    @Override
    public Long addCheckImeiRequest(IMEI imei, RequestedEquipmentInfo requestedEquipmentInfo, MAPExtensionContainer extensionContainer) throws MAPException {
        return null;
    }

    @Override
    public Long addCheckImeiRequest(long customInvokeTimeout, IMEI imei, RequestedEquipmentInfo requestedEquipmentInfo, MAPExtensionContainer extensionContainer) throws MAPException {
        return null;
    }

    @Override
    public void addCheckImeiResponse(long invokeId, EquipmentStatus equipmentStatus, UESBIIu bmuef, MAPExtensionContainer extensionContainer) throws MAPException {

    }

    @Override
    public Long addCheckImeiRequest_Huawei(IMEI imei, RequestedEquipmentInfo requestedEquipmentInfo, MAPExtensionContainer extensionContainer, IMSI imsi) throws MAPException {
        return null;
    }

    @Override
    public Long addCheckImeiRequest_Huawei(long customInvokeTimeout, IMEI imei, RequestedEquipmentInfo requestedEquipmentInfo, MAPExtensionContainer extensionContainer, IMSI imsi) throws MAPException {
        return null;
    }

    @Override
    public Long addActivateTraceModeRequest(IMSI imsi, TraceReference traceReference, TraceType traceType, AddressString omcId, MAPExtensionContainer extensionContainer, TraceReference2 traceReference2, TraceDepthList traceDepthList, TraceNETypeList traceNeTypeList, TraceInterfaceList traceInterfaceList, TraceEventList traceEventList, GSNAddress traceCollectionEntity, MDTConfiguration mdtConfiguration) throws MAPException {
        return null;
    }

    @Override
    public Long addActivateTraceModeRequest(int customInvokeTimeout, IMSI imsi, TraceReference traceReference, TraceType traceType, AddressString omcId, MAPExtensionContainer extensionContainer, TraceReference2 traceReference2, TraceDepthList traceDepthList, TraceNETypeList traceNeTypeList, TraceInterfaceList traceInterfaceList, TraceEventList traceEventList, GSNAddress traceCollectionEntity, MDTConfiguration mdtConfiguration) throws MAPException {
        return null;
    }

    @Override
    public void addActivateTraceModeResponse(long invokeId, MAPExtensionContainer extensionContainer, boolean traceSupportIndicator) throws MAPException {

    }

    @Override
    public Long addForwardShortMessageRequest(SM_RP_DA sm_RP_DA, SM_RP_OA sm_RP_OA, SmsSignalInfo sm_RP_UI, boolean moreMessagesToSend) throws MAPException {
        return null;
    }

    @Override
    public Long addForwardShortMessageRequest(int customInvokeTimeout, SM_RP_DA sm_RP_DA, SM_RP_OA sm_RP_OA, SmsSignalInfo sm_RP_UI, boolean moreMessagesToSend) throws MAPException {
        return null;
    }

    @Override
    public void addForwardShortMessageResponse(long invokeId) throws MAPException {

    }

    @Override
    public Long addMoForwardShortMessageRequest(SM_RP_DA sm_RP_DA, SM_RP_OA sm_RP_OA, SmsSignalInfo sm_RP_UI, MAPExtensionContainer extensionContainer, IMSI imsi) throws MAPException {
        return null;
    }

    @Override
    public Long addMoForwardShortMessageRequest(int customInvokeTimeout, SM_RP_DA sm_RP_DA, SM_RP_OA sm_RP_OA, SmsSignalInfo sm_RP_UI, MAPExtensionContainer extensionContainer, IMSI imsi) throws MAPException {
        return null;
    }

    @Override
    public void addMoForwardShortMessageResponse(long invokeId, SmsSignalInfo sm_RP_UI, MAPExtensionContainer extensionContainer) throws MAPException {

    }

    @Override
    public Long addMtForwardShortMessageRequest(SM_RP_DA sm_RP_DA, SM_RP_OA sm_RP_OA, SmsSignalInfo sm_RP_UI, boolean moreMessagesToSend, MAPExtensionContainer extensionContainer) throws MAPException {
        return null;
    }

    @Override
    public Long addMtForwardShortMessageRequest(int customInvokeTimeout, SM_RP_DA sm_RP_DA, SM_RP_OA sm_RP_OA, SmsSignalInfo sm_RP_UI, boolean moreMessagesToSend, MAPExtensionContainer extensionContainer) throws MAPException {
        return null;
    }

    @Override
    public void addMtForwardShortMessageResponse(long invokeId, SmsSignalInfo sm_RP_UI, MAPExtensionContainer extensionContainer) throws MAPException {

    }

    @Override
    public Long addSendRoutingInfoForSMRequest(ISDNAddressString msisdn, boolean sm_RP_PRI, AddressString serviceCentreAddress, MAPExtensionContainer extensionContainer, boolean gprsSupportIndicator, SM_RP_MTI sM_RP_MTI, SM_RP_SMEA sM_RP_SMEA, TeleserviceCode teleservice) throws MAPException {
        return null;
    }

    @Override
    public Long addSendRoutingInfoForSMRequest(int customInvokeTimeout, ISDNAddressString msisdn, boolean sm_RP_PRI, AddressString serviceCentreAddress, MAPExtensionContainer extensionContainer, boolean gprsSupportIndicator, SM_RP_MTI sM_RP_MTI, SM_RP_SMEA sM_RP_SMEA, TeleserviceCode teleservice) throws MAPException {
        return null;
    }

    @Override
    public void addSendRoutingInfoForSMResponse(long invokeId, IMSI imsi, LocationInfoWithLMSI locationInfoWithLMSI, MAPExtensionContainer extensionContainer, Boolean mwdSet) throws MAPException {

    }

    @Override
    public Long addReportSMDeliveryStatusRequest(ISDNAddressString msisdn, AddressString serviceCentreAddress, SMDeliveryOutcome sMDeliveryOutcome, Integer absentSubscriberDiagnosticSM, MAPExtensionContainer extensionContainer, boolean gprsSupportIndicator, boolean deliveryOutcomeIndicator, SMDeliveryOutcome additionalSMDeliveryOutcome, Integer additionalAbsentSubscriberDiagnosticSM) throws MAPException {
        return null;
    }

    @Override
    public Long addReportSMDeliveryStatusRequest(int customInvokeTimeout, ISDNAddressString msisdn, AddressString serviceCentreAddress, SMDeliveryOutcome sMDeliveryOutcome, Integer absentSubscriberDiagnosticSM, MAPExtensionContainer extensionContainer, boolean gprsSupportIndicator, boolean deliveryOutcomeIndicator, SMDeliveryOutcome additionalSMDeliveryOutcome, Integer additionalAbsentSubscriberDiagnosticSM) throws MAPException {
        return null;
    }

    @Override
    public void addReportSMDeliveryStatusResponse(long invokeId, ISDNAddressString storedMSISDN, MAPExtensionContainer extensionContainer) throws MAPException {

    }

    @Override
    public Long addInformServiceCentreRequest(ISDNAddressString storedMSISDN, MWStatus mwStatus, MAPExtensionContainer extensionContainer, Integer absentSubscriberDiagnosticSM, Integer additionalAbsentSubscriberDiagnosticSM) throws MAPException {
        return null;
    }

    @Override
    public Long addInformServiceCentreRequest(int customInvokeTimeout, ISDNAddressString storedMSISDN, MWStatus mwStatus, MAPExtensionContainer extensionContainer, Integer absentSubscriberDiagnosticSM, Integer additionalAbsentSubscriberDiagnosticSM) throws MAPException {
        return null;
    }

    @Override
    public Long addAlertServiceCentreRequest(ISDNAddressString msisdn, AddressString serviceCentreAddress) throws MAPException {
        return null;
    }

    @Override
    public Long addAlertServiceCentreRequest(int customInvokeTimeout, ISDNAddressString msisdn, AddressString serviceCentreAddress) throws MAPException {
        return null;
    }

    @Override
    public void addAlertServiceCentreResponse(long invokeId) throws MAPException {

    }

    @Override
    public Long addReadyForSMRequest(IMSI imsi, AlertReason alertReason, boolean alertReasonIndicator, MAPExtensionContainer extensionContainer, boolean additionalAlertReasonIndicator) throws MAPException {
        return null;
    }

    @Override
    public Long addReadyForSMRequest(int customInvokeTimeout, IMSI imsi, AlertReason alertReason, boolean alertReasonIndicator, MAPExtensionContainer extensionContainer, boolean additionalAlertReasonIndicator) throws MAPException {
        return null;
    }

    @Override
    public void addReadyForSMResponse(long invokeId, MAPExtensionContainer extensionContainer) throws MAPException {

    }

    @Override
    public Long addNoteSubscriberPresentRequest(IMSI imsi) throws MAPException {
        return null;
    }

    @Override
    public Long addNoteSubscriberPresentRequest(int customInvokeTimeout, IMSI imsi) throws MAPException {
        return null;
    }

    @Override
    public MAPDialogState getState() {
        return null;
    }

    @Override
    public SccpAddress getLocalAddress() {
        return null;
    }

    @Override
    public void setLocalAddress(SccpAddress localAddress) {

    }

    @Override
    public SccpAddress getRemoteAddress() {
        return null;
    }

    @Override
    public void setRemoteAddress(SccpAddress remoteAddress) {

    }

    @Override
    public void setReturnMessageOnError(boolean val) {

    }

    @Override
    public boolean getReturnMessageOnError() {
        return false;
    }

    @Override
    public MessageType getTCAPMessageType() {
        return null;
    }

    @Override
    public AddressString getReceivedOrigReference() {
        return null;
    }

    @Override
    public AddressString getReceivedDestReference() {
        return null;
    }

    @Override
    public MAPExtensionContainer getReceivedExtensionContainer() {
        return null;
    }

    @Override
    public int getNetworkId() {
        return 0;
    }

    @Override
    public void setNetworkId(int networkId) {

    }

    @Override
    public void release() {

    }

    @Override
    public void keepAlive() {

    }

    @Override
    public Long getLocalDialogId() {
        return null;
    }

    @Override
    public Long getRemoteDialogId() {
        return null;
    }

    @Override
    public MAPServiceBase getService() {
        return null;
    }

    @Override
    public void setExtentionContainer(MAPExtensionContainer extContainer) {

    }

    @Override
    public void send() throws MAPException {

    }

    @Override
    public void close(boolean prearrangedEnd) throws MAPException {

    }

    @Override
    public void sendDelayed() throws MAPException {

    }

    @Override
    public void closeDelayed(boolean prearrangedEnd) throws MAPException {

    }

    @Override
    public void abort(MAPUserAbortChoice mapUserAbortChoice) throws MAPException {

    }

    @Override
    public void refuse(Reason reason) throws MAPException {

    }

    @Override
    public void processInvokeWithoutAnswer(Long invokeId) {

    }

    @Override
    public void sendInvokeComponent(Invoke invoke) throws MAPException {

    }

    @Override
    public void sendReturnResultComponent(ReturnResult returnResult) throws MAPException {

    }

    @Override
    public void sendReturnResultLastComponent(ReturnResultLast returnResultLast) throws MAPException {

    }

    @Override
    public void sendErrorComponent(Long invokeId, MAPErrorMessage mapErrorMessage) throws MAPException {

    }

    @Override
    public void sendRejectComponent(Long invokeId, Problem problem) throws MAPException {

    }

    @Override
    public void resetInvokeTimer(Long invokeId) throws MAPException {

    }

    @Override
    public boolean cancelInvocation(Long invokeId) throws MAPException {
        return false;
    }

    @Override
    public Object getUserObject() {
        return null;
    }

    @Override
    public void setUserObject(Object userObject) {

    }

    @Override
    public MAPApplicationContext getApplicationContext() {
        return null;
    }

    @Override
    public int getMaxUserDataLength() {
        return 0;
    }

    @Override
    public int getMessageUserDataLengthOnSend() throws MAPException {
        return 0;
    }

    @Override
    public int getMessageUserDataLengthOnClose(boolean prearrangedEnd) throws MAPException {
        return 0;
    }

    @Override
    public void addEricssonData(IMSI imsi, AddressString vlrNo) {

    }
}
