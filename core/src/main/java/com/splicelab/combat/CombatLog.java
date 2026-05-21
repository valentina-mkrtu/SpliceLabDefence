package com.splicelab.combat;

import com.badlogic.gdx.Gdx;
import com.splicelab.app.AppConstants;

public final class CombatLog {
    private CombatLog() {
    }

    public static void d(String msg) {
        Gdx.app.log(AppConstants.LOG_TAG, msg);
    }
}

