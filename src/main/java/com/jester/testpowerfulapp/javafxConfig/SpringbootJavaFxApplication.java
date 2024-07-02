package com.jester.testpowerfulapp.javafxConfig;

import com.jester.testpowerfulapp.TestPowerfulAppApplication;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class SpringbootJavaFxApplication extends Application {

    private ConfigurableApplicationContext context;

    @Override
    public void init() {
        this.context = new SpringApplicationBuilder() //(1)
                .sources(TestPowerfulAppApplication.class)
                .run(getParameters().getRaw().toArray(new String[0]));
    }

    @Override
    public void start(Stage primaryStage) {
        context.publishEvent(new StageReadyEvent(primaryStage)); //(2)
    }

    @Override
    public void stop() { //(3)
        this.context.close();
        Platform.exit();
        System.exit(0);
    }
}
