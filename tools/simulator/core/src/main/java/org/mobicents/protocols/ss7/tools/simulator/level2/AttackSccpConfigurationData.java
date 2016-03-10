package org.mobicents.protocols.ss7.tools.simulator.level2;

import javolution.xml.XMLFormat;
import org.mobicents.protocols.ss7.indicator.NatureOfAddress;
import org.mobicents.protocols.ss7.indicator.NumberingPlan;
import org.mobicents.protocols.ss7.sccp.SccpProtocolVersion;

import javax.xml.stream.XMLStreamException;

/**
 * @author Kristoffer Jensen
 */
public class AttackSccpConfigurationData extends SccpConfigurationData {

    protected static final String REMOTE_ON_GT_MODE = "routeOnGtMode";
    protected static final String REMOTE_SPC = "remoteSpc";
    protected static final String LOCAL_SPC = "localSpc";
    protected static final String NI = "ni";
    protected static final String REMOTE_SSN = "remoteSsn";
    protected static final String LOCAL_SSN = "localSsn";
    protected static final String GLOBAL_TITLE_TYPE = "globalTitleType";
    protected static final String ADDRESS_NATURE = "addressNature";
    protected static final String NUMBERING_PLAN = "numberingPlan";
    protected static final String TRANSLATION_TYTE = "translationType";
    protected static final String CALLING_PARTY_ADDRESS_DIGITS = "callingPartyAddressDigits";
    protected static final String SCCP_PROTOCOL_VERSION = "sccpProtocolVersion";

    private boolean routeOnGtMode;
    private int remoteSpc = 0;
    private int remoteSpc2 = 0;
    private int localSpc = 0;
    private int localSpc2 = 0;
    private int localSsn;
    private int remoteSsn;
    private int ni = 0;
    private GlobalTitleType globalTitleType = new GlobalTitleType(GlobalTitleType.VAL_TT_NP_ES_NOA);
    private NatureOfAddress natureOfAddress = NatureOfAddress.INTERNATIONAL;
    private NumberingPlan numberingPlan = NumberingPlan.ISDN_MOBILE;
    private int translationType = 0;
    private String callingPartyAddressDigits = "";
    private SccpProtocolVersion sccpProtocolVersion = SccpProtocolVersion.ITU;

    public boolean isRouteOnGtMode() {
        return routeOnGtMode;
    }

    public void setRouteOnGtMode(boolean routeOnGtMode) {
        this.routeOnGtMode = routeOnGtMode;
    }

    public int getRemoteSpc() {
        return remoteSpc;
    }

    public void setRemoteSpc(int remoteSpc) {
        this.remoteSpc = remoteSpc;
    }

    public int getRemoteSpc2() {
        return remoteSpc2;
    }

    public void setRemoteSpc2(int remoteSpc2) {
        this.remoteSpc2 = remoteSpc2;
    }

    public int getLocalSpc() {
        return localSpc;
    }

    public void setLocalSpc(int localSpc) {
        this.localSpc = localSpc;
    }

    public int getLocalSpc2() {
        return localSpc2;
    }

    public void setLocalSpc2(int localSpc2) {
        this.localSpc2 = localSpc2;
    }

    public int getLocalSsn() {
        return localSsn;
    }

    public void setLocalSsn(int localSsn) {
        this.localSsn = localSsn;
    }

    public int getRemoteSsn() {
        return remoteSsn;
    }

    public void setRemoteSsn(int remoteSsn) {
        this.remoteSsn = remoteSsn;
    }

    public int getNi() {
        return ni;
    }

    public void setNi(int ni) {
        this.ni = ni;
    }

    public GlobalTitleType getGlobalTitleType() {
        return globalTitleType;
    }

    public void setGlobalTitleType(GlobalTitleType globalTitleType) {
        this.globalTitleType = globalTitleType;
    }

    public NatureOfAddress getNatureOfAddress() {
        return natureOfAddress;
    }

    public void setNatureOfAddress(NatureOfAddress natureOfAddress) {
        this.natureOfAddress = natureOfAddress;
    }

    public NumberingPlan getNumberingPlan() {
        return numberingPlan;
    }

    public void setNumberingPlan(NumberingPlan numberingPlan) {
        this.numberingPlan = numberingPlan;
    }

    public int getTranslationType() {
        return translationType;
    }

    public void setTranslationType(int translationType) {
        this.translationType = translationType;
    }

    public String getCallingPartyAddressDigits() {
        return callingPartyAddressDigits;
    }

    public void setCallingPartyAddressDigits(String callingPartyAddressDigits) {
        this.callingPartyAddressDigits = callingPartyAddressDigits;
    }

    public SccpProtocolVersion getSccpProtocolVersion() {
        return sccpProtocolVersion;
    }

    public void setSccpProtocolVersion(SccpProtocolVersion sccpProtocolVersion) {
        this.sccpProtocolVersion = sccpProtocolVersion;
    }

}
