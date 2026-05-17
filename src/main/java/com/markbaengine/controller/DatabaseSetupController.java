package com.markbaengine.controller;

import com.markbaengine.db.DBConnection;
import com.markbaengine.model.SchemaValidationResult;
import com.markbaengine.service.WorkspaceService;
import com.markbaengine.util.AlertUtil;
import com.markbaengine.util.Navigation;
import com.markbaengine.util.MarkbaLogoView;
import com.markbaengine.util.WorkspaceSession;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Path;

/**
 * Controller for the Database Workspace Setup screen.
 *
 * Layout structure (foreground):
 *   Top bar → Hero logo zone (logoContainer) → Gap → Setup card
 *
 * The logo lives OUTSIDE the card in the hero zone.
 * Sweeping speed arcs are drawn directly into windLinesPane (background)
 * so they pass visually BEHIND the logo, giving the "cutting through wind"
 * effect from the reference image.
 */
public class DatabaseSetupController {

    /* ── FXML-injected UI controls ── */
    @FXML private Label selectedFileLabel;
    @FXML private Label workspacePathLabel;
    @FXML private StackPane logoContainer;   // hero zone above the card
    @FXML private Label requiredTablesLabel;
    @FXML private TextArea validationTextArea;
    @FXML private Button continueButton;

    /* ── Background scene elements ── */
    @FXML private Pane bgScene;
    @FXML private Pane windLinesPane;   // receives both arcs and straight streaks
    @FXML private Pane laneLinesPane;


    // Sky rectangle — stretched to fill the whole background
    @FXML private Rectangle skyRect;

    // Green band + road
    @FXML private Rectangle greenBand;
    @FXML private Rectangle roadBand;
    @FXML private Line roadTop;
    @FXML private Line roadBottom;

    // Left landscape
    @FXML private Circle treeL1;
    @FXML private Rectangle trunkL1;
    @FXML private Rectangle houseL;
    @FXML private Line roofL1;
    @FXML private Line roofL2;
    @FXML private Rectangle winL;
    @FXML private Circle treeL2;
    @FXML private Rectangle trunkL2;

    // Right landscape
    @FXML private Circle treeR1;
    @FXML private Rectangle trunkR1;
    @FXML private Circle treeR2;
    @FXML private Rectangle trunkR2;
    @FXML private Rectangle houseR;
    @FXML private Line roofR1;
    @FXML private Line roofR2;

    private final WorkspaceService workspaceService = new WorkspaceService();

    /* ================================================================
     *  INITIALIZATION
     * ============================================================= */

    @FXML
    private void initialize() {
        // ── Seed initial UI state ──
        requiredTablesLabel.setText("Required tables: " + workspaceService.getRequiredTablesText());
        selectedFileLabel.setText("No SQL file selected yet.");
        workspacePathLabel.setText("Workspace database: not created yet.");
        validationTextArea.setText(
                "Choose a valid Urban Fleet .sql file or create an empty one to start.");
        continueButton.setDisable(true);

        // ── Inject the stylized hero logo above the card ──
        StackPane logo = MarkbaLogoView.createMarkbaEngineLogo(); // default 52px
        logoContainer.getChildren().add(logo);

        ChangeListener<Number> resizeListener = (obs, oldVal, newVal) -> layoutBackground();
        bgScene.widthProperty().addListener(resizeListener);
        bgScene.heightProperty().addListener(resizeListener);

        // Run after first layout pass so bgScene has real dimensions
        Platform.runLater(() -> {
            layoutBackground();
            startAnimations();
        });
    }

    /* ================================================================
     *  BACKGROUND LAYOUT  (responsive — recalculated on resize)
     * ============================================================= */

