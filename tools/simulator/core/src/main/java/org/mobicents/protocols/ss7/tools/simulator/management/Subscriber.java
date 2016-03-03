package org.mobicents.protocols.ss7.tools.simulator.management;

import org.mobicents.protocols.ss7.map.api.primitives.IMEI;
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

    public Subscriber(int subscriberId, IMSI imsi, ISDNAddressString msisdn, SubscriberInfo subscriberInfo) {
        this.subscriberId = subscriberId;
        this.imsi = imsi;
        this.msisdn = msisdn;
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
}
