package org.mobicents.protocols.ss7.tools.simulator;

import org.mobicents.protocols.ss7.tools.simulator.management.AttackTesterHost;

import java.util.Random;

/**
 * @author Kristoffer Jensen
 */
public class AttackSimulationOrganizer implements Stoppable {
    private Random rng;

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

    public AttackSimulationOrganizer(String simulatorHome) {
        this.rng = new Random(System.currentTimeMillis());

        this.mscAmscB = new AttackTesterHost("MSC_A_MSC_B", simulatorHome, AttackTesterHost.AttackType.MSC_A_MSC_B);
        this.mscBmscA = new AttackTesterHost("MSC_B_MSC_A", simulatorHome, AttackTesterHost.AttackType.MSC_B_MSC_A);

        this.mscAhlrA = new AttackTesterHost("MSC_A_HLR_A", simulatorHome, AttackTesterHost.AttackType.MSC_A_HLR_A);
        this.hlrAmscA = new AttackTesterHost("HLR_A_MSC_A", simulatorHome, AttackTesterHost.AttackType.HLR_A_MSC_A);

        this.mscAsmscA = new AttackTesterHost("MSC_A_SMSC_A", simulatorHome, AttackTesterHost.AttackType.MSC_A_SMSC_A);
        this.smscAmscA = new AttackTesterHost("SMSC_A_MSC_A", simulatorHome, AttackTesterHost.AttackType.SMSC_A_MSC_A);

        this.mscAvlrA = new AttackTesterHost("MSC_A_VLR_A", simulatorHome, AttackTesterHost.AttackType.MSC_A_VLR_A);
        this.vlrAmscA = new AttackTesterHost("VLR_A_MSC_A", simulatorHome, AttackTesterHost.AttackType.VLR_A_MSC_A);

        this.hlrAvlrA = new AttackTesterHost("HLR_A_VLR_A", simulatorHome, AttackTesterHost.AttackType.HLR_A_VLR_A);
        this.vlrAhlrA = new AttackTesterHost("VLR_A_HLR_A", simulatorHome, AttackTesterHost.AttackType.VLR_A_HLR_A);
    }

    private void startAttackSimulationHosts() {
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
    }

    private boolean waitForM3UALinks() {
        while (true) {
            try {
                Thread.sleep(100);

                if(mscAmscB.getM3uaMan().getState().contains("ACTIVE") &&
                        mscAhlrA.getM3uaMan().getState().contains("ACTIVE") &&
                        mscAsmscA.getM3uaMan().getState().contains("ACTIVE") &&
                        mscAvlrA.getM3uaMan().getState().contains("ACTIVE") &&
                        hlrAvlrA.getM3uaMan().getState().contains("ACTIVE"))
                    return true;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private boolean testerHostsNeedQuit() {
        return this.mscAmscB.isNeedQuit() || this.mscBmscA.isNeedQuit() ||
                this.mscAhlrA.isNeedQuit() || this.hlrAmscA.isNeedQuit() ||
                this.mscAsmscA.isNeedQuit() || this.smscAmscA.isNeedQuit() ||
                this.mscAvlrA.isNeedQuit() || this.vlrAmscA.isNeedQuit() ||
                this.hlrAvlrA.isNeedQuit() || this.vlrAhlrA.isNeedQuit();
    }

    private void testerHostsExecuteCheckStore() {
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
    }

    public void start() {
        startAttackSimulationHosts();

        if (!waitForM3UALinks())
            return;

        int sleepTime = 100;

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

    private void sendRandomMessage(Random rng, int num) {

        switch (num) {
            case 0:
                this.mscAmscB.getTestAttackClient().performProvideSubscriberInfoRequest();
                break;
            case 1:
                this.mscAhlrA.getTestAttackClient().performProvideSubscriberInfoRequest();
                break;
            case 2:
                this.mscAsmscA.getTestAttackClient().performProvideSubscriberInfoRequest();
                break;
            case 3:
                this.mscAvlrA.getTestAttackClient().performProvideSubscriberInfoRequest();
                break;
            case 4:
                this.hlrAvlrA.getTestAttackClient().performProvideSubscriberInfoRequest();
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
