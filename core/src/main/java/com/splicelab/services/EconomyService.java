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
            case DNA -> saves.get().dna = Math.max(0, saves.get().dna + amount);
            case CRY -> saves.get().cry = Math.max(0, saves.get().cry + amount);
        }
        saves.save();
    }

    public int getBalance(CurrencyType currency) {
        if (currency == null) return 0;
        return switch (currency) {
            case DNA -> saves.get().dna;
            case CRY -> saves.get().cry;
        };
    }
}
