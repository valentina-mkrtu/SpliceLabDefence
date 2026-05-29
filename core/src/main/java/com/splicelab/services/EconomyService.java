package com.splicelab.services;

import com.splicelab.data.SaveRepository;
import com.splicelab.model.CurrencyType;

public final class EconomyService {
    private final SaveRepository saves;

    public EconomyService(SaveRepository saves) {
        this.saves = saves;
    }

    public boolean canSpend(CurrencyType currency, int amount) {
        if (currency == null || amount <= 0) return false;
        return getBalance(currency) >= amount;
    }

    public boolean spend(CurrencyType currency, int amount) {
        if (!canSpend(currency, amount)) return false;
        add(currency, -amount);
        return true;
    }

    public void add(CurrencyType currency, int amount) {
        if (currency == null || amount == 0) return;
        switch (currency) {
            case COINS -> saves.get().coins = Math.max(0, saves.get().coins + amount);
            case DNA -> saves.get().dna = Math.max(0, saves.get().dna + amount);
            case CRYSTALS -> saves.get().crystals = Math.max(0, saves.get().crystals + amount);
        }
        // T-2.5: mark dirty instead of flushing synchronously on every mutation.
        // Call saves.flushIfDirty() at natural checkpoints (level complete, lobby, app pause).
        saves.markDirty();
    }

    public int getBalance(CurrencyType currency) {
        if (currency == null) return 0;
        return switch (currency) {
            case COINS -> saves.get().coins;
            case DNA -> saves.get().dna;
            case CRYSTALS -> saves.get().crystals;
        };
    }
}
