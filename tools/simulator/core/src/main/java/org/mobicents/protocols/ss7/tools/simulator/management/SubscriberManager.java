package org.mobicents.protocols.ss7.tools.simulator.management;

import org.mobicents.protocols.ss7.isup.impl.message.parameter.LocationNumberImpl;
import org.mobicents.protocols.ss7.isup.message.parameter.LocationNumber;
import org.mobicents.protocols.ss7.map.MAPParameterFactoryImpl;
import org.mobicents.protocols.ss7.map.MAPProviderImpl;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.MAPParameterFactory;
import org.mobicents.protocols.ss7.map.api.primitives.*;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.*;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.LSAIdentity;
import org.mobicents.protocols.ss7.map.primitives.CellGlobalIdOrServiceAreaIdFixedLengthImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Kristoffer Jensen
 */
public class SubscriberManager {
    private MAPParameterFactory mapParameterFactory;

    private Random random;
    private List<Subscriber> subscribers;

    public SubscriberManager() {
        this.random = new Random(System.currentTimeMillis());
        this.subscribers = new ArrayList<Subscriber>();
        this.mapParameterFactory = new MAPParameterFactoryImpl();
    }

    public void createRandomSubscribers(int number) {
        for(int i = 0; i < number; i++) {
            this.addSubscriber(this.createRandomSubscriber());
        }
    }

    private Subscriber createRandomSubscriber() {
        int subscriberId = this.getSubscriber(this.getNumberOfSubscribers() - 1).getSubscriberId() + 1;
        IMSI imsi = this.mapParameterFactory.createIMSI(randomNumericalString(15));
        ISDNAddressString msisdn = this.mapParameterFactory.createISDNAddressString(AddressNature.international_number, NumberingPlan.ISDN, randomNumericalString(10));
        try {
            return new Subscriber(subscriberId, imsi, msisdn, this.createRandomSubscriberInfo());
        } catch(MAPException ex) {
            System.out.println("Exception when creating Subscriber data: " + ex.toString());
            return null;
        }
    }

    private SubscriberInfo createRandomSubscriberInfo() throws MAPException {
        LocationInformation locationInformation = this.createRandomLocationInformation();
        SubscriberState subscriberState = mapParameterFactory.createSubscriberState(SubscriberStateChoice.assumedIdle, null);
        MAPExtensionContainer mapExtensionContainer = null;
        LocationInformationGPRS locationInformationGPRS = null;
        PSSubscriberState psSubscriberState = null;
        IMEI imei = mapParameterFactory.createIMEI("24201" + randomNumericalString(10));
        MSClassmark2 msClassmark2 = null;
        GPRSMSClass gprsmsClass = null;
        MNPInfoRes mnpInfoRes = null;

        SubscriberInfo subscriberInfo = mapParameterFactory.createSubscriberInfo(locationInformation,
                subscriberState,
                mapExtensionContainer,
                locationInformationGPRS,
                psSubscriberState,
                imei,
                msClassmark2,
                gprsmsClass,
                mnpInfoRes);

        return subscriberInfo;
    }


    private LocationInformation createRandomLocationInformation() throws MAPException {
        int ageOfLocationInformation = 0;
        GeographicalInformation geographicalInformation = null;
        ISDNAddressString vlrNumber = this.mapParameterFactory.createISDNAddressString(AddressNature.international_number, NumberingPlan.ISDN, "22222222");
        LocationNumberMap locationNumber = this.mapParameterFactory.createLocationNumberMap(new LocationNumberImpl(4,
                "88888888",
                LocationNumber._NPI_ISDN,
                LocationNumber._INN_ROUTING_ALLOWED,
                LocationNumber._APRI_ALLOWED,
                LocationNumber._SI_USER_PROVIDED_VERIFIED_PASSED));
        CellGlobalIdOrServiceAreaIdOrLAI cgiosaiol = mapParameterFactory.createCellGlobalIdOrServiceAreaIdOrLAI(
                new CellGlobalIdOrServiceAreaIdFixedLengthImpl(242,01, 115, 8462));
        MAPExtensionContainer mapExtensionContainer = null;
        LSAIdentity lsaIdentity = null;
        ISDNAddressString mscNumber = null;
        GeodeticInformation geodeticInformation = null;
        boolean currentLocationRetrieved = true;
        boolean saiPresent = false;
        LocationInformationEPS locationInformationEPS = null;
        UserCSGInformation userCSGInformation = null;

        return mapParameterFactory.createLocationInformation(ageOfLocationInformation, geographicalInformation,
                vlrNumber, locationNumber, cgiosaiol, mapExtensionContainer,lsaIdentity,
                mscNumber, geodeticInformation, currentLocationRetrieved, saiPresent, locationInformationEPS,
                userCSGInformation);
    }


    public void addSubscriber(Subscriber subscriber) {
        this.subscribers.add(subscriber);
    }

    public Subscriber getSubscriber(int subscriberId) {
        return this.subscribers.get(subscriberId);
    }

    public Subscriber getSubscriber(IMSI imsi) {
        Subscriber returnSubscriber = null;

        for(Subscriber sub : subscribers)
            if(sub.getImsi().getData().equals(imsi.getData()))
                returnSubscriber = sub;

        return returnSubscriber;
    }

    public int getNumberOfSubscribers() {
        return this.subscribers.size();
    }

    private String randomNumericalString(int length) {
        char[] buffer = new char[length];
        char[] symbols;

        StringBuilder sb = new StringBuilder();
        for(char ch = '0'; ch <= '9'; ch++)
            sb.append(ch);
        symbols = sb.toString().toCharArray();

        for(int i = 0; i < buffer.length; i++)
            buffer[i] = symbols[random.nextInt(symbols.length)];

        return new String(buffer);
    }

    public Subscriber getRandomSubscriber() {
        return this.subscribers.get(random.nextInt(subscribers.size()));
    }
}
