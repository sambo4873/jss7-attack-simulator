package org.mobicents.protocols.ss7.tools.simulator;

import java.util.Random;

/**
 * @author Kristoffer Jensen
 */
public class AttackSimulationOrganizer implements Stoppable {
    private AttackSimulationHost attackServer;
    private AttackSimulationHost attackClient;

    public AttackSimulationOrganizer(AttackSimulationHost attackServer, AttackSimulationHost attackClient) {
        this.attackServer = attackServer;
        this.attackClient = attackClient;
    }

    private void startAttackSimulationHosts() {
        attackClient.start();
        attackServer.start();
    }

    private boolean waitForM3UALink() {
        while (true) {
            try {
                Thread.sleep(100);
                if(attackClient.getM3uaMan().getState().contains("ACTIVE") && attackServer.getM3uaMan().getState().contains("ACTIVE"))
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
            if (attackClient.isNeedQuit() || attackServer.isNeedQuit()) {
                attackClient.stop();
                attackServer.stop();
                break;
            }

            this.attackClient.execute();
            this.attackServer.execute();
            this.attackClient.checkStore();
            this.attackServer.checkStore();

            if(sentSRINum < 20) {
                this.sendRandomMessage(rng);
                sentSRINum++;
            }
        }
    }

    private void sendRandomMessage(Random rng) {

        switch(rng.nextInt(4)) {
            case 0:
                this.attackServer.getTestSmsServerMan().performSRIForSM("123123123");
                break;
            case 1:
                this.attackServer.getTestSmsServerMan().performMtForwardSM("MSG", "81238912831923", "37271", "998319283");
                break;
            case 2:
                this.attackClient.getTestSmsClientMan().performAlertServiceCentre("128381928");
                break;
            case 3:
                this.attackClient.getTestSmsClientMan().performMoForwardSM("MSG", "7123984", "810740293874");
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
