package org.mobicents.protocols.ss7.tools.simulator.management;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.management.Notification;

import javolution.text.TextBuilder;
import javolution.xml.XMLBinding;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mobicents.protocols.api.IpChannelType;
import org.mobicents.protocols.ss7.indicator.NatureOfAddress;
import org.mobicents.protocols.ss7.indicator.NumberingPlan;
import org.mobicents.protocols.ss7.isup.ISUPStack;
import org.mobicents.protocols.ss7.m3ua.ExchangeType;
import org.mobicents.protocols.ss7.m3ua.Functionality;
import org.mobicents.protocols.ss7.m3ua.IPSPType;
import org.mobicents.protocols.ss7.m3ua.parameter.TrafficModeType;
import org.mobicents.protocols.ss7.map.api.primitives.AddressNature;
import org.mobicents.protocols.ss7.map.api.smstpdu.NumberingPlanIdentification;
import org.mobicents.protocols.ss7.map.api.smstpdu.TypeOfNumber;
import org.mobicents.protocols.ss7.mtp.Mtp3UserPart;
import org.mobicents.protocols.ss7.mtp.RoutingLabelFormat;
import org.mobicents.protocols.ss7.sccp.SccpProtocolVersion;
import org.mobicents.protocols.ss7.sccp.SccpStack;
import org.mobicents.protocols.ss7.tools.simulator.AttackSimulationOrganizer;
import org.mobicents.protocols.ss7.tools.simulator.Stoppable;
import org.mobicents.protocols.ss7.tools.simulator.common.AttackConfigurationData;
import org.mobicents.protocols.ss7.tools.simulator.level1.*;
import org.mobicents.protocols.ss7.tools.simulator.level2.*;
import org.mobicents.protocols.ss7.tools.simulator.level3.*;
import org.mobicents.protocols.ss7.tools.simulator.tests.attack.TestAttackClient;
import org.mobicents.protocols.ss7.tools.simulator.tests.attack.TestAttackClientConfigurationData;
import org.mobicents.protocols.ss7.tools.simulator.tests.attack.TestAttackServer;
import org.mobicents.protocols.ss7.tools.simulator.tests.attack.TestAttackServerConfigurationData;
import org.mobicents.protocols.ss7.tools.simulator.tests.sms.*;

/**
 * @author Kristoffer Jensen
 */
public class AttackTesterHost extends TesterHost implements TesterHostMBean, Stoppable {
    private static final Logger logger = Logger.getLogger(AttackTesterHost.class);

    private static final String TESTER_HOST_PERSIST_DIR_KEY = "testerhost.persist.dir";
    private static final String USER_DIR_KEY = "user.dir";
    public static String SOURCE_NAME = "ATTACK_HOST";
    public static String SS7_EVENT = "SS7Event";

    private static final String CLASS_ATTRIBUTE = "type";
    private static final String TAB_INDENT = "\t";
    private static final String PERSIST_FILE_NAME_OLD = "simulator.xml";
    private static final String PERSIST_FILE_NAME = "simulator2.xml";
    private static final String CONFIGURATION_DATA = "configurationData";

    public static String SIMULATOR_HOME_VAR = "SIMULATOR_HOME";

    private final String appName;
    private String persistDir = null;
    private final TextBuilder persistFile = TextBuilder.newInstance();
    private static final XMLBinding binding = new XMLBinding();

    // SETTINGS
    private boolean isStarted = false;
    private boolean needQuit = false;
    private boolean needStore = false;
    private AttackConfigurationData configurationData = new AttackConfigurationData();
    private long sequenceNumber = 0;

    // Layers
    private Stoppable instance_L1_B = null;
    private Stoppable instance_L2_B = null;
    private Stoppable instance_L3_B = null;
    private Stoppable instance_TestTask_B = null;

    // levels
    M3uaMan m3ua;
    SccpMan sccp;
    MapMan map;
    CapMan cap;
    TestAttackClient testAttackClient;
    TestAttackServer testAttackServer;

    private AttackNode attackNode;
    private boolean attackDone;
    private int isupNi;
    private int isupLocalSpc;
    private int isupDpc;

    private AttackSimulationOrganizer attackSimulationOrganizer;


    public AttackTesterHost() {
        this.appName = null;
    }

    public AttackTesterHost(String appName, String persistDir, AttackNode attackNode, AttackSimulationOrganizer attackSimulationOrganizer) {
        this.attackNode = attackNode;
        this.attackDone = false;
        this.appName = appName;
        this.persistDir = persistDir;
        this.attackSimulationOrganizer = attackSimulationOrganizer;

        this.m3ua = new M3uaMan(appName);
        this.m3ua.setTesterHost(this);

        this.sccp = new SccpMan(appName);
        this.sccp.setTesterHost(this);

        this.map = new MapMan(appName);
        this.map.setTesterHost(this);

        this.cap = new CapMan(appName);
        this.cap.setTesterHost(this);

        this.testAttackClient = new TestAttackClient(appName);
        this.testAttackClient.setTesterHost(this);

        this.testAttackServer = new TestAttackServer(appName);
        this.testAttackServer.setTesterHost(this);

        //this.setupLog4j(appName);

        binding.setClassAttribute(CLASS_ATTRIBUTE);

        this.configurationData.setSccpConfigurationData(new AttackSccpConfigurationData());
        this.configureNode(attackNode);
    }

    public AttackNode getAttackNode() {
        return attackNode;
    }

    public AttackSimulationOrganizer getAttackSimulationOrganizer() {
        return this.attackSimulationOrganizer;
    }

