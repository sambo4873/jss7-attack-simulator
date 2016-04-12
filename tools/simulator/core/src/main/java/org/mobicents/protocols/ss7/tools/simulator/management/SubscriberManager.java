package org.mobicents.protocols.ss7.tools.simulator.management;

import org.apache.log4j.spi.LocationInfo;
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
    private long currentImsi;
    private long currentImei;
    private long currentMsisdn;
    private long currentMsisdnB;

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
        this.currentImsi = 111111110L;
        this.currentImei = 999999990L;
        this.currentMsisdn  = 47111111;
        this.currentMsisdnB = 46111111;
    }

    public void createRandomSubscribers(int number, boolean simple) {
        int numASubscribers;
        int numBSubscribers;

        if(simple) {
            numASubscribers = 1;
            numBSubscribers = 0;
        } else {
            numASubscribers = (int) Math.floor(number * 0.9);
            numBSubscribers = number - numASubscribers;
        }

        for(int i = 0; i < numASubscribers; i++) {
            if(i == 0)
                this.addSubscriber(this.createVIPSubscriber());
            else
                this.addSubscriber(this.createRandomSubscriber(true));
        }

        for(int i = 0; i < numBSubscribers; i++) {
            this.addSubscriber(this.createRandomSubscriber(false));
        }

        System.out.println("SubscriberManager: added " + numASubscribers + " operator A subscribers");
        System.out.println("SubscriberManager: added " + numBSubscribers + " operator B subscribers");
        System.out.println("SubscriberManager: added " + number + " total subscribers");
    }

    private Subscriber createVIPSubscriber() {
        int subscriberId = 0;
        boolean subscriberLocatedInA = true;

        IMSI imsi = this.mapParameterFactory.createIMSI("242" + "01" + currentImsi++);
        ISDNAddressString msisdn = this.mapParameterFactory.createISDNAddressString(AddressNature.international_number, NumberingPlan.ISDN, Long.toString(currentMsisdn++));
        try {
            return new Subscriber(subscriberId, imsi, msisdn, this.createVIPSubscriberInfo(), this.defaultMscNumber, this.defaultVlrNumber, this.defaultHlrNumber, true, false);
        } catch(MAPException ex) {
            System.out.println("Exception when creating Subscriber data: " + ex.toString());
            return null;
        }
    }

    private Subscriber createRandomSubscriber(boolean operatorAHome) {
        int subscriberId;
        if(this.subscribers.size() > 0)
            subscriberId = this.getSubscriber(this.getNumberOfSubscribers() - 1).getSubscriberId() + 1;
        else
            subscriberId = 0;

        boolean subscriberLocatedInA = this.random.nextBoolean();

        IMSI imsi;
        if(operatorAHome)
            imsi = this.mapParameterFactory.createIMSI("242" + "01" + currentImsi++);
        else
            imsi = this.mapParameterFactory.createIMSI("240" + "06" + currentImsi++);

        ISDNAddressString msisdn;

        try {

            Subscriber subscriber;
            if(operatorAHome) {
                msisdn = this.mapParameterFactory.createISDNAddressString(AddressNature.international_number, NumberingPlan.ISDN, Long.toString(currentMsisdn++));
                if(subscriberLocatedInA)
                    subscriber = new Subscriber(subscriberId, imsi, msisdn, this.createRandomSubscriberInfo(operatorAHome, subscriberLocatedInA), this.defaultMscNumber, this.defaultVlrNumber, this.defaultHlrNumber, true, false);
                else
                    subscriber = new Subscriber(subscriberId, imsi, msisdn, this.createRandomSubscriberInfo(operatorAHome, subscriberLocatedInA), this.defaultMscBNumber, this.defaultVlrBNumber, this.defaultHlrNumber, true, false);
            } else {
                msisdn = this.mapParameterFactory.createISDNAddressString(AddressNature.international_number, NumberingPlan.ISDN, Long.toString(currentMsisdnB++));
                if(subscriberLocatedInA)
                    subscriber = new Subscriber(subscriberId, imsi, msisdn, this.createRandomSubscriberInfo(operatorAHome, subscriberLocatedInA), this.defaultMscNumber, this.defaultVlrNumber, this.defaultHlrBNumber, false, false);
                else
                    subscriber = new Subscriber(subscriberId, imsi, msisdn, this.createRandomSubscriberInfo(operatorAHome, subscriberLocatedInA), this.defaultMscBNumber, this.defaultVlrBNumber, this.defaultHlrBNumber, false, false);
            }

            return subscriber;
        } catch(MAPException ex) {
            System.out.println("Exception when creating Subscriber data: " + ex.toString());
            return null;
        }
    }

    private SubscriberInfo createVIPSubscriberInfo() throws MAPException {
        LocationInformation locationInformation = this.createVIPLocationInformation();
        SubscriberState subscriberState = mapParameterFactory.createSubscriberState(SubscriberStateChoice.assumedIdle, null);
        MAPExtensionContainer mapExtensionContainer = null;
        LocationInformationGPRS locationInformationGPRS = null;
        PSSubscriberState psSubscriberState = null;

        IMEI imei = mapParameterFactory.createIMEI("24201" + currentImei++);

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

    private SubscriberInfo createRandomSubscriberInfo(boolean operatorAHome, boolean inA) throws MAPException {
        LocationInformation locationInformation = this.createRandomLocationInformation(operatorAHome, inA);
        SubscriberState subscriberState = mapParameterFactory.createSubscriberState(SubscriberStateChoice.assumedIdle, null);
        MAPExtensionContainer mapExtensionContainer = null;
        LocationInformationGPRS locationInformationGPRS = null;
        PSSubscriberState psSubscriberState = null;

        IMEI imei;
        if(operatorAHome)
            imei = mapParameterFactory.createIMEI("24201" + currentImei++);
        else
            imei = mapParameterFactory.createIMEI("24202" + currentImei++);

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

    private LocationInformation createVIPLocationInformation() throws MAPException {
        int ageOfLocationInformation = 0;
        GeographicalInformation geographicalInformation = null;

        ISDNAddressString vlrNumber = this.defaultVlrNumber;
        String address = this.defaultHlrNumber.getAddress();

        //Defined in ITU-T Rec Q.763
        LocationNumberMap locationNumber = this.mapParameterFactory.createLocationNumberMap(new LocationNumberImpl(
                LocationNumber._NAI_INTERNATIONAL_NUMBER,
                address,
                LocationNumber._NPI_ISDN,
                LocationNumber._INN_ROUTING_ALLOWED,
                LocationNumber._APRI_ALLOWED,
                LocationNumber._SI_USER_PROVIDED_VERIFIED_PASSED));

        CellGlobalIdOrServiceAreaIdOrLAI cgiosaiol = mapParameterFactory.createCellGlobalIdOrServiceAreaIdOrLAI(
                    new CellGlobalIdOrServiceAreaIdFixedLengthImpl(242, 01, AttackSimulationOrganizer.LAC_A_1, AttackSimulationOrganizer.CELLID_A_1));

        MAPExtensionContainer mapExtensionContainer = null;
        LSAIdentity lsaIdentity = null;

        ISDNAddressString mscNumber = this.defaultMscNumber;

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

    public SubscriberInfo createNewSubscriberLocation(Subscriber subscriber, int lac, int cellID) {
        SubscriberInfo oldSubscriberInfo = subscriber.getSubscriberInfo();
        SubscriberState subscriberState = oldSubscriberInfo.getSubscriberState();
        MAPExtensionContainer mapExtensionContainer = null;
        LocationInformationGPRS locationInformationGPRS = null;
        PSSubscriberState psSubscriberState = null;
        IMEI imei = oldSubscriberInfo.getIMEI();
        MSClassmark2 msClassmark2 = null;
        GPRSMSClass gprsmsClass = null;
        MNPInfoRes mnpInfoRes = null;
        int ageOfLocationInformation = 0;
        GeographicalInformation geographicalInformation = null;
        LSAIdentity lsaIdentity = null;
        ISDNAddressString mscNumber = oldSubscriberInfo.getLocationInformation().getMscNumber();
        GeodeticInformation geodeticInformation = null;
        boolean currentLocationRetrieved = true;
        boolean saiPresent = false;
        LocationInformationEPS locationInformationEPS = null;
        UserCSGInformation userCSGInformation = null;

        ISDNAddressString vlrNumber = oldSubscriberInfo.getLocationInformation().getVlrNumber();
        LocationNumberMap locationNumber = oldSubscriberInfo.getLocationInformation().getLocationNumber();

        try {
            CellGlobalIdOrServiceAreaIdOrLAI cgiosaiol = mapParameterFactory.createCellGlobalIdOrServiceAreaIdOrLAI(
                    new CellGlobalIdOrServiceAreaIdFixedLengthImpl(
                            oldSubscriberInfo.getLocationInformation().getCellGlobalIdOrServiceAreaIdOrLAI()
                                    .getCellGlobalIdOrServiceAreaIdFixedLength().getMCC(),
                            oldSubscriberInfo.getLocationInformation().getCellGlobalIdOrServiceAreaIdOrLAI()
                                    .getCellGlobalIdOrServiceAreaIdFixedLength().getMNC(),
                            lac, cellID));

            LocationInformation locationInformation = mapParameterFactory.createLocationInformation(ageOfLocationInformation,
                    geographicalInformation, vlrNumber, locationNumber, cgiosaiol, mapExtensionContainer, lsaIdentity,
                    mscNumber, geodeticInformation, currentLocationRetrieved, saiPresent, locationInformationEPS,
                    userCSGInformation);

            SubscriberInfo subscriberInfo = mapParameterFactory.createSubscriberInfo(locationInformation,
                    subscriberState, mapExtensionContainer, locationInformationGPRS, psSubscriberState,
                    imei, msClassmark2, gprsmsClass, mnpInfoRes);

            return subscriberInfo;
        } catch (MAPException ex) {
            System.out.println("Exception when creating new Subscriber data: " + ex.toString());
            return null;
        }
    }

    private LocationInformation createRandomLocationInformation(boolean operatorAHome, boolean inA) throws MAPException {
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

        int lac = 0,
                cellID = 0,
                rand = random.nextInt(3);

        CellGlobalIdOrServiceAreaIdOrLAI cgiosaiol;
        if(operatorAHome) {
            switch(rand) {
                case 0:
                    if(inA) {
                        lac = AttackSimulationOrganizer.LAC_A_1;
                        cellID = AttackSimulationOrganizer.CELLID_A_1;
                    } else {
                        lac = AttackSimulationOrganizer.LAC_B_1;
                        cellID = AttackSimulationOrganizer.CELLID_B_1;
                    }
                    break;
                case 1:
                    if(inA) {
                        lac = AttackSimulationOrganizer.LAC_A_2;
                        cellID = AttackSimulationOrganizer.CELLID_A_2;
                    } else {
                        lac = AttackSimulationOrganizer.LAC_B_2;
                        cellID = AttackSimulationOrganizer.CELLID_B_2;
                    }
                    break;
                case 2:
                    if(inA) {
                        lac = AttackSimulationOrganizer.LAC_A_3;
                        cellID = AttackSimulationOrganizer.CELLID_A_3;
                    } else {
                        lac = AttackSimulationOrganizer.LAC_B_3;
                        cellID = AttackSimulationOrganizer.CELLID_B_3;
                    }
                    break;
            }
            cgiosaiol = mapParameterFactory.createCellGlobalIdOrServiceAreaIdOrLAI(
                    new CellGlobalIdOrServiceAreaIdFixedLengthImpl(242, 01, lac, cellID));
        } else {
            switch(rand) {
                case 0:
                    if(inA) {
                        lac = AttackSimulationOrganizer.LAC_A_1;
                        cellID = AttackSimulationOrganizer.CELLID_A_1;
                    } else {
                        lac = AttackSimulationOrganizer.LAC_B_1;
                        cellID = AttackSimulationOrganizer.CELLID_B_1;
                    }
                    break;
                case 1:
                    if(inA) {
                        lac = AttackSimulationOrganizer.LAC_A_2;
                        cellID = AttackSimulationOrganizer.CELLID_A_2;
                    } else {
                        lac = AttackSimulationOrganizer.LAC_B_2;
                        cellID = AttackSimulationOrganizer.CELLID_B_2;
                    }
                    break;
                case 2:
                    if(inA) {
                        lac = AttackSimulationOrganizer.LAC_A_3;
                        cellID = AttackSimulationOrganizer.CELLID_A_3;
                    } else {
                        lac = AttackSimulationOrganizer.LAC_B_3;
                        cellID = AttackSimulationOrganizer.CELLID_B_3;
                    }
                    break;
            }
            cgiosaiol = mapParameterFactory.createCellGlobalIdOrServiceAreaIdOrLAI(
                    mapParameterFactory.createCellGlobalIdOrServiceAreaIdFixedLength(242, 02, lac, cellID));
        }

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
        //int subscriberId = random.nextInt(((subscribers.size() - 1) - 1) + 1) + 1;
        int subscriberId = random.nextInt(subscribers.size());
        return this.subscribers.get(subscriberId);
    }

    public Subscriber getVipSubscriber() {
        return this.subscribers.get(0);
    }
}