    /**
     * Positions every background shape relative to the current scene size.
     * Called whenever the window is resized.
     */
    private void layoutBackground() {
        double w = bgScene.getWidth();
        double h = bgScene.getHeight();
        if (w < 1 || h < 1) return;

        // Sky fills the entire background
        skyRect.setWidth(w);
        skyRect.setHeight(h);

        // Green landscape strip (~62–72% vertical)
        double greenY = h * 0.62;
        double greenH = h * 0.10;
        greenBand.setX(0);        greenBand.setY(greenY);
        greenBand.setWidth(w);    greenBand.setHeight(greenH);

        // Road band (below green strip)
        double roadY = greenY + greenH;
        double roadH = h - roadY;
        roadBand.setX(0);         roadBand.setY(roadY);
        roadBand.setWidth(w);     roadBand.setHeight(roadH);

        // Road edge lines
        roadTop.setStartX(0);      roadTop.setStartY(roadY);
        roadTop.setEndX(w);        roadTop.setEndY(roadY);
        roadBottom.setStartX(0);   roadBottom.setStartY(h - 4);
        roadBottom.setEndX(w);     roadBottom.setEndY(h - 4);

        // Lane lines pane — middle of the road band
        double laneY = roadY + roadH * 0.38;
        laneLinesPane.setLayoutX(0);
        laneLinesPane.setLayoutY(laneY);
        laneLinesPane.setPrefWidth(w);
        laneLinesPane.setPrefHeight(roadH * 0.24);

        // Wind pane — spans the upper section where the logo sits
        windLinesPane.setLayoutX(0);
        windLinesPane.setLayoutY(h * 0.08);
        windLinesPane.setPrefWidth(w);
        windLinesPane.setPrefHeight(h * 0.52);


        // ── Left landscape ──
        double groundY = greenY + 4;
        treeL1.setCenterX(w * 0.07);
        treeL1.setCenterY(groundY - 26);
        trunkL1.setX(w * 0.07 - 3.5);
        trunkL1.setY(groundY - 4);

        houseL.setX(w * 0.10);
        houseL.setY(groundY - 46);
        roofL1.setStartX(w * 0.10);      roofL1.setStartY(groundY - 46);
        roofL1.setEndX(w * 0.10 + 32);   roofL1.setEndY(groundY - 80);
        roofL2.setStartX(w * 0.10 + 32); roofL2.setStartY(groundY - 80);
        roofL2.setEndX(w * 0.10 + 64);   roofL2.setEndY(groundY - 46);
        winL.setX(w * 0.10 + 24);
        winL.setY(groundY - 32);

        treeL2.setCenterX(w * 0.17);
        treeL2.setCenterY(groundY - 20);
        trunkL2.setX(w * 0.17 - 3);
        trunkL2.setY(groundY - 4);

        // ── Right landscape ──
        treeR1.setCenterX(w * 0.78);
        treeR1.setCenterY(groundY - 22);
        trunkR1.setX(w * 0.78 - 3);
        trunkR1.setY(groundY - 4);

        treeR2.setCenterX(w * 0.84);
        treeR2.setCenterY(groundY - 28);
        trunkR2.setX(w * 0.84 - 3.5);
        trunkR2.setY(groundY - 4);

        houseR.setX(w * 0.88);
        houseR.setY(groundY - 40);
        roofR1.setStartX(w * 0.88);       roofR1.setStartY(groundY - 40);
        roofR1.setEndX(w * 0.88 + 37);    roofR1.setEndY(groundY - 72);
        roofR2.setStartX(w * 0.88 + 37);  roofR2.setStartY(groundY - 72);
        roofR2.setEndX(w * 0.88 + 74);    roofR2.setEndY(groundY - 40);
    }

    /* ================================================================
     *  ANIMATIONS
     * ============================================================= */

    private void startAnimations() {

        // ── 1. Hero logo subtle float (gentle vertical bob) ──────
        Timeline logoFloat = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(logoContainer.translateYProperty(), 0),
                        new KeyValue(logoContainer.opacityProperty(), 0.93)),
                new KeyFrame(Duration.millis(2400),
                        new KeyValue(logoContainer.translateYProperty(), -4),
                        new KeyValue(logoContainer.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(4800),
                        new KeyValue(logoContainer.translateYProperty(), 0),
                        new KeyValue(logoContainer.opacityProperty(), 0.93))
        );
        logoFloat.setCycleCount(Animation.INDEFINITE);
        logoFloat.play();

        // ── 2. Wind hitting the logo ─────────────────────────────
        spawnWindThatHitsLogo();

