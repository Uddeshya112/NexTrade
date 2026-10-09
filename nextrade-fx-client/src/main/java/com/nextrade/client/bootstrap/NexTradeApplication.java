package com.nextrade.client.bootstrap;

import com.nextrade.client.core.DIContainer;
import com.nextrade.client.view.LoginView;
import com.nextrade.client.core.NavigationManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NexTradeApplication extends Application {
    private static final Logger log = LoggerFactory.getLogger(NexTradeApplication.class);
    private DIContainer container;
    private Stage primaryStage;

    @Override public void init() { container = new DIContainer(); container.initialize(); }
    @Override public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("NexTrade - Algorithmic Trading Platform");
        stage.setMinWidth(1200); stage.setMinHeight(800);
        LoginView loginView = container.get(LoginView.class);
        Scene scene = new Scene(loginView, 1200, 800);
        NavigationManager.getInstance().setNavigator(screen -> {
            if ("login".equalsIgnoreCase(screen)) {
                scene.setRoot(container.get(LoginView.class));
            } else {
                log.warn("Screen '{}' is not included in this build yet", screen);
            }
        });
        scene.getStylesheets().add(getClass().getResource("/css/nextrade-dark.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(e -> { Platform.exit(); System.exit(0); });
    }
    @Override public void stop() { if (container != null) container.shutdown(); }
    public static void main(String[] args) { launch(args); }
}