    private void configureNode(AttackNode attackNode) {
        switch(attackNode) {
            case ATTACK_SERVER:
                this.configureAttackServer();
                break;
            case ATTACK_CLIENT:
                this.configureAttackClient();
                break;
            case MSC_A_MSC_B:
                this.configureMscAMscB();
                break;
            case MSC_B_MSC_A:
                this.configureMscBMscA();
                break;
            case MSC_A_HLR_A:
                this.configureMscAHlrA();
                break;
            case HLR_A_MSC_A:
                this.configureHlrAMscA();
                break;
            case MSC_A_SMSC_A:
                this.configureMscASmscA();
                break;
            case SMSC_A_MSC_A:
                this.configureSmscAMscA();
                break;
            case MSC_A_VLR_A:
                this.configureMscAVlrA();
                break;
            case VLR_A_MSC_A:
                this.configureVlrAMscA();
                break;
            case HLR_A_VLR_A:
                this.configureHlrAVlrA();
                break;
            case VLR_A_HLR_A:
                this.configureVlrAHlrA();
                break;
            case ATTACKER_B_MSC_A:
                this.configureAttackerBMscA();
                break;
            case MSC_A_ATTACKER_B:
                this.configureMscAAttackerB();
                break;
            case ATTACKER_B_HLR_A:
                this.configureAttackerBHlrA();
                break;
            case HLR_A_ATTACKER_B:
                this.configureHlrAAttackerB();
                break;
            case ATTACKER_B_SMSC_A:
                this.configureAttackerBSmscA();
                break;
            case SMSC_A_ATTACKER_B:
                this.configureSmscAAttackerB();
                break;
            case ATTACKER_B_VLR_A:
                this.configureAttackerBVlrA();
                break;
            case VLR_A_ATTACKER_B:
                this.configureVlrAAttackerB();
                break;
            case SGSN_A_HLR_A:
                this.configureSgsnAHlrA();
                break;
            case HLR_A_SGSN_A:
                this.configureHlrASgsnA();
                break;
            case GSMSCF_A_HLR_A:
                this.configureGsmscfAHlrA();
                break;
            case HLR_A_GSMSCF_A:
                this.configureHlrAGsmscfA();
                break;
            case GSMSCF_A_VLR_A:
                this.configureGsmscfAVlrA();
                break;
            case VLR_A_GSMSCF_A:
                this.configureVlrAGsmscfA();
                break;
            case ISUP_CLIENT:
                this.configureISUPClient();
                break;
            case ISUP_SERVER:
                this.configureISUPServer();
                break;
            case SMSC_A_HLR_A:
                this.configureSmscAHlrA();
                break;
            case HLR_A_SMSC_A:
                this.configureHlrASmscA();
                break;
            case SMSC_A_SMSC_B:
                this.configureSmscASmscB();
                break;
            case SMSC_B_SMSC_A:
                this.configureSmscBSmscA();
                break;
            case SMSC_A_HLR_B:
                this.configureSmscAHlrB();
                break;
            case HLR_B_SMSC_A:
                this.configureHlrBSmscA();
                break;
            case MSC_B_HLR_A:
                this.configureMscBHlrA();
                break;
            case HLR_A_MSC_B:
                this.configureHlrAMscB();
                break;
            case MSC_B_SMSC_A:
                this.configureMscBSmscA();
                break;
            case SMSC_A_MSC_B:
                this.configureSmscAMscB();
                break;
            case MSC_B_VLR_A:
                this.configureMscBVlrA();
                break;
            case VLR_A_MSC_B:
                this.configureVlrAMscB();
                break;
            case HLR_B_VLR_A:
                this.configureHlrBVlrA();
                break;
            case VLR_A_HLR_B:
                this.configureVlrAHlrB();
                break;
            case VLR_B_VLR_A:
                this.configureVlrBVlrA();
                break;
            case VLR_A_VLR_B:
                this.configureVlrAVlrB();
                break;
            case SMSC_B_HLR_A:
                this.configureSmscBHlrA();
                break;
            case HLR_A_SMSC_B:
                this.configureHlrASmscB();
                break;
            case VLR_B_HLR_A:
                this.configureVlrBHlrA();
                break;
            case HLR_A_VLR_B:
                this.configureHlrAVlrB();
                break;

            default:
                break;
        }
    }

