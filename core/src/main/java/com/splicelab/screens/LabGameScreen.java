package com.splicelab.screens;

import com.splicelab.app.GameContext;
import com.splicelab.app.SpliceLabGame;
import com.splicelab.combat.CombatController;
import com.splicelab.combat.CombatState;
import com.splicelab.ui.screens.LabGameView;

public final class LabGameScreen extends BaseScreen {
    private final int levelNumber;
    private LabGameView view;
    private CombatController combatController;
    private CombatState combatState;

    public LabGameScreen(SpliceLabGame game, GameContext context, int levelNumber) {
        super(game, context);
        this.levelNumber = levelNumber;
    }

    @Override
    protected void buildUi() {
        view = new LabGameView(context);
        stage.addActor(view.getRoot());

        combatController = new CombatController(context);
        combatState = combatController.startLevel(levelNumber);

        view.setOnTubeTapped(() -> combatController.requestTubeSpawn());
        view.bindDragDrop(combatController);
    }

    @Override
    protected void update(float delta) {
        combatController.update(delta);
        view.syncFromState(combatController.getState());

        switch (combatController.getState().result) {
            case WIN -> game.setScreen(new LevelCompleteScreen(game, context));
            case LOSE -> game.setScreen(new DefeatScreen(game, context));
            default -> {
            }
        }
    }
}

