package me.arzi.eventsystem;

public class CancelableEvent extends Event {

    private boolean canceled;

    public CancelableEvent() {
        canceled = false;
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCanceled(boolean canceled) {
        this.canceled = canceled;
    }
}
