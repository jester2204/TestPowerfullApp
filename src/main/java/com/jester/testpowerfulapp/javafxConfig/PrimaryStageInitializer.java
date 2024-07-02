package com.jester.testpowerfulapp.javafxConfig;

import com.jester.testpowerfulapp.screens.Main;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class PrimaryStageInitializer implements ApplicationListener<StageReadyEvent> {
    private final ScreenManager screenManager;

    @Autowired
    public PrimaryStageInitializer(ScreenManager screenManager) {
        this.screenManager = screenManager;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        Stage stage = event.stage;
        screenManager.setStage(stage);
        screenManager.addScreen("main", Main.class);
    }
}
