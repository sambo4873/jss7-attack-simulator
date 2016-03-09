package org.mobicents.protocols.ss7.tools.simulator;

import org.mobicents.protocols.ss7.map.MAPParameterFactoryImpl;
import org.mobicents.protocols.ss7.map.api.MAPParameterFactory;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.primitives.IMSI;
import org.mobicents.protocols.ss7.map.api.primitives.ISDNAddressString;
import org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.ProvideSubscriberInfoResponse;
import org.mobicents.protocols.ss7.map.api.service.sms.SendRoutingInfoForSMResponse;
import org.mobicents.protocols.ss7.tools.simulator.common.AttackConfigurationData;
import org.mobicents.protocols.ss7.tools.simulator.management.AttackTesterHost;
import org.mobicents.protocols.ss7.tools.simulator.management.Subscriber;
import org.mobicents.protocols.ss7.tools.simulator.management.SubscriberManager;

import java.util.Random;

/**
 * @author Kristoffer Jensen
 */
public class AttackSimulationOrganizer implements Stoppable {
    private Random random;
    private boolean simpleSimulation;

    private SimpleAttackGoal simpleAttackGoal;

    private static int chanceOfAttack;
    private static int numberOfSubscribers;

    private static long countGenuine = 0;
    private static long countAttack = 0;

    private ISDNAddressString defaultMscAddress;
    private ISDNAddressString defaultHlrAddress;
    private ISDNAddressString defaultSmscAddress;
    private ISDNAddressString defaultVlrAddress;

    private SubscriberManager subscriberManager;

    private AttackTesterHost mscAmscB;
    private AttackTesterHost mscBmscA;

    private AttackTesterHost mscAhlrA;
    private AttackTesterHost hlrAmscA;

    private AttackTesterHost mscAsmscA;
    private AttackTesterHost smscAmscA;

    private AttackTesterHost mscAvlrA;
    private AttackTesterHost vlrAmscA;

    private AttackTesterHost smscAhlrA;
    private AttackTesterHost hlrAsmscA;

    private AttackTesterHost hlrAvlrA;
    private AttackTesterHost vlrAhlrA;

    private AttackTesterHost sgsnAhlrA;
    private AttackTesterHost hlrAsgsnA;

    private AttackTesterHost gsmscfAhlrA;
    private AttackTesterHost hlrAgsmscfA;

    private AttackTesterHost gsmscfAvlrA;
    private AttackTesterHost vlrAgsmscfA;

    private AttackTesterHost mscBhlrA;
    private AttackTesterHost hlrAmscB;

    private AttackTesterHost mscBsmscA;
    private AttackTesterHost smscAmscB;

    private AttackTesterHost mscBvlrA;
    private AttackTesterHost vlrAmscB;

    private AttackTesterHost hlrBsmscA;
    private AttackTesterHost smscAhlrB;

    private AttackTesterHost hlrBvlrA;
    private AttackTesterHost vlrAhlrB;

    private AttackTesterHost vlrBvlrA;
    private AttackTesterHost vlrAvlrB;

    private AttackTesterHost smscBsmscA;
    private AttackTesterHost smscAsmscB;

    private AttackTesterHost smscBhlrA;
    private AttackTesterHost hlrAsmscB;

    private AttackTesterHost vlrBhlrA;
    private AttackTesterHost hlrAvlrB;

    private AttackTesterHost attackerBmscA;
    private AttackTesterHost mscAattackerB;

    private AttackTesterHost attackerBhlrA;
    private AttackTesterHost hlrAattackerB;

    private AttackTesterHost attackerBsmscA;
    private AttackTesterHost smscAattackerB;

    private AttackTesterHost attackerBvlrA;
    private AttackTesterHost vlrAattackerB;

    private AttackTesterHost isupClient;
    private AttackTesterHost isupServer;

    public static final String LOCALHOST = "127.0.0.1";

    public static final int HLR_SSN = 6;
    public static final int VLR_SSN = 7;
    public static final int MSC_SSN = 8;
    public static final int SMSC_SSN = MSC_SSN;
    public static final int SGSN_SSN = 149;
    public static final int GSMSCF_SSN = 147;
    public static final int ATTACKER_SSN = 8;

    public static final int MSC_A_OPC = 1;
    public static final int HLR_A_OPC = 2;
    public static final int SMSC_A_OPC = 3;
    public static final int VLR_A_OPC = 4;
    public static final int SGSN_A_OPC = 5;
    public static final int GSMSCF_A_OPC = 6;
    public static final int MSC_B_OPC = 11;
    public static final int HLR_B_OPC = 12;
    public static final int SMSC_B_OPC = 13;
    public static final int VLR_B_OPC = 14;
    public static final int ATTACKER_OPC = 11;

    public static final int MSC_A_SPC = MSC_A_OPC;
    public static final int HLR_A_SPC = HLR_A_OPC;
    public static final int SMSC_A_SPC = SMSC_A_OPC;
    public static final int VLR_A_SPC = VLR_A_OPC;
    public static final int SGSN_A_SPC = SGSN_A_OPC;
    public static final int GSMSCF_A_SPC = GSMSCF_A_OPC;
    public static final int MSC_B_SPC = MSC_B_OPC;
    public static final int HLR_B_SPC = HLR_B_OPC;
    public static final int SMSC_B_SPC = SMSC_B_OPC;
    public static final int VLR_B_SPC = VLR_B_OPC;
    public static final int ATTACKER_SPC = ATTACKER_OPC;

    public static final String MSC_A_GT = "1111";
    public static final String HLR_A_GT = "1112";
    public static final String SMSC_A_GT = "1113";
    public static final String VLR_A_GT = "1114";
    public static final String SGSN_A_GT = "1115";
    public static final String GSMSCF_A_GT = "1116";
    public static final String MSC_B_GT = "2221";
    public static final String HLR_B_GT = "2222";
    public static final String SMSC_B_GT = "2223";
    public static final String VLR_B_GT = "2224";
    public static final String ATTACKER_GT = "2221";

    public static final int MSC_A_MSC_B_PORT = 8011;
    public static final int MSC_B_MSC_A_PORT = 8012;

    public static final int MSC_A_HLR_A_PORT = 8013;
    public static final int HLR_A_MSC_A_PORT = 8014;

    public static final int MSC_A_SMSC_A_PORT = 8015;
    public static final int SMSC_A_MSC_A_PORT = 8016;

    public static final int MSC_A_VLR_A_PORT = 8017;
    public static final int VLR_A_MSC_A_PORT = 8018;

    public static final int SMSC_A_HLR_A_PORT = 8019;
    public static final int HLR_A_SMSC_A_PORT = 8020;

    public static final int HLR_A_VLR_A_PORT = 8021;
    public static final int VLR_A_HLR_A_PORT = 8022;

    public static final int SGSN_A_HLR_A_PORT = 8023;
    public static final int HLR_A_SGSN_A_PORT = 8024;

    public static final int GSMSCF_A_HLR_A_PORT = 8025;
    public static final int HLR_A_GSMSCF_A_PORT = 8026;

    public static final int GSMSCF_A_VLR_A_PORT = 8027;
    public static final int VLR_A_GSMSCF_A_PORT = 8028;

