package org.mobicents.protocols.ss7.tools.simulator;

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
import org.mobicents.protocols.ss7.tools.simulator.common.ConfigurationData;
import org.mobicents.protocols.ss7.tools.simulator.level1.M3uaConfigurationData;
import org.mobicents.protocols.ss7.tools.simulator.level1.M3uaMan;
import org.mobicents.protocols.ss7.tools.simulator.level2.GlobalTitleType;
import org.mobicents.protocols.ss7.tools.simulator.level2.SccpConfigurationData;
import org.mobicents.protocols.ss7.tools.simulator.level2.SccpMan;
import org.mobicents.protocols.ss7.tools.simulator.level3.CapMan;
import org.mobicents.protocols.ss7.tools.simulator.level3.MapConfigurationData;
import org.mobicents.protocols.ss7.tools.simulator.level3.MapMan;
import org.mobicents.protocols.ss7.tools.simulator.level3.MapProtocolVersion;
import org.mobicents.protocols.ss7.tools.simulator.management.*;
import org.mobicents.protocols.ss7.tools.simulator.tests.ati.TestAtiClientMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.ati.TestAtiServerMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.attack.location.TestSRIForSMClientMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.attack.location.TestSRIForSMServerMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.cap.TestCapScfMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.cap.TestCapSsfMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.sms.*;
import org.mobicents.protocols.ss7.tools.simulator.tests.ussd.TestUssdClientMan;
import org.mobicents.protocols.ss7.tools.simulator.tests.ussd.TestUssdServerMan;

import javax.management.Notification;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import static org.mobicents.protocols.ss7.tools.simulator.AttackSimulationHost.AttackType.*;

/**
 * @author Kristoffer Jensen
 */
public class AttackSimulationHost extends TesterHost implements Stoppable {

    private static final Logger logger = Logger.getLogger(AttackSimulationHost.class);

    private final String SOURCE_NAME = "ATTACK_HOST";
    private final String appName;

    private boolean isStarted = false;
    private boolean needQuit = false;
    private long sequenceNumber = 0;

    private ConfigurationData configurationData = new ConfigurationData();

    private Stoppable instance_L1_B = null;
    private Stoppable instance_L2_B = null;
    private Stoppable instance_L3_B = null;
    private Stoppable instance_TestTask_B = null;

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

    public AttackSimulationHost() {
        super();
        this.appName = null;
    }

    public AttackSimulationHost(String appName, AttackType attackType) {
        this.appName = appName;
        this.attackType = attackType;

        //this.serverHost.addNotificationListener(this, null, null);

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

        start();
    }

    private void setupLayers(String appName) {
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


        configureL1(this, dpc, isSctpServer, localHost, localPort, ipspType, opc, remoteHost, remotePort);
        configureL2(this, callingPartyAddressDigits, localSpc, localSsn, remoteSpc, remoteSsn, routeonGtMode);
        configureL3(this, destReferenceDigits, origReferenceDigits, remoteAddressDigits);
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
                localSsn = 8,
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


        configureL1(this, dpc, isSctpServer, localHost, localPort, ipspType, opc, remoteHost, remotePort);
        configureL2(this, callingPartyAddressDigits, localSpc, localSsn, remoteSpc, remoteSsn, routeonGtMode);
        configureL3(this, destReferenceDigits, origReferenceDigits, remoteAddressDigits);
        configureSMSTestClient();
    }

    public void configureL1(AttackSimulationHost testerHost, int dpc, boolean isSctpServer, String localHost, int localPort, IPSPType ipspType, int opc, String remoteHost, int remotePort) {
        testerHost.setInstance_L1(Instance_L1.createInstance("M3UA"));
        M3uaConfigurationData m3uaConfigurationData = testerHost.getConfigurationData().getM3uaConfigurationData();

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
    }

