package org.mobicents.protocols.ss7.tools.simulator.management;

/**
 * @author Kristoffer Jensen
 */
public class DialogInfo {
    public long invokeId;
    public long remoteDialogId;

    public DialogInfo(long invokeId, long remoteDialogId) {
        this.invokeId = invokeId;
        this.remoteDialogId = remoteDialogId;
    }
}
