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

    public void start() {
        startAttackSimulationHosts();

        boolean sentSRI = false;


        while (true) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
            if(attackClient.isNeedQuit() || attackServer.isNeedQuit()) {
                attackClient.stop();
                attackServer.stop();
                break;
            }


            if(!sentSRI && attackClient.getM3uaMan().getState().contains("ACTIVE") && attackServer.getM3uaMan().getState().contains("ACTIVE")) {
                try{
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                attackClient.getTestSmsServerMan().performSRIForSM("123123123");
                sentSRI = true;
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
