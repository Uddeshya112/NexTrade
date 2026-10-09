package com.nextrade.client.security;

import com.nextrade.common.identifier.Identifier;
import com.nextrade.common.enumtype.UserRole;
import javafx.beans.property.*;

public final class AuthContext {
    private static final AuthContext INSTANCE = new AuthContext();

    private final BooleanProperty authenticated = new SimpleBooleanProperty(false);
    private final StringProperty accessToken = new SimpleStringProperty();
    private final StringProperty refreshToken = new SimpleStringProperty();
    private final ObjectProperty<com.nextrade.common.identifier.UserId> userId = new SimpleObjectProperty<>();
    private final StringProperty username = new SimpleStringProperty();
    private final ObjectProperty<UserRole> userRole = new SimpleObjectProperty<>();
    private final StringProperty userEmail = new SimpleStringProperty();
    private final StringProperty userStatus = new SimpleStringProperty();
    private final BooleanProperty loading = new SimpleBooleanProperty(false);
    private final StringProperty error = new SimpleStringProperty();

    private AuthContext() {}

    public static AuthContext getInstance() { return INSTANCE; }

    // Getters
    public BooleanProperty authenticatedProperty() { return authenticated; }
    public boolean isAuthenticated() { return authenticated.get(); }
    public StringProperty accessTokenProperty() { return accessToken; }
    public String getAccessToken() { return accessToken.get(); }
    public StringProperty refreshTokenProperty() { return refreshToken; }
    public String getRefreshToken() { return refreshToken.get(); }
    public ObjectProperty<com.nextrade.common.identifier.UserId> userIdProperty() { return userId; }
    public com.nextrade.common.identifier.UserId getUserId() { return userId.get(); }
    public StringProperty usernameProperty() { return username; }
    public String getUsername() { return username.get(); }
    public ObjectProperty<UserRole> userRoleProperty() { return userRole; }
    public UserRole getUserRole() { return userRole.get(); }
    public StringProperty userEmailProperty() { return userEmail; }
    public String getUserEmail() { return userEmail.get(); }
    public StringProperty userStatusProperty() { return userStatus; }
    public String getUserStatus() { return userStatus.get(); }
    public BooleanProperty loadingProperty() { return loading; }
    public boolean isLoading() { return loading.get(); }
    public StringProperty errorProperty() { return error; }
    public String getError() { return error.get(); }

    public boolean isAdmin() { return UserRole.ADMIN.equals(userRole.get()); }
    public boolean isTrader() { return UserRole.TRADER.equals(userRole.get()); }
    public boolean isActive() { return "ACTIVE".equals(userStatus.get()); }

    public void setAuthenticated(String accessToken, String refreshToken, com.nextrade.common.identifier.UserId userId, String username, UserRole role, String email, String status, boolean rememberMe) {
        this.accessToken.set(accessToken);
        this.refreshToken.set(refreshToken);
        this.userId.set(userId);
        this.username.set(username);
        this.userRole.set(role);
        this.userEmail.set(email);
        this.userStatus.set(status);
        this.authenticated.set(true);
        this.error.set(null);
    }

    public void updateTokens(String accessToken, String refreshToken) {
        this.accessToken.set(accessToken);
        this.refreshToken.set(refreshToken);
    }

    public void logout() {
        authenticated.set(false);
        accessToken.set(null);
        refreshToken.set(null);
        userId.set(null);
        username.set(null);
        userRole.set(null);
        userEmail.set(null);
        userStatus.set(null);
        error.set(null);
    }

    public void setLoading(boolean loading) { this.loading.set(loading); }
    public void setError(String error) { this.error.set(error); }
    public void clearError() { this.error.set(null); }
}
