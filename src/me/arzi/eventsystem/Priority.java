package me.arzi.eventsystem;

public enum Priority {
    High(3),
    Mid(2),
    Low(1);

    private final int val;

    Priority(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }
}
