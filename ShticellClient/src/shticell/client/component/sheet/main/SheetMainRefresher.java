package shticell.client.component.sheet.main;

import immutable.objects.SheetDTO;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import shticell.client.component.sheet.center.CenterController;
import shticell.client.util.Constants;

import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

public class SheetMainRefresherTask extends TimerTask {

    private final CenterController centerController;
    private final SharedModel sharedModel;
    private final ComboBox<String> sheetVersionSelector;

    public SheetMainRefresherTask(CenterController centerController, SharedModel sharedModel, ComboBox<String> sheetVersionSelector) {
        this.centerController = centerController;
        this.sharedModel = sharedModel;
        this.sheetVersionSelector = sheetVersionSelector;
    }

    @Override
    public void run() {
        CompletableFuture<Map<Integer, SheetDTO>> versionHistoryFuture =
                centerController.getServerEngineService().getVersionHistory(sharedModel.getSheetName());

        versionHistoryFuture.thenAccept(versionHistory -> {
            int latestVersion = versionHistory.keySet().stream().max(Integer::compare).orElse(0);
            int currentVersion = sharedModel.getCurrentVersion();

            if (latestVersion > currentVersion) {
                Platform.runLater(() -> {
                    sheetVersionSelector.getStyleClass().add("highlight-selector");
                    sheetVersionSelector.setTooltip(new javafx.scene.control.Tooltip("A newer version is available."));
                });
            } else {
                Platform.runLater(() -> {
                    sheetVersionSelector.getStyleClass().removeAll("highlight-selector");
                    sheetVersionSelector.setTooltip(null);
                });
            }
        }).exceptionally(ex -> {
            Platform.runLater(() -> {
                System.err.println("Error checking version history: " + ex.getMessage());
            });
            return null;
        });
    }
}