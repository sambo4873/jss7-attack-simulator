package org.mobicents.protocols.ss7.tools.simulator;

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
import org.mobicents.protocols.ss7.mtp.RoutingLabelFormat;
import org.mobicents.protocols.ss7.sccp.SccpProtocolVersion;
import org.mobicents.protocols.ss7.tools.simulator.level1.M3uaConfigurationData;
import org.mobicents.protocols.ss7.tools.simulator.level2.GlobalTitleType;
import org.mobicents.protocols.ss7.tools.simulator.level2.SccpConfigurationData;
import org.mobicents.protocols.ss7.tools.simulator.level3.MapConfigurationData;
import org.mobicents.protocols.ss7.tools.simulator.level3.MapProtocolVersion;
import org.mobicents.protocols.ss7.tools.simulator.management.*;
import org.mobicents.protocols.ss7.tools.simulator.tests.sms.*;

import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

/**
 * @author Kristoffer Jensen
 */
public class AttackSimulation implements NotificationListener {
    private TesterHost serverHost;
    private TesterHost clientHost;

    private AttackType attackType;

    public AttackSimulation() {

    }

    public AttackSimulation(TesterHost serverHost, TesterHost clientHost, AttackType attackType) {
        this.serverHost = serverHost;
        this.clientHost = clientHost;
        this.attackType = attackType;

        this.serverHost.addNotificationListener(this, null, null);

        configureAttackServer();
        configureAttackClient();

        start();
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


        configureL1(serverHost, dpc, isSctpServer, localHost, localPort, ipspType, opc, remoteHost, remotePort);
        configureL2(serverHost, callingPartyAddressDigits, localSpc, localSsn, remoteSpc, remoteSsn, routeonGtMode);
        configureL3(serverHost, destReferenceDigits, origReferenceDigits, remoteAddressDigits);
        configureSMSTestServer(serverHost);
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


        configureL1(clientHost, dpc, isSctpServer, localHost, localPort, ipspType, opc, remoteHost, remotePort);
        configureL2(clientHost, callingPartyAddressDigits, localSpc, localSsn, remoteSpc, remoteSsn, routeonGtMode);
        configureL3(clientHost, destReferenceDigits, origReferenceDigits, remoteAddressDigits);
        configureSMSTestClient(clientHost);
    }

    public void configureL1(TesterHost testerHost, int dpc, boolean isSctpServer, String localHost, int localPort, IPSPType ipspType, int opc, String remoteHost, int remotePort) {
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

    public void configureL3(TesterHost testerHost, String destReferenceDigits, String origReferenceDigits, String remoteAddressDigits) {
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

    public void configureSMSTestClient(TesterHost testerHost) {
        testerHost.setInstance_TestTask(Instance_TestTask.createInstance("SMS_TEST_CLIENT"));
        TestSmsClientConfigurationData testSmsClientConfigurationData = testerHost.getConfigurationData().getTestSmsClientConfigurationData();

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

    public void configureSMSTestServer(TesterHost testerHost) {
        testerHost.setInstance_TestTask(Instance_TestTask.createInstance("SMS_TEST_SERVER"));
        TestSmsServerConfigurationData testSmsServerConfigurationData = testerHost.getConfigurationData().getTestSmsServerConfigurationData();

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

    public void start() {
        this.clientHost.start();
        this.serverHost.start();

        while (true) {
            try {
                Thread.sleep(500);
                this.clientHost.getM3uaMan().getState();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (clientHost.isNeedQuit() || serverHost.isNeedQuit())
                break;
        }

        //TestSmsClientMan testSmsClientMan = this.clientHost.getTestSmsClientMan();
        //TestSmsServerMan testSmsServerMan = this.serverHost.getTestSmsServerMan();
        //testSmsServerMan.performSRIForSM("12121212");
    }

    @Override
    public void handleNotification(Notification notification, Object handback) {
        System.out.println("MESSAGE: " + notification.getType() + "  |  " + notification.getMessage() + "  |  " + notification.getUserData());
    }

    public enum AttackType {
        ALL,
        LOCATION_SRIFORSM,
        INTERCEPT_SMS
    }
}
