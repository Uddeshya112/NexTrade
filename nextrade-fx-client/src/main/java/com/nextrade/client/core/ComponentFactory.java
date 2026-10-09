package com.nextrade.client.core;

import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

/** Small JavaFX component helpers shared by the sign-in screens. */
public final class ComponentFactory {
    private ComponentFactory() { }

    public static Button primaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("nx-button-primary");
        button.setMaxWidth(Double.MAX_VALUE);
        return button;
    }

    public static Separator divider() {
        Separator separator = new Separator();
        separator.getStyleClass().add("nx-divider");
        return separator;
    }

    public static VBox card() {
        VBox card = new VBox();
        card.getStyleClass().add("nx-card");
        card.setPadding(new Insets(24));
        return card;
    }

    public static VBox formFieldWithValidation(String labelText, Node input, StringProperty errorProperty) {
        VBox field = new VBox(6);
        Label label = new Label(labelText);
        label.getStyleClass().add("nx-field-label");
        Label error = new Label();
        error.getStyleClass().add("nx-field-error");
        error.textProperty().bind(errorProperty);
        error.visibleProperty().bind(errorProperty.isNotEmpty());
        error.managedProperty().bind(error.visibleProperty());
        if (input instanceof javafx.scene.control.Control control) {
            control.setMaxWidth(Double.MAX_VALUE);
        }
        field.getChildren().addAll(label, input, error);
        return field;
    }
}
