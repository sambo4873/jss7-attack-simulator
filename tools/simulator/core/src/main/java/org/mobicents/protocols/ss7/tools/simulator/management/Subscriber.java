package org.mobicents.protocols.ss7.tools.simulator.management;

import org.mobicents.protocols.ss7.map.api.primitives.IMSI;
import org.mobicents.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.SubscriberInfo;

/**
 * @author Kristoffer Jensen
 */
public class Subscriber {

    int subscriberId;
    private IMSI imsi;
    private ISDNAddressString msisdn;
    private SubscriberInfo subscriberInfo;
    private ISDNAddressString currentMscNumber;
    private ISDNAddressString currentVlrNumber;
    private ISDNAddressString currentHlrNumber;
    private boolean operatorAHome;
    private boolean vip;

    public Subscriber(int subscriberId, IMSI imsi, ISDNAddressString msisdn, SubscriberInfo subscriberInfo,
                      ISDNAddressString currentMscNumber, ISDNAddressString currentVlrNumber,
                      ISDNAddressString currentHlrNumber, boolean operatorAHome, boolean vip) {
        this.subscriberId = subscriberId;
        this.imsi = imsi;
        this.msisdn = msisdn;
        this.subscriberInfo = subscriberInfo;
        this.currentMscNumber = currentMscNumber;
        this.currentVlrNumber = currentVlrNumber;
        this.currentHlrNumber = currentHlrNumber;
        this.operatorAHome = operatorAHome;
        this.vip = vip;
    }

    public void setVIP(boolean vip) {
        this.vip = vip;
    }

    public boolean isVIP() {
        return this.vip;
    }

    public boolean isOperatorAHome() {
        return operatorAHome;
    }

    public void setOperatorAHome(boolean operatorAHome) {
        this.operatorAHome = operatorAHome;
    }

    public SubscriberInfo getSubscriberInfo() {
        return subscriberInfo;
    }

    public void setSubscriberInfo(SubscriberInfo subscriberInfo) {
        this.subscriberInfo = subscriberInfo;
    }

    public int getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
    }

    public IMSI getImsi() {
        return imsi;
    }

    public void setImsi(IMSI imsi) {
        this.imsi = imsi;
    }

    public ISDNAddressString getMsisdn() {
        return msisdn;
    }

    public void setMsisdn(ISDNAddressString msisdn) {
        this.msisdn = msisdn;
    }

    public ISDNAddressString getCurrentMscNumber() {
        return currentMscNumber;
    }

    public void setCurrentMscNumber(ISDNAddressString currentMscNumber) {
        this.currentMscNumber = currentMscNumber;
    }

    public ISDNAddressString getCurrentVlrNumber() {
        return currentVlrNumber;
    }

    public void setCurrentVlrNumber(ISDNAddressString currentVlrNumber) {
        this.currentVlrNumber = currentVlrNumber;
    }

    public ISDNAddressString getCurrentHlrNumber() {
        return currentHlrNumber;
    }

    public void setCurrentHlrNumber(ISDNAddressString currentHlrNumber) {
        this.currentHlrNumber = currentHlrNumber;
    }
}
