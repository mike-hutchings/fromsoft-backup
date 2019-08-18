import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class Main extends Application {

    private TabPane tabs;

    public static void main(String[] args) {
        FileManager.setup();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        ArrayList<CompatibleGame> detectedGames = FileManager.getAllDetectedGames();

        tabs = new TabPane();

        detectedGames.forEach(game -> {
            Tab tab = new Tab();
            tab.setText(game.toString());
            tab.setContent(populateBackupList(game));
            tab.setClosable(false);
            tabs.getTabs().add(tab);
        });

        Scene scene = new Scene(tabs, 350, 400); // Manage scene size

        primaryStage.setTitle("FS Save Backup");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private Pane populateBackupList(CompatibleGame game) {

        ArrayList<String> backups = FileManager.getAllBackups(game);
        ObservableList<String> items = FXCollections.observableArrayList();
        ListView<String> backupListView = new ListView<>();

        items.addAll(Objects.requireNonNull(backups));

        BorderPane border = new BorderPane();
        border.setPadding(new Insets(20, 0, 20, 20));

        backupListView.setItems(items);
        backupListView.setMaxHeight(Control.USE_PREF_SIZE);
        backupListView.setPrefWidth(150.0);

        border.setLeft(backupListView);
        border.setRight(createButtonColumn());

        return border;
    }

    private VBox createButtonColumn() {

        Button btnAdd = new Button("Add");
        Button btnDelete = new Button("Delete");
        Button btnRename = new Button("Rename");
        Button btnLoad = new Button("Load");

        btnAdd.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btnDelete.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btnRename.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btnLoad.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btnLoad.setStyle("-fx-background-color: Green");

        // Add new backup button event handler
        btnAdd.setOnAction(event -> {
            String currentGameName = tabs.getSelectionModel().getSelectedItem().getText();
            CompatibleGame game = CompatibleGame.valueOf(currentGameName);

            TextInputDialog createAlert = new TextInputDialog("New backup");
            createAlert.setTitle("Create backup");
            createAlert.setHeaderText("Create New " + game + " Backup");
            createAlert.setContentText("Please enter name of backup:");

            Optional<String> result = createAlert.showAndWait();
            result.ifPresent(name -> {
                FileManager.createBackup(game, name);

                Alert saveAlert = new Alert(Alert.AlertType.INFORMATION);
                saveAlert.setTitle("Create backup");
                saveAlert.setHeaderText(null);
                saveAlert.setContentText("New " + game + " backup created!");
                saveAlert.showAndWait();

                tabs.getSelectionModel().getSelectedItem().setContent(populateBackupList(game));
            });
        });

        // Rename backup button event handler
        btnRename.setOnAction(event -> {

            String currentGameName = tabs.getSelectionModel().getSelectedItem().getText();
            CompatibleGame game = CompatibleGame.valueOf(currentGameName);
            BorderPane currentTabBorderPane = (BorderPane) tabs.getSelectionModel().getSelectedItem().getContent();
            ListView listView = (ListView) currentTabBorderPane.getLeft();
            String selection = listView.getSelectionModel().getSelectedItem().toString();

            TextInputDialog createAlert = new TextInputDialog("Rename backup");
            createAlert.setTitle("Rename backup");
            createAlert.setHeaderText("Rename " + game + " Backup '" + selection + "'");
            createAlert.setContentText("Please enter new name of backup:");

            Optional<String> result = createAlert.showAndWait();
            result.ifPresent(name -> {
                FileManager.renameBackup(game, selection, name);

                Alert renameAlert = new Alert(Alert.AlertType.INFORMATION);
                renameAlert.setTitle("Rename backup");
                renameAlert.setHeaderText(null);
                renameAlert.setContentText(game + " backup renamed!");
                renameAlert.showAndWait();

                tabs.getSelectionModel().getSelectedItem().setContent(populateBackupList(game));
            });
        });

        // Delete backup button event handlers
        btnDelete.setOnAction(event -> {

            String currentGameName = tabs.getSelectionModel().getSelectedItem().getText();
            CompatibleGame game = CompatibleGame.valueOf(currentGameName);
            BorderPane currentTabBorderPane = (BorderPane) tabs.getSelectionModel().getSelectedItem().getContent();
            ListView listView = (ListView) currentTabBorderPane.getLeft();
            String selection = listView.getSelectionModel().getSelectedItem().toString();

            Alert createAlert = new Alert(Alert.AlertType.CONFIRMATION);
            createAlert.setTitle("Delete backup");
            createAlert.setHeaderText("Delete " + game + " Backup '" + selection + "'");
            createAlert.setContentText("Are you sure?");

            Optional<ButtonType> result = createAlert.showAndWait();
            result.ifPresent(confirmation -> {
                if (confirmation == ButtonType.OK) {
                    FileManager.deleteBackup(game, selection);

                    Alert deleteAlert = new Alert(Alert.AlertType.INFORMATION);
                    deleteAlert.setTitle("Delete backup");
                    deleteAlert.setHeaderText(null);
                    deleteAlert.setContentText("'" + game + "' backup deleted!");
                    deleteAlert.showAndWait();

                    tabs.getSelectionModel().getSelectedItem().setContent(populateBackupList(game));
                }
            });
        });

        // Load backup button event handlers
        btnLoad.setOnAction(event -> {
            String currentGameName = tabs.getSelectionModel().getSelectedItem().getText();
            CompatibleGame game = CompatibleGame.valueOf(currentGameName);
            BorderPane currentTabBorderPane = (BorderPane) tabs.getSelectionModel().getSelectedItem().getContent();
            ListView listView = (ListView) currentTabBorderPane.getLeft();
            String selection = listView.getSelectionModel().getSelectedItem().toString();
            FileManager.loadBackup(game, selection);

            Alert loadAlert = new Alert(Alert.AlertType.INFORMATION);
            loadAlert.setTitle("Load backup");
            loadAlert.setHeaderText(null);
            loadAlert.setContentText(game + " backup '" + selection + "' loaded!");
            loadAlert.showAndWait();
        });

        VBox vbButtons = new VBox();
        vbButtons.setSpacing(10);
        vbButtons.setPadding(new Insets(0, 20, 10, 20));
        vbButtons.getChildren().addAll(btnAdd, btnDelete, btnRename, btnLoad);

        return vbButtons;
    }
}