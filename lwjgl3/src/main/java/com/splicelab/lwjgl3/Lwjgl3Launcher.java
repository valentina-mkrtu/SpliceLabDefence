package com.splicelab.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.splicelab.app.SpliceLabGame;

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Splice Lab");
        config.setWindowedMode(540, 960);
        config.useVsync(true);
        new Lwjgl3Application(new SpliceLabGame(), config);
    }
}