    public static final int MSC_B_HLR_A_PORT = 8029;
    public static final int HLR_A_MSC_B_PORT = 8030;

    public static final int MSC_B_SMSC_A_PORT = 8031;
    public static final int SMSC_A_MSC_B_PORT = 8032;

    public static final int MSC_B_VLR_A_PORT = 8033;
    public static final int VLR_A_MSC_B_PORT = 8034;

    public static final int HLR_B_SMSC_A_PORT = 8035;
    public static final int SMSC_A_HLR_B_PORT = 8036;

    public static final int HLR_B_VLR_A_PORT = 8037;
    public static final int VLR_A_HLR_B_PORT = 8038;

    public static final int VLR_B_VLR_A_PORT = 8039;
    public static final int VLR_A_VLR_B_PORT = 8040;

    public static final int SMSC_B_SMSC_A_PORT = 8041;
    public static final int SMSC_A_SMSC_B_PORT = 8042;

    public static final int SMSC_B_HLR_A_PORT = 8043;
    public static final int HLR_A_SMSC_B_PORT = 8044;

    public static final int VLR_B_HLR_A_PORT = 8045;
    public static final int HLR_A_VLR_B_PORT = 8046;

    public static final int ATTACKER_MSC_A_PORT = 8047;
    public static final int MSC_A_ATTACK_PORT = 8048;

    public static final int ATTACKER_HLR_A_PORT = 8049;
    public static final int HLR_A_ATTACKER_PORT = 8050;

    public static final int ATTACKER_SMSC_A_PORT = 8051;
    public static final int SMSC_A_ATTACKER_PORT = 8052;

    public static final int ATTACKER_VLR_A_PORT = 8053;
    public static final int VLR_A_ATTACKER_PORT = 8054;

    public AttackSimulationOrganizer(String simulatorHome, boolean simpleSimulation, String simpleAttackGoal, int numberOfSubscribers, int chanceOfAttack) {
        this.random = new Random(System.currentTimeMillis());
        this.simpleSimulation = simpleSimulation;
        this.numberOfSubscribers = numberOfSubscribers;
        this.chanceOfAttack = chanceOfAttack;

        MAPParameterFactory mapParameterFactory = new MAPParameterFactoryImpl();

        this.defaultMscAddress = mapParameterFactory.createISDNAddressString(
                AddressNature.international_number,
                NumberingPlan.ISDN,
                AttackConfigurationData.MSC_A_NUMBER);
        this.defaultSmscAddress = mapParameterFactory.createISDNAddressString(
                AddressNature.international_number,
                NumberingPlan.ISDN,
                AttackConfigurationData.SMSC_A_NUMBER);
        this.defaultHlrAddress = mapParameterFactory.createISDNAddressString(
                AddressNature.international_number,
                NumberingPlan.ISDN,
                AttackConfigurationData.HLR_A_NUMBER);
        this.defaultVlrAddress = mapParameterFactory.createISDNAddressString(
                AddressNature.international_number,
                NumberingPlan.ISDN,
                AttackConfigurationData.VLR_A_NUMBER);

        this.subscriberManager = new SubscriberManager(defaultMscAddress, defaultVlrAddress, defaultHlrAddress);
        this.subscriberManager.createRandomSubscribers(numberOfSubscribers);

        if (this.simpleSimulation) {
            //this.isupClient = new AttackTesterHost("ISUP_CLIENT", simulatorHome, AttackTesterHost.AttackNode.ISUP_CLIENT, this);
            //this.isupServer = new AttackTesterHost("ISUP_SERVER", simulatorHome, AttackTesterHost.AttackNode.ISUP_SERVER, this);

            switch(simpleAttackGoal) {
                case "location:ati":
                    this.simpleAttackGoal = SimpleAttackGoal.LOCATION_ATI;
                    this.attackerBhlrA = new AttackTesterHost("ATTACKER_B_HLR_A", simulatorHome, AttackTesterHost.AttackNode.ATTACKER_B_HLR_A, this);
                    this.hlrAattackerB = new AttackTesterHost("HLR_A_ATTACKER_B", simulatorHome, AttackTesterHost.AttackNode.HLR_A_ATTACKER_B, this);
                    break;
                case "location:psi":
                    this.simpleAttackGoal = SimpleAttackGoal.LOCATION_PSI;
                    this.attackerBhlrA = new AttackTesterHost("ATTACKER_B_HLR_A", simulatorHome, AttackTesterHost.AttackNode.ATTACKER_B_HLR_A, this);
                    this.hlrAattackerB = new AttackTesterHost("HLR_A_ATTACKER_B", simulatorHome, AttackTesterHost.AttackNode.HLR_A_ATTACKER_B, this);
                    this.attackerBvlrA = new AttackTesterHost("ATTACKER_B_VLR_A", simulatorHome, AttackTesterHost.AttackNode.ATTACKER_B_VLR_A, this);
                    this.vlrAattackerB = new AttackTesterHost("VLR_A_ATTACKER_B", simulatorHome, AttackTesterHost.AttackNode.VLR_A_ATTACKER_B, this);
                    break;
                case "intercept:sms":
                    this.simpleAttackGoal = SimpleAttackGoal.INTERCEPT_SMS;
                    this.attackerBhlrA = new AttackTesterHost("ATTACKER_B_HLR_A", simulatorHome, AttackTesterHost.AttackNode.ATTACKER_B_HLR_A, this);
                    this.hlrAattackerB = new AttackTesterHost("HLR_A_ATTACKER_B", simulatorHome, AttackTesterHost.AttackNode.HLR_A_ATTACKER_B, this);
                    this.smscAhlrA = new AttackTesterHost("SMSC_A_HLR_A", simulatorHome, AttackTesterHost.AttackNode.SMSC_A_HLR_A, this);
                    this.hlrAsmscA = new AttackTesterHost("HLR_A_SMSC_A", simulatorHome, AttackTesterHost.AttackNode.HLR_A_SMSC_A, this);
                    this.mscAsmscA = new AttackTesterHost("MSC_A_SMSC_A", simulatorHome, AttackTesterHost.AttackNode.MSC_A_SMSC_A, this);
                    this.smscAmscA = new AttackTesterHost("SMSC_A_MSC_A", simulatorHome, AttackTesterHost.AttackNode.SMSC_A_MSC_A, this);
                    break;
                case "test:ports":
                    this.simpleAttackGoal = SimpleAttackGoal.TEST_PORTS;
                    this.mscAhlrA = new AttackTesterHost("MSC_A_HLR_A", simulatorHome, AttackTesterHost.AttackNode.MSC_A_HLR_A, this);
                    this.hlrAmscA = new AttackTesterHost("HLR_A_MSC_A", simulatorHome, AttackTesterHost.AttackNode.HLR_A_MSC_A, this);
                    this.smscAhlrA = new AttackTesterHost("SMSC_A_HLR_A", simulatorHome, AttackTesterHost.AttackNode.SMSC_A_HLR_A, this);
                    this.hlrAsmscA = new AttackTesterHost("HLR_A_SMSC_A", simulatorHome, AttackTesterHost.AttackNode.HLR_A_SMSC_A, this);
                    break;

                default:
                    System.out.println("ERROR: Unknown simple attack goal: " + simpleAttackGoal);
                    System.exit(-1);
            }
        } else {
            initateAllNodes(simulatorHome);
        }
    }

