package org.mobicents.protocols.ss7.tools.simulator;

import org.mobicents.protocols.ss7.map.api.primitives.IMSI;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.AnyTimeInterrogationResponse;
import org.mobicents.protocols.ss7.map.api.service.mobility.subscriberInformation.ProvideSubscriberInfoResponse;
import org.mobicents.protocols.ss7.map.api.service.sms.SendRoutingInfoForSMRequest;
import org.mobicents.protocols.ss7.map.api.service.sms.SendRoutingInfoForSMResponse;
import org.mobicents.protocols.ss7.tools.simulator.management.AttackTesterHost;

import java.util.Random;

/**
 * @author Kristoffer Jensen
 */
public class AttackSimulationOrganizer implements Stoppable {
    private Random rng;
    private boolean simpleSimulation;

    private AttackTesterHost mscAmscB;
    private AttackTesterHost mscBmscA;

    private AttackTesterHost mscAhlrA;
    private AttackTesterHost hlrAmscA;


    private AttackTesterHost mscAsmscA;
    private AttackTesterHost smscAmscA;

    private AttackTesterHost mscAvlrA;
    private AttackTesterHost vlrAmscA;

    private AttackTesterHost hlrAvlrA;
    private AttackTesterHost vlrAhlrA;

    private AttackTesterHost attackerBmscA;
    private AttackTesterHost mscAattackerB;

    private AttackTesterHost attackerBhlrA;
    private AttackTesterHost hlrAattackerB;

    private AttackTesterHost attackerBsmscA;
    private AttackTesterHost smscAattackB;

    private AttackTesterHost attackerBvlrA;
    private AttackTesterHost vlrAattackerB;

    public AttackSimulationOrganizer(String simulatorHome, boolean simpleSimulation) {
        this.rng = new Random(System.currentTimeMillis());
        this.simpleSimulation = simpleSimulation;

        if (this.simpleSimulation) {
            this.attackerBhlrA = new AttackTesterHost("ATTACKER_B_HLR_A", simulatorHome, AttackTesterHost.AttackNode.ATTACKER_B_HLR_A);
            this.hlrAattackerB = new AttackTesterHost("HLR_A_ATTACKER_B", simulatorHome, AttackTesterHost.AttackNode.HLR_A_ATTACKER_B);

            this.attackerBvlrA = new AttackTesterHost("ATTACKER_B_VLR_A", simulatorHome, AttackTesterHost.AttackNode.ATTACKER_B_VLR_A);
            this.vlrAattackerB = new AttackTesterHost("VLR_A_ATTACKER_B", simulatorHome, AttackTesterHost.AttackNode.VLR_A_ATTACKER_B);
        } else {
            this.mscAmscB = new AttackTesterHost("MSC_A_MSC_B", simulatorHome, AttackTesterHost.AttackNode.MSC_A_MSC_B);
            this.mscBmscA = new AttackTesterHost("MSC_B_MSC_A", simulatorHome, AttackTesterHost.AttackNode.MSC_B_MSC_A);

            this.mscAhlrA = new AttackTesterHost("MSC_A_HLR_A", simulatorHome, AttackTesterHost.AttackNode.MSC_A_HLR_A);
            this.hlrAmscA = new AttackTesterHost("HLR_A_MSC_A", simulatorHome, AttackTesterHost.AttackNode.HLR_A_MSC_A);

            this.mscAsmscA = new AttackTesterHost("MSC_A_SMSC_A", simulatorHome, AttackTesterHost.AttackNode.MSC_A_SMSC_A);
            this.smscAmscA = new AttackTesterHost("SMSC_A_MSC_A", simulatorHome, AttackTesterHost.AttackNode.SMSC_A_MSC_A);

            this.mscAvlrA = new AttackTesterHost("MSC_A_VLR_A", simulatorHome, AttackTesterHost.AttackNode.MSC_A_VLR_A);
            this.vlrAmscA = new AttackTesterHost("VLR_A_MSC_A", simulatorHome, AttackTesterHost.AttackNode.VLR_A_MSC_A);

            this.hlrAvlrA = new AttackTesterHost("HLR_A_VLR_A", simulatorHome, AttackTesterHost.AttackNode.HLR_A_VLR_A);
            this.vlrAhlrA = new AttackTesterHost("VLR_A_HLR_A", simulatorHome, AttackTesterHost.AttackNode.VLR_A_HLR_A);

            this.attackerBmscA = new AttackTesterHost("ATTACKER_B_MSC_A", simulatorHome, AttackTesterHost.AttackNode.ATTACKER_B_MSC_A);
            this.mscAattackerB = new AttackTesterHost("MSC_A_ATTACKER_B", simulatorHome, AttackTesterHost.AttackNode.MSC_A_ATTACKER_B);

            this.attackerBhlrA = new AttackTesterHost("ATTACKER_B_HLR_A", simulatorHome, AttackTesterHost.AttackNode.ATTACKER_B_HLR_A);
            this.hlrAattackerB = new AttackTesterHost("HLR_A_ATTACKER_B", simulatorHome, AttackTesterHost.AttackNode.HLR_A_ATTACKER_B);

            this.attackerBsmscA = new AttackTesterHost("ATTACKER_B_SMSC_A", simulatorHome, AttackTesterHost.AttackNode.ATTACKER_B_SMSC_A);
            this.smscAattackB = new AttackTesterHost("SMSC_A_ATTACKER_B", simulatorHome, AttackTesterHost.AttackNode.SMSC_A_ATTACKER_B);

            this.attackerBvlrA = new AttackTesterHost("ATTACKER_B_VLR_A", simulatorHome, AttackTesterHost.AttackNode.ATTACKER_B_VLR_A);
            this.vlrAattackerB = new AttackTesterHost("VLR_A_ATTACKER_B", simulatorHome, AttackTesterHost.AttackNode.VLR_A_ATTACKER_B);
        }
    }

