package main;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class AnimationController {

    private boolean animationsEnabled = false;

    public void startAnimations(Node node) {
        // Play all animations
        fade(node);
        rotate(node);
        scale(node);
        translate(node);
    }

    // Method to perform a fade animation
    public void fade(Node node) {
        if (!animationsEnabled) {
            return; // If animations are disabled, don't perform them
        }

        FadeTransition fade = new FadeTransition(Duration.seconds(2), node);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    // Method to perform a rotate animation
    public void rotate(Node node) {
        if (!animationsEnabled) {
            return; // If animations are disabled, don't perform them
        }

        RotateTransition rotate = new RotateTransition(Duration.seconds(2), node);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.play();
    }

    // Method to perform a scale animation
    public void scale(Node node) {
        if (!animationsEnabled) {
            return; // If animations are disabled, don't perform them
        }

        ScaleTransition scale = new ScaleTransition(Duration.seconds(2), node);
        scale.setFromX(0.5);
        scale.setToX(1.0);
        scale.setFromY(0.5);
        scale.setToY(1.0);
        scale.play();
    }

    // Method to perform a translate (move) animation
    public void translate(Node node) {
        if (!animationsEnabled) {
            return; // If animations are disabled, don't perform them
        }

        TranslateTransition translate = new TranslateTransition(Duration.seconds(2), node);
        translate.setFromX(-100);
        translate.setToX(0);
        translate.play();
    }


    // Method to enable/disable animations
    public void setAnimationsEnabled(boolean enabled) {
        this.animationsEnabled = enabled;
    }
}
