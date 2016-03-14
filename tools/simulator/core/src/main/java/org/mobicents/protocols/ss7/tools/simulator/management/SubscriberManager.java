package org.mobicents.protocols.ss7.tools.simulator.management;

import org.mobicents.protocols.ss7.isup.impl.message.parameter.LocationNumberImpl;
import org.mobicents.protocols.ss7.isup.message.parameter.LocationNumber;
import org.mobicents.protocols.ss7.map.MAPParameterFactoryImpl;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.mobicents.protocols.ss7.map.api.MAPParameterFactory;
import org.mobicents.protocols.ss7.map.api.primitives.*;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.*;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberManagement.LSAIdentity;
import org.mobicents.protocols.ss7.map.primitives.CellGlobalIdOrServiceAreaIdFixedLengthImpl;
import org.mobicents.protocols.ss7.tools.simulator.AttackSimulationOrganizer;

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
    private ISDNAddressString defaultMscNumber;
    private ISDNAddressString defaultMscBNumber;
    private ISDNAddressString defaultVlrNumber;
    private ISDNAddressString defaultVlrBNumber;
    private ISDNAddressString defaultHlrNumber;
    private ISDNAddressString defaultHlrBNumber;

    public SubscriberManager(ISDNAddressString defaultMscNumber, ISDNAddressString defaultMscBNumber,
                             ISDNAddressString defaultVlrNumber, ISDNAddressString defaultVlrBNumber,
                             ISDNAddressString defaultHlrNumber, ISDNAddressString defaultHlrBNumber) {
        this.random = new Random(System.currentTimeMillis());
        this.subscribers = new ArrayList<Subscriber>();
        this.mapParameterFactory = new MAPParameterFactoryImpl();
        this.defaultMscNumber = defaultMscNumber;
        this.defaultMscBNumber = defaultMscBNumber;
        this.defaultVlrNumber = defaultVlrNumber;
        this.defaultVlrBNumber = defaultVlrBNumber;
        this.defaultHlrNumber = defaultHlrNumber;
        this.defaultHlrBNumber = defaultHlrBNumber;
    }

    public void createRandomSubscribers(int number) {
        for(int i = 0; i < number; i++) {
            this.addSubscriber(this.createRandomSubscriber());
        }
        System.out.println("SubscriberManager: added " + number + " number of random subscribers");
    }

    private Subscriber createRandomSubscriber() {
        int subscriberId;
        if(this.subscribers.size() > 0)
            subscriberId = this.getSubscriber(this.getNumberOfSubscribers() - 1).getSubscriberId() + 1;
        else
            subscriberId = 0;
        boolean operatorAHome = this.random.nextBoolean(),
                subscriberLocatedInA = this.random.nextBoolean();

        IMSI imsi;
        if(operatorAHome)
            imsi = this.mapParameterFactory.createIMSI("242" + "01" + generateRandomNumericalString(10));
        else
            imsi = this.mapParameterFactory.createIMSI("242" + "02" + generateRandomNumericalString(10));

        ISDNAddressString msisdn = this.mapParameterFactory.createISDNAddressString(AddressNature.international_number, NumberingPlan.ISDN, generateRandomNumericalString(10));

        try {

            Subscriber subscriber;
            if(operatorAHome) {
                if(subscriberLocatedInA)
                    subscriber = new Subscriber(subscriberId, imsi, msisdn, this.createRandomSubscriberInfo(operatorAHome), this.defaultMscNumber, this.defaultVlrNumber, this.defaultHlrNumber, true);
                else
                    subscriber = new Subscriber(subscriberId, imsi, msisdn, this.createRandomSubscriberInfo(operatorAHome), this.defaultMscBNumber, this.defaultVlrBNumber, this.defaultHlrNumber, true);
            } else {
                if(subscriberLocatedInA)
                    subscriber = new Subscriber(subscriberId, imsi, msisdn, this.createRandomSubscriberInfo(operatorAHome), this.defaultMscNumber, this.defaultVlrNumber, this.defaultHlrBNumber, false);
                else
                    subscriber = new Subscriber(subscriberId, imsi, msisdn, this.createRandomSubscriberInfo(operatorAHome), this.defaultMscBNumber, this.defaultVlrBNumber, this.defaultHlrBNumber, false);
            }

            return subscriber;
        } catch(MAPException ex) {
            System.out.println("Exception when creating Subscriber data: " + ex.toString());
            return null;
        }
    }

    private SubscriberInfo createRandomSubscriberInfo(boolean operatorAHome) throws MAPException {
        LocationInformation locationInformation = this.createRandomLocationInformation(operatorAHome);
        SubscriberState subscriberState = mapParameterFactory.createSubscriberState(SubscriberStateChoice.assumedIdle, null);
        MAPExtensionContainer mapExtensionContainer = null;
        LocationInformationGPRS locationInformationGPRS = null;
        PSSubscriberState psSubscriberState = null;

        IMEI imei;
        if(operatorAHome)
            imei = mapParameterFactory.createIMEI("24201" + generateRandomNumericalString(10));
        else
            imei = mapParameterFactory.createIMEI("24202" + generateRandomNumericalString(10));

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


    private LocationInformation createRandomLocationInformation(boolean operatorAHome) throws MAPException {
        int ageOfLocationInformation = 0;
        GeographicalInformation geographicalInformation = null;

        ISDNAddressString vlrNumber;
        if (operatorAHome)
            vlrNumber = this.defaultVlrNumber;
        else
            vlrNumber = this.defaultVlrBNumber;

        String address;
        if (operatorAHome)
            address = this.defaultHlrNumber.getAddress();
        else
            address = this.defaultHlrBNumber.getAddress();

        //Defined in ITU-T Rec Q.763
        LocationNumberMap locationNumber = this.mapParameterFactory.createLocationNumberMap(new LocationNumberImpl(
                LocationNumber._NAI_INTERNATIONAL_NUMBER,
                address,
                LocationNumber._NPI_ISDN,
                LocationNumber._INN_ROUTING_ALLOWED,
                LocationNumber._APRI_ALLOWED,
                LocationNumber._SI_USER_PROVIDED_VERIFIED_PASSED));

        CellGlobalIdOrServiceAreaIdOrLAI cgiosaiol;
        if(operatorAHome)
            cgiosaiol = mapParameterFactory.createCellGlobalIdOrServiceAreaIdOrLAI(
                    new CellGlobalIdOrServiceAreaIdFixedLengthImpl(242, 01, 115, 8462));
        else
            cgiosaiol = mapParameterFactory.createCellGlobalIdOrServiceAreaIdOrLAI(
                    mapParameterFactory.createCellGlobalIdOrServiceAreaIdFixedLength(242, 02, 116, 8742));

        MAPExtensionContainer mapExtensionContainer = null;
        LSAIdentity lsaIdentity = null;

        ISDNAddressString mscNumber;
        if(operatorAHome)
            mscNumber = this.defaultMscNumber;
        else
            mscNumber = this.defaultMscBNumber;

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
            if(sub.getImsi().equals(imsi))
                returnSubscriber = sub;

        return returnSubscriber;
    }

    public Subscriber getSubscriber(ISDNAddressString msisdn) {
        Subscriber returnSubscriber = null;

        for(Subscriber sub : this.subscribers)
            if(sub.getMsisdn().equals(msisdn))
                returnSubscriber = sub;

        return returnSubscriber;
    }

    public Subscriber getSubscriber(String msisdn) {
        Subscriber returnSubscriber = null;

        for(Subscriber sub : this.subscribers)
            if(sub.getMsisdn().getAddress().equals(msisdn))
                returnSubscriber = sub;

        return returnSubscriber;
    }

    public int getNumberOfSubscribers() {
        return this.subscribers.size();
    }

    private String generateRandomNumericalString(int length) {
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