        // ── 3. Road lane lines (left → loop) ─────────────────────
        spawnLaneLines();
    }

    /**
     * Straight wind streaks flying right-to-left across the windLinesPane.
     * Each line's start X is staggered across the full scene width so they
     * are evenly distributed at startup — no clustering on the left.
     */
    private void spawnWindThatHitsLogo() {
        double[] yPositions = { 22, 48, 68, 92, 112, 132, 158, 178,
                                38, 82, 122, 52, 102, 142, 28, 162 };

        for (int i = 0; i < 16; i++) {
            double lineLen = 40 + (i % 5) * 22;
            Line streak = new Line(0, 0, lineLen, 0);
            streak.getStyleClass().add(i % 3 == 0 ? "wind-line-bright" : "wind-line-soft");
            streak.setLayoutX(0);
            streak.setLayoutY(yPositions[i]);
            windLinesPane.getChildren().add(streak);

            double dur = 1.4 + (i % 5) * 0.22;

            // Stagger initial positions evenly across the scene width
            // so at startup lines are spread out, not all arriving from the same point.
            // We read bgScene width (now valid after Platform.runLater).
            double sceneW = Math.max(bgScene.getWidth(), 800);
            double startOffset = sceneW * ((double) i / 16.0); // 0 .. sceneW-1

            Timeline motion = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(streak.translateXProperty(), sceneW + lineLen + startOffset)),
                    new KeyFrame(Duration.seconds(dur),
                            new KeyValue(streak.translateXProperty(), -(lineLen + 20)))
            );
            motion.setCycleCount(Animation.INDEFINITE);
            motion.play();
        }
    }



    /**
     * Draws dashed-center-line segments that scroll left to simulate road motion.
     */
    private void spawnLaneLines() {
        for (int i = 0; i < 30; i++) {
            Line lane = new Line(0, 0, 120, 0);
            lane.getStyleClass().add("moving-lane-line");
            lane.setLayoutY(0);
            lane.setLayoutX(i * 210);
            laneLinesPane.getChildren().add(lane);

            Timeline laneMotion = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(lane.translateXProperty(), 0)),
                    new KeyFrame(Duration.seconds(1.6),
                            new KeyValue(lane.translateXProperty(), -210))
            );
            laneMotion.setCycleCount(Animation.INDEFINITE);
            laneMotion.play();
        }
    }

    /* ================================================================
     *  ACTION HANDLERS  (logic unchanged — do not modify)
     * ============================================================= */

    @FXML
    private void chooseSqlFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Urban Fleet SQL File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SQL files", "*.sql"));
        File selectedFile = fileChooser.showOpenDialog(getWindow());
        if (selectedFile == null) return;

        Path sqlPath = selectedFile.toPath();
        SchemaValidationResult validationResult = workspaceService.validateSqlFile(sqlPath);
        validationTextArea.setText(validationResult.toDisplayText());

        if (!validationResult.isValid()) {
            selectedFileLabel.setText("Invalid file: " + selectedFile.getName());
            workspacePathLabel.setText("Workspace database: not created.");
            continueButton.setDisable(true);
            AlertUtil.error("Invalid SQL file", validationResult.toDisplayText());
            return;
        }

        try {
            Path databasePath = workspaceService.loadSqlFileAsWorkspace(sqlPath);
            selectedFileLabel.setText("Selected SQL: " + sqlPath.getFileName());
            workspacePathLabel.setText("Workspace database: " + databasePath.toAbsolutePath());
            continueButton.setDisable(false);
            AlertUtil.info("Workspace ready",
                    "The SQL file is valid and the workspace database was created successfully.");
        } catch (RuntimeException ex) {
            continueButton.setDisable(true);
            selectedFileLabel.setText(
                    "File selected but could not be loaded: " + selectedFile.getName());
            workspacePathLabel.setText("Workspace database: not created.");
            AlertUtil.error("Could not load SQL file", ex.getMessage());
        }
    }

    @FXML
    private void createEmptyDatabase() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Empty Urban Fleet SQL File");
        fileChooser.setInitialFileName("urban_fleet_empty_database.sql");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("SQL files", "*.sql"));
        File outputFile = fileChooser.showSaveDialog(getWindow());
        if (outputFile == null) return;

        try {
            Path databasePath = workspaceService.createEmptySqlWorkspace(outputFile.toPath());
            selectedFileLabel.setText("Created SQL: " + outputFile.toPath().getFileName());
            workspacePathLabel.setText("Workspace database: " + databasePath.toAbsolutePath());
            validationTextArea.setText(
                    "Empty Urban Fleet schema created successfully.\n"
                    + "No business data was inserted. Start from the dashboard by adding "
                    + "lookup data first: depots, models, mechanics, suppliers, then vehicles and logs.");
            continueButton.setDisable(false);
            AlertUtil.info("Empty database created",
                    "The empty SQL file and its local workspace database are ready.");
        } catch (RuntimeException ex) {
            continueButton.setDisable(true);
            AlertUtil.error("Could not create empty database", ex.getMessage());
        }
    }

    @FXML
    private void useDemoWorkspace() {
        DBConnection.useDefaultWorkspaceDatabase();
        WorkspaceSession.setWorkspace(
                null, DBConnection.getWorkspacePath(), "Using bundled demo workspace.");
        selectedFileLabel.setText("Using bundled demo data.");
        workspacePathLabel.setText("Workspace database: " + DBConnection.getWorkspacePath());
        validationTextArea.setText(
                "Demo workspace selected. It contains seeded sample data for testing "
                + "the screens and reports.");
        continueButton.setDisable(false);
    }

    @FXML
    private void continueToDashboard() {
        if (!WorkspaceSession.isLoaded()) {
            AlertUtil.error("Workspace required",
                    "Choose or create a database workspace first.");
            return;
        }
        Navigation.showDashboard();
    }

    private Window getWindow() {
        return selectedFileLabel.getScene().getWindow();
    }
}