    public void configureL2(TesterHost testerHost, String callingPartyAddressDigits, int localSpc, int localSsn, int remoteSpc, int remoteSsn, boolean routeonGtMode) {
        testerHost.setInstance_L2(Instance_L2.createInstance("SCCP"));
        SccpConfigurationData sccpConfigurationData = testerHost.getConfigurationData().getSccpConfigurationData();

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

    public void configureL3(AttackSimulationHost testerHost, String destReferenceDigits, String origReferenceDigits, String remoteAddressDigits) {
        testerHost.setInstance_L3(Instance_L3.createInstance("TCAP+MAP"));
        MapConfigurationData mapConfigurationData = testerHost.getConfigurationData().getMapConfigurationData();

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
        // if (showInConsole) {
        // logger.error(msg, e);
        // } else {
        // logger.debug(msg, e);
        // }
    }

    @Override
    public void sendNotif(String source, String msg, String userData, Level logLevel) {

        this.doSendNotif(source, msg, userData);

        logger.log(Level.INFO, msg + "\n" + userData);
//        logger.log(logLevel, msg + "\n" + userData);

        // if (showInConsole) {
        // logger.warn(msg);
        // } else {
        // logger.debug(msg);
        // }
    }

    private synchronized void doSendNotif(String source, String msg, String userData) {
        Notification notif = new Notification(SS7_EVENT + "-" + source, "TesterHost", ++sequenceNumber,
                System.currentTimeMillis(), msg);
        notif.setUserData(userData);
        this.sendNotification(notif);
    }

    @Override
    public void start() {
        this.stop();

        // Start L1
        boolean started = false;
        Mtp3UserPart mtp3UserPart = null;
        this.instance_L1_B = this.m3ua;
        started = this.m3ua.start();
        mtp3UserPart = this.m3ua.getMtp3UserPart();

        if(!started) {
            this.sendNotif(this.SOURCE_NAME, "Layer 1 has not started.", "", Level.WARN);
            this.stop();
            return;
        }

        // Start L2
        started = false;
        SccpStack sccpStack = null;
        if (mtp3UserPart == null) {
            this.sendNotif(this.SOURCE_NAME, "Error initializing SCCP: No Mtp3UserPart is defined at L1", "", Level.WARN);
        } else {
            this.instance_L2_B = this.sccp;
            this.sccp.setMtp3UserPart(mtp3UserPart);
            started = this.sccp.start();
            sccpStack = this.sccp.getSccpStack();
        }

        if(!started) {
            this.sendNotif(this.SOURCE_NAME, "Layer 2 has not started.", "", Level.WARN);
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
                    this.sendNotif(this.SOURCE_NAME, "Error initializing TCAP+MAP: No SccpStack is defined at L2", "",
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
                    this.sendNotif(this.SOURCE_NAME, "Error initializing TCAP+CAP: No SccpStack is defined at L2", "",
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
                this.sendNotif(this.SOURCE_NAME, "Instance_L3." + this.configurationData.getInstance_L3().toString()
                        + " has not been implemented yet", "", Level.WARN);
                break;
        }
        if (!started) {
            this.sendNotif(this.SOURCE_NAME, "Layer 3 has not started", "", Level.WARN);
            this.stop();
            return;
        }

        // Strart Testers
        started = false;
        switch (this.configurationData.getInstance_TestTask().intValue()) {
            case Instance_TestTask.VAL_USSD_TEST_CLIENT:
                if (curMap == null) {
                    this.sendNotif(this.SOURCE_NAME,
                            "Error initializing USSD_TEST_CLIENT: No MAP stack is defined at L3", "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testUssdClientMan;
                    this.testUssdClientMan.setMapMan(curMap);
                    started = this.testUssdClientMan.start();
                }
                break;

            case Instance_TestTask.VAL_USSD_TEST_SERVER:
                if (curMap == null) {
                    this.sendNotif(this.SOURCE_NAME,
                            "Error initializing USSD_TEST_SERVER: No MAP stack is defined at L3", "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testUssdServerMan;
                    this.testUssdServerMan.setMapMan(curMap);
                    started = this.testUssdServerMan.start();
                }
                break;

            case Instance_TestTask.VAL_SMS_TEST_CLIENT:
                if (curMap == null) {
                    this.sendNotif(this.SOURCE_NAME, "Error initializing SMS_TEST_CLIENT: No MAP stack is defined at L3",
                            "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testSmsClientMan;
                    this.testSmsClientMan.setMapMan(curMap);
                    started = this.testSmsClientMan.start();
                }
                break;

            case Instance_TestTask.VAL_SMS_TEST_SERVER:
                if (curMap == null) {
                    this.sendNotif(this.SOURCE_NAME, "Error initializing SMS_TEST_SERVER: No MAP stack is defined at L3",
                            "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testSmsServerMan;
                    this.testSmsServerMan.setMapMan(curMap);
                    started = this.testSmsServerMan.start();
                }
                break;

            case Instance_TestTask.VAL_CAP_TEST_SCF:
                if (curCap == null) {
                    this.sendNotif(this.SOURCE_NAME,
                            "Error initializing VAL_CAP_TEST_SCF: No CAP stack is defined at L3", "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testCapScfMan;
                    this.testCapScfMan.setCapMan(curCap);
                    started = this.testCapScfMan.start();
                }
                break;

            case Instance_TestTask.VAL_CAP_TEST_SSF:
                if (curCap == null) {
                    this.sendNotif(this.SOURCE_NAME,
                            "Error initializing VAL_CAP_TEST_SSF: No CAP stack is defined at L3", "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testCapSsfMan;
                    this.testCapSsfMan.setCapMan(curCap);
                    started = this.testCapSsfMan.start();
                }
                break;

            case Instance_TestTask.VAL_ATI_TEST_CLIENT:
                if (curMap == null) {
                    this.sendNotif(this.SOURCE_NAME, "Error initializing ATI_TEST_CLIENT: No MAP stack is defined at L3",
                            "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testAtiClientMan;
                    this.testAtiClientMan.setMapMan(curMap);
                    started = this.testAtiClientMan.start();
                }
                break;

            case Instance_TestTask.VAL_ATI_TEST_SERVER:
                if (curMap == null) {
                    this.sendNotif(this.SOURCE_NAME, "Error initializing ATI_TEST_SERVER: No MAP stack is defined at L3",
                            "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testAtiServerMan;
                    this.testAtiServerMan.setMapMan(curMap);
                    started = this.testAtiServerMan.start();
                }
                break;

            case Instance_TestTask.VAL_SRI_ATTACK_TEST_CLIENT:
                if (curMap == null) {
                    this.sendNotif(this.SOURCE_NAME, "Error initializing SRI_ATTACK_CLIENT: No MAP stack is defined at L3",
                            "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testSRIForSMClientMan;
                    this.testSRIForSMClientMan.setMapMan(curMap);
                    started = this.testSRIForSMClientMan.start();
                }
                break;

            case Instance_TestTask.VAL_SRI_ATTACK_TEST_SERVER:
                if (curMap == null) {
                    this.sendNotif(this.SOURCE_NAME, "Error initializing SRI_ATTACK_SERVER: No MAP stack is defined at L3",
                            "", Level.WARN);
                } else {
                    this.instance_TestTask_B = this.testSRIForSMServerMan;
                    this.testSRIForSMServerMan.setMapMan(curMap);
                    started = this.testSRIForSMServerMan.start();
                }
                break;

            default:
                // TODO: implement others test tasks ...
                this.sendNotif(this.SOURCE_NAME, "Instance_TestTask."
                                + this.configurationData.getInstance_TestTask().toString() + " has not been implemented yet", "",
                        Level.WARN);
                break;
        }
        if (!started) {
            this.sendNotif(this.SOURCE_NAME, "Testing task has not started", "", Level.WARN);
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
    public String getState() {
        return this.SOURCE_NAME + ": " + (this.isStarted() ? "Started" : "Stopped");
    }

    @Override
    public void quit() {
        this.stop();
        this.needQuit = true;
    }

    private void setupLog4j(String appName) {

        // InputStream inStreamLog4j = getClass().getResourceAsStream("/log4j.properties");

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

        // logger.setLevel(Level.TRACE);
        logger.debug("log4j configured");

    }

    @Override
    public String getName() {
        return this.appName;
    }

    public enum AttackType {
        ALL,
        SMS_CLIENT,
        SMS_SERVER,
        LOCATION_SRIFORSM,
        INTERCEPT_SMS
    }
}
