package com.splicelab.input;

public final class InputLock {
    private boolean locked;

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }

    public boolean isLocked() {
        return locked;
    }
}

