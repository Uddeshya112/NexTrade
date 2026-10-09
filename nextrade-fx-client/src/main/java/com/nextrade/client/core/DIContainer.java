package com.nextrade.client.core;

import com.nextrade.client.view.LoginView;
import com.nextrade.client.viewmodel.auth.LoginViewModel;

import java.util.HashMap;
import java.util.Map;

/** Minimal deterministic container for the screens implemented in this distribution. */
public final class DIContainer implements AutoCloseable {
    private final Map<Class<?>, Object> components = new HashMap<>();

    public void initialize() {
        LoginViewModel loginViewModel = new LoginViewModel();
        components.put(LoginViewModel.class, loginViewModel);
        components.put(LoginView.class, new LoginView(loginViewModel));
    }

    public <T> T get(Class<T> type) {
        Object value = components.get(type);
        if (value == null) throw new IllegalStateException("No component registered for " + type.getName());
        return type.cast(value);
    }

    @Override
    public void close() {
        components.values().forEach(value -> {
            if (value instanceof AutoCloseable closeable) {
                try { closeable.close(); } catch (Exception ignored) { }
            }
        });
        components.clear();
    }

    public void shutdown() { close(); }
}
