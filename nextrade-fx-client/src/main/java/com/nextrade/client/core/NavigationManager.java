package com.nextrade.client.core;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/** UI-thread-safe navigation hook. Screens not wired yet are reported rather than silently swallowed. */
public final class NavigationManager {
    private static final Logger log = LoggerFactory.getLogger(NavigationManager.class);
    private static final NavigationManager INSTANCE = new NavigationManager();
    private final AtomicReference<Consumer<String>> navigator = new AtomicReference<>(
            screen -> log.warn("Navigation requested for screen '{}' but no navigator is registered", screen));

    private NavigationManager() { }
    public static NavigationManager getInstance() { return INSTANCE; }

    public void setNavigator(Consumer<String> handler) {
        navigator.set(Objects.requireNonNull(handler, "handler"));
    }

    public void navigateTo(String screen) {
        if (screen == null || screen.isBlank()) throw new IllegalArgumentException("screen is required");
        Runnable action = () -> navigator.get().accept(screen);
        if (Platform.isFxApplicationThread()) action.run(); else Platform.runLater(action);
    }
}
