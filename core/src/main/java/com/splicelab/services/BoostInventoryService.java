package com.splicelab.services;

import com.splicelab.data.SaveRepository;

public final class BoostInventoryService {
    private final SaveRepository saves;

    public BoostInventoryService(SaveRepository saves) {
        this.saves = saves;
    }

    public int count(String boostName) {
        return saves.get().getBoostCount(boostName);
    }

    public void grant(String boostName, int n) {
        saves.get().addBoost(boostName, n);
        saves.save();
    }

    public boolean consume(String boostName) {
        boolean ok = saves.get().consumeBoost(boostName);
        if (ok) saves.save();
        return ok;
    }
}

