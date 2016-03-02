package org.mobicents.protocols.ss7.tools.simulator.level2;

import org.mobicents.protocols.ss7.isup.ISUPStack;
import org.mobicents.protocols.ss7.isup.impl.CircuitManagerImpl;
import org.mobicents.protocols.ss7.isup.impl.ISUPStackImpl;
import org.mobicents.protocols.ss7.mtp.Mtp3UserPart;
import org.mobicents.protocols.ss7.scheduler.DefaultClock;
import org.mobicents.protocols.ss7.scheduler.Scheduler;
import org.mobicents.protocols.ss7.tools.simulator.Stoppable;

/**
 * @author Kristoffer Jensen
 */
public class IsupMan implements Stoppable {
    private ISUPStack isupStack;
    private Scheduler scheduler;

    public void initIsup(Mtp3UserPart mtp3UserPart, int ni, int localSpc, int dpc) {
        this.scheduler = new Scheduler();
        this.scheduler.setClock(new DefaultClock());

        this.isupStack = new ISUPStackImpl(this.scheduler, localSpc, ni);

        this.isupStack.setCircuitManager(new CircuitManagerImpl());
        this.isupStack.setMtp3UserPart(mtp3UserPart);

        this.isupStack.start();

        this.isupStack.getCircuitManager().addCircuit(1, dpc);
    }

    @Override
    public void stop() {
        this.isupStack.stop();
    }

    @Override
    public void execute() {

    }

    @Override
    public String getState() {
        return null;
    }
}
