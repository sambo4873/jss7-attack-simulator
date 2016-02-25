package org.mobicents.protocols.ss7.tools.simulator;

import org.mobicents.protocols.ss7.indicator.RoutingIndicator;
import org.mobicents.protocols.ss7.sccp.Router;
import org.mobicents.protocols.ss7.sccp.parameter.ParameterFactory;
import org.mobicents.protocols.ss7.tools.simulator.management.AttackTesterHost;

import java.util.Random;

/**
 * @author Kristoffer Jensen
 */
public class AttackSimulationOrganizer implements Stoppable {
    private AttackTesterHost attackTesterHostClient;
    private AttackTesterHost attackTesterHostServer;

    private AttackTesterHost stp1;
    private AttackTesterHost stp2;
    private AttackTesterHost stp3;
    private AttackTesterHost stp4;
    private AttackTesterHost stp5;
    private AttackTesterHost stp6;

    private AttackTesterHost mscAmscB;
    private AttackTesterHost mscBmscA;

    public AttackSimulationOrganizer(AttackTesterHost attackTesterHostClient, AttackTesterHost attackTesterHostServer) {
        this.attackTesterHostClient = attackTesterHostClient;
        this.attackTesterHostServer = attackTesterHostServer;
    }

    public AttackSimulationOrganizer(AttackTesterHost stp1, AttackTesterHost stp2, AttackTesterHost stp3, AttackTesterHost stp4, AttackTesterHost stp5, AttackTesterHost stp6) {
        this.stp1 = stp1;
        this.stp2 = stp2;
        this.stp3 = stp3;
        this.stp4 = stp4;
        this.stp5 = stp5;
        this.stp6 = stp6;
    }

    public AttackSimulationOrganizer(String simulatorHome) {
        this.mscAmscB = new AttackTesterHost("MSA_A_MSC_B", simulatorHome, AttackTesterHost.AttackType.MSC_A_MSC_B);
        this.mscBmscA = new AttackTesterHost("MSA_B_MSC_A", simulatorHome, AttackTesterHost.AttackType.MSC_B_MSC_A);

    }

    private void startAttackSimulationHosts() {
        this.mscAmscB.start();
        this.mscBmscA.start();
    }

    private void startAttackSimulationHostsLarge() {
        this.stp1.start();
        this.stp2.start();
        this.stp3.start();
        this.stp4.start();
        this.stp5.start();
        this.stp6.start();
    }

    private boolean waitForM3UALink() {
        while (true) {
            try {
                Thread.sleep(100);
                if(mscAmscB.getM3uaMan().getState().contains("ACTIVE") &&
                        mscBmscA.getM3uaMan().getState().contains("ACTIVE"))
                    return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private boolean waitForM3UALinkLarge() {
        while (true) {
            try {
                Thread.sleep(100);
                if(stp1.getM3uaMan().getState().contains("ACTIVE") && stp3.getM3uaMan().getState().contains("ACTIVE") && stp5.getM3uaMan().getState().contains("ACTIVE"))
                    return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public void startLarge() {
        Random rng = new Random();
        startAttackSimulationHostsLarge();

        int sentSRINum = 0;

        if (!waitForM3UALinkLarge())
            return;

        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }

            if (this.stp1.isNeedQuit() || this.stp2.isNeedQuit() || this.stp3.isNeedQuit() || this.stp4.isNeedQuit() || this.stp5.isNeedQuit() || this.stp6.isNeedQuit()) {
                this.stp1.stop();
                this.stp2.stop();
                this.stp3.stop();
                this.stp4.stop();
                this.stp5.stop();
                this.stp6.stop();
                break;
            }

            this.stp1.execute();
            this.stp2.execute();
            this.stp3.execute();
            this.stp4.execute();
            this.stp5.execute();
            this.stp6.execute();

            this.stp1.checkStore();
            this.stp2.checkStore();
            this.stp3.checkStore();
            this.stp4.checkStore();
            this.stp5.checkStore();
            this.stp6.checkStore();

            if(sentSRINum < 1) {

                this.testLargeSimulation();

                sentSRINum++;
            }
        }
    }

    private boolean testerHostsNeedQuit() {
        return this.mscAmscB.isNeedQuit() || this.mscBmscA.isNeedQuit();
    }

    private void testerHostsExecuteCheckStore() {
        this.mscAmscB.execute();
        this.mscAmscB.checkStore();
    }

    private void testLargeSimulation() {
        this.stp1.getTestAttackServer().performSRIForSM("33333333");
    }

    public void start() {
        int sentSRINum = 0;
        Random rng = new Random();

        startAttackSimulationHosts();

        if (!waitForM3UALink()) return;

        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }

            if(testerHostsNeedQuit())
                break;

            testerHostsExecuteCheckStore();

            if(sentSRINum < 1) {
                this.sendRandomMessage(rng);
                sentSRINum++;
            }
        }
    }

    private void modifyCallingPartyAddressDigits(AttackTesterHost attackTesterHost, String cgGT) {
        attackTesterHost.getSccpMan().setCallingPartyAddressDigits(cgGT);
    }

    private void sendSRIForSM() {
        //Sets local SSN for SRI on sender
        this.attackTesterHostServer.getSccpMan().setLocalSsn(8);

        //Sets remote SSN for SRI on sender
        this.attackTesterHostServer.getTestAttackServer().setHlrSsn(6);

        //Sets local SSN for SRI on receiver
        //this.attackTesterHostClient.

    }

    private void sendRandomMessage(Random rng) {

        this.mscAmscB.getTestAttackClient().performProvideSubscriberInfoRequest();

        //switch(rng.nextInt(4)) {
        //    case 0:
        //        this.attackTesterHostServer.getTestAttackServer().performSRIForSM("123123123");
        //        break;
        //    case 1:
        //        this.attackTesterHostServer.getTestAttackServer().performMtForwardSM("MSG", "81238912831923", "37271", "998319283");
        //        break;
        //    case 2:
        //        this.attackTesterHostClient.getTestAttackClient().performAlertServiceCentre("123178237");
        //        break;
        //    case 3:
        //        this.attackTesterHostClient.getTestAttackClient().performMoForwardSM("MSG", "7123984", "810740293874");
        //        break;
        //    default:
        //        break;
        //}
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
