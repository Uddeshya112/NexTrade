package com.nextrade.client.view;

import com.nextrade.client.core.ComponentFactory;
import com.nextrade.client.viewmodel.auth.LoginViewModel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class LoginView extends VBox {
    private final LoginViewModel viewModel;

    public LoginView(LoginViewModel viewModel) {
        this.viewModel = viewModel;
        initializeUI();
        bindViewModel();
    }

    private void initializeUI() {
        getStyleClass().add("nx-auth-view");
        setAlignment(Pos.CENTER);
        setPadding(new Insets(40));
        setSpacing(24);
        setMaxWidth(440);
        setFillWidth(false);

        // Brand section
        VBox brand = createBrandSection();

        // Welcome text
        VBox welcome = createWelcomeSection();

        // Form
        VBox form = createFormSection();

        // Options
        HBox options = createOptionsSection();

        // Login button
        Button loginBtn = ComponentFactory.primaryButton("Sign In");
        loginBtn.setOnAction(event -> viewModel.login());
        loginBtn.getStyleClass().add("nx-button-full");
        loginBtn.setPrefHeight(48);
        loginBtn.disableProperty().bind(viewModel.loggingInProperty());

        // Divider
        HBox divider = createDividerSection();

        // Register link
        HBox register = createRegisterSection();

        VBox cardContent = new VBox(24, brand, welcome, ComponentFactory.divider(), form, options, loginBtn, divider, register);
        cardContent.setAlignment(Pos.CENTER);
        cardContent.setFillWidth(true);

        VBox card = ComponentFactory.card();
        card.getStyleClass().add("nx-auth-card");
        card.setPadding(new Insets(32));
        card.setMaxWidth(440);
        card.getChildren().setAll(cardContent);

        getChildren().add(card);
    }

    private VBox createBrandSection() {
        VBox brand = new VBox(8);
        brand.setAlignment(Pos.CENTER);
        Label logo = new Label("NEXTRADE");
        logo.getStyleClass().add("nx-logo-text");
        Label tagline = new Label("Algorithmic Trading Platform");
        tagline.getStyleClass().add("nx-tagline");
        brand.getChildren().addAll(logo, tagline);
        return brand;
    }

    private VBox createWelcomeSection() {
        VBox welcome = new VBox(4);
        welcome.setAlignment(Pos.CENTER);
        Label title = new Label("Welcome back");
        title.getStyleClass().add("nx-heading-2");
        Label subtitle = new Label("Sign in to access your trading dashboard");
        subtitle.getStyleClass().add("nx-body-secondary");
        welcome.getChildren().addAll(title, subtitle);
        return welcome;
    }

    private VBox createFormSection() {
        VBox form = new VBox(16);
        form.setFillWidth(true);

        TextField emailField = new TextField();
        emailField.textProperty().bindBidirectional(viewModel.emailOrUsernameProperty());
        emailField.getStyleClass().addAll("nx-input", "nx-input-lg");
        emailField.setPromptText("Email or Username");
        emailField.setPrefHeight(48);

        PasswordField passwordField = new PasswordField();
        passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());
        passwordField.getStyleClass().addAll("nx-input", "nx-input-lg", "nx-password-field");
        passwordField.setPromptText("Password");
        passwordField.setPrefHeight(48);
        passwordField.visibleProperty().bind(viewModel.showPasswordProperty().not());
        passwordField.managedProperty().bind(passwordField.visibleProperty());

        TextField visiblePasswordField = new TextField();
        visiblePasswordField.textProperty().bindBidirectional(viewModel.passwordProperty());
        visiblePasswordField.getStyleClass().addAll("nx-input", "nx-input-lg", "nx-password-field");
        visiblePasswordField.setPromptText("Password");
        visiblePasswordField.setPrefHeight(48);
        visiblePasswordField.visibleProperty().bind(viewModel.showPasswordProperty());
        visiblePasswordField.managedProperty().bind(visiblePasswordField.visibleProperty());

        CheckBox showPassword = new CheckBox("Show password");
        showPassword.selectedProperty().bindBidirectional(viewModel.showPasswordProperty());
        showPassword.getStyleClass().add("nx-show-password-toggle");
        HBox passwordInput = new HBox(8, passwordField, visiblePasswordField, showPassword);
        HBox.setHgrow(passwordField, Priority.ALWAYS);
        HBox.setHgrow(visiblePasswordField, Priority.ALWAYS);

        VBox emailWrapper = ComponentFactory.formFieldWithValidation("Email / Username", emailField, viewModel.emailErrorProperty());
        VBox passwordWrapper = ComponentFactory.formFieldWithValidation("Password", passwordInput, viewModel.passwordErrorProperty());

        Label rateLimitLabel = new Label();
        rateLimitLabel.textProperty().bind(viewModel.rateLimitErrorProperty());
        rateLimitLabel.getStyleClass().add("nx-rate-limit-error");
        rateLimitLabel.visibleProperty().bind(viewModel.rateLimitErrorProperty().isNotEmpty());
        rateLimitLabel.managedProperty().bind(viewModel.rateLimitErrorProperty().isNotEmpty());

        form.getChildren().addAll(emailWrapper, passwordWrapper, rateLimitLabel);
        return form;
    }

    private HBox createOptionsSection() {
        HBox options = new HBox();
        options.setAlignment(Pos.CENTER_LEFT);
        options.setSpacing(16);

        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.getStyleClass().add("nx-checkbox");
        rememberMe.selectedProperty().bindBidirectional(viewModel.rememberMeProperty());

        Hyperlink forgotPassword = new Hyperlink("Forgot password?");
        forgotPassword.getStyleClass().add("nx-link");
        forgotPassword.setOnAction(e -> viewModel.forgotPassword());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        options.getChildren().addAll(rememberMe, spacer, forgotPassword);
        return options;
    }

    private HBox createDividerSection() {
        HBox divider = new HBox(12);
        divider.setAlignment(Pos.CENTER);
        Separator left = new Separator(); left.getStyleClass().add("nx-divider"); HBox.setHgrow(left, Priority.ALWAYS);
        Label text = new Label("or"); text.getStyleClass().add("nx-divider-text");
        Separator right = new Separator(); right.getStyleClass().add("nx-divider"); HBox.setHgrow(right, Priority.ALWAYS);
        divider.getChildren().addAll(left, text, right);
        return divider;
    }

    private HBox createRegisterSection() {
        HBox register = new HBox(4);
        register.setAlignment(Pos.CENTER);
        Label text = new Label("Don't have an account?");
        text.getStyleClass().add("nx-body-secondary");
        Hyperlink link = new Hyperlink("Create account");
        link.getStyleClass().add("nx-link-primary");
        link.setOnAction(e -> viewModel.navigateToRegister());
        register.getChildren().addAll(text, link);
        return register;
    }

    private void bindViewModel() {
        viewModel.loggingInProperty().addListener((obs, old, val) -> {
            // Update button text/spinner
        });
    }
}
