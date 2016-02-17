package org.mobicents.protocols.ss7.tools.simulator;

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
                Thread.sleep(500);
                if(attackClient.getM3uaMan().getState().contains("ACTIVE") && attackServer.getM3uaMan().getState().contains("ACTIVE"))
                    return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public void start() {
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

            if(sentSRINum < 2) {
                this.attackServer.getTestSmsServerMan().performSRIForSM("123123123");
                sentSRINum++;
            }
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
