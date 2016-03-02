package org.mobicents.protocols.ss7.tools.simulator.level2;

import org.mobicents.protocols.ss7.isup.ISUPStack;
import org.mobicents.protocols.ss7.isup.impl.CircuitManagerImpl;
import org.mobicents.protocols.ss7.isup.impl.ISUPProviderImpl;
import org.mobicents.protocols.ss7.isup.impl.ISUPStackImpl;
import org.mobicents.protocols.ss7.mtp.Mtp3UserPart;
import org.mobicents.protocols.ss7.scheduler.Clock;
import org.mobicents.protocols.ss7.scheduler.DefaultClock;
import org.mobicents.protocols.ss7.scheduler.Scheduler;
import org.mobicents.protocols.ss7.tools.simulator.Stoppable;

/**
 * @author Kristoffer Jensen
 */
public class IsupMan implements Stoppable {
    private ISUPStack isupStack;
    private Scheduler scheduler;

    private int ni;
    private int localSpc;
    private int dpc;


    public ISUPStack getIsupStack() {
        return this.isupStack;
    }

    public void initIsup(Mtp3UserPart mtp3UserPart, int ni, int localSpc, int dpc) {
        this.ni = ni;
        this.localSpc = localSpc;
        this.dpc = dpc;

        this.scheduler = new Scheduler();
        this.scheduler.setClock(new DefaultClock());
        this.scheduler.start();

        this.isupStack = new ISUPStackImpl(this.scheduler, localSpc, ni);
        this.isupStack.setCircuitManager(new CircuitManagerImpl());
        this.isupStack.setMtp3UserPart(mtp3UserPart);
        this.isupStack.start();

        this.isupStack.getCircuitManager().addCircuit(1, dpc);

        ISUPProviderImpl provider = (ISUPProviderImpl) this.isupStack.getIsupProvider();
        provider.start();
    }

    public int getNi() {
        return this.ni;
    }

    public int getLocalSpc() {
        return this.localSpc;
    }

    public int getDpc() {
        return this.dpc;
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
