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
import org.mobicents.protocols.ss7.tools.simulator.common.AddressNatureType;
import org.mobicents.protocols.ss7.tools.simulator.common.ConfigurationData;
import org.mobicents.protocols.ss7.tools.simulator.level1.*;
import org.mobicents.protocols.ss7.tools.simulator.level2.*;
import org.mobicents.protocols.ss7.tools.simulator.level3.*;
import org.mobicents.protocols.ss7.tools.simulator.tests.ati.TestAtiClientMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.ati.TestAtiServerMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.attack.location.TestSRIForSMClientMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.attack.location.TestSRIForSMServerMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.cap.TestCapScfMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.cap.TestCapSsfMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.sms.*;
import org.mobicents.protocols.ss7.tools.simulator.tests.ussd.TestUssdClientConfigurationData_OldFormat;
import org.mobicents.protocols.ss7.tools.simulator.tests.ussd.TestUssdClientMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.ussd.TestUssdServerConfigurationData_OldFormat;
import org.mobicents.protocols.ss7.tools.simulator.tests.ussd.TestUssdServerMan;

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
    private ConfigurationData configurationData = new ConfigurationData();
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
    TestUssdClientMan testUssdClientMan;
    TestUssdServerMan testUssdServerMan;
    TestSmsClientMan testSmsClientMan;
    TestSmsServerMan testSmsServerMan;
    TestCapSsfMan testCapSsfMan;
    TestCapScfMan testCapScfMan;
    TestAtiClientMan testAtiClientMan;
    TestAtiServerMan testAtiServerMan;
    TestSRIForSMClientMan testSRIForSMClientMan;
    TestSRIForSMServerMan testSRIForSMServerMan;

    private AttackType attackType;

    // testers

    public AttackTesterHost() {
        this.appName = null;
    }

    public AttackTesterHost(String appName, String persistDir, AttackType attackType) {
        this.attackType = attackType;
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

        this.testUssdClientMan = new TestUssdClientMan(appName);
        this.testUssdClientMan.setTesterHost(this);

        this.testUssdServerMan = new TestUssdServerMan(appName);
        this.testUssdServerMan.setTesterHost(this);

        this.testSmsClientMan = new TestSmsClientMan(appName);
        this.testSmsClientMan.setTesterHost(this);

        this.testSmsServerMan = new TestSmsServerMan(appName);
        this.testSmsServerMan.setTesterHost(this);

        this.testCapSsfMan = new TestCapSsfMan(appName);
        this.testCapSsfMan.setTesterHost(this);

        this.testCapScfMan = new TestCapScfMan(appName);
        this.testCapScfMan.setTesterHost(this);

        this.testAtiClientMan = new TestAtiClientMan(appName);
        this.testAtiClientMan.setTesterHost(this);

        this.testAtiServerMan = new TestAtiServerMan(appName);
        this.testAtiServerMan.setTesterHost(this);

        this.testSRIForSMClientMan = new TestSRIForSMClientMan(appName);
        this.testSRIForSMClientMan.setTesterHost(this);

        this.testSRIForSMServerMan = new TestSRIForSMServerMan(appName);
        this.testSRIForSMServerMan.setTesterHost(this);

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

        File fnOld = new File(persistFileOld.toString());
        File fn = new File(persistFile.toString());

        if (this.loadOld(fnOld)) {
            this.store();
        } else {
            this.load(fn);
        }
        if (fnOld.exists())
            fnOld.delete();


        switch(attackType) {
            case SMS_SERVER:
                this.configureAttackServer();
                break;
            case SMS_CLIENT:
                this.configureAttackClient();
                break;
            default:
                break;
        }
    }

    private void configureAttackServer() {

        //////// L1 Configuration Data //////////

        int dpc = 1,
                opc = 2,
                localPort = 8012,
                remotePort = 8011;

        String localHost = "127.0.0.1",
                remoteHost = "127.0.0.1";

        boolean isSctpServer = true;
        IPSPType ipspType = IPSPType.SERVER;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 2,
                localSsn = 8,
                remoteSpc = 1,
                remoteSsn = 8;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "000000000000002";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "000000000000001";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////
        ///////////////////////////////////////////


        configureL1(dpc, isSctpServer, localHost, localPort, ipspType, opc, remoteHost, remotePort);
        configureL2(callingPartyAddressDigits, localSpc, localSsn, remoteSpc, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);
        configureSMSTestServer();
    }

    private void configureAttackClient() {

        //////// L1 Configuration Data //////////

        int dpc = 2,
                opc = 1,
                localPort = 8011,
                remotePort = 8012;

        String localHost = "127.0.0.1",
                remoteHost = "127.0.0.1";

        boolean isSctpServer = false;
        IPSPType ipspType = IPSPType.CLIENT;

        ////////////////////////////////////////


        //////// L2 Configuration Data //////////

        int localSpc = 1,
                localSsn = 6,
                remoteSpc = 2,
                remoteSsn = 8;
        boolean routeonGtMode = true;
        String callingPartyAddressDigits = "000000000000001";

        ////////////////////////////////////////


        //////// L3 Configuration Data //////////

        String destReferenceDigits = "",
                origReferenceDigits = "",
                remoteAddressDigits = "000000000000002";

        ////////////////////////////////////////


        //////// Test Configuration Data //////////



        ///////////////////////////////////////////


        configureL1(dpc, isSctpServer, localHost, localPort, ipspType, opc, remoteHost, remotePort);
        configureL2(callingPartyAddressDigits, localSpc, localSsn, remoteSpc, remoteSsn, routeonGtMode);
        configureL3(destReferenceDigits, origReferenceDigits, remoteAddressDigits);
        configureSMSTestClient();
    }

    public void configureL1(int dpc, boolean isSctpServer, String localHost, int localPort, IPSPType ipspType, int opc, String remoteHost, int remotePort) {
        this.setInstance_L1(Instance_L1.createInstance("M3UA"));
        M3uaConfigurationData m3uaConfigurationData = this.getConfigurationData().getM3uaConfigurationData();

        m3uaConfigurationData.setDpc(dpc);
        m3uaConfigurationData.setIpChannelType(IpChannelType.SCTP);
        m3uaConfigurationData.setIsSctpServer(isSctpServer);
        m3uaConfigurationData.setLocalHost(localHost);
        m3uaConfigurationData.setLocalPort(localPort);
        m3uaConfigurationData.setM3uaExchangeType(ExchangeType.SE);
        m3uaConfigurationData.setM3uaFunctionality(Functionality.IPSP);
        m3uaConfigurationData.setM3uaIPSPType(ipspType);
        m3uaConfigurationData.setNetworkAppearance(102L);
        m3uaConfigurationData.setOpc(opc);
        m3uaConfigurationData.setRemoteHost(remoteHost);
        m3uaConfigurationData.setRemotePort(remotePort);
        m3uaConfigurationData.setRoutingContext(101L);
        m3uaConfigurationData.setRoutingLabelFormat(RoutingLabelFormat.ITU);
        m3uaConfigurationData.setSi(3);
        m3uaConfigurationData.setStorePcapTrace(false);
        m3uaConfigurationData.setTrafficModeType(TrafficModeType.Loadshare);
        m3uaConfigurationData.setTrafficModeType(TrafficModeType.Loadshare);

        this.setInstance_L1(Instance_L1.createInstance("M3UA"));
    }

    public void configureL2(String callingPartyAddressDigits, int localSpc, int localSsn, int remoteSpc, int remoteSsn, boolean routeonGtMode) {
        this.setInstance_L2(Instance_L2.createInstance("SCCP"));
        SccpConfigurationData sccpConfigurationData = this.getConfigurationData().getSccpConfigurationData();

        sccpConfigurationData.setCallingPartyAddressDigits(callingPartyAddressDigits);
        sccpConfigurationData.setGlobalTitleType(GlobalTitleType.createInstance("Translation type, numbering plan, encoding scheme and NOA ind"));
        sccpConfigurationData.setLocalSpc(localSpc);
        sccpConfigurationData.setLocalSsn(localSsn);
        sccpConfigurationData.setNatureOfAddress(NatureOfAddress.INTERNATIONAL);
        sccpConfigurationData.setNi(2);
        sccpConfigurationData.setNumberingPlan(NumberingPlan.ISDN_MOBILE);
        sccpConfigurationData.setRemoteSpc(remoteSpc);
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

    public void configureSMSTestClient() {
        this.setInstance_TestTask(Instance_TestTask.createInstance("SMS_TEST_CLIENT"));
        TestSmsClientConfigurationData testSmsClientConfigurationData = this.getConfigurationData().getTestSmsClientConfigurationData();

        //testSmsClientConfigurationData.setAddressNature();
        testSmsClientConfigurationData.setContinueDialog(false);
        testSmsClientConfigurationData.setMapProtocolVersion(MapProtocolVersion.createInstance("MAP protocol version 3"));
        testSmsClientConfigurationData.setMtFSMReaction(MtFSMReaction.createInstance("Return success"));
        testSmsClientConfigurationData.setNationalLanguageCode(0);
        testSmsClientConfigurationData.setNumberingPlan(org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN);
        testSmsClientConfigurationData.setNumberingPlanIdentification(NumberingPlanIdentification.ISDNTelephoneNumberingPlan);
        testSmsClientConfigurationData.setOneNotificationFor100Dialogs(false);
        testSmsClientConfigurationData.setReportSMDeliveryStatusReaction(ReportSMDeliveryStatusReaction.createInstance("Return success"));
        testSmsClientConfigurationData.setReturn20PersDeliveryErrors(false);
        //testSmsClientConfigurationData.setServiceCenterAddress();
        testSmsClientConfigurationData.setSmsCodingType(SmsCodingType.createInstance("GSM7"));
        testSmsClientConfigurationData.setSmscSsn(8);
        testSmsClientConfigurationData.setSRIInformServiceCenter(SRIInformServiceCenter.createInstance("No data in MWD file"));
        testSmsClientConfigurationData.setSRIReaction(SRIReaction.createInstance("Return success"));
        testSmsClientConfigurationData.setSriResponseImsi("1234567890");
        testSmsClientConfigurationData.setSriResponseVlr("11111111");
        testSmsClientConfigurationData.setSRIScAddressNotIncluded(false);
        testSmsClientConfigurationData.setTypeOfNumber(TypeOfNumber.InternationalNumber);

    }

    public void configureSMSTestServer() {
        this.setInstance_TestTask(Instance_TestTask.createInstance("SMS_TEST_SERVER"));
        TestSmsServerConfigurationData testSmsServerConfigurationData = this.getConfigurationData().getTestSmsServerConfigurationData();

        testSmsServerConfigurationData.setAddressNature(AddressNature.international_number);
        testSmsServerConfigurationData.setGprsSupportIndicator(false);
        testSmsServerConfigurationData.setHlrSsn(6);
        testSmsServerConfigurationData.setMapProtocolVersion(MapProtocolVersion.createInstance("MAP protocol version 3"));
        testSmsServerConfigurationData.setNumberingPlan(org.mobicents.protocols.ss7.map.api.primitives.NumberingPlan.ISDN);
        testSmsServerConfigurationData.setNumberingPlanIdentification(NumberingPlanIdentification.ISDNTelephoneNumberingPlan);
        testSmsServerConfigurationData.setSendSrsmdsIfError(false);
        //testSmsServerConfigurationData.setServiceCenterAddress();
        testSmsServerConfigurationData.setSmsCodingType(SmsCodingType.createInstance("GSM7"));
        testSmsServerConfigurationData.setTypeOfNumber(TypeOfNumber.InternationalNumber);
        testSmsServerConfigurationData.setVlrSsn(8);
    }

    @Override
    public ConfigurationData getConfigurationData() {
        return this.configurationData;
    }

    @Override
    public M3uaMan getM3uaMan() {
        return this.m3ua;
    }

    @Override
    public DialogicMan getDialogicMan() {
        return this.dialogic;
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

    @Override
    public TestUssdClientMan getTestUssdClientMan() {
        return this.testUssdClientMan;
    }

    @Override
    public TestUssdServerMan getTestUssdServerMan() {
        return this.testUssdServerMan;
    }

    @Override
    public TestSmsClientMan getTestSmsClientMan() {
        return this.testSmsClientMan;
    }

    @Override
    public TestSmsServerMan getTestSmsServerMan() {
        return this.testSmsServerMan;
    }

    @Override
    public TestCapSsfMan getTestCapSsfMan() {
        return this.testCapSsfMan;
    }

    @Override
    public TestCapScfMan getTestCapScfMan() {
        return this.testCapScfMan;
    }

    @Override
    public TestAtiClientMan getTestAtiClientMan() {
        return this.testAtiClientMan;
    }

    @Override
    public TestAtiServerMan getTestAtiServerMan() {
        return this.testAtiServerMan;
    }

    @Override
    public TestSRIForSMClientMan getTestSRIForSMClientMan() {
        return this.testSRIForSMClientMan;
    }

    @Override
    public TestSRIForSMServerMan getTestSRIForSMServerMan() {
        return this.testSRIForSMServerMan;
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
            case Instance_TestTask.VAL_USSD_TEST_CLIENT:
                if (curMap == null) {
                    this.sendNotif(AttackTesterHost.SOURCE_NAME,
                            "Error initializing USSD_TEST_CLIENT: No MAP stack is defined at L3", "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testUssdClientMan;
                    this.testUssdClientMan.setMapMan(curMap);
                    started = this.testUssdClientMan.start();
                }
                break;

            case Instance_TestTask.VAL_USSD_TEST_SERVER:
                if (curMap == null) {
                    this.sendNotif(AttackTesterHost.SOURCE_NAME,
                            "Error initializing USSD_TEST_SERVER: No MAP stack is defined at L3", "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testUssdServerMan;
                    this.testUssdServerMan.setMapMan(curMap);
                    started = this.testUssdServerMan.start();
                }
                break;

            case Instance_TestTask.VAL_SMS_TEST_CLIENT:
                if (curMap == null) {
                    this.sendNotif(AttackTesterHost.SOURCE_NAME, "Error initializing SMS_TEST_CLIENT: No MAP stack is defined at L3",
                            "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testSmsClientMan;
                    this.testSmsClientMan.setMapMan(curMap);
                    started = this.testSmsClientMan.start();
                }
                break;

            case Instance_TestTask.VAL_SMS_TEST_SERVER:
                if (curMap == null) {
                    this.sendNotif(AttackTesterHost.SOURCE_NAME, "Error initializing SMS_TEST_SERVER: No MAP stack is defined at L3",
                            "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testSmsServerMan;
                    this.testSmsServerMan.setMapMan(curMap);
                    started = this.testSmsServerMan.start();
                }
                break;

            case Instance_TestTask.VAL_CAP_TEST_SCF:
                if (curCap == null) {
                    this.sendNotif(AttackTesterHost.SOURCE_NAME,
                            "Error initializing VAL_CAP_TEST_SCF: No CAP stack is defined at L3", "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testCapScfMan;
                    this.testCapScfMan.setCapMan(curCap);
                    started = this.testCapScfMan.start();
                }
                break;

            case Instance_TestTask.VAL_CAP_TEST_SSF:
                if (curCap == null) {
                    this.sendNotif(AttackTesterHost.SOURCE_NAME,
                            "Error initializing VAL_CAP_TEST_SSF: No CAP stack is defined at L3", "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testCapSsfMan;
                    this.testCapSsfMan.setCapMan(curCap);
                    started = this.testCapSsfMan.start();
                }
                break;

            case Instance_TestTask.VAL_ATI_TEST_CLIENT:
                if (curMap == null) {
                    this.sendNotif(AttackTesterHost.SOURCE_NAME, "Error initializing ATI_TEST_CLIENT: No MAP stack is defined at L3",
                            "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testAtiClientMan;
                    this.testAtiClientMan.setMapMan(curMap);
                    started = this.testAtiClientMan.start();
                }
                break;

            case Instance_TestTask.VAL_ATI_TEST_SERVER:
                if (curMap == null) {
                    this.sendNotif(AttackTesterHost.SOURCE_NAME, "Error initializing ATI_TEST_SERVER: No MAP stack is defined at L3",
                            "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testAtiServerMan;
                    this.testAtiServerMan.setMapMan(curMap);
                    started = this.testAtiServerMan.start();
                }
                break;

            case Instance_TestTask.VAL_SRI_ATTACK_TEST_CLIENT:
                if (curMap == null) {
                    this.sendNotif(AttackTesterHost.SOURCE_NAME, "Error initializing SRI_ATTACK_CLIENT: No MAP stack is defined at L3",
                            "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testSRIForSMClientMan;
                    this.testSRIForSMClientMan.setMapMan(curMap);
                    started = this.testSRIForSMClientMan.start();
                }
                break;

            case Instance_TestTask.VAL_SRI_ATTACK_TEST_SERVER:
                if (curMap == null) {
                    this.sendNotif(AttackTesterHost.SOURCE_NAME, "Error initializing SRI_ATTACK_SERVER: No MAP stack is defined at L3",
                            "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testSRIForSMServerMan;
                    this.testSRIForSMServerMan.setMapMan(curMap);
                    started = this.testSRIForSMServerMan.start();
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

            this.configurationData = reader.read(CONFIGURATION_DATA, ConfigurationData.class);

            reader.close();

            return true;

        } catch (Exception ex) {
            this.sendNotif(SOURCE_NAME, "Error while reading the Host state from file", ex, Level.WARN);
            return false;
        }
    }

    private boolean loadOld(File fn) {

        XMLObjectReader reader = null;
        try {
            if (!fn.exists()) {
                // this.sendNotif(SOURCE_NAME, "Error while reading the Host state from file: file not found: " + persistFile,
                // "", Level.WARN);
                return false;
            }

            reader = XMLObjectReader.newInstance(new FileInputStream(fn));

            reader.setBinding(binding);
            this.configurationData.setInstance_L1(Instance_L1.createInstance(reader.read(ConfigurationData.INSTANCE_L1,
                    String.class)));
            this.configurationData.setInstance_L2(Instance_L2.createInstance(reader.read(ConfigurationData.INSTANCE_L2,
                    String.class)));
            this.configurationData.setInstance_L3(Instance_L3.createInstance(reader.read(ConfigurationData.INSTANCE_L3,
                    String.class)));
            this.configurationData.setInstance_TestTask(Instance_TestTask.createInstance(reader.read(
                    ConfigurationData.INSTANCE_TESTTASK, String.class)));

            M3uaConfigurationData_OldFormat _m3ua = reader.read(ConfigurationData.M3UA, M3uaConfigurationData_OldFormat.class);
            this.m3ua.setSctpLocalHost(_m3ua.getLocalHost());
            this.m3ua.setSctpLocalPort(_m3ua.getLocalPort());
            this.m3ua.setSctpRemoteHost(_m3ua.getRemoteHost());
            this.m3ua.setSctpRemotePort(_m3ua.getRemotePort());
            this.configurationData.getM3uaConfigurationData().setIpChannelType(_m3ua.getIpChannelType());
            this.m3ua.setSctpIsServer(_m3ua.getIsSctpServer());
            this.m3ua.doSetExtraHostAddresses(_m3ua.getSctpExtraHostAddresses());
            this.configurationData.getM3uaConfigurationData().setM3uaFunctionality(_m3ua.getM3uaFunctionality());
            this.configurationData.getM3uaConfigurationData().setM3uaIPSPType(_m3ua.getM3uaIPSPType());
            this.configurationData.getM3uaConfigurationData().setM3uaExchangeType(_m3ua.getM3uaExchangeType());
            this.m3ua.setM3uaDpc(_m3ua.getDpc());
            this.m3ua.setM3uaOpc(_m3ua.getOpc());
            this.m3ua.setM3uaSi(_m3ua.getSi());

            DialogicConfigurationData_OldFormat _dial = reader.read(ConfigurationData.DIALOGIC,
                    DialogicConfigurationData_OldFormat.class);
            this.dialogic.setSourceModuleId(_dial.getSourceModuleId());
            this.dialogic.setDestinationModuleId(_dial.getDestinationModuleId());

            SccpConfigurationData_OldFormat _sccp = reader.read(ConfigurationData.SCCP, SccpConfigurationData_OldFormat.class);
            this.sccp.setRouteOnGtMode(_sccp.isRouteOnGtMode());
            this.sccp.setRemoteSpc(_sccp.getRemoteSpc());
            this.sccp.setLocalSpc(_sccp.getLocalSpc());
            this.sccp.setNi(_sccp.getNi());
            this.sccp.setRemoteSsn(_sccp.getRemoteSsn());
            this.sccp.setLocalSsn(_sccp.getLocalSsn());
            this.sccp.setGlobalTitleType(_sccp.getGlobalTitleType());
            this.sccp.setNatureOfAddress(new NatureOfAddressType(_sccp.getNatureOfAddress().getValue()));
            this.sccp.setNumberingPlan(new NumberingPlanSccpType(_sccp.getNumberingPlan().getValue()));
            this.sccp.setTranslationType(_sccp.getTranslationType());
            this.sccp.setCallingPartyAddressDigits(_sccp.getCallingPartyAddressDigits());
            // this.sccp.setExtraLocalAddressDigits(_sccp.getExtraLocalAddressDigits());

            MapConfigurationData_OldFormat _map = reader.read(ConfigurationData.MAP, MapConfigurationData_OldFormat.class);
            // this.map.setLocalSsn(_map.getLocalSsn());
            // this.map.setRemoteSsn(_map.getRemoteSsn());
            this.map.setRemoteAddressDigits(_map.getRemoteAddressDigits());
            this.map.setOrigReference(_map.getOrigReference());
            this.map.setOrigReferenceAddressNature(new AddressNatureType(_map.getOrigReferenceAddressNature().getIndicator()));
            this.map.setOrigReferenceNumberingPlan(new NumberingPlanMapType(_map.getOrigReferenceNumberingPlan().getIndicator()));
            this.map.setDestReference(_map.getDestReference());
            this.map.setDestReferenceAddressNature(new AddressNatureType(_map.getDestReferenceAddressNature().getIndicator()));
            this.map.setDestReferenceNumberingPlan(new NumberingPlanMapType(_map.getDestReferenceNumberingPlan().getIndicator()));

            TestUssdClientConfigurationData_OldFormat _TestUssdClientMan = reader.read(ConfigurationData.TEST_USSD_CLIENT,
                    TestUssdClientConfigurationData_OldFormat.class);
            this.testUssdClientMan.setMsisdnAddress(_TestUssdClientMan.getMsisdnAddress());
            this.testUssdClientMan.setMsisdnAddressNature(new AddressNatureType(_TestUssdClientMan.getMsisdnAddressNature()
                    .getIndicator()));
            this.testUssdClientMan.setMsisdnNumberingPlan(new NumberingPlanMapType(_TestUssdClientMan.getMsisdnNumberingPlan()
                    .getIndicator()));
            this.testUssdClientMan.setDataCodingScheme(_TestUssdClientMan.getDataCodingScheme());
            this.testUssdClientMan.setAlertingPattern(_TestUssdClientMan.getAlertingPattern());
            this.testUssdClientMan.setUssdClientAction(_TestUssdClientMan.getUssdClientAction());
            this.testUssdClientMan.setAutoRequestString(_TestUssdClientMan.getAutoRequestString());
            this.testUssdClientMan.setMaxConcurrentDialogs(_TestUssdClientMan.getMaxConcurrentDialogs());
            this.testUssdClientMan.setOneNotificationFor100Dialogs(_TestUssdClientMan.isOneNotificationFor100Dialogs());

            TestUssdServerConfigurationData_OldFormat _TestUssdServerMan = reader.read(ConfigurationData.TEST_USSD_SERVER,
                    TestUssdServerConfigurationData_OldFormat.class);
            this.testUssdServerMan.setMsisdnAddress(_TestUssdServerMan.getMsisdnAddress());
            this.testUssdServerMan.setMsisdnAddressNature(new AddressNatureType(_TestUssdServerMan.getMsisdnAddressNature()
                    .getIndicator()));
            this.testUssdServerMan.setMsisdnNumberingPlan(new NumberingPlanMapType(_TestUssdServerMan.getMsisdnNumberingPlan()
                    .getIndicator()));
            this.testUssdServerMan.setDataCodingScheme(_TestUssdServerMan.getDataCodingScheme());
            this.testUssdServerMan.setAlertingPattern(_TestUssdServerMan.getAlertingPattern());
            this.testUssdServerMan.setProcessSsRequestAction(_TestUssdServerMan.getProcessSsRequestAction());
            this.testUssdServerMan.setAutoResponseString(_TestUssdServerMan.getAutoResponseString());
            this.testUssdServerMan.setAutoUnstructured_SS_RequestString(_TestUssdServerMan
                    .getAutoUnstructured_SS_RequestString());
            this.testUssdServerMan.setOneNotificationFor100Dialogs(_TestUssdServerMan.isOneNotificationFor100Dialogs());

            TestSmsClientConfigurationData_OldFormat _TestSmsClientMan = reader.read(ConfigurationData.TEST_SMS_CLIENT,
                    TestSmsClientConfigurationData_OldFormat.class);
            this.testSmsClientMan.setAddressNature(new AddressNatureType(_TestSmsClientMan.getAddressNature().getIndicator()));
            this.testSmsClientMan
                    .setNumberingPlan(new NumberingPlanMapType(_TestSmsClientMan.getNumberingPlan().getIndicator()));
            this.testSmsClientMan.setServiceCenterAddress(_TestSmsClientMan.getServiceCenterAddress());
            this.testSmsClientMan.setMapProtocolVersion(_TestSmsClientMan.getMapProtocolVersion());
            this.testSmsClientMan.setSRIResponseImsi(_TestSmsClientMan.getSriResponseImsi());
            this.testSmsClientMan.setSRIResponseVlr(_TestSmsClientMan.getSriResponseVlr());
            this.testSmsClientMan.setSmscSsn(_TestSmsClientMan.getSmscSsn());
            this.testSmsClientMan.setTypeOfNumber(new TypeOfNumberType(_TestSmsClientMan.getTypeOfNumber().getCode()));
            this.testSmsClientMan.setNumberingPlanIdentification(new NumberingPlanIdentificationType(_TestSmsClientMan
                    .getNumberingPlanIdentification().getCode()));
            this.testSmsClientMan.setSmsCodingType(_TestSmsClientMan.getSmsCodingType());

            TestSmsServerConfigurationData_OldFormat _TestSmsServerMan = reader.read(ConfigurationData.TEST_SMS_SERVER,
                    TestSmsServerConfigurationData_OldFormat.class);
            this.testSmsServerMan.setAddressNature(new AddressNatureType(_TestSmsServerMan.getAddressNature().getIndicator()));
            this.testSmsServerMan
                    .setNumberingPlan(new NumberingPlanMapType(_TestSmsServerMan.getNumberingPlan().getIndicator()));
            this.testSmsServerMan.setServiceCenterAddress(_TestSmsServerMan.getServiceCenterAddress());
            this.testSmsServerMan.setMapProtocolVersion(_TestSmsServerMan.getMapProtocolVersion());
            this.testSmsServerMan.setHlrSsn(_TestSmsServerMan.getHlrSsn());
            this.testSmsServerMan.setVlrSsn(_TestSmsServerMan.getVlrSsn());
            this.testSmsServerMan.setTypeOfNumber(new TypeOfNumberType(_TestSmsServerMan.getTypeOfNumber().getCode()));
            this.testSmsServerMan.setNumberingPlanIdentification(new NumberingPlanIdentificationType(_TestSmsServerMan
                    .getNumberingPlanIdentification().getCode()));
            this.testSmsServerMan.setSmsCodingType(_TestSmsServerMan.getSmsCodingType());

            reader.close();

            return true;

        } catch (Exception ex) {
            this.sendNotif(SOURCE_NAME, "Error while reading the Host state from file", ex, Level.WARN);
            return false;
        }
    }


    public enum AttackType {
        ALL,
        SMS_CLIENT,
        SMS_SERVER,
        LOCATION_SRIFORSM,
        INTERCEPT_SMS
    }
}