    private void initateAllNodes(String simulatorHome) {
        this.mscAmscB = new AttackTesterHost("MSC_A_MSC_B", simulatorHome, AttackTesterHost.AttackNode.MSC_A_MSC_B, this);
        this.mscBmscA = new AttackTesterHost("MSC_B_MSC_A", simulatorHome, AttackTesterHost.AttackNode.MSC_B_MSC_A, this);

        this.mscAhlrA = new AttackTesterHost("MSC_A_HLR_A", simulatorHome, AttackTesterHost.AttackNode.MSC_A_HLR_A, this);
        this.hlrAmscA = new AttackTesterHost("HLR_A_MSC_A", simulatorHome, AttackTesterHost.AttackNode.HLR_A_MSC_A, this);

        this.mscAsmscA = new AttackTesterHost("MSC_A_SMSC_A", simulatorHome, AttackTesterHost.AttackNode.MSC_A_SMSC_A, this);
        this.smscAmscA = new AttackTesterHost("SMSC_A_MSC_A", simulatorHome, AttackTesterHost.AttackNode.SMSC_A_MSC_A, this);

        this.mscAvlrA = new AttackTesterHost("MSC_A_VLR_A", simulatorHome, AttackTesterHost.AttackNode.MSC_A_VLR_A, this);
        this.vlrAmscA = new AttackTesterHost("VLR_A_MSC_A", simulatorHome, AttackTesterHost.AttackNode.VLR_A_MSC_A, this);

        this.smscAhlrA = new AttackTesterHost("SMSC_A_HLR_A", simulatorHome, AttackTesterHost.AttackNode.SMSC_A_HLR_A, this);
        this.hlrAsmscA = new AttackTesterHost("HLR_A_SMSC_A", simulatorHome, AttackTesterHost.AttackNode.HLR_A_SMSC_A, this);

        this.hlrAvlrA = new AttackTesterHost("HLR_A_VLR_A", simulatorHome, AttackTesterHost.AttackNode.HLR_A_VLR_A, this);
        this.vlrAhlrA = new AttackTesterHost("VLR_A_HLR_A", simulatorHome, AttackTesterHost.AttackNode.VLR_A_HLR_A, this);

        this.sgsnAhlrA = new AttackTesterHost("SGSN_A_HLR_A", simulatorHome, AttackTesterHost.AttackNode.SGSN_A_HLR_A, this);
        this.hlrAsgsnA = new AttackTesterHost("HLR_A_SGSN_A", simulatorHome, AttackTesterHost.AttackNode.HLR_A_SGSN_A, this);

        this.gsmscfAhlrA = new AttackTesterHost("GSMSCF_A_HLR_A", simulatorHome, AttackTesterHost.AttackNode.GSMSCF_A_HLR_A, this);
        this.hlrAgsmscfA = new AttackTesterHost("HLR_A_GSMSCF_A", simulatorHome, AttackTesterHost.AttackNode.HLR_A_GSMSCF_A, this);

        this.gsmscfAvlrA = new AttackTesterHost("GSMSCF_A_VLR_A", simulatorHome, AttackTesterHost.AttackNode.GSMSCF_A_VLR_A, this);
        this.vlrAgsmscfA = new AttackTesterHost("VLR_A_GSMSCF_A", simulatorHome, AttackTesterHost.AttackNode.VLR_A_GSMSCF_A, this);

        this.attackerBmscA = new AttackTesterHost("ATTACKER_B_MSC_A", simulatorHome, AttackTesterHost.AttackNode.ATTACKER_B_MSC_A, this);
        this.mscAattackerB = new AttackTesterHost("MSC_A_ATTACKER_B", simulatorHome, AttackTesterHost.AttackNode.MSC_A_ATTACKER_B, this);

        this.attackerBhlrA = new AttackTesterHost("ATTACKER_B_HLR_A", simulatorHome, AttackTesterHost.AttackNode.ATTACKER_B_HLR_A, this);
        this.hlrAattackerB = new AttackTesterHost("HLR_A_ATTACKER_B", simulatorHome, AttackTesterHost.AttackNode.HLR_A_ATTACKER_B, this);

        this.attackerBsmscA = new AttackTesterHost("ATTACKER_B_SMSC_A", simulatorHome, AttackTesterHost.AttackNode.ATTACKER_B_SMSC_A, this);
        this.smscAattackerB = new AttackTesterHost("SMSC_A_ATTACKER_B", simulatorHome, AttackTesterHost.AttackNode.SMSC_A_ATTACKER_B, this);

        this.attackerBvlrA = new AttackTesterHost("ATTACKER_B_VLR_A", simulatorHome, AttackTesterHost.AttackNode.ATTACKER_B_VLR_A, this);
        this.vlrAattackerB = new AttackTesterHost("VLR_A_ATTACKER_B", simulatorHome, AttackTesterHost.AttackNode.VLR_A_ATTACKER_B, this);

        this.smscAsmscB = new AttackTesterHost("SMSC_A_SMSC_B", simulatorHome, AttackTesterHost.AttackNode.SMSC_A_SMSC_B, this);
        this.smscBsmscA = new AttackTesterHost("SMSC_B_SMSC_A", simulatorHome, AttackTesterHost.AttackNode.SMSC_B_SMSC_A, this);

        this.smscAhlrB = new AttackTesterHost("SMSC_A_HLR_B", simulatorHome, AttackTesterHost.AttackNode.SMSC_A_HLR_B, this);
        this.hlrBsmscA = new AttackTesterHost("HLR_B_SMSC_A", simulatorHome, AttackTesterHost.AttackNode.HLR_B_SMSC_A, this);

        this.smscBhlrA = new AttackTesterHost("SMSC_B_HLR_A", simulatorHome, AttackTesterHost.AttackNode.SMSC_B_HLR_A, this);
        this.hlrAsmscB = new AttackTesterHost("HLR_A_SMSC_B", simulatorHome, AttackTesterHost.AttackNode.HLR_A_SMSC_B, this);

        this.mscBhlrA = new AttackTesterHost("MSC_B_HLR_A", simulatorHome, AttackTesterHost.AttackNode.MSC_B_HLR_A, this);
        this.hlrAmscB = new AttackTesterHost("HLR_A_MSC_B", simulatorHome, AttackTesterHost.AttackNode.HLR_A_MSC_B, this);

        this.mscBsmscA = new AttackTesterHost("MSC_B_SMSC_A", simulatorHome, AttackTesterHost.AttackNode.MSC_B_SMSC_A, this);
        this.smscAmscB = new AttackTesterHost("SMSC_A_MSC_B", simulatorHome, AttackTesterHost.AttackNode.SMSC_A_MSC_B, this);

        this.mscBvlrA = new AttackTesterHost("MSC_B_VLR_A", simulatorHome, AttackTesterHost.AttackNode.MSC_B_VLR_A, this);
        this.vlrAmscB = new AttackTesterHost("VLR_A_MSC_B", simulatorHome, AttackTesterHost.AttackNode.VLR_A_MSC_B, this);

        this.hlrBvlrA = new AttackTesterHost("HLR_B_VLR_A", simulatorHome, AttackTesterHost.AttackNode.HLR_B_VLR_A, this);
        this.vlrAhlrB = new AttackTesterHost("VLR_A_HLR_B", simulatorHome, AttackTesterHost.AttackNode.VLR_A_HLR_B, this);

        this.vlrBvlrA = new AttackTesterHost("VLR_B_VLR_A", simulatorHome, AttackTesterHost.AttackNode.VLR_B_VLR_A, this);
        this.vlrAvlrB = new AttackTesterHost("VLR_A_VLR_B", simulatorHome, AttackTesterHost.AttackNode.VLR_A_VLR_B, this);

        this.vlrBhlrA = new AttackTesterHost("VLR_B_HLR_A", simulatorHome, AttackTesterHost.AttackNode.VLR_B_HLR_A, this);
        this.hlrAvlrB = new AttackTesterHost("HLR_A_VLR_B", simulatorHome, AttackTesterHost.AttackNode.HLR_A_VLR_B, this);
    }

