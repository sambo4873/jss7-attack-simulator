package org.mobicents.protocols.ss7.tools.simulator.tests.attack;

import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan;
import org.mobicents.protocols.ss7.map.api.smstpdu.NumberingPlanIdentification;
import org.mobicents.protocols.ss7.map.api.smstpdu.TypeOfNumber;
import org.mobicents.protocols.ss7.tools.simulator.level3.MapProtocolVersion;
import org.mobicents.protocols.ss7.tools.simulator.tests.sms.*;

/**
 * @author Kristoffer Jensen
 */
public class TestAttackClientConfigurationData {
    protected static final String ADDRESS_NATURE = "addressNature";
    protected static final String NUMBERING_PLAN = "numberingPlan";
    protected static final String SERVICE_CENTER_ADDRESS = "serviceCenterAddress";
    protected static final String MAP_PROTOCOL_VERSION = "mapProtocolVersion";
    protected static final String SRI_RESPONSE_IMSI = "sriResponseImsi";
    protected static final String SRI_RESPONSE_VLR = "sriResponseVlr";
    protected static final String SMSC_SSN = "smscSsn";
    protected static final String NATIONAL_LANGUAGE_CODE = "nationalLanguageCode";
    protected static final String TYPE_OF_NUMBER = "typeOfNumber";
    protected static final String NUMBERING_PLAN_IDENTIFICATION = "numberingPlanIdentification";
    protected static final String SMS_CODING_TYPE = "smsCodingType";
    protected static final String SRI_REACTION = "sriReaction";
    protected static final String SRI_INFORM_SERVICE_CENTER = "sriInformServiceCenter";
    protected static final String SRI_SC_ADDRESS_NOT_INCLUDED = "sriScAddressNotIncluded";
    protected static final String MT_FSM_REACTION = "mtFSMReaction";
    protected static final String ESM_DEL_STAT = "esmDelStat";
    protected static final String ONE_NOTIFICATION_FOR_100_DIALOGS = "oneNotificationFor100Dialogs";
    protected static final String RETURN_20_PERS_DELIVERY_ERRORS = "return20PersDeliveryErrors";
    protected static final String CONTINUE_DIALOG = "continueDialog";

    protected AddressNature addressNature = AddressNature.international_number;
    protected NumberingPlan numberingPlan = NumberingPlan.ISDN;
    protected String serviceCenterAddress = "";
    protected MapProtocolVersion mapProtocolVersion = new MapProtocolVersion(MapProtocolVersion.VAL_MAP_V3);
    protected String sriResponseImsi = "";
    protected String sriResponseVlr = "";
    protected int smscSsn = 8;
    protected TypeOfNumber typeOfNumber = TypeOfNumber.InternationalNumber;
    protected NumberingPlanIdentification numberingPlanIdentification = NumberingPlanIdentification.ISDNTelephoneNumberingPlan;
    protected SmsCodingType smsCodingType = new SmsCodingType(SmsCodingType.VAL_GSM7);
    protected int nationalLanguageCode = 0;

    protected SRIReaction sriReaction = new SRIReaction(SRIReaction.VAL_RETURN_SUCCESS);
    protected SRIInformServiceCenter sriInformServiceCenter = new SRIInformServiceCenter(SRIInformServiceCenter.MWD_NO);
    protected boolean sriScAddressNotIncluded = false;
    protected MtFSMReaction mtFSMReaction = new MtFSMReaction(MtFSMReaction.VAL_RETURN_SUCCESS);
    protected ReportSMDeliveryStatusReaction reportSMDeliveryStatusReaction = new ReportSMDeliveryStatusReaction(
            ReportSMDeliveryStatusReaction.VAL_RETURN_SUCCESS);
    protected boolean oneNotificationFor100Dialogs = false;
    protected boolean return20PersDeliveryErrors = false;
    protected boolean continueDialog = false;

    public AddressNature getAddressNature() {
        return addressNature;
    }

    public void setAddressNature(AddressNature addressNature) {
        this.addressNature = addressNature;
    }

