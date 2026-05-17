package com.markbaengine.util;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Reusable stylized logo component for MarkbaEngine.
 *
 * Renders "Markba" in white/silver and "Engine" in electric blue,
 * bold italic with a slight forward slant, layered drop shadow + glow,
 * and a small tire icon positioned at the "g" in Engine.
 *
 * The sweeping speed arcs are intentionally NOT part of this component —
 * they live in the background windLinesPane so they pass behind the logo
 * at the page level, not clipped inside the logo node.
 *
 * Usage:
 *   StackPane logo = MarkbaLogoView.createMarkbaEngineLogo();
 *   someContainer.getChildren().add(logo);
 */
public final class MarkbaLogoView {

    private MarkbaLogoView() {
    }

    /**
     * Creates the stylized logo at the default hero size (52px font).
     */
    public static StackPane createMarkbaEngineLogo() {
        return createMarkbaEngineLogo(52);
    }

    /**
     * Creates the stylized logo at a specific font size.
     *
     * @param fontSize base font size in pixels
     * @return a StackPane containing the text + tire group
     */
    public static StackPane createMarkbaEngineLogo(double fontSize) {

        // ── "Markba" text — white/silver gradient ──────────────
        Font logoFont = Font.font("Segoe UI", FontWeight.BLACK, FontPosture.ITALIC, fontSize);

        Text markbaText = new Text("Markba");
        markbaText.setFont(logoFont);
        markbaText.setFill(new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.WHITE),
                new Stop(0.30, Color.web("#f1f5f9")),
                new Stop(0.65, Color.web("#cbd5e1")),
                new Stop(1.00, Color.web("#94a3b8"))
        ));

        // ── "Engine" text — electric blue gradient ──────────────
        Text engineText = new Text("Engine");
        engineText.setFont(logoFont);
        engineText.setFill(new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.00, Color.web("#bae6fd")),
                new Stop(0.30, Color.web("#38bdf8")),
                new Stop(0.65, Color.web("#0ea5e9")),
                new Stop(1.00, Color.web("#0369a1"))
        ));

        // ── HBox joins the two text nodes with no gap ────────────
        HBox textRow = new HBox(0, markbaText, engineText);
        textRow.setAlignment(Pos.CENTER);
        // Slight forward lean — italic already, this adds extra "speed" tilt
        textRow.setRotate(-2.0);

        // ── Effects: dark shadow beneath + blue glow ─────────────
        DropShadow darkShadow = new DropShadow();
        darkShadow.setColor(Color.web("#000000", 0.75));
        darkShadow.setRadius(18);
        darkShadow.setSpread(0.06);
        darkShadow.setOffsetX(4);
        darkShadow.setOffsetY(6);

        DropShadow blueGlow = new DropShadow();
        blueGlow.setColor(Color.web("#0ea5e9", 0.55));
        blueGlow.setRadius(26);
        blueGlow.setSpread(0.12);
        blueGlow.setOffsetX(0);
        blueGlow.setOffsetY(0);
        blueGlow.setInput(darkShadow);

        Glow softGlow = new Glow(0.22);
        softGlow.setInput(blueGlow);
        textRow.setEffect(softGlow);

        // ── Assemble ─────────────────────────────────────────────
        StackPane logoPane = new StackPane(textRow);
        logoPane.setAlignment(Pos.CENTER);
        logoPane.setPickOnBounds(false);

        return logoPane;
    }

}