    public ISDNAddressString getDefaultMscAddress() {
        return defaultMscAddress;
    }

    public ISDNAddressString getDefaultSmscAddress() {
        return defaultSmscAddress;
    }

    public ISDNAddressString getDefaultHlrAddress() {
        return defaultHlrAddress;
    }

    public ISDNAddressString getDefaultVlrAddress() {
        return defaultVlrAddress;
    }

    public SubscriberManager getSubscriberManager() {
        return subscriberManager;
    }

    private void startAttackSimulationHosts() {
        if (this.simpleSimulation) {
            switch(this.simpleAttackGoal) {
                case LOCATION_ATI:
                    this.attackerBhlrA.start();
                    this.hlrAattackerB.start();
                    break;
                case LOCATION_PSI:
                    this.attackerBhlrA.start();
                    this.hlrAattackerB.start();
                    this.attackerBvlrA.start();
                    this.vlrAattackerB.start();
                    break;
                case INTERCEPT_SMS:
                    this.attackerBhlrA.start();
                    this.hlrAattackerB.start();
                    this.smscAhlrA.start();
                    this.hlrAsmscA.start();
                    this.mscAsmscA.start();
                    this.smscAmscA.start();
                    break;
                case TEST_PORTS:
                    this.mscAhlrA.start();
                    this.hlrAmscA.start();
                    this.smscAhlrA.start();
                    this.hlrAsmscA.start();
                    break;
            }
        } else {
            startAllAttackSimulationHosts();
        }
    }

    private void startAllAttackSimulationHosts() {
        this.mscAmscB.start();
        this.mscBmscA.start();

        this.mscAhlrA.start();
        this.hlrAmscA.start();

        this.mscAsmscA.start();
        this.smscAmscA.start();

        this.mscAvlrA.start();
        this.vlrAmscA.start();

        this.smscAhlrA.start();
        this.hlrAsmscA.start();

        this.hlrAvlrA.start();
        this.vlrAhlrA.start();

        this.sgsnAhlrA.start();
        this.hlrAsgsnA.start();

        this.gsmscfAhlrA.start();
        this.hlrAgsmscfA.start();

        this.gsmscfAvlrA.start();
        this.vlrAgsmscfA.start();

        this.attackerBmscA.start();
        this.mscAattackerB.start();

        this.attackerBhlrA.start();
        this.hlrAattackerB.start();

        this.attackerBsmscA.start();
        this.smscAattackerB.start();

        this.attackerBvlrA.start();
        this.vlrAattackerB.start();

        this.smscAsmscB.start();
        this.smscBsmscA.start();

        this.smscAhlrB.start();
        this.hlrBsmscA.start();

        this.smscBhlrA.start();
        this.hlrAsmscB.start();

        this.mscBhlrA.start();
        this.hlrAmscB.start();

        this.mscBsmscA.start();
        this.smscAmscB.start();

        this.mscBvlrA.start();
        this.vlrAmscB.start();

        this.hlrBvlrA.start();
        this.vlrAhlrB.start();

        this.vlrBvlrA.start();
        this.vlrAvlrB.start();

        this.vlrBhlrA.start();
        this.hlrAvlrB.start();
    }