    public NumberingPlan getNumberingPlan() {
        return numberingPlan;
    }

    public void setNumberingPlan(NumberingPlan numberingPlan) {
        this.numberingPlan = numberingPlan;
    }

    public String getServiceCenterAddress() {
        return serviceCenterAddress;
    }

    public void setServiceCenterAddress(String serviceCenterAddress) {
        this.serviceCenterAddress = serviceCenterAddress;
    }

    public MapProtocolVersion getMapProtocolVersion() {
        return mapProtocolVersion;
    }

    public void setMapProtocolVersion(MapProtocolVersion mapProtocolVersion) {
        this.mapProtocolVersion = mapProtocolVersion;
    }

    public SRIReaction getSRIReaction() {
        return sriReaction;
    }

    public void setSRIReaction(SRIReaction val) {
        sriReaction = val;
    }

    public SRIInformServiceCenter getSRIInformServiceCenter() {
        return sriInformServiceCenter;
    }

    public void setSRIInformServiceCenter(SRIInformServiceCenter val) {
        sriInformServiceCenter = val;
    }

    public boolean isSRIScAddressNotIncluded() {
        return sriScAddressNotIncluded;
    }

    public void setSRIScAddressNotIncluded(boolean val) {
        sriScAddressNotIncluded = val;
    }

    public MtFSMReaction getMtFSMReaction() {
        return mtFSMReaction;
    }

    public void setMtFSMReaction(MtFSMReaction val) {
        mtFSMReaction = val;
    }

    public ReportSMDeliveryStatusReaction getReportSMDeliveryStatusReaction() {
        return reportSMDeliveryStatusReaction;
    }

    public void setReportSMDeliveryStatusReaction(ReportSMDeliveryStatusReaction val) {
        reportSMDeliveryStatusReaction = val;
    }

    public String getSriResponseImsi() {
        return sriResponseImsi;
    }

    public void setSriResponseImsi(String sriResponseImsi) {
        this.sriResponseImsi = sriResponseImsi;
    }

    public String getSriResponseVlr() {
        return sriResponseVlr;
    }

    public void setSriResponseVlr(String sriResponseVlr) {
        this.sriResponseVlr = sriResponseVlr;
    }

    public int getSmscSsn() {
        return smscSsn;
    }

    public void setSmscSsn(int smscSsn) {
        this.smscSsn = smscSsn;
    }

    public int getNationalLanguageCode() {
        return nationalLanguageCode;
    }

    public void setNationalLanguageCode(int nationalLanguageCode) {
        this.nationalLanguageCode = nationalLanguageCode;
    }

    public TypeOfNumber getTypeOfNumber() {
        return typeOfNumber;
    }

    public void setTypeOfNumber(TypeOfNumber typeOfNumber) {
        this.typeOfNumber = typeOfNumber;
    }

    public NumberingPlanIdentification getNumberingPlanIdentification() {
        return numberingPlanIdentification;
    }

    public void setNumberingPlanIdentification(NumberingPlanIdentification numberingPlanIdentification) {
        this.numberingPlanIdentification = numberingPlanIdentification;
    }

    public SmsCodingType getSmsCodingType() {
        return smsCodingType;
    }

    public void setSmsCodingType(SmsCodingType smsCodingType) {
        this.smsCodingType = smsCodingType;
    }

    public boolean isOneNotificationFor100Dialogs() {
        return oneNotificationFor100Dialogs;
    }

    public void setOneNotificationFor100Dialogs(boolean oneNotificationFor100Dialogs) {
        this.oneNotificationFor100Dialogs = oneNotificationFor100Dialogs;
    }

    public boolean isReturn20PersDeliveryErrors() {
        return return20PersDeliveryErrors;
    }

    public void setReturn20PersDeliveryErrors(boolean val) {
        this.return20PersDeliveryErrors = val;
    }

    public boolean isContinueDialog() {
        return this.continueDialog;
    }

    public void setContinueDialog(boolean val) {
        this.continueDialog = val;
    }
}
