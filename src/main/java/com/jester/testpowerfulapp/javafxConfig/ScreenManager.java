package com.jester.testpowerfulapp.javafxConfig;

import com.jester.testpowerfulapp.screens.Main;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ScreenManager{
    private final FxWeaver fxWeaver;
    private Scene scene;
    private final Map<String, Class<?>> screenMap;

    @Autowired
    public ScreenManager(FxWeaver fxWeaver) {
        this.fxWeaver = fxWeaver;
        this.screenMap = new HashMap<>();
    }

    public void setStage(Stage stage) {
        this.scene = new Scene(fxWeaver.loadView(Main.class)); // Initial screen
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public void addScreen(String name, Class<?> screenClass) {
        screenMap.put(name, screenClass);
    }

    public void setScreen(String screenName) {
        Class<?> screenClass = screenMap.get(screenName);
        if (screenClass != null) {
            Platform.runLater(() -> scene.setRoot(fxWeaver.loadView(screenClass)));
        } else {
            log.error("Screen not found: {}", screenName);
        }
    }
}