    private boolean waitForM3UALinks() {
        while (true) {
            try {
                Thread.sleep(50);
                if(!this.simpleSimulation) {
                    if (mscAmscB.getM3uaMan().getState().contains("ACTIVE") &&
                            mscAhlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            mscAsmscA.getM3uaMan().getState().contains("ACTIVE") &&
                            mscAvlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            smscAhlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            hlrAvlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            sgsnAhlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            gsmscfAhlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            gsmscfAvlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            attackerBmscA.getM3uaMan().getState().contains("ACTIVE") &&
                            attackerBhlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            attackerBsmscA.getM3uaMan().getState().contains("ACTIVE") &&
                            attackerBvlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            smscAsmscB.getM3uaMan().getState().contains("ACTIVE") &&
                            smscAhlrB.getM3uaMan().getState().contains("ACTIVE") &&
                            smscBhlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            mscBhlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            mscBsmscA.getM3uaMan().getState().contains("ACTIVE") &&
                            mscBvlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            hlrBvlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            vlrBvlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            vlrBhlrA.getM3uaMan().getState().contains("ACTIVE"))
                        return true;
                } else {
                    switch(this.simpleAttackGoal) {
                        case LOCATION_ATI:
                            if(this.attackerBhlrA.getM3uaMan().getState().contains("ACTIVE"))
                                return true;
                            break;
                        case LOCATION_PSI:
                            if(this.attackerBhlrA.getM3uaMan().getState().contains("ACTIVE") &&
                                    this.attackerBvlrA.getM3uaMan().getState().contains("ACTIVE"))
                                return true;
                            break;
                        case INTERCEPT_SMS:
                            if(this.attackerBhlrA.getM3uaMan().getState().contains("ACTIVE") &&
                                    this.smscAhlrA.getM3uaMan().getState().contains("ACTIVE") &&
                                    this.mscAsmscA.getM3uaMan().getState().contains("ACTIVE"))
                                return true;
                            break;
                        case TEST_PORTS:
                            if (mscAhlrA.getM3uaMan().getState().contains("ACTIVE") &&
                                    smscAhlrA.getM3uaMan().getState().contains("ACTIVE"))
                                return true;
                            break;
                    }
                    //if (this.isupClient.getM3uaMan().getState().contains("ACTIVE") &&
                    //        this.isupServer.getM3uaMan().getState().contains("ACTIVE"))
                    //    return true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private boolean testerHostsNeedQuit() {
        if(simpleSimulation) {
            switch(this.simpleAttackGoal) {
                case LOCATION_ATI:
                    return this.attackerBhlrA.isNeedQuit() || this.hlrAattackerB.isNeedQuit();
                case LOCATION_PSI:
                    return this.attackerBhlrA.isNeedQuit() || this.hlrAattackerB.isNeedQuit() ||
                            this.attackerBvlrA.isNeedQuit() || this.vlrAattackerB.isNeedQuit();
                case INTERCEPT_SMS:
                    return this.attackerBhlrA.isNeedQuit() || this.hlrAattackerB.isNeedQuit() ||
                            this.smscAhlrA.isNeedQuit() || this.hlrAsmscA.isNeedQuit() ||
                            this.mscAsmscA.isNeedQuit() || this.smscAmscA.isNeedQuit();
                case TEST_PORTS:
                    return this.mscAhlrA.isNeedQuit() || this.hlrAmscA.isNeedQuit() ||
                            this.smscAhlrA.isNeedQuit() || this.hlrAsmscA.isNeedQuit();
            }
            return false;
            //return this.isupClient.isNeedQuit() || this.isupServer.isNeedQuit();
        } else {
            return this.mscAmscB.isNeedQuit() || this.mscBmscA.isNeedQuit() ||
                    this.mscAhlrA.isNeedQuit() || this.hlrAmscA.isNeedQuit() ||
                    this.mscAsmscA.isNeedQuit() || this.smscAmscA.isNeedQuit() ||
                    this.mscAvlrA.isNeedQuit() || this.vlrAmscA.isNeedQuit() ||
                    this.smscAhlrA.isNeedQuit() || this.hlrAsmscA.isNeedQuit() ||
                    this.hlrAvlrA.isNeedQuit() || this.vlrAhlrA.isNeedQuit() ||
                    this.sgsnAhlrA.isNeedQuit() || this.hlrAsgsnA.isNeedQuit() ||
                    this.gsmscfAhlrA.isNeedQuit() || this.hlrAgsmscfA.isNeedQuit() ||
                    this.gsmscfAvlrA.isNeedQuit() || this.vlrAgsmscfA.isNeedQuit() ||
                    this.attackerBmscA.isNeedQuit() || this.mscAattackerB.isNeedQuit() ||
                    this.attackerBhlrA.isNeedQuit() || this.hlrAattackerB.isNeedQuit() ||
                    this.attackerBsmscA.isNeedQuit() || this.smscAattackerB.isNeedQuit() ||
                    this.attackerBvlrA.isNeedQuit() || this.vlrAattackerB.isNeedQuit() ||
                    this.smscAsmscB.isNeedQuit() || this.smscBsmscA.isNeedQuit() ||
                    this.smscAhlrB.isNeedQuit() || this.hlrBsmscA.isNeedQuit() ||
                    this.smscBhlrA.isNeedQuit() || this.hlrAsmscB.isNeedQuit() ||
                    this.mscBhlrA.isNeedQuit() || this.hlrAmscB.isNeedQuit() ||
                    this.mscBsmscA.isNeedQuit() || this.smscAmscB.isNeedQuit() ||
                    this.mscBvlrA.isNeedQuit() || this.vlrAmscB.isNeedQuit() ||
                    this.hlrBvlrA.isNeedQuit() || this.vlrAhlrB.isNeedQuit() ||
                    this.vlrBvlrA.isNeedQuit() || this.vlrAvlrB.isNeedQuit() ||
                    this.vlrBhlrA.isNeedQuit() || this.hlrAvlrB.isNeedQuit();
        }
    }

    private void testerHostsExecuteCheckStore() {
        if (simpleSimulation) {
            switch(this.simpleAttackGoal) {
                case LOCATION_ATI:
                    this.attackerBhlrA.execute();
                    this.hlrAattackerB.execute();
                    break;
                case LOCATION_PSI:
                    this.attackerBhlrA.execute();
                    this.hlrAattackerB.execute();
                    this.attackerBvlrA.execute();
                    this.vlrAattackerB.execute();
                    break;
                case INTERCEPT_SMS:
                    this.attackerBhlrA.execute();
                    this.hlrAattackerB.execute();
                    this.smscAhlrA.execute();
                    this.hlrAsmscA.execute();
                    this.mscAsmscA.execute();
                    this.smscAmscA.execute();
                    break;
                case TEST_PORTS:
                    this.mscAhlrA.execute();
                    this.hlrAmscA.execute();
                    this.smscAhlrA.execute();
                    this.hlrAsmscA.execute();
                    break;
            }
            //this.isupClient.execute();
            //this.isupServer.execute();

            //this.isupClient.checkStore();
            //this.isupServer.checkStore();
        } else {
            this.mscAmscB.execute();
            this.mscBmscA.execute();
            this.mscAhlrA.execute();
            this.hlrAmscA.execute();
            this.mscAsmscA.execute();
            this.smscAmscA.execute();
            this.mscAvlrA.execute();
            this.vlrAmscA.execute();
            this.smscAhlrA.execute();
            this.hlrAsmscA.execute();
            this.hlrAvlrA.execute();
            this.vlrAhlrA.execute();
            this.sgsnAhlrA.execute();
            this.hlrAsgsnA.execute();
            this.gsmscfAhlrA.execute();
            this.hlrAgsmscfA.execute();
            this.gsmscfAvlrA.execute();
            this.vlrAgsmscfA.execute();
            this.attackerBmscA.execute();
            this.mscAattackerB.execute();
            this.attackerBhlrA.execute();
            this.hlrAattackerB.execute();
            this.attackerBsmscA.execute();
            this.smscAattackerB.execute();
            this.attackerBvlrA.execute();
            this.vlrAattackerB.execute();
            this.smscAsmscB.execute();
            this.smscBsmscA.execute();
            this.smscAhlrB.execute();
            this.hlrBsmscA.execute();
            this.smscBhlrA.execute();
            this.hlrAsmscB.execute();
            this.mscBhlrA.execute();
            this.hlrAmscB.execute();
            this.mscBsmscA.execute();
            this.smscAmscB.execute();
            this.mscBvlrA.execute();
            this.vlrAmscB.execute();
            this.hlrBvlrA.execute();
            this.vlrAhlrB.execute();
            this.vlrBvlrA.execute();
            this.vlrAvlrB.execute();
            this.vlrBhlrA.execute();
            this.hlrAvlrB.execute();
        }
    }

    private void configureShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                System.out.println();
                System.out.println("Exiting..");
                System.out.println("Chance of attack used: " + chanceOfAttack);
                System.out.println("Number of genuine messages generated: " + countGenuine);
                System.out.println("Number of attacks generated: " + countAttack);
            }
        });
    }

    private void printSentMessage(String messageSent, boolean genuineMessage) {
        //if(genuineMessage)
        //    System.out.print("Generating noise: ");
        //else
        //    System.out.print("Generating attack: ");

        //System.out.println(messageSent);
    }

    public void start() {
        configureShutDownHook();
        startAttackSimulationHosts();

        if (!waitForM3UALinks())
            return;

        int sleepTime = 50;

        while (true) {
            try {
                sleepTime = this.random.nextInt((1000 - 100) + 1) + 100;
                sleepTime = 50;
                Thread.sleep(sleepTime);

                if (this.testerHostsNeedQuit())
                    break;

                this.testerHostsExecuteCheckStore();

                if (simpleSimulation) {
                    switch (this.simpleAttackGoal) {
                        case LOCATION_ATI:
                            this.attackLocationAti();
                            break;
                        case LOCATION_PSI:
                            this.attackLocationPsi();
                            break;
                        case INTERCEPT_SMS:
                            this.attackInterceptSms();
                            break;
                        case TEST_PORTS:
                            break;
                    }
                    break;
                } else {
                    this.generateTraffic();
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void generateTraffic() {
        boolean generateNoise = this.random.nextInt(100) >= this.chanceOfAttack;

        if(generateNoise) {
            this.countGenuine++;
            this.generateNoise();
        } else {
            this.countAttack++;
            this.generateAttack();
        }
    }

    private void generateNoise() {
       this.sendRandomMessage();
    }

    private void generateAttack() {
        int numberOfAttacks = 3;
        int randomAttack = this.random.nextInt(numberOfAttacks);

        switch(randomAttack) {
            case 0:
                printSentMessage("Location:AnyTimeInterrogation", false);
                this.attackLocationAti();
                break;
            case 1:
                printSentMessage("Location:ProvideSubscriberInfo", false);
                this.attackLocationPsi();
                break;
            case 2:
                printSentMessage("Intercept:SMS", false);
                this.attackInterceptSms();
                break;
        }
    }

    private void modifyCallingPartyAddressDigits(AttackTesterHost attackTesterHost, String cgGT) {
        attackTesterHost.getSccpMan().setCallingPartyAddressDigits(cgGT);
    }

    private void sendRandomIsupMessage(int num) {
        switch(num) {
            case 0:
                this.isupClient.getTestAttackClient().performIsupIAM();
        }
    }

    private void sendRandomMessage() {
        int numberOfAvailableMessages = 35;
        int randomMessage = this.random.nextInt(numberOfAvailableMessages);

        switch (randomMessage) {
            case 0:
                printSentMessage("MoForwardSMS", true);
                this.performMoSMS();
                break;
            case 1:
                printSentMessage("MtForwardSMS", true);
                this.performMtSMS();
                break;
            case 2:
                printSentMessage("UpdateLocation", true);
                this.performUpdateLocation();
                break;
            case 3:
                printSentMessage("CancelLocation", true);
                this.performCancelLocation();
                break;
            case 4:
                printSentMessage("SendIdentification", true);
                this.performSendIdentification();
                break;
            case 5:
                printSentMessage("PurgeMS", true);
                this.performPurgeMS();
                break;
            case 6:
                printSentMessage("UpdateGPRSLocation", true);
                this.performUpdateGPRSLocation();
                break;
            case 7:
                printSentMessage("CheckIMEI", true);
                this.performCheckIMEI();
                break;
            case 8:
                printSentMessage("SubscriberData", true);
                this.performInsertSubscriberData();
                break;
            case 9:
                printSentMessage("DeleteSubscriberData", true);
                this.performDeleteSubscriberData();
                break;
            case 10:
                printSentMessage("ForwardCheckSSIndication", true);
                this.performForwardCheckSSIndication();
                break;
            case 11:
                printSentMessage("RestoreData", true);
                this.performRestoreData();
                break;
            case 12:
                printSentMessage("AnyTimeInterrogation", true);
                this.performAnyTimeInterrogation();
                break;
            case 13:
                printSentMessage("ProvideSubscriberInfo", true);
                this.performProvideSubscriberInfo();
                break;
            case 14:
                printSentMessage("ActivateTraceMode_Oam", true);
                this.performActivateTraceMode_Oam();
                break;
            case 15:
                printSentMessage("ActivateTraceMode_Mobility", true);
                this.performActivateTraceMode_Mobility();
                break;
            case 16:
                printSentMessage("SendIMSI", true);
                this.performSendIMSI();
                break;
            case 17:
                printSentMessage("SendRoutingInformation", true);
                this.performSendRoutingInformation();
                break;
            case 18:
                printSentMessage("ProvideRoamingNumber", true);
                this.performProvideRoamingNumber();
                break;
            case 19:
                printSentMessage("RegisterSS", true);
                this.performRegisterSS();
                break;
            case 20:
                printSentMessage("EraseSS", true);
                this.performEraseSS();
                break;
            case 21:
                printSentMessage("ActivateSS", true);
                this.performActivateSS();
                break;
            case 22:
                printSentMessage("DeactivateSS", true);
                this.performDeactivateSS();
                break;
            case 23:
                printSentMessage("InterrogateSS", true);
                this.performInterrogateSS();
                break;
            case 24:
                printSentMessage("RegisterPassword", true);
                this.performRegisterPassword();
                break;
            case 25:
                printSentMessage("GetPassword", true);
                this.performGetPassword();
                break;
            case 26:
                printSentMessage("ProcessUnstructuredSSRequest", true);
                this.performProcessUnstructuredSSRequest();
                break;
            case 27:
                printSentMessage("UnstructuredSSRequest", true);
                this.performUnstructuredSSRequest();
                break;
            case 28:
                printSentMessage("UnstructuredSSNotify", true);
                this.performUnstructuredSSNotify();
                break;
            case 29:
                printSentMessage("SendRoutingInfoForSM", true);
                this.performSendRoutingInfoForSM();
                break;
            case 30:
                printSentMessage("ReportSMDeliveryStatus", true);
                this.performReportSMDeliveryStatus();
                break;
            case 31:
                printSentMessage("ReadyForSM", true);
                this.performReadyForSM();
                break;
            case 32:
                printSentMessage("AlertServiceCentre", true);
                this.performAlertServiceCentre();
                break;
            case 33:
                printSentMessage("InformServiceCentre", true);
                this.performInformServiceCentre();
                break;
            case 34:
                printSentMessage("SendRoutingInfoForGPRS", true);
                this.performSendRoutingInfoForGPRS();
                break;

            default:
                break;
        }
    }

    private void attackLocationAti() {
        Subscriber subscriber = this.getSubscriberManager().getRandomSubscriber();
        this.attackerBhlrA.getTestAttackClient().performATI(subscriber.getMsisdn().getAddress());

        //while(!this.attackerBhlrA.gotAtiResponse()) {
        //    try {
        //        Thread.sleep(50);
        //    } catch (InterruptedException e) {
        //        return;
        //    }
        //}

        //AnyTimeInterrogationResponse atiResponse = this.attackerBhlrA.getTestAttackClient().getLastAtiResponse();
    }

    private void attackLocationPsi() {
        Subscriber subscriber = this.getSubscriberManager().getRandomSubscriber();

        //Get necessary information from request, use in next message.
        this.attackerBhlrA.getTestAttackClient().performSendRoutingInfoForSM(subscriber.getMsisdn().getAddress(),
                this.hlrAattackerB.getTestAttackServer().getServiceCenterAddress());

        while(!this.attackerBhlrA.gotSRIForSMResponse()) {
            try{
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.exit(50);
            }
        }

        SendRoutingInfoForSMResponse sriResponse = this.attackerBhlrA.getTestAttackClient().getLastSRIForSMResponse();
        this.attackerBhlrA.getTestAttackClient().clearLastSRIForSMResponse();

        IMSI victimImsi = sriResponse.getIMSI();
        String victimVlrAddress = sriResponse.getLocationInfoWithLMSI().getNetworkNodeNumber().getAddress();

        this.attackerBvlrA.getConfigurationData().getMapConfigurationData().setRemoteAddressDigits(victimVlrAddress);
        this.attackerBvlrA.getTestAttackClient().performProvideSubscriberInfoRequest(victimImsi);

        while(!this.attackerBvlrA.gotPSIResponse()) {
            try{
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.exit(50);
            }
        }

        ProvideSubscriberInfoResponse psiResponse = this.attackerBvlrA.getTestAttackClient().getLastPsiResponse();
        this.attackerBvlrA.getTestAttackClient().clearLastPsiResponse();
    }

    private void attackInterceptSms() {
        MAPParameterFactory mapParameterFactory = this.attackerBhlrA.getMapMan().getMAPStack().getMAPProvider().getMAPParameterFactory();
        Subscriber subscriber = this.getSubscriberManager().getRandomSubscriber();

        this.smscAhlrA.getTestAttackClient().performSendRoutingInfoForSM(subscriber.getMsisdn().getAddress(), this.hlrAsmscA.getConfigurationData().getTestAttackServerConfigurationData().getServiceCenterAddress());

        while(!this.smscAhlrA.gotSRIForSMResponse()) {
            try {
                Thread.sleep(10);
            } catch(InterruptedException e) {
                System.exit(50);
            }
        }

        SendRoutingInfoForSMResponse sriResponse = this.smscAhlrA.getTestAttackClient().getLastSRIForSMResponse();
        this.smscAhlrA.getTestAttackClient().clearLastSRIForSMResponse();
        this.smscAmscA.getTestAttackServer().performMtForwardSM("SMS Message", sriResponse.getIMSI().getData(), sriResponse.getLocationInfoWithLMSI().getNetworkNodeNumber().getAddress(), this.getSubscriberManager().getRandomSubscriber().getMsisdn().getAddress());

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            System.exit(50);
        }

        this.attackerBhlrA.getTestAttackClient().performSendRoutingInfoForSM(subscriber.getMsisdn().getAddress(), this.hlrAsmscA.getConfigurationData().getTestAttackServerConfigurationData().getServiceCenterAddress());

        while(!this.attackerBhlrA.gotSRIForSMResponse()) {
            try {
                Thread.sleep(10);
            } catch(InterruptedException e) {
                System.exit(50);
            }
        }

        sriResponse = this.attackerBhlrA.getTestAttackClient().getLastSRIForSMResponse();
        this.attackerBhlrA.getTestAttackClient().clearLastSRIForSMResponse();

        ISDNAddressString newMscAddress = mapParameterFactory.createISDNAddressString(
                this.attackerBhlrA.getConfigurationData().getTestAttackClientConfigurationData().getAddressNature(),
                this.attackerBhlrA.getConfigurationData().getTestAttackClientConfigurationData().getNumberingPlan(),
                this.attackerBhlrA.getConfigurationData().getSccpConfigurationData().getCallingPartyAddressDigits());
        ISDNAddressString newVlrAddress = mapParameterFactory.createISDNAddressString(
                this.attackerBhlrA.getConfigurationData().getTestAttackClientConfigurationData().getAddressNature(),
                this.attackerBhlrA.getConfigurationData().getTestAttackClientConfigurationData().getNumberingPlan(),
                this.attackerBhlrA.getConfigurationData().getSccpConfigurationData().getCallingPartyAddressDigits());

        this.attackerBhlrA.getTestAttackClient().performUpdateLocationRequest(sriResponse.getIMSI(), newMscAddress, newVlrAddress);

        try {
            Thread.sleep(2000);
        } catch(InterruptedException e) {
            System.exit(50);
        }

        this.smscAhlrA.getTestAttackClient().performSendRoutingInfoForSM(subscriber.getMsisdn().getAddress(), this.hlrAsmscA.getConfigurationData().getTestAttackServerConfigurationData().getServiceCenterAddress());

        while(!this.smscAhlrA.gotSRIForSMResponse()) {
            try {
                Thread.sleep(10);
            } catch(InterruptedException e) {
                System.exit(50);
            }
        }

        sriResponse = this.smscAhlrA.getTestAttackClient().getLastSRIForSMResponse();
        this.smscAhlrA.getTestAttackClient().clearLastSRIForSMResponse();
        this.smscAmscA.getTestAttackServer().performMtForwardSM("SMS Message", sriResponse.getIMSI().getData(), sriResponse.getLocationInfoWithLMSI().getNetworkNodeNumber().getAddress(), this.getSubscriberManager().getRandomSubscriber().getMsisdn().getAddress());
    }

    private void performMoSMS() {
        Subscriber originator = this.subscriberManager.getRandomSubscriber();
        Subscriber destination = this.subscriberManager.getRandomSubscriber();

        String origIsdnNumber = originator.getMsisdn().getAddress();
        String destIsdnNumber = destination.getMsisdn().getAddress();

        this.mscAsmscA.getTestAttackClient().performMoForwardSM("SMS Message", destIsdnNumber, origIsdnNumber, this.getDefaultSmscAddress().getAddress());
    }

    private void performMtSMS() {
        Subscriber destination = this.subscriberManager.getRandomSubscriber();

        String destIsdnNumber = destination.getMsisdn().getAddress();

        this.smscAhlrA.getTestAttackClient().performSendRoutingInfoForSM(destIsdnNumber,
                hlrAsmscA.getConfigurationData().getSccpConfigurationData().getCallingPartyAddressDigits());
    }

    private void performUpdateLocation() {
        this.vlrAhlrA.getTestAttackServer().performUpdateLocation();
    }

    private void performCancelLocation() {
        this.hlrAvlrA.getTestAttackClient().performCancelLocation();
    }

    private void performSendIdentification() {
        //this.vlrAvlrB.getTestAttackClient().performSendIdentification();
        //this.vlrBvlrA.getTestAttackServer().performSendIdentification();
    }

    private void performPurgeMS() {
        Subscriber subscriber = this.getSubscriberManager().getRandomSubscriber();
        this.vlrAhlrA.getTestAttackServer().performPurgeMS(subscriber.getImsi(), subscriber.getCurrentVlrNumber());
    }

    private void performUpdateGPRSLocation() {
        //this.sgsnAhlrA.getTestAttackClient().performUpdateGPRSLocation();
    }

    private void performCheckIMEI() {
        Subscriber subscriber = this.getSubscriberManager().getRandomSubscriber();

        this.vlrAmscA.getTestAttackServer().performCheckIMEI(subscriber.getSubscriberInfo().getIMEI());
        //this.mscAeirA.getTestAttackClient().performCheckIMEI();
        //this.sgsnAeirA.getTestAttackClient().performCheckIMEI();
    }

    private void performInsertSubscriberData() {
        this.hlrAvlrA.getTestAttackClient().performInsertSubscriberData();
    }

    private void performDeleteSubscriberData() {
        Subscriber subscriber = this.getSubscriberManager().getRandomSubscriber();
        this.hlrAvlrA.getTestAttackClient().performDeleteSubscriberData(subscriber.getImsi());
    }

    private void performForwardCheckSSIndication() {
        this.hlrAmscA.getTestAttackServer().performForwardCheckSSIndication();
    }

    private void performRestoreData() {
        Subscriber subscriber = this.getSubscriberManager().getRandomSubscriber();
        this.vlrAhlrA.getTestAttackServer().performRestoreData(subscriber.getImsi());
    }

    private void performAnyTimeInterrogation() {
        Subscriber subscriber = this.getSubscriberManager().getRandomSubscriber();
        this.gsmscfAhlrA.getTestAttackClient().performATI(subscriber.getMsisdn().getAddress());
    }

    private void performProvideSubscriberInfo() {
        Subscriber subscriber = this.getSubscriberManager().getRandomSubscriber();
        this.mscAvlrA.getTestAttackClient().performProvideSubscriberInfoRequest(subscriber.getImsi());
    }

    private void performActivateTraceMode_Oam() {
        Subscriber subscriber = this.getSubscriberManager().getRandomSubscriber();
        this.hlrAvlrA.getTestAttackClient().performActivateTraceMode_Oam(subscriber.getImsi());
    }

    private void performActivateTraceMode_Mobility() {
        Subscriber subscriber = this.getSubscriberManager().getRandomSubscriber();
        this.hlrAvlrA.getTestAttackClient().performActivateTraceMode_Mobility(subscriber.getImsi());
    }

    private void performSendIMSI() {
        Subscriber subscriber = this.getSubscriberManager().getRandomSubscriber();
        this.vlrAhlrA.getTestAttackServer().performSendIMSI(subscriber.getMsisdn());
    }

    private void performSendRoutingInformation() {
        Subscriber subscriber = this.getSubscriberManager().getRandomSubscriber();
        this.mscAhlrA.getTestAttackClient().performSendRoutingInformation(subscriber.getMsisdn());
    }

    private void performProvideRoamingNumber() {
        Subscriber subscriber = this.getSubscriberManager().getRandomSubscriber();
        this.hlrAvlrA.getTestAttackClient().performProvideRoamingNumber(subscriber.getImsi(), subscriber.getCurrentMscNumber());
    }

    private void performRegisterSS() {
        this.mscAvlrA.getTestAttackClient().performRegisterSS();
        //this.vlrAhlrA.getTestAttackServer().performRegisterSS();
    }

    private void performEraseSS() {
        this.mscAvlrA.getTestAttackClient().performEraseSS();
        //this.vlrAhlrA.getTestAttackServer().performEraseSS();
    }

    private void performActivateSS() {
        //this.mscAvlrA.getTestAttackClient().performActivateSS();
        //this.vlrAhlrA.getTestAttackServer().performActivateSS();
    }

    private void performDeactivateSS() {
        //this.mscAvlrA.getTestAttackClient().performDeactivateSS();
        //this.vlrAhlrA.getTestAttackServer().performDeactivateSS();
    }

    private void performInterrogateSS() {
        //this.mscAvlrA.getTestAttackClient().performInterrogateSS();
        //this.vlrAhlrA.getTestAttackServer().performInterrogateSS();
    }

    private void performRegisterPassword() {
        //this.mscAvlrA.getTestAttackClient().performRegisterSS();
        //this.vlrAhlrA.getTestAttackServer().performRegisterSS();
    }

    private void performGetPassword() {
        //this.hlrAvlrA.getTestAttackClient().performGetPassword();
        //this.vlrAmscA.getTestAttackServer().performGetPassword();
    }

    private void performProcessUnstructuredSSRequest() {
        //this.mscAvlrA.getTestAttackClient().performProcessUnstructuredSSRequest();
        //this.vlrAhlrA.getTestAttackClient().performProcessUnstructuredSSRequest();
        //this.vlrAgsmscfA.getTestAttackClient().performProcessUnstructuredSSRequest();
    }

    private void performUnstructuredSSRequest() {
        //this.hlrAvlrA.getTestAttackClient().performUnstructuredSSRequest();
        //this.gsmscfAvlrA.getTestAttackClient().performUnstructuredSSRequest();
        //this.vlrAmscA.getTestAttackClient().performUnstructuredSSRequest();
    }

    private void performUnstructuredSSNotify () {
        //this.hlrAvlrA.getTestAttackClient().performUnstructuredSSRequest();
        //this.gsmscfAvlrA.getTestAttackClient().performUnstructuredSSRequest();
        //this.vlrAmscA.getTestAttackClient().performUnstructuredSSRequest();
    }

    private void performSendRoutingInfoForSM() {
        Subscriber subscriber = this.getSubscriberManager().getRandomSubscriber();
        //this.mscAhlrA.getTestAttackClient().performSendRoutingInfoForSM("", "");
        this.smscAhlrA.getTestAttackClient().performSendRoutingInfoForSM(subscriber.getMsisdn().getAddress(), this.smscAhlrA.getTestAttackClient().getServiceCenterAddress());
    }

    private void performReportSMDeliveryStatus() {
        Subscriber subscriber = this.getSubscriberManager().getRandomSubscriber();
        this.smscAhlrA.getTestAttackClient().performReportSMDeliveryStatus(subscriber.getMsisdn());
    }

    private void performReadyForSM() {
        Subscriber subscriber = this.getSubscriberManager().getRandomSubscriber();

        //this.mscAvlrA.getTestAttackClient().performReadyForSM();
        this.vlrAhlrA.getTestAttackServer().performReadyForSM(subscriber.getImsi());
        //this.sgsnAhlrA.getTestAttackClient().performReadyForSM();
        //this.smscAhlrA.getTestAttackClient().performReadyForSM();
    }

    private void performAlertServiceCentre() {
        //this.hlrAmscA.getTestAttackServer().performAlertServiceCentre();
    }

    private void performInformServiceCentre() {
        //this.hlrAmscA.getTestAttackServer().performInformServiceCentre();
    }

    private void performSendRoutingInfoForGPRS() {
        //this.sgsnAhlrA.getTestAttackClient().performSendRoutingInfoForGPRS();
    }

    @Override
    public synchronized void stop() {
        this.mscAmscB.stop();
        this.mscBmscA.stop();
        this.mscAhlrA.stop();
        this.hlrAmscA.stop();
        this.mscAsmscA.stop();
        this.smscAmscA.stop();
        this.mscAvlrA.stop();
        this.vlrAmscA.stop();
        this.smscAhlrA.stop();
        this.hlrAsmscA.stop();
        this.hlrAvlrA.stop();
        this.vlrAhlrA.stop();
        this.sgsnAhlrA.stop();
        this.hlrAsgsnA.stop();
        this.gsmscfAhlrA.stop();
        this.hlrAgsmscfA.stop();
        this.gsmscfAvlrA.stop();
        this.vlrAgsmscfA.stop();
        this.attackerBmscA.stop();
        this.mscAattackerB.stop();
        this.attackerBhlrA.stop();
        this.hlrAattackerB.stop();
        this.attackerBsmscA.stop();
        this.smscAattackerB.stop();
        this.attackerBvlrA.stop();
        this.vlrAattackerB.stop();
        this.smscAsmscB.stop();
        this.smscBsmscA.stop();
        this.smscAhlrB.stop();
        this.hlrBsmscA.stop();
        this.smscBhlrA.stop();
        this.hlrAsmscB.stop();
        this.mscBhlrA.stop();
        this.hlrAmscB.stop();
        this.mscBsmscA.stop();
        this.smscAmscB.stop();
        this.mscBvlrA.stop();
        this.vlrAmscB.stop();
        this.hlrBvlrA.stop();
        this.vlrAhlrB.stop();
        this.vlrBvlrA.stop();
        this.vlrAvlrB.stop();
        this.vlrBhlrA.stop();
        this.hlrAvlrB.stop();
    }

    @Override
    public void execute() {
    }

    @Override
    public String getState() {
        return null;
    }

    public enum SimpleAttackGoal {
        LOCATION_ATI,
        LOCATION_PSI,
        INTERCEPT_SMS,
        TEST_PORTS,
    }
}