    private void configureHlrAVlrB() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.HLR_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.VLR_B_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.HLR_A_VLR_B_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.VLR_B_HLR_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.HLR_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.HLR_SSN,
                remoteSpc = AttackSimulationOrganizer.VLR_B_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.VLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.VLR_B_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.HLR_A_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureVlrBHlrA() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.VLR_B_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.HLR_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.VLR_B_HLR_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.HLR_A_VLR_B_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.VLR_B_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.VLR_SSN,
                remoteSpc = AttackSimulationOrganizer.HLR_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.HLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.HLR_A_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.VLR_B_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureHlrASmscB() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.HLR_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.SMSC_B_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.HLR_A_SMSC_B_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.SMSC_B_HLR_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.HLR_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.HLR_SSN,
                remoteSpc = AttackSimulationOrganizer.SMSC_B_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.SMSC_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureSmscBHlrA() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.SMSC_B_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.HLR_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.SMSC_B_HLR_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.HLR_A_SMSC_B_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.SMSC_B_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.SMSC_SSN,
                remoteSpc = AttackSimulationOrganizer.HLR_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.HLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureVlrAVlrB() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.VLR_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.VLR_B_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.VLR_A_VLR_B_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.VLR_B_VLR_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.VLR_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.VLR_SSN,
                remoteSpc = AttackSimulationOrganizer.VLR_B_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.VLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.VLR_B_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.VLR_A_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureVlrBVlrA() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.VLR_B_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.VLR_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.VLR_B_VLR_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.VLR_A_VLR_B_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.VLR_B_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.VLR_SSN,
                remoteSpc = AttackSimulationOrganizer.VLR_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.VLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.VLR_A_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.VLR_B_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureVlrAHlrB() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.VLR_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.HLR_B_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.VLR_A_HLR_B_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.HLR_B_VLR_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.VLR_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.VLR_SSN,
                remoteSpc = AttackSimulationOrganizer.HLR_B_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.HLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.HLR_B_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.VLR_A_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureHlrBVlrA() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.HLR_B_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.VLR_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.HLR_B_VLR_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.VLR_A_HLR_B_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.HLR_B_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.HLR_SSN,
                remoteSpc = AttackSimulationOrganizer.VLR_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.VLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.VLR_A_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.HLR_B_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureVlrAMscB() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.VLR_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.MSC_B_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.VLR_A_MSC_B_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.MSC_B_VLR_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.VLR_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.VLR_SSN,
                remoteSpc = AttackSimulationOrganizer.MSC_B_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.MSC_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.MSC_B_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.VLR_A_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureMscBVlrA() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.MSC_B_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.VLR_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.MSC_B_VLR_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.VLR_A_MSC_B_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.MSC_B_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.MSC_SSN,
                remoteSpc = AttackSimulationOrganizer.VLR_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.VLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.VLR_A_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.MSC_B_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureSmscAMscB() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.SMSC_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.MSC_B_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.SMSC_A_MSC_B_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.MSC_B_SMSC_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.SMSC_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.SMSC_SSN,
                remoteSpc = AttackSimulationOrganizer.MSC_B_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.MSC_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureMscBSmscA() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.MSC_B_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.SMSC_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.MSC_B_SMSC_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.SMSC_A_MSC_B_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.MSC_B_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.MSC_SSN,
                remoteSpc = AttackSimulationOrganizer.SMSC_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.SMSC_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureHlrAMscB() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.HLR_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.MSC_B_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.HLR_A_MSC_B_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.MSC_B_HLR_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.HLR_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.HLR_SSN,
                remoteSpc = AttackSimulationOrganizer.MSC_B_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.MSC_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.MSC_B_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.HLR_A_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureMscBHlrA(){
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.MSC_B_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.HLR_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.MSC_B_HLR_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.HLR_A_MSC_B_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.MSC_B_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.MSC_SSN,
                remoteSpc = AttackSimulationOrganizer.HLR_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.HLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.HLR_A_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.MSC_B_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureHlrBSmscA() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.HLR_B_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.SMSC_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.HLR_B_SMSC_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.SMSC_A_HLR_B_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.HLR_B_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.HLR_SSN,
                remoteSpc = AttackSimulationOrganizer.SMSC_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.SMSC_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureSmscAHlrB() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.SMSC_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.HLR_B_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.SMSC_A_HLR_B_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.HLR_B_SMSC_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.SMSC_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.SMSC_SSN,
                remoteSpc = AttackSimulationOrganizer.HLR_B_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.HLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureSmscBSmscA() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.SMSC_B_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.SMSC_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.SMSC_B_SMSC_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.SMSC_A_SMSC_B_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.SMSC_B_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.SMSC_SSN,
                remoteSpc = AttackSimulationOrganizer.SMSC_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.SMSC_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureSmscASmscB() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.SMSC_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.SMSC_B_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.SMSC_A_SMSC_B_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.SMSC_B_SMSC_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.SMSC_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.SMSC_SSN,
                remoteSpc = AttackSimulationOrganizer.SMSC_B_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.SMSC_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureSmscAHlrA() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.SMSC_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.HLR_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.SMSC_A_HLR_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.HLR_A_SMSC_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.SMSC_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.SMSC_SSN,
                remoteSpc = AttackSimulationOrganizer.HLR_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.HLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureHlrASmscA() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.HLR_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.SMSC_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.HLR_A_SMSC_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.SMSC_A_HLR_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.HLR_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.HLR_SSN,
                remoteSpc = AttackSimulationOrganizer.SMSC_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.SMSC_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureVlrAAttackerB() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.VLR_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.ATTACKER_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.VLR_A_ATTACKER_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.ATTACKER_VLR_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.VLR_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.VLR_SSN,
                remoteSpc = AttackSimulationOrganizer.ATTACKER_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.ATTACKER_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.ATTACKER_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.VLR_A_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_C_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureAttackerBVlrA() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.ATTACKER_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.VLR_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.ATTACKER_VLR_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.VLR_A_ATTACKER_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.ATTACKER_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.ATTACKER_SSN,
                remoteSpc = AttackSimulationOrganizer.VLR_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.VLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_C_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.VLR_A_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.ATTACKER_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureSmscAAttackerB() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.SMSC_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.ATTACKER_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.SMSC_A_ATTACKER_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.ATTACKER_SMSC_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.SMSC_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.SMSC_SSN,
                remoteSpc = AttackSimulationOrganizer.ATTACKER_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.ATTACKER_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_C_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureAttackerBSmscA() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.ATTACKER_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.SMSC_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.ATTACKER_SMSC_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.SMSC_A_ATTACKER_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.ATTACKER_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.ATTACKER_SSN,
                remoteSpc = AttackSimulationOrganizer.SMSC_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.SMSC_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_C_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureHlrAAttackerB() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.HLR_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.ATTACKER_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.HLR_A_ATTACKER_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.ATTACKER_HLR_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.HLR_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.HLR_SSN,
                remoteSpc = AttackSimulationOrganizer.ATTACKER_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.ATTACKER_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.ATTACKER_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.HLR_A_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_C_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureAttackerBHlrA() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.ATTACKER_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.HLR_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.ATTACKER_HLR_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.HLR_A_ATTACKER_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.ATTACKER_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.ATTACKER_SSN,
                remoteSpc = AttackSimulationOrganizer.HLR_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.HLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_C_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.HLR_A_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.ATTACKER_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureMscAAttackerB() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.MSC_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.ATTACKER_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.MSC_A_ATTACK_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.ATTACKER_MSC_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.MSC_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.MSC_SSN,
                remoteSpc = AttackSimulationOrganizer.ATTACKER_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.ATTACKER_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.ATTACKER_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.MSC_A_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_C_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureAttackerBMscA() {
        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.ATTACKER_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.MSC_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.ATTACKER_MSC_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.MSC_A_ATTACK_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.ATTACKER_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.ATTACKER_SSN,
                remoteSpc = AttackSimulationOrganizer.MSC_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.MSC_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_C_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.MSC_A_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.ATTACKER_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureMscAMscB() {

        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.MSC_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.MSC_B_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.MSC_A_MSC_B_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.MSC_B_MSC_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = null;

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.MSC_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.MSC_SSN,
                remoteSpc = AttackSimulationOrganizer.MSC_B_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.MSC_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.MSC_B_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.MSC_A_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureMscBMscA() {

        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.MSC_B_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.MSC_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.MSC_B_MSC_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.MSC_A_MSC_B_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.MSC_B_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.MSC_SSN,
                remoteSpc = AttackSimulationOrganizer.MSC_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.MSC_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_B_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.MSC_A_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.MSC_B_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureMscAHlrA() {

        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.MSC_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.HLR_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.MSC_A_HLR_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.HLR_A_MSC_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.MSC_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.MSC_SSN,
                remoteSpc = AttackSimulationOrganizer.HLR_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.HLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.HLR_A_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.MSC_A_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureHlrAMscA() {

        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.HLR_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.MSC_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.HLR_A_MSC_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.MSC_A_HLR_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.HLR_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.HLR_SSN,
                remoteSpc = AttackSimulationOrganizer.MSC_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.MSC_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.MSC_A_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.HLR_A_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureMscASmscA() {

        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.MSC_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.SMSC_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.MSC_A_SMSC_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.SMSC_A_MSC_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.MSC_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.MSC_SSN,
                remoteSpc = AttackSimulationOrganizer.SMSC_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.SMSC_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureSmscAMscA() {

        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.SMSC_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.MSC_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.SMSC_A_MSC_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.MSC_A_SMSC_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.SMSC_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.SMSC_SSN,
                remoteSpc = AttackSimulationOrganizer.MSC_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.MSC_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }


    private void configureMscAVlrA() {

        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.MSC_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.VLR_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.MSC_A_VLR_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.VLR_A_MSC_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.MSC_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.MSC_SSN,
                remoteSpc = AttackSimulationOrganizer.VLR_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.VLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.VLR_A_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.MSC_A_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureVlrAMscA() {

        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.VLR_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.MSC_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.VLR_A_MSC_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.MSC_A_VLR_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.VLR_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.VLR_SSN,
                remoteSpc = AttackSimulationOrganizer.MSC_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.MSC_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.MSC_A_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.VLR_A_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureHlrAVlrA() {

        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.HLR_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.VLR_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.HLR_A_VLR_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.VLR_A_HLR_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.HLR_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.HLR_SSN,
                remoteSpc = AttackSimulationOrganizer.VLR_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.VLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.VLR_A_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.HLR_A_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureVlrAHlrA() {

        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.VLR_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.HLR_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.VLR_A_HLR_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.HLR_A_VLR_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////

        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.VLR_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.VLR_SSN,
                remoteSpc = AttackSimulationOrganizer.HLR_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.HLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = AttackSimulationOrganizer.HLR_A_MAP_REFERENCE,
                origReferenceDigits = AttackSimulationOrganizer.VLR_A_MAP_REFERENCE,
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureSgsnAHlrA() {

        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.SGSN_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.HLR_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.SGSN_A_HLR_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.HLR_A_SGSN_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.SGSN_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.SGSN_SSN,
                remoteSpc = AttackSimulationOrganizer.HLR_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.HLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureHlrASgsnA() {

        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.HLR_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.SGSN_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.HLR_A_SGSN_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.SGSN_A_HLR_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////

        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.HLR_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.HLR_SSN,
                remoteSpc = AttackSimulationOrganizer.SGSN_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.SGSN_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureGsmscfAHlrA() {

        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.GSMSCF_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.HLR_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.GSMSCF_A_HLR_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.HLR_A_GSMSCF_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////

        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.GSMSCF_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.GSMSCF_SSN,
                remoteSpc = AttackSimulationOrganizer.HLR_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.HLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureHlrAGsmscfA() {

        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.HLR_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.GSMSCF_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.HLR_A_GSMSCF_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.GSMSCF_A_HLR_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////

        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.HLR_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.HLR_SSN,
                remoteSpc = AttackSimulationOrganizer.GSMSCF_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.GSMSCF_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureGsmscfAVlrA() {

        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.GSMSCF_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.VLR_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.GSMSCF_A_VLR_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.VLR_A_GSMSCF_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////

        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.GSMSCF_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.GSMSCF_SSN,
                remoteSpc = AttackSimulationOrganizer.VLR_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.VLR_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureVlrAGsmscfA() {

        //////// L1 Configuration Data //////////

        int opc = AttackSimulationOrganizer.VLR_A_OPC,
                opc2 = 0,
                dpc = AttackSimulationOrganizer.GSMSCF_A_OPC,
                dpc2 = 0,
                localPort = AttackSimulationOrganizer.VLR_A_GSMSCF_A_PORT,
                localPort2 = 0,
                remotePort = AttackSimulationOrganizer.GSMSCF_A_VLR_A_PORT,
                remotePort2 = 0;

        String localHost = AttackSimulationOrganizer.LOCALHOST,
                localHost2 = "",
                remoteHost = AttackSimulationOrganizer.LOCALHOST,
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////

        //////// L2 Configuration Data //////////

        int localSpc = AttackSimulationOrganizer.VLR_A_SPC,
                localSpc2 = 0,
                localSsn = AttackSimulationOrganizer.VLR_SSN,
                remoteSpc = AttackSimulationOrganizer.GSMSCF_A_SPC,
                remoteSpc2 = 0,
                remoteSsn = AttackSimulationOrganizer.GSMSCF_SSN;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = AttackSimulationOrganizer.OPERATOR_A_GT;

        ////////////////////////////////////////

        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureISUPClient() {
        //////// L1 Configuration Data //////////

        int opc = 20,
                opc2 = 0,
                dpc = 21,
                dpc2 = 0,
                localPort = 8040,
                localPort2 = 0,
                remotePort = 8041,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2ISUP(2, opc, dpc);
        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureISUPServer() {
        //////// L1 Configuration Data //////////

        int opc = 21,
                opc2 = 0,
                dpc = 20,
                dpc2 = 0,
                localPort = 8041,
                localPort2 = 0,
                remotePort = 8040,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2ISUP(2, opc, dpc);
        if(isSctpServer)
            configureTestAttackServer();
        else
            configureTestAttackClient();
    }

    private void configureAttackServer() {

        //////// L1 Configuration Data //////////

        int dpc = 1,
                dpc2 = 0,
                opc = 2,
                opc2 = 0,
                localPort = 8012,
                localPort2 = 0,
                remotePort = 8011,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 2,
                localSsn = 8,
                remoteSpc = 1,
                remoteSsn = 8;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "22222222";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "11111111";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


        //configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        //configureL2(callingPartyAddressDigits, localSpc, localSsn, remoteSpc, remoteSsn, routeonGtMode);
        //configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);
        //configureTestAttackServer();
    }

    private void configureAttackClient() {

        //////// L1 Configuration Data //////////

        int dpc = 2,
                dpc2 = 0,
                opc = 1,
                opc2 = 0,
                localPort = 8011,
                localPort2 = 0,
                remotePort = 8012,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 1,
                localSsn = 8,
                remoteSpc = 2,
                remoteSsn = 8;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "11111111";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "22222222";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////



        ///////////////////////////////////////////


        //configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        //configureL2(callingPartyAddressDigits, localSpc, localSsn, remoteSpc, remoteSsn, routeonGtMode);
        //configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);
        //configureTestAttackClient();
    }

    public void configureL1(int dpc, int dpc2, boolean isSctpServer, String localHost, String localHost2, int localPort, int localPort2, IPSPType ipspType, int opc, int opc2, String remoteHost, String remoteHost2, int remotePort, int remotePort2) {
        this.setInstance_L1(Instance_L1.createInstance("M3UA"));
        M3uaConfigurationData m3uaConfigurationData = this.getConfigurationData().getM3uaConfigurationData();

        m3uaConfigurationData.setDpc(dpc);
        m3uaConfigurationData.setDpc2(dpc2);
        m3uaConfigurationData.setIpChannelType(IpChannelType.SCTP);
        m3uaConfigurationData.setIsSctpServer(isSctpServer);
        m3uaConfigurationData.setLocalHost(localHost);
        m3uaConfigurationData.setLocalHost2(localHost2);
        m3uaConfigurationData.setLocalPort(localPort);
        m3uaConfigurationData.setLocalPort2(localPort2);
        m3uaConfigurationData.setM3uaExchangeType(ExchangeType.SE);
        m3uaConfigurationData.setM3uaFunctionality(Functionality.IPSP);
        m3uaConfigurationData.setM3uaIPSPType(ipspType);
        m3uaConfigurationData.setNetworkAppearance(102L);
        m3uaConfigurationData.setOpc(opc);
        m3uaConfigurationData.setOpc2(opc2);
        m3uaConfigurationData.setRemoteHost(remoteHost);
        m3uaConfigurationData.setRemoteHost2(remoteHost2);
        m3uaConfigurationData.setRemotePort(remotePort);
        m3uaConfigurationData.setRemotePort2(remotePort2);
        m3uaConfigurationData.setRoutingContext(101L);
        m3uaConfigurationData.setRoutingLabelFormat(RoutingLabelFormat.ITU);
        m3uaConfigurationData.setSi(3);
        m3uaConfigurationData.setStorePcapTrace(false);
        m3uaConfigurationData.setTrafficModeType(TrafficModeType.Loadshare);
    }

    public void configureL2ISUP(int ni, int isupLocalSpc, int dpc) {
        this.setInstance_L2(Instance_L2.createInstance("ISUP"));

        this.isupNi = ni;
        this.isupLocalSpc = isupLocalSpc;
        this.isupDpc = dpc;
    }

    public void configureL2(String callingPartyAddressDigits, int localSpc, int localSpc2, int localSsn, int remoteSpc, int remoteSpc2, int remoteSsn, boolean routeonGtMode) {
        this.setInstance_L2(Instance_L2.createInstance("SCCP"));
        AttackSccpConfigurationData sccpConfigurationData = this.getConfigurationData().getSccpConfigurationData();

        sccpConfigurationData.setCallingPartyAddressDigits(callingPartyAddressDigits);
        sccpConfigurationData.setGlobalTitleType(GlobalTitleType.createInstance("Translation type, numbering plan, encoding scheme and NOA ind"));
        sccpConfigurationData.setLocalSpc(localSpc);
        sccpConfigurationData.setLocalSpc2(localSpc2);
        sccpConfigurationData.setLocalSsn(localSsn);
        sccpConfigurationData.setNatureOfAddress(NatureOfAddress.INTERNATIONAL);
        sccpConfigurationData.setNi(2);
        sccpConfigurationData.setNumberingPlan(NumberingPlan.ISDN_MOBILE);
        sccpConfigurationData.setRemoteSpc(remoteSpc);
        sccpConfigurationData.setRemoteSpc2(remoteSpc2);
        sccpConfigurationData.setRemoteSsn(remoteSsn);
        sccpConfigurationData.setRouteOnGtMode(routeonGtMode);
        sccpConfigurationData.setSccpProtocolVersion(SccpProtocolVersion.ITU);
        sccpConfigurationData.setTranslationType(0);
    }

    public void configureL3(String destReferenceDigits, String origReferenceDigits, String remoteAddressDigits) {
        this.setInstance_L3(Instance_L3.createInstance("TCAP+MAP"));
        MapConfigurationData mapConfigurationData = this.getConfigurationData().getMapConfigurationData();

        mapConfigurationData.setDestReference(destReferenceDigits); //Destination reference digits
        mapConfigurationData.setDestReferenceAddressNature(AddressNature.international_number);
        mapConfigurationData.setDestReferenceNumberingPlan(org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN);

        mapConfigurationData.setOrigReference(origReferenceDigits); //Origin reference digits
        mapConfigurationData.setOrigReferenceAddressNature(AddressNature.international_number);
        mapConfigurationData.setOrigReferenceNumberingPlan(org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN);

        //If empty RoutingOnDpcAndSsn is used for CallingPartyAddress (remoteSpc from SCCP)
        //If not empty RoutingOnGT is used(address and Ssn as defined in MAP layer)
        // This option may be ignored by some test tasks that supply their own digits
        mapConfigurationData.setRemoteAddressDigits(remoteAddressDigits);
    }

    public void configureTestAttackClient() {
        this.setInstance_TestTask(Instance_TestTask.createInstance("ATTACK_TEST_CLIENT"));
        TestAttackClientConfigurationData testAttackClientConfigurationData = this.getConfigurationData().getTestAttackClientConfigurationData();

        testAttackClientConfigurationData.setAddressNature(AddressNature.international_number);
        testAttackClientConfigurationData.setContinueDialog(false);
        testAttackClientConfigurationData.setMapProtocolVersion(MapProtocolVersion.createInstance("MAP protocol version 3"));
        testAttackClientConfigurationData.setMtFSMReaction(MtFSMReaction.createInstance("Return success"));
        testAttackClientConfigurationData.setNationalLanguageCode(0);
        testAttackClientConfigurationData.setNumberingPlan(org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN);
        testAttackClientConfigurationData.setNumberingPlanIdentification(NumberingPlanIdentification.ISDNTelephoneNumberingPlan);
        testAttackClientConfigurationData.setOneNotificationFor100Dialogs(false);
        testAttackClientConfigurationData.setReportSMDeliveryStatusReaction(ReportSMDeliveryStatusReaction.createInstance("Return success"));
        testAttackClientConfigurationData.setReturn20PersDeliveryErrors(false);
        testAttackClientConfigurationData.setServiceCenterAddress("");
        testAttackClientConfigurationData.setSmsCodingType(SmsCodingType.createInstance("GSM8"));
        testAttackClientConfigurationData.setSmscSsn(8);
        testAttackClientConfigurationData.setSRIInformServiceCenter(SRIInformServiceCenter.createInstance("No data in MWD file"));
        testAttackClientConfigurationData.setSRIReaction(SRIReaction.createInstance("Return success"));
        testAttackClientConfigurationData.setSriResponseImsi("1234567890");
        testAttackClientConfigurationData.setSriResponseVlr("0987654321");
        testAttackClientConfigurationData.setSRIScAddressNotIncluded(false);
        testAttackClientConfigurationData.setTypeOfNumber(TypeOfNumber.InternationalNumber);

    }

    public void configureTestAttackServer() {
        this.setInstance_TestTask(Instance_TestTask.createInstance("ATTACK_TEST_SERVER"));
        TestAttackServerConfigurationData testAttackServerConfigurationData = this.getConfigurationData().getTestAttackServerConfigurationData();

        testAttackServerConfigurationData.setAddressNature(AddressNature.international_number);
        testAttackServerConfigurationData.setGprsSupportIndicator(false);
        testAttackServerConfigurationData.setHlrSsn(8);
        testAttackServerConfigurationData.setMapProtocolVersion(MapProtocolVersion.createInstance("MAP protocol version 3"));
        testAttackServerConfigurationData.setNumberingPlan(org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN);
        testAttackServerConfigurationData.setNumberingPlanIdentification(NumberingPlanIdentification.ISDNTelephoneNumberingPlan);
        testAttackServerConfigurationData.setSendSrsmdsIfError(false);
        testAttackServerConfigurationData.setServiceCenterAddress("45454545");
        testAttackServerConfigurationData.setSmsCodingType(SmsCodingType.createInstance("GSM8"));
        testAttackServerConfigurationData.setTypeOfNumber(TypeOfNumber.InternationalNumber);
        testAttackServerConfigurationData.setVlrSsn(8);

        testAttackServerConfigurationData.setSriResponseImsi("454545454545454");
        testAttackServerConfigurationData.setSriResponseVlr("1114");
    }

    @Override
    public AttackConfigurationData getConfigurationData() {
        return this.configurationData;
    }

    @Override
    public M3uaMan getM3uaMan() {
        return this.m3ua;
    }

    @Override
    public SccpMan getSccpMan() {
        return this.sccp;
    }

    @Override
    public MapMan getMapMan() {
        return this.map;
    }

    @Override
    public CapMan getCapMan() {
        return this.cap;
    }

    public TestAttackClient getTestAttackClient() {
        return this.testAttackClient;
    }

    public TestAttackServer getTestAttackServer() {
        return this.testAttackServer;
    }

    private void setupLog4j(String appName) {
        String propFileName = appName + ".log4j.properties";
        File f = new File("./" + propFileName);
        if (f.exists()) {

            try {
                InputStream inStreamLog4j = new FileInputStream(f);
                Properties propertiesLog4j = new Properties();

                propertiesLog4j.load(inStreamLog4j);
                PropertyConfigurator.configure(propertiesLog4j);
            } catch (Exception e) {
                e.printStackTrace();
                BasicConfigurator.configure();
            }
        } else {
            BasicConfigurator.configure();
        }

        logger.debug("log4j configured");
    }

    @Override
    public void sendNotif(String source, String msg, Throwable e, Level logLevel) {
        //StringBuilder sb = new StringBuilder();
        //for (StackTraceElement st : e.getStackTrace()) {
        //    if (sb.length() > 0)
        //        sb.append("\n");
        //    sb.append(st.toString());
        //}
        //this.doSendNotif(source, msg + " - " + e.toString(), sb.toString());

        //logger.log(logLevel, msg, e);
    }

    @Override
    public void sendNotif(String source, String msg, String userData, Level logLevel) {

        //this.doSendNotif(source, msg, userData);

        //logger.log(Level.INFO, msg + "\n" + userData);
    }

    private synchronized void doSendNotif(String source, String msg, String userData) {
        Notification notif = new Notification(SS7_EVENT + "-" + source, "AttackTesterHost", ++sequenceNumber,
                System.currentTimeMillis(), msg);
        notif.setUserData(userData);
        this.sendNotification(notif);
    }

    @Override
    public boolean isNeedQuit() {
        return needQuit;
    }

    @Override
    public boolean isStarted() {
        return isStarted;
    }

    @Override
    public Instance_L1 getInstance_L1() {
        return configurationData.getInstance_L1();
    }

    @Override
    public void setInstance_L1(Instance_L1 val) {
        configurationData.setInstance_L1(val);
        this.markStore();
    }

    @Override
    public Instance_L2 getInstance_L2() {
        return configurationData.getInstance_L2();
    }

    @Override
    public void setInstance_L2(Instance_L2 val) {
        configurationData.setInstance_L2(val);
        this.markStore();
    }

    @Override
    public Instance_L3 getInstance_L3() {
        return configurationData.getInstance_L3();
    }

    @Override
    public void setInstance_L3(Instance_L3 val) {
        configurationData.setInstance_L3(val);
        this.markStore();
    }

    @Override
    public Instance_TestTask getInstance_TestTask() {
        return configurationData.getInstance_TestTask();
    }

    @Override
    public void setInstance_TestTask(Instance_TestTask val) {
        configurationData.setInstance_TestTask(val);
        this.markStore();
    }

    @Override
    public String getInstance_L1_Value() {
        return configurationData.getInstance_L1().toString();
    }

    @Override
    public String getInstance_L2_Value() {
        return configurationData.getInstance_L2().toString();
    }

    @Override
    public String getInstance_L3_Value() {
        return configurationData.getInstance_L3().toString();
    }

    @Override
    public String getInstance_TestTask_Value() {
        return configurationData.getInstance_TestTask().toString();
    }

    @Override
    public String getState() {
        return AttackTesterHost.SOURCE_NAME + ": " + (this.isStarted() ? "Started" : "Stopped");
    }

    @Override
    public String getL1State() {
        if (this.instance_L1_B != null)
            return this.instance_L1_B.getState();
        else
            return "";
    }

    @Override
    public String getL2State() {
        if (this.instance_L2_B != null)
            return this.instance_L2_B.getState();
        else
            return "";
    }

    @Override
    public String getL3State() {
        if (this.instance_L3_B != null)
            return this.instance_L3_B.getState();
        else
            return "";
    }

    @Override
    public String getTestTaskState() {
        if (this.instance_TestTask_B != null)
            return this.instance_TestTask_B.getState();
        else
            return "";
    }

    @Override
    public void start() {

        this.store();
        this.stop();

        boolean started;
        Mtp3UserPart mtp3UserPart;

        // Start L1
        this.instance_L1_B = this.m3ua;
        started = this.m3ua.start();
        mtp3UserPart = this.m3ua.getMtp3UserPart();

        if(!started) {
            this.sendNotif(AttackTesterHost.SOURCE_NAME, "Layer 1 has not started.", "", Level.WARN);
            this.stop();
            return;
        }

        // Start L2
        started = false;
        SccpStack sccpStack = null;
        ISUPStack isupStack = null;

        if (mtp3UserPart == null) {
            this.sendNotif(AttackTesterHost.SOURCE_NAME, "Error initializing SCCP: No Mtp3UserPart is defined at L1", "", Level.WARN);
        } else {
            switch(this.configurationData.getInstance_L2().intValue()) {
                case Instance_L2.VAL_SCCP:
                    this.instance_L2_B = this.sccp;
                    this.sccp.setMtp3UserPart(mtp3UserPart);
                    started = this.sccp.start();
                    sccpStack = this.sccp.getSccpStack();
                    break;
                case Instance_L2.VAL_ISUP:
                    started = true;
                    break;
            }
        }

        if(!started) {
            this.sendNotif(AttackTesterHost.SOURCE_NAME, "Layer 2 has not started.", "", Level.WARN);
            this.stop();
            return;
        }

        // Start L3
        started = false;
        MapMan curMap = null;
        CapMan curCap = null;

        switch (this.configurationData.getInstance_L3().intValue()) {
            case Instance_L3.VAL_MAP:
                if (sccpStack == null) {
                    this.sendNotif(AttackTesterHost.SOURCE_NAME, "Error initializing TCAP+MAP: No SccpStack is defined at L2", "",
                            Level.WARN);
                } else {
                    this.instance_L3_B = this.map;
                    this.map.setSccpStack(sccpStack);
                    started = this.map.start();
                    curMap = this.map;
                }
                break;
            case Instance_L3.VAL_CAP:
                if (sccpStack == null) {
                    this.sendNotif(AttackTesterHost.SOURCE_NAME, "Error initializing TCAP+CAP: No SccpStack is defined at L2", "",
                            Level.WARN);
                } else {
                    this.instance_L3_B = this.cap;
                    this.cap.setSccpStack(sccpStack);
                    started = this.cap.start();
                    curCap = this.cap;
                }
                break;

            default:
                // TODO: implement others test tasks ...
                this.sendNotif(AttackTesterHost.SOURCE_NAME, "Instance_L3." + this.configurationData.getInstance_L3().toString()
                        + " has not been implemented yet", "", Level.WARN);
                break;
        }

        if (!started) {
            this.sendNotif(AttackTesterHost.SOURCE_NAME, "Layer 3 has not started", "", Level.WARN);
            this.stop();
            return;
        }

        // Start Testers
        started = false;
        boolean isL2ISUP = this.configurationData.getInstance_L2().intValue() == Instance_L2.VAL_ISUP;
        switch (this.configurationData.getInstance_TestTask().intValue()) {
            case Instance_TestTask.VAL_ATTACK_CLIENT:
                if (curMap == null && !isL2ISUP) {
                    this.sendNotif(AttackTesterHost.SOURCE_NAME, "Error initializing ATTACK_CLIENT: No MAP stack is defined at L3",
                            "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testAttackClient;
                    this.testAttackClient.setMapMan(curMap);
                    started = this.testAttackClient.start();
                }
                break;

            case Instance_TestTask.VAL_ATTACK_SERVER:
                if (curMap == null && !isL2ISUP) {
                    this.sendNotif(AttackTesterHost.SOURCE_NAME, "Error initializing ATTACK_SERVER: No MAP stack is defined at L3",
                            "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testAttackServer;
                    this.testAttackServer.setMapMan(curMap);
                    started = this.testAttackServer.start();
                }
                break;

            default:
                // TODO: implement others test tasks ...
                this.sendNotif(AttackTesterHost.SOURCE_NAME, "Instance_TestTask."
                                + this.configurationData.getInstance_TestTask().toString() + " has not been implemented yet", "",
                        Level.WARN);
                break;
        }

        if (!started) {
            this.sendNotif(AttackTesterHost.SOURCE_NAME, "Testing task has not started", "", Level.WARN);
            this.stop();
            return;
        }

        this.isStarted = true;
    }

    @Override
    public void stop() {

        this.isStarted = false;

        // TestTask
        if (this.instance_TestTask_B != null) {
            this.instance_TestTask_B.stop();
            this.instance_TestTask_B = null;
        }

        // L3
        if (this.instance_L3_B != null) {
            this.instance_L3_B.stop();
            this.instance_L3_B = null;
        }

        // L2
        if (this.instance_L2_B != null) {
            this.instance_L2_B.stop();
            this.instance_L2_B = null;
        }

        // L1
        if (this.instance_L1_B != null) {
            this.instance_L1_B.stop();
            this.instance_L1_B = null;
        }
    }

    @Override
    public void execute() {
        if (this.instance_L1_B != null) {
            this.instance_L1_B.execute();
        }
        if (this.instance_L2_B != null) {
            this.instance_L2_B.execute();
        }
        if (this.instance_L3_B != null) {
            this.instance_L3_B.execute();
        }
        if (this.instance_TestTask_B != null) {
            this.instance_TestTask_B.execute();
        }
    }

    @Override
    public void quit() {
        this.stop();
        this.store();
        this.needQuit = true;
    }

    @Override
    public String getName() {
        return appName;
    }

    @Override
    public String getPersistDir() {
        return persistDir;
    }

    @Override
    public void markStore() {
        needStore = true;
    }

    public boolean gotPSIResponse(boolean client) {
        if(client)
            return this.getTestAttackClient().getLastPsiResponse() != null;
        else
            return this.getTestAttackServer().getLastPsiResponse() != null;
    }

    public boolean gotSRIForSMResponse() {
        return this.getTestAttackClient().getLastSRIForSMResponse() != null;
    }

    public boolean gotMtForwardSMResponse(boolean client) {
        if(client)
            return this.getTestAttackClient().getLastMtForwardSMResponse() != null;
        else
            return this.getTestAttackServer().getLastMtForwardSMResponse() != null;
    }

    public boolean gotAtiResponse() {
        return this.getTestAttackClient().getLastAtiResponse() != null;
    }

    public boolean gotProvideRoamingNumberResponse(boolean client) {
        if(client)
            return this.getTestAttackClient().getLastProvideRoamingNumberResponse() != null;
        else
            return this.getTestAttackServer().getLastProvideRoamingNumberResponse() != null;
    }

    public boolean gotRegisterSSResponse(boolean client) {
        if(client)
            return this.getTestAttackClient().getLastRegisterSSResponse() != null;
        else
            return this.getTestAttackServer().getLastRegisterSSResponse() != null;
    }

    public boolean gotEraseSSResponse(boolean client) {
        if(client)
            return this.getTestAttackClient().getLastEraseSSResponse() != null;
        else
            return this.getTestAttackServer().getLastEraseSSResponse() != null;
    }

    public boolean gotSendRoutingInfoResponse() {
        return this.getTestAttackClient().getLastSendRoutingInfoResponse() != null;
    }

    public boolean gotInsertSubscriberDataResponse(boolean client) {
        if(client)
            return this.getTestAttackClient().getLastInsertSubscriberDataResponse() != null;
        else
            return this.getTestAttackServer().getLastInsertSubscriberDataResponse() != null;
    }

    public boolean gotCancelLocationResponse(boolean client) {
        if(client)
            return this.getTestAttackClient().getLastCancelLocationResponse() != null;
        else
            return this.getTestAttackServer().getLastCancelLocationResponse() != null;
    }

    public boolean gotActivateTraceModeResponse(boolean client) {
        if(client)
            return this.getTestAttackClient().getLastActivateTraceModeResponse() != null;
        else
            return this.getTestAttackServer().getLastActivateTraceModeResponse() != null;
    }

    public enum AttackNode {
        ALL,
        ATTACK_CLIENT,
        ATTACK_SERVER,
        MSC_A_MSC_B,
        MSC_B_MSC_A,

        MSC_A_HLR_A,
        HLR_A_MSC_A,

        MSC_A_SMSC_A,
        SMSC_A_MSC_A,

        MSC_A_VLR_A,
        SMSC_A_HLR_A,

        HLR_A_SMSC_A,
        VLR_A_MSC_A,

        HLR_A_VLR_A,
        VLR_A_HLR_A,

        SGSN_A_HLR_A,
        HLR_A_SGSN_A,

        GSMSCF_A_HLR_A,
        HLR_A_GSMSCF_A,

        GSMSCF_A_VLR_A,
        VLR_A_GSMSCF_A,

        ATTACKER_B_MSC_A,
        MSC_A_ATTACKER_B,

        ATTACKER_B_HLR_A,
        HLR_A_ATTACKER_B,

        ATTACKER_B_SMSC_A,
        SMSC_A_ATTACKER_B,

        ATTACKER_B_VLR_A,
        VLR_A_ATTACKER_B,

        SMSC_A_SMSC_B,
        SMSC_B_SMSC_A,

        SMSC_A_HLR_B,
        HLR_B_SMSC_A,

        SMSC_B_HLR_A,
        HLR_A_SMSC_B,

        MSC_B_HLR_A,
        HLR_A_MSC_B,

        MSC_B_SMSC_A,
        SMSC_A_MSC_B,

        MSC_B_VLR_A,
        VLR_A_MSC_B,

        HLR_B_VLR_A,
        VLR_A_HLR_B,

        VLR_B_VLR_A,
        VLR_A_VLR_B,

        VLR_B_HLR_A,
        HLR_A_VLR_B,

        ISUP_CLIENT,
        ISUP_SERVER,
    }
}
