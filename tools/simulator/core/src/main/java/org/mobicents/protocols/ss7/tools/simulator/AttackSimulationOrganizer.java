package org.mobicents.protocols.ss7.tools.simulator;

import org.mobicents.protocols.ss7.tools.simulator.management.AttackTesterHost;

import java.util.Random;

/**
 * @author Kristoffer Jensen
 */
public class AttackSimulationOrganizer implements Stoppable {
    private AttackTesterHost attackTesterHostClient;
    private AttackTesterHost attackTesterHostServer;

    public AttackSimulationOrganizer(AttackTesterHost attackTesterHostClient, AttackTesterHost attackTesterHostServer) {
        this.attackTesterHostClient = attackTesterHostClient;
        this.attackTesterHostServer = attackTesterHostServer;
    }

    private void startAttackSimulationHosts() {
        this.attackTesterHostClient.start();
        this.attackTesterHostServer.start();
    }

    private boolean waitForM3UALink() {
        while (true) {
            try {
                Thread.sleep(100);
                if(attackTesterHostClient.getM3uaMan().getState().contains("ACTIVE") && attackTesterHostServer.getM3uaMan().getState().contains("ACTIVE"))
                    return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public void start() {
        Random rng = new Random();
        startAttackSimulationHosts();

        int sentSRINum = 0;

        if (!waitForM3UALink())
            return;

        System.out.println("-----------------M3UA Link Active");

        System.out.println("-----------------ENTERING MAIN LOOP");
        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            if (attackTesterHostClient.isNeedQuit() || attackTesterHostServer.isNeedQuit()) {
                attackTesterHostClient.stop();
                attackTesterHostServer.stop();
                break;
            }

            this.attackTesterHostClient.execute();
            this.attackTesterHostServer.execute();
            this.attackTesterHostClient.checkStore();
            this.attackTesterHostServer.checkStore();

            if(sentSRINum < 20) {
                this.sendRandomMessage(rng);
                sentSRINum++;
            }
        }
    }

    private void sendRandomMessage(Random rng) {
        switch(rng.nextInt(4)) {
            case 0:
                this.attackTesterHostServer.getTestSmsServerMan().performSRIForSM("123123123");
                break;
            case 1:
                this.attackTesterHostServer.getTestSmsServerMan().performMtForwardSM("MSG", "81238912831923", "37271", "998319283");
                break;
            case 2:
                this.attackTesterHostClient.getTestSmsClientMan().performAlertServiceCentre("128381928");
                break;
            case 3:
                this.attackTesterHostClient.getTestSmsClientMan().performMoForwardSM("MSG", "7123984", "810740293874");
                break;
            default:
                break;
        }
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