    private void startAttackSimulationHosts() {
        if (this.simpleSimulation) {
            this.attackerBhlrA.start();
            this.hlrAattackerB.start();

            this.attackerBvlrA.start();
            this.vlrAattackerB.start();
        } else {
            this.mscAmscB.start();
            this.mscBmscA.start();

            this.mscAhlrA.start();
            this.hlrAmscA.start();

            this.mscAsmscA.start();
            this.smscAmscA.start();

            this.mscAvlrA.start();
            this.vlrAmscA.start();

            this.hlrAvlrA.start();
            this.vlrAhlrA.start();

            this.attackerBmscA.start();
            this.mscAattackerB.start();

            this.attackerBhlrA.start();
            this.hlrAattackerB.start();

            this.attackerBsmscA.start();
            this.smscAattackB.start();

            this.attackerBvlrA.start();
            this.vlrAattackerB.start();
        }
    }

    private boolean waitForM3UALinks() {
        while (true) {
            try {
                Thread.sleep(100);
                if(!this.simpleSimulation) {
                    if (mscAmscB.getM3uaMan().getState().contains("ACTIVE") &&
                            mscAhlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            mscAsmscA.getM3uaMan().getState().contains("ACTIVE") &&
                            mscAvlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            hlrAvlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            attackerBmscA.getM3uaMan().getState().contains("ACTIVE") &&
                            attackerBhlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            attackerBsmscA.getM3uaMan().getState().contains("ACTIVE") &&
                            attackerBvlrA.getM3uaMan().getState().contains("ACTIVE"))
                        return true;
                } else {
                    if (attackerBvlrA.getM3uaMan().getState().contains("ACTIVE") &&
                            attackerBhlrA.getM3uaMan().getState().contains("ACTIVE"))
                        return true;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private boolean testerHostsNeedQuit() {
        if(simpleSimulation)
            return this.attackerBvlrA.isNeedQuit() || this.vlrAattackerB.isNeedQuit() ||
                    this.attackerBhlrA.isNeedQuit() || this.hlrAattackerB.isNeedQuit();
        else
            return this.mscAmscB.isNeedQuit() || this.mscBmscA.isNeedQuit() ||
                this.mscAhlrA.isNeedQuit() || this.hlrAmscA.isNeedQuit() ||
                this.mscAsmscA.isNeedQuit() || this.smscAmscA.isNeedQuit() ||
                this.mscAvlrA.isNeedQuit() || this.vlrAmscA.isNeedQuit() ||
                this.hlrAvlrA.isNeedQuit() || this.vlrAhlrA.isNeedQuit() ||
                this.attackerBmscA.isNeedQuit() || this.mscAattackerB.isNeedQuit() ||
                this.attackerBhlrA.isNeedQuit() || this.hlrAattackerB.isNeedQuit() ||
                this.attackerBsmscA.isNeedQuit() || this.smscAattackB.isNeedQuit() ||
                this.attackerBvlrA.isNeedQuit() || this.vlrAattackerB.isNeedQuit();
    }

    private void testerHostsExecuteCheckStore() {
        if (simpleSimulation) {
            this.attackerBhlrA.execute();
            this.vlrAattackerB.execute();
            this.attackerBvlrA.execute();
            this.vlrAattackerB.execute();

            this.attackerBhlrA.checkStore();
            this.hlrAattackerB.checkStore();
            this.attackerBvlrA.checkStore();
            this.vlrAattackerB.checkStore();
        } else {
            this.mscAmscB.execute();
            this.mscBmscA.execute();
            this.mscAhlrA.execute();
            this.hlrAmscA.execute();
            this.mscAsmscA.execute();
            this.smscAmscA.execute();
            this.mscAvlrA.execute();
            this.vlrAmscA.execute();
            this.hlrAvlrA.execute();
            this.vlrAhlrA.execute();
            this.attackerBmscA.execute();
            this.mscAattackerB.execute();
            this.attackerBhlrA.execute();
            this.vlrAattackerB.execute();
            this.attackerBsmscA.execute();
            this.smscAattackB.execute();
            this.attackerBvlrA.execute();
            this.vlrAattackerB.execute();

            this.mscAmscB.checkStore();
            this.mscBmscA.checkStore();
            this.mscAhlrA.checkStore();
            this.hlrAmscA.checkStore();
            this.mscAsmscA.checkStore();
            this.smscAmscA.checkStore();
            this.mscAvlrA.checkStore();
            this.vlrAmscA.checkStore();
            this.hlrAvlrA.checkStore();
            this.vlrAhlrA.checkStore();
            this.attackerBmscA.checkStore();
            this.mscAattackerB.checkStore();
            this.attackerBhlrA.checkStore();
            this.hlrAattackerB.checkStore();
            this.attackerBsmscA.checkStore();
            this.smscAattackB.checkStore();
            this.attackerBvlrA.checkStore();
            this.vlrAattackerB.checkStore();
        }
    }

    public void start() {
        startAttackSimulationHosts();

        if (!waitForM3UALinks())
            return;

        int sleepTime = 100;

        int msgNum = 0;

        while (true) {
            try {
                sleepTime = this.rng.nextInt((1000 - 100) + 1) + 100;
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }

            if(this.testerHostsNeedQuit())
                break;

            this.testerHostsExecuteCheckStore();
            this.generateTraffic();

            this.sendRandomMessage(msgNum);

            msgNum++;
        }
    }

    private void generateTraffic() {
        double attackChance = 1.0;
        boolean generateNoise = this.rng.nextInt(10) >= attackChance;

        if(generateNoise)
            this.generateNoise();
        else
            this.generateAttack();
    }

    private void generateNoise() {

    }

    private void generateAttack() {

    }

    private void modifyCallingPartyAddressDigits(AttackTesterHost attackTesterHost, String cgGT) {
        attackTesterHost.getSccpMan().setCallingPartyAddressDigits(cgGT);
    }

    private void sendRandomMessage(int num) {

        if (num == 0)
            this.attackLocationAti();

    //    switch (num) {
    //        case 0:
    //            this.mscAmscB.getTestAttackClient().performProvideSubscriberInfoRequest();
    //            break;
    //        case 1:
    //            this.mscAhlrA.getTestAttackClient().performProvideSubscriberInfoRequest();
    //            break;
    //        case 2:
    //            this.mscAsmscA.getTestAttackClient().performProvideSubscriberInfoRequest();
    //            break;
    //        case 3:
    //            this.mscAvlrA.getTestAttackClient().performProvideSubscriberInfoRequest();
    //            break;
    //        case 4:
    //            this.hlrAvlrA.getTestAttackClient().performProvideSubscriberInfoRequest();
    //            break;
    //        case 5:
    //            this.attackerBmscA.getTestAttackClient().performProvideSubscriberInfoRequest();
    //            break;
    //        case 6:
    //            this.attackerBhlrA.getTestAttackClient().performProvideSubscriberInfoRequest();
    //            break;
    //        case 7:
    //            this.attackerBsmscA.getTestAttackClient().performProvideSubscriberInfoRequest();
    //            break;
    //        case 8:
    //            this.attackerBvlrA.getTestAttackClient().performProvideSubscriberInfoRequest();
    //            break;
    //        case 9:
    //            this.hlrAvlrA.getTestAttackClient().performProvideSubscriberInfoRequest();
    //            break;
    //        case 10:
    //            break;
    //        default:
    //            break;
    //    }

    }

    private void attackLocationAti() {
        this.attackerBhlrA.getTestAttackClient().performATI("98979695");

        while(!this.attackerBhlrA.gotAtiResponse()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                return;
            }
        }

        AnyTimeInterrogationResponse atiResponse = this.attackerBhlrA.getTestAttackClient().getLastAtiResponse();
    }

    private void attackLocationPsi() {
        System.out.println("-----------STARTING LOCATION PSI ATTACK-----------");
        //Get necessary information from request, use in next message.
        this.attackerBhlrA.getTestAttackClient().performSendRoutingInfoForSM("98979695", "0000000");

        while(!this.attackerBhlrA.gotSRIForSMResponse()){
            try{
                Thread.sleep(50);
            } catch (InterruptedException e) {
                System.exit(50);
            }
        }

        SendRoutingInfoForSMResponse sriResponse = this.attackerBhlrA.getTestAttackClient().getLastSRIForSMResponse();
        this.attackerBhlrA.getTestAttackClient().clearLastSRIForSMResponse();

        String victimImsi = sriResponse.getIMSI().getData();
        String victimVlrAddress = sriResponse.getLocationInfoWithLMSI().getNetworkNodeNumber().getAddress();

        this.attackerBvlrA.getConfigurationData().getMapConfigurationData().setRemoteAddressDigits(victimVlrAddress);
        this.attackerBvlrA.getTestAttackClient().performProvideSubscriberInfoRequest();

        while(!this.attackerBvlrA.gotPSIResponse()){
            try{
                Thread.sleep(50);
            } catch (InterruptedException e) {
                System.exit(50);
            }
        }

        System.out.println("-----------GOT PSI RESPONSE-----------");

        ProvideSubscriberInfoResponse psiResponse = this.attackerBvlrA.getTestAttackClient().getLastPsiResponse();
        this.attackerBvlrA.getTestAttackClient().clearLastPsiResponse();

        System.out.println(psiResponse.toString());

        //Location information aquired.
    }

    private String attackInterceptSms() {
        String response;

        //Update subscriber info.
        response = this.vlrAhlrA.getTestAttackClient().performUpdateLocationRequest();

        //Introduce a delay before sending an sms.
        response = this.smscAmscA.getTestAttackServer().performSRIForSM("");
        response = this.smscAmscA.getTestAttackServer().performMtForwardSM("", "", "", "");

        return response;
    }

    private void updateLocation() {
        this.vlrAhlrA.getTestAttackServer().performUpdateLocation();
    }

    private void cancelLocation() {
        this.hlrAvlrA.getTestAttackClient().performCancelLocation();
    }

    private void sendIdentification() {
        //this.vlrAvlrB.getTestAttackClient().performSendIdentification();
        //this.vlrBvlrA.getTestAttackServer().performSendIdentification();
    }

    private void purgeMS() {
        this.vlrAhlrA.getTestAttackServer().performPurgeMS();
    }

    private void updateGPRSLocation() {
        //this.sgsnAhlrA.getTestAttackClient().performUpdateGPRSLocation();
    }

    private void checkIMEI() {
        //this.vlrAmscA.getTestAttackServer().performCheckIMEI();
        //this.mscAeirA.getTestAttackClient().performCheckIMEI();
        //this.sgsnAeirA.getTestAttackClient().performCheckIMEI();
    }

    private void insertSubscriberData() {
        //this.hlrAvlrA.getTestAttackClient().performInsertSubscriberData();
    }

    private void deleteSubscriberData() {
        //this.hlrAvlrA.getTestAttackClient().performDeleteSubscriberData();
    }

    private void forwardCheckSSIndication() {
        //this.hlrAmscA.getTestAttackServer().performForwardCheckSSIndication();
    }

    private void restoreData() {
        //this.vlrAhlrA.getTestAttackServer().performRestoreData();
    }

    private void anyTimeInterrogation() {
        //this.gsmscfAhlrA.getTestAttackClient().performAnyTimeInterrogation();
    }

    private void provideSubscriberInfo() {
        this.mscAvlrA.getTestAttackClient().performProvideSubscriberInfoRequest();
    }

    private void activateTraceMode() {
        //this.hlrAvlrA.getTestAttackServer().performActivateTraceMode();
    }

    private void sendIMSI() {

    }

    private void sendRoutingInformation() {
        //this.mscAhlrA.getTestAttackClient().performSendRoutingInformation();
    }

    private void provideRoamingNumber() {
        //this.hlrAvlrA.getTestAttackClient().performProvideRoamingNumber();
    }

    private void registerSS() {
        //this.mscAvlrA.getTestAttackClient().performRegisterSS();
        //this.vlrAhlrA.getTestAttackServer().performRegisterSS();
    }

    private void eraseSS() {
        //this.mscAvlrA.getTestAttackClient().performEraseSS();
        //this.vlrAhlrA.getTestAttackServer().performEraseSS();
    }

    private void activateSS() {
        //this.mscAvlrA.getTestAttackClient().performActivateSS();
        //this.vlrAhlrA.getTestAttackServer().performActivateSS();
    }

    private void deactivateSS() {
        //this.mscAvlrA.getTestAttackClient().performDeactivateSS();
        //this.vlrAhlrA.getTestAttackServer().performDeactivateSS();
    }

    private void interrogateSS() {
        //this.mscAvlrA.getTestAttackClient().performInterrogateSS();
        //this.vlrAhlrA.getTestAttackServer().performInterrogateSS();
    }

    private void registerPassword() {
        //this.mscAvlrA.getTestAttackClient().performRegisterSS();
        //this.vlrAhlrA.getTestAttackServer().performRegisterSS();
    }

    private void getPassword() {
        //this.hlrAvlrA.getTestAttackClient().performGetPassword();
        //this.vlrAmscA.getTestAttackServer().performGetPassword();
    }

    private void processUnstructuredSSRequest() {
        //this.mscAvlrA.getTestAttackClient().performProcessUnstructuredSSRequest();
        //this.vlrAhlrA.getTestAttackClient().performProcessUnstructuredSSRequest();
        //this.vlrAgsmscfA.getTestAttackClient().performProcessUnstructuredSSRequest();
    }

    private void unstructuredSSRequest() {
        //this.hlrAvlrA.getTestAttackClient().performUnstructuredSSRequest();
        //this.gsmscfAvlrA.getTestAttackClient().performUnstructuredSSRequest();
        //this.vlrAmscA.getTestAttackClient().performUnstructuredSSRequest();
    }

    private void unstructuredSSNotify () {
        //this.hlrAvlrA.getTestAttackClient().performUnstructuredSSRequest();
        //this.gsmscfAvlrA.getTestAttackClient().performUnstructuredSSRequest();
        //this.vlrAmscA.getTestAttackClient().performUnstructuredSSRequest();
    }

    private void sendRoutingInfoForSM() {
        //this.mscAhlrA.getTestAttackClient().performSendRoutingInfoForSM("", "");
        //this.smscAhlrA.getTestAttackClient().performSendRoutingInfoForSM("", "");
    }

    private void moForwardSM() {
        //this.mscAsmscA.getTestAttackClient().performMoForwardSM("", "", "");
    }

    private void reportSMDeliveryStatus() {
        //this.mscAhlrA.getTestAttackClient().performReportSMDeliveryStatus();
    }

    private void readyForSM() {
        //this.mscAvlrA.getTestAttackClient().performReadyForSM();
        //this.vlrAhlrA.getTestAttackServer().performReadyForSM();
        //this.sgsnAhlrA.getTestAttackClient().performReadyForSM();
        //this.smscAhlrA.getTestAttackClient().performReadyForSM();
    }

    private void alertServiceCentre() {
        //this.hlrAmscA.getTestAttackServer().performAlertServiceCentre();
    }

    private void informServiceCentre() {
        //this.hlrAmscA.getTestAttackServer().performInformServiceCentre();
    }

    private void mtForwardSM() {
        //this.mscAmscB.getTestAttackClient().performMtForwardSM();
        //this.mscAsgsnA.getTestAttackClient().performMtForwardSM();
    }

    private void sendRoutingInfoForGPRS() {
        //this.sgsnAhlrA.getTestAttackClient().performSendRoutingInfoForGPRS();
    }

    @Override
    public void stop() {

    }

    @Override
    public void execute() {

    }

    @Override
    public String getState() {
        return null;
    }
}
