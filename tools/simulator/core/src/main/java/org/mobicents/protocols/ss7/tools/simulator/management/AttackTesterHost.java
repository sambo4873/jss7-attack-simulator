package org.mobicents.protocols.ss7.tools.simulator.management;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.management.Notification;

import javolution.text.TextBuilder;
import javolution.xml.XMLBinding;
import javolution.xml.XMLObjectReader;
import javolution.xml.XMLObjectWriter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.mobicents.protocols.api.IpChannelType;
import org.mobicents.protocols.ss7.indicator.NatureOfAddress;
import org.mobicents.protocols.ss7.indicator.NumberingPlan;
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
import org.mobicents.protocols.ss7.tools.simulator.Stoppable;
import org.mobicents.protocols.ss7.tools.simulator.common.AttackConfigurationData;
import org.mobicents.protocols.ss7.tools.simulator.common.ConfigurationData;
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

    // testers

    public AttackTesterHost() {
        this.appName = null;
    }

    public AttackTesterHost(String appName, String persistDir, AttackNode attackNode) {
        this.attackNode = attackNode;
        this.attackDone = false;
        this.appName = appName;
        this.persistDir = persistDir;

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

        this.setupLog4j(appName);

        binding.setClassAttribute(CLASS_ATTRIBUTE);

        this.persistFile.clear();
        TextBuilder persistFileOld = new TextBuilder();

        if (persistDir != null) {
            persistFileOld.append(persistDir).append(File.separator).append(this.appName).append("_")
                    .append(PERSIST_FILE_NAME_OLD);
            this.persistFile.append(persistDir).append(File.separator).append(this.appName).append("_")
                    .append(PERSIST_FILE_NAME);
        } else {
            persistFileOld.append(System.getProperty(TESTER_HOST_PERSIST_DIR_KEY, System.getProperty(USER_DIR_KEY)))
                    .append(File.separator).append(this.appName).append("_").append(PERSIST_FILE_NAME_OLD);
            this.persistFile.append(System.getProperty(TESTER_HOST_PERSIST_DIR_KEY, System.getProperty(USER_DIR_KEY)))
                    .append(File.separator).append(this.appName).append("_").append(PERSIST_FILE_NAME);
        }

        File fn = new File(persistFile.toString());
        this.load(fn);

        this.configurationData.setSccpConfigurationData(new AttackSccpConfigurationData());
        this.configureNode(attackNode);
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

            default:
                break;
        }
    }

    private void configureVlrAAttackerB() {
        //////// L1 Configuration Data //////////

        int opc = 4,
                opc2 = 0,
                dpc = 9,
                dpc2 = 0,
                localPort = 8028,
                localPort2 = 0,
                remotePort = 8027,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 4,
                localSpc2 = 0,
                localSsn = 7,
                remoteSpc = 9,
                remoteSpc2 = 0,
                remoteSsn = 8;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1114";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "2225";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 9,
                opc2 = 0,
                dpc = 4,
                dpc2 = 0,
                localPort = 8027,
                localPort2 = 0,
                remotePort = 8028,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 9,
                localSpc2 = 0,
                localSsn = 8,
                remoteSpc = 4,
                remoteSpc2 = 0,
                remoteSsn = 7;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "2225";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1114";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 3,
                opc2 = 0,
                dpc = 9,
                dpc2 = 0,
                localPort = 8026,
                localPort2 = 0,
                remotePort = 8025,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 3,
                localSpc2 = 0,
                localSsn = 8,
                remoteSpc = 9,
                remoteSpc2 = 0,
                remoteSsn = 8;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1113";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "2225";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 9,
                opc2 = 0,
                dpc = 3,
                dpc2 = 0,
                localPort = 8025,
                localPort2 = 0,
                remotePort = 8026,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 9,
                localSpc2 = 0,
                localSsn = 8,
                remoteSpc = 3,
                remoteSpc2 = 0,
                remoteSsn = 8;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "2225";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1113";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 2,
                opc2 = 0,
                dpc = 9,
                dpc2 = 0,
                localPort = 8024,
                localPort2 = 0,
                remotePort = 8023,
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
                localSpc2 = 0,
                localSsn = 6,
                remoteSpc = 9,
                remoteSpc2 = 0,
                remoteSsn = 8;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1112";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "2225";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 9,
                opc2 = 0,
                dpc = 2,
                dpc2 = 0,
                localPort = 8023,
                localPort2 = 0,
                remotePort = 8024,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 9,
                localSpc2 = 0,
                localSsn = 8,
                remoteSpc = 2,
                remoteSpc2 = 0,
                remoteSsn = 6;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "2225";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1112";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 1,
                opc2 = 0,
                dpc = 9,
                dpc2 = 0,
                localPort = 8030,
                localPort2 = 0,
                remotePort = 8029,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 1,
                localSpc2 = 0,
                localSsn = 8,
                remoteSpc = 9,
                remoteSpc2 = 0,
                remoteSsn = 8;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1111";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "2225";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 9,
                opc2 = 0,
                dpc = 1,
                dpc2 = 0,
                localPort = 8029,
                localPort2 = 0,
                remotePort = 8030,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 9,
                localSpc2 = 0,
                localSsn = 8,
                remoteSpc = 1,
                remoteSpc2 = 0,
                remoteSsn = 8;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "2225";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1111";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 1,
                opc2 = 0,
                dpc = 5,
                dpc2 = 0,
                localPort = 8011,
                localPort2 = 0,
                remotePort = 8111,
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
                localSpc2 = 0,
                localSsn = 8,
                remoteSpc = 5,
                remoteSpc2 = 0,
                remoteSsn = 8;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1111";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "2221";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 5,
                opc2 = 0,
                dpc = 1,
                dpc2 = 0,
                localPort = 8111,
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

        int localSpc = 5,
                localSpc2 = 0,
                localSsn = 8,
                remoteSpc = 1,
                remoteSpc2 = 0,
                remoteSsn = 8;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "2221";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1111";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


        //dpc2 = 0;
        //opc2 = 0;
        //localPort2 = 0;
        //remotePort2 = 0;
        //localHost2 = "";
        //remoteHost2 = "";


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

        int opc = 1,
                opc2 = 0,
                dpc = 2,
                dpc2 = 0,
                localPort = 8012,
                localPort2 = 0,
                remotePort = 8015,
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
                localSpc2 = 0,
                localSsn = 8,
                remoteSpc = 2,
                remoteSpc2 = 0,
                remoteSsn = 6;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1111";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1112";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


        //dpc2 = 0;
        //opc2 = 0;
        //localPort2 = 0;
        //remotePort2 = 0;
        //localHost2 = "";
        //remoteHost2 = "";


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

        int opc = 2,
                opc2 = 0,
                dpc = 1,
                dpc2 = 0,
                localPort = 8015,
                localPort2 = 0,
                remotePort = 8012,
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
                localSpc2 = 0,
                localSsn = 6,
                remoteSpc = 1,
                remoteSpc2 = 0,
                remoteSsn = 8;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1112";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1111";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


        //dpc2 = 0;
        //opc2 = 0;
        //localPort2 = 0;
        //remotePort2 = 0;
        //localHost2 = "";
        //remoteHost2 = "";


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

        int opc = 1,
                opc2 = 0,
                dpc = 3,
                dpc2 = 0,
                localPort = 8013,
                localPort2 = 0,
                remotePort = 8016,
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
                localSpc2 = 0,
                localSsn = 8,
                remoteSpc = 3,
                remoteSpc2 = 0,
                remoteSsn = 8;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1111";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1113";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


        //dpc2 = 0;
        //opc2 = 0;
        //localPort2 = 0;
        //remotePort2 = 0;
        //localHost2 = "";
        //remoteHost2 = "";


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

        int opc = 3,
                opc2 = 0,
                dpc = 1,
                dpc2 = 0,
                localPort = 8016,
                localPort2 = 0,
                remotePort = 8013,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 3,
                localSpc2 = 0,
                localSsn = 8,
                remoteSpc = 1,
                remoteSpc2 = 0,
                remoteSsn = 8;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1113";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1111";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


        //dpc2 = 0;
        //opc2 = 0;
        //localPort2 = 0;
        //remotePort2 = 0;
        //localHost2 = "";
        //remoteHost2 = "";


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

        int opc = 1,
                opc2 = 0,
                dpc = 4,
                dpc2 = 0,
                localPort = 8014,
                localPort2 = 0,
                remotePort = 8017,
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
                localSpc2 = 0,
                localSsn = 8,
                remoteSpc = 4,
                remoteSpc2 = 0,
                remoteSsn = 7;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1111";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1114";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 4,
                opc2 = 0,
                dpc = 1,
                dpc2 = 0,
                localPort = 8017,
                localPort2 = 0,
                remotePort = 8014,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 4,
                localSpc2 = 0,
                localSsn = 7,
                remoteSpc = 1,
                remoteSpc2 = 0,
                remoteSsn = 8;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1114";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1111";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 2,
                opc2 = 0,
                dpc = 4,
                dpc2 = 0,
                localPort = 8020,
                localPort2 = 0,
                remotePort = 8021,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 2,
                localSpc2 = 0,
                localSsn = 6,
                remoteSpc = 4,
                remoteSpc2 = 0,
                remoteSsn = 7;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1112";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1114";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 4,
                opc2 = 0,
                dpc = 2,
                dpc2 = 0,
                localPort = 8021,
                localPort2 = 0,
                remotePort = 8020,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 4,
                localSpc2 = 0,
                localSsn = 7,
                remoteSpc = 2,
                remoteSpc2 = 0,
                remoteSsn = 6;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1114";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1112";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 10,
                opc2 = 0,
                dpc = 2,
                dpc2 = 0,
                localPort = 8035,
                localPort2 = 0,
                remotePort = 8036,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 10,
                localSpc2 = 0,
                localSsn = 149,
                remoteSpc = 2,
                remoteSpc2 = 0,
                remoteSsn = 6;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1115";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1112";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 2,
                opc2 = 0,
                dpc = 10,
                dpc2 = 0,
                localPort = 8036,
                localPort2 = 0,
                remotePort = 8035,
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
                localSpc2 = 0,
                localSsn = 6,
                remoteSpc = 10,
                remoteSpc2 = 0,
                remoteSsn = 149;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1112";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1115";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 11,
                opc2 = 0,
                dpc = 2,
                dpc2 = 0,
                localPort = 8031,
                localPort2 = 0,
                remotePort = 8032,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 11,
                localSpc2 = 0,
                localSsn = 147,
                remoteSpc = 2,
                remoteSpc2 = 0,
                remoteSsn = 6;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1116";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1112";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 2,
                opc2 = 0,
                dpc = 11,
                dpc2 = 0,
                localPort = 8032,
                localPort2 = 0,
                remotePort = 8031,
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
                localSpc2 = 0,
                localSsn = 6,
                remoteSpc = 11,
                remoteSpc2 = 0,
                remoteSsn = 147;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1112";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1116";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 11,
                opc2 = 0,
                dpc = 4,
                dpc2 = 0,
                localPort = 8033,
                localPort2 = 0,
                remotePort = 8034,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 11,
                localSpc2 = 0,
                localSsn = 147,
                remoteSpc = 4,
                remoteSpc2 = 0,
                remoteSsn = 7;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1116";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1114";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


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

        int opc = 4,
                opc2 = 0,
                dpc = 11,
                dpc2 = 0,
                localPort = 8034,
                localPort2 = 0,
                remotePort = 8033,
                remotePort2 = 0;

        String localHost = "127.0.0.1",
                localHost2 = "",
                remoteHost = "127.0.0.1",
                remoteHost2 = "";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 4,
                localSpc2 = 0,
                localSsn = 7,
                remoteSpc = 11,
                remoteSpc2 = 0,
                remoteSsn = 147;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "1114";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "1116";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


        configureL1(dpc, dpc2, isSctpServer, localHost, localHost2, localPort, localPort2, ipspType, opc, opc2, remoteHost, remoteHost2, remotePort, remotePort2);
        configureL2(callingPartyAddressDigits, localSpc, localSpc2, localSsn, remoteSpc, remoteSpc2, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);

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

        this.setInstance_L1(Instance_L1.createInstance("M3UA"));
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
        testAttackClientConfigurationData.setServiceCenterAddress("45454545");
        testAttackClientConfigurationData.setSmsCodingType(SmsCodingType.createInstance("GSM7"));
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
        testAttackServerConfigurationData.setSmsCodingType(SmsCodingType.createInstance("GSM7"));
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
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement st : e.getStackTrace()) {
            if (sb.length() > 0)
                sb.append("\n");
            sb.append(st.toString());
        }
        this.doSendNotif(source, msg + " - " + e.toString(), sb.toString());

        logger.log(logLevel, msg, e);
    }

    @Override
    public void sendNotif(String source, String msg, String userData, Level logLevel) {

        this.doSendNotif(source, msg, userData);

        logger.log(Level.INFO, msg + "\n" + userData);
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
        if (mtp3UserPart == null) {
            this.sendNotif(AttackTesterHost.SOURCE_NAME, "Error initializing SCCP: No Mtp3UserPart is defined at L1", "", Level.WARN);
        } else {
            this.instance_L2_B = this.sccp;
            this.sccp.setMtp3UserPart(mtp3UserPart);
            started = this.sccp.start();
            sccpStack = this.sccp.getSccpStack();
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
        switch (this.configurationData.getInstance_TestTask().intValue()) {
            case Instance_TestTask.VAL_ATTACK_CLIENT:
                if (curMap == null) {
                    this.sendNotif(AttackTesterHost.SOURCE_NAME, "Error initializing ATTACK_CLIENT: No MAP stack is defined at L3",
                            "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testAttackClient;
                    this.testAttackClient.setMapMan(curMap);
                    started = this.testAttackClient.start();
                }
                break;

            case Instance_TestTask.VAL_ATTACK_SERVER:
                if (curMap == null) {
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
    public void putInstance_L1Value(String val) {
        Instance_L1 x = Instance_L1.createInstance(val);
        if (x != null)
            this.setInstance_L1(x);
    }

    @Override
    public void putInstance_L2Value(String val) {
        Instance_L2 x = Instance_L2.createInstance(val);
        if (x != null)
            this.setInstance_L2(x);
    }

    @Override
    public void putInstance_L3Value(String val) {
        Instance_L3 x = Instance_L3.createInstance(val);
        if (x != null)
            this.setInstance_L3(x);
    }

    @Override
    public void putInstance_TestTaskValue(String val) {
        Instance_TestTask x = Instance_TestTask.createInstance(val);
        if (x != null)
            this.setInstance_TestTask(x);
    }

    @Override
    public String getName() {
        return appName;
    }

    @Override
    public String getPersistDir() {
        return persistDir;
    }

//    public void setPersistDir(String persistDir) {
//        this.persistDir = persistDir;
//    }

    @Override
    public void markStore() {
        needStore = true;
    }

    @Override
    public void checkStore() {
        if (needStore) {
            needStore = false;
            this.store();
        }
    }

    @Override
    public synchronized void store() {

        try {
            XMLObjectWriter writer = XMLObjectWriter.newInstance(new FileOutputStream(persistFile.toString()));
            writer.setBinding(binding);
            // writer.setReferenceResolver(new XMLReferenceResolver());
            writer.setIndentation(TAB_INDENT);

            writer.write(this.configurationData, CONFIGURATION_DATA, ConfigurationData.class);

            writer.close();
        } catch (Exception e) {
            this.sendNotif(SOURCE_NAME, "Error while persisting the Host state in file", e, Level.ERROR);
        }
    }

    private boolean load(File fn) {

        XMLObjectReader reader = null;
        try {
            if (!fn.exists()) {
                this.sendNotif(SOURCE_NAME, "Error while reading the Host state from file: file not found: " + persistFile, "",
                        Level.WARN);
                return false;
            }

            reader = XMLObjectReader.newInstance(new FileInputStream(fn));

            reader.setBinding(binding);

            this.configurationData = reader.read(CONFIGURATION_DATA, AttackConfigurationData.class);

            reader.close();

            return true;

        } catch (Exception ex) {
            this.sendNotif(SOURCE_NAME, "Error while reading the Host state from file", ex, Level.WARN);
            return false;
        }
    }

    private void performAttackLocationPSI() {
        this.getTestAttackClient().performProvideSubscriberInfoRequest();
    }

    public boolean isAttackDone() {
        return this.attackDone;
    }

    public void setAttackDone(boolean attackDone) {
        this.attackDone = attackDone;
    }

    public boolean gotPSIResponse() {
        return this.getTestAttackClient().getLastPsiResponse() != null;
    }

    public boolean gotSRIForSMResponse() {
        return this.getTestAttackClient().getLastSRIForSMResponse() != null;
    }

    public boolean gotAtiResponse() {
        return this.getTestAttackClient().getLastAtiResponse() != null;
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
    }
}
