package shticell.client.component.sheet.main;

import immutable.objects.SheetDTO;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import shticell.client.component.sheet.center.CenterController;
import shticell.client.component.sheet.top.TopController;

import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

public class SheetMainRefresher extends TimerTask {

    private final CenterController centerController;
    private final TopController topController;
    private final SharedModel sharedModel;
    private final ComboBox<String> sheetVersionSelector;

    public SheetMainRefresher(CenterController centerController, TopController topController, SharedModel sharedModel) {
        this.centerController = centerController;
        this.topController = topController;
        this.sharedModel = sharedModel;
        this.sheetVersionSelector = topController.getSheetVersionSelector();
    }

    @Override
    public void run() {
        CompletableFuture<Map<Integer, SheetDTO>> versionHistoryFuture =
                centerController.getServerEngineService().getVersionHistory(sharedModel.getSheetName());

        versionHistoryFuture.thenAccept(versionHistory -> {
            int latestVersion = versionHistory.keySet().stream().max(Integer::compare).orElse(0);
            int currentVersion = sharedModel.getCurrentVersionLoaded();

            if (latestVersion > currentVersion) {
                Platform.runLater(() -> {
                    sheetVersionSelector.getStyleClass().add("highlight-selector");
                    sheetVersionSelector.setTooltip(new Tooltip("A newer version is available."));
                    Platform.runLater(() -> {topController.populateVersionSelectorFromRefresher();});

                });
            } else {
                Platform.runLater(() -> {
                    sheetVersionSelector.getStyleClass().removeAll("highlight-selector");
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