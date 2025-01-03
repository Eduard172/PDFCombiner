package me.edward.pdfcombiner;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class MainPage implements Initializable {

    @FXML private Label titleLabel, top1, top2, top3, selectedPDFsLabel;
    @FXML public Label statusOnFindLabel, statusLabel;
    @FXML public AnchorPane anchorPane;
    @FXML public Button pathButton, combineButton, resetButton;
    @FXML public TextField pathField;
    @FXML public ListView<String> pdfFoundList, pdfList;
    @FXML public Hyperlink sourceCode;

    public final LambdaObj<String> pdfSelectedPath = LambdaObj.of();
    public final List<Pair<String, String>> PDFs = new ArrayList<>();

    private Stage stage;

    public static MainPage instance;

    private final ChangeListener<? super Number> widthChange = (obs, oldVal, newVal) -> this.fitLabelsRelativeToAppWidthAndHeight();
    private final ChangeListener<? super Number> heightChange = (obs, oldVal, newVal) -> this.fitLabelsRelativeToAppWidthAndHeight();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        this.setupComponentsFunctionality();
    }

    public void positionElements(Stage stage){
        this.stage = stage;
        Scene scene = stage.getScene();
        this.pdfFoundList.setVisible(false);
        this.resetButton.setVisible(false);
        this.combineButton.setVisible(false);
        this.fitLabelsRelativeToAppWidthAndHeight();
        scene.widthProperty().addListener(this.widthChange);
        scene.heightProperty().addListener(this.heightChange);
        this.pathField.setText(MainApp.instance.DESKTOP_PATH);
        this.statusOnFindLabel.requestFocus();
        instance = this;
    }

    private void fitLabelsRelativeToAppWidthAndHeight(){
        //Title stays the same
        //Hard coded values were found by checking the element's boundaries using Scene Builder.
        final double currentSceneWidth = this.stage.getScene().getWidth();
        final double currentSceneHeight = this.stage.getScene().getHeight();
        this.top1.setLayoutX(currentSceneWidth - this.top1.getWidth() - 14);
        this.top2.setLayoutX(currentSceneWidth - this.top2.getWidth() - 33);
        this.top3.setLayoutX(currentSceneWidth - this.top3.getWidth() - 93);
        this.pdfFoundList.setPrefHeight(currentSceneHeight - (153 + 10));
        this.pdfList.setPrefHeight(currentSceneHeight - (153 + 50 + this.combineButton.getHeight()));
        this.pdfList.setLayoutX(currentSceneWidth - this.pdfList.getWidth() - 14);
        this.combineButton.setLayoutX(currentSceneWidth - this.combineButton.getWidth() - 14);
        this.combineButton.setLayoutY(currentSceneHeight - this.combineButton.getHeight() - 14);
        this.resetButton.setLayoutX(currentSceneWidth - this.resetButton.getWidth() - 153);
        this.resetButton.setLayoutY(currentSceneHeight - this.resetButton.getHeight() - 14);
        this.statusLabel.setLayoutY(currentSceneHeight - this.statusLabel.getHeight() - 18);
        this.statusLabel.setLayoutX(currentSceneWidth / 2 - this.statusLabel.getWidth() / 2);
        this.selectedPDFsLabel.setLayoutX(currentSceneWidth - this.selectedPDFsLabel.getWidth() - 156);
    }

    private void setupComponentsFunctionality(){
        this.pathButton.setOnAction(event -> {
            DirectorySelector selector = new DirectorySelector()
                                            .setTitle("Selecteaza Path-ul manual pentru a selecta PDF-urile.")
                                            .setInitialDirectory(new File(MainApp.instance.DESKTOP_PATH));
            selector.launch();
            if(selector.getResult() == null){
                return;
            }
            this.pathField.setText(selector.getResult().getAbsolutePath());
        });
        this.pathField.textProperty().addListener((obs, oldVal, newVal) -> {
            this.statusOnFindLabel.setText("Se cauta PDF-uri...");
            this.pdfFoundList.getItems().clear();
            this.populatePDFList(newVal);
        });

        this.sourceCode.setOnAction(event -> {
            try {
                Desktop.getDesktop().browse(URI.create("https://github.com/Eduard172/PDFCombiner"));
            }catch (IOException exception){
                exception.printStackTrace(System.err);
            }
        });

        this.resetButton.setOnAction(event -> {
            this.pdfList.getItems().clear();
            this.PDFs.clear();
            this.resetButton.setVisible(false);
            this.combineButton.setVisible(false);
            this.statusOnFindLabel.requestFocus();
            this.populatePDFList(this.pdfSelectedPath.get());
        });

        this.pdfFoundList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            int index = this.pdfFoundList.getSelectionModel().getSelectedIndex();
            if(this.pdfFoundList.getSelectionModel().getSelectedItem() == null)
                return;
            this.sleep(Duration.millis(50), 1, run -> {
                this.pdfList.getItems().add(this.pdfFoundList.getItems().get(index));
                this.PDFs.add(Pair.of(this.pdfFoundList.getItems().get(index), this.pdfSelectedPath.get()+"\\"+this.pdfFoundList.getItems().get(index)));
                this.pdfFoundList.getSelectionModel().clearSelection();
                boolean b = !this.pdfList.getItems().isEmpty();
                this.resetButton.setVisible(b);
                this.combineButton.setVisible(b);
                if(!this.pdfFoundList.getItems().isEmpty()){
                    this.pdfFoundList.getItems().remove(index);
                }
            }, null);
        });

        this.pdfFoundList.setStyle("-fx-border-color: gray;");
        this.pdfList.setStyle("-fx-border-color: gray;");
        this.pdfFoundList.setCellFactory(listview -> new ListCell<>(){
            @Override
            protected void updateItem(String text, boolean empty) {
                super.updateItem(text, empty);
                if(text == null || empty){
                    this.setText(null);
                }else{
                    this.setText(text);
                    this.setStyle("-fx-text-fill: lime;");
                    this.setOnMouseEntered(event -> {
                        if(this.getIndex() >= this.getListView().getItems().size()){
                            this.setStyle("-fx-background-color: transparent;");
                            return;
                        }
                        this.setStyle((this.isSelected()) ?
                                "-fx-background-color: #5c5c5c;" :
                                "-fx-background-color: gray; -fx-text-fill: #00f7ff;");
                    });
                    this.setOnMouseExited(event -> this.setStyle((this.isSelected()
                            ? "-fx-background-color: #5c5c5c;" : "-fx-background-color: #575757;")
                            +" -fx-text-fill: lime;"));
                }
            }
        });
        this.pdfList.setCellFactory(this.pdfFoundList.getCellFactory());

        this.combineButton.setOnAction(event -> {
            try {
                if(pdfList.getItems().size() < 2){
                    this.setStatusLabel("Cel putin 2 pdf-uri trebuie selectate pentru a continua.");
                    return;
                }
                FXMLLoader fxmlLoader = new FXMLLoader(MainApp.instance.getClass().getResource("ConfirmWindow.fxml"));
                Pane pane = fxmlLoader.load();
                ConfirmWindow confirmWindow = fxmlLoader.getController();
                Scene scene = new Scene(pane);
                scene.getStylesheets().add(Objects.requireNonNull(this.getClass().getResource("StyleSheets/MainPage.css")).toString());
                Stage confirmWindowStage = new Stage();
                confirmWindowStage.getIcons().add(new Image(Objects.requireNonNull(MainApp.instance.getClass().getResourceAsStream("icons/icon.png"))));
                confirmWindowStage.setScene(scene);
                confirmWindowStage.setResizable(false);
                confirmWindowStage.setTitle("Ultimii pasi inainte de a incepe procesul...");
                confirmWindowStage.initModality(Modality.APPLICATION_MODAL);
                confirmWindowStage.initOwner(this.stage);
                confirmWindowStage.show();
                confirmWindow.setupComponentsFunctionality(confirmWindowStage);
                confirmWindow.setNumberOfPDFs(this.pdfList.getItems().size());
            }catch (IOException exception){
                exception.printStackTrace(System.err);
            }

        });

    }

    public void setStatusLabel(String text){
        this.statusLabel.setText(text);
        this.sleep(Duration.millis(20), 1, run ->
                this.statusLabel.setLayoutX(this.stage.getScene().getWidth() / 2 - this.statusLabel.getWidth() / 2), null);
    }

    private void populatePDFList(String path){
        this.pdfFoundList.getItems().clear();
        File file = new File(path);
        if(!file.exists() || !file.isDirectory()){
            this.pdfFoundList.setVisible(false);
            this.statusOnFindLabel.setText("Nu s-a gasit niciun PDF deoarece Path-ul este invalid.");
            return;
        }
        File[] files = file.listFiles();
        assert files != null && files.length != 0;
        int found = 0;
        for(File f : files){
            if(f.getAbsolutePath().endsWith(".pdf")){
                pdfFoundList.getItems().add(f.getName());
                found++;
            }
        }
        this.pdfSelectedPath.set(path);
        if(found == 0){
            this.statusOnFindLabel.setText("Nu s-a gasit niciun PDF.");
        }else{
            this.statusOnFindLabel.setText("Au fost gasite "+found+" "+(found == 1 ? "PDF" : "PDF-uri")+".");
        }
        this.pdfFoundList.setVisible(found != 0);
    }

    private String getPDFAbsPathByName(String name){
        String path = this.pdfSelectedPath.get();
        File dir = new File(path);
        File[] files = dir.listFiles();
        assert files != null;
        for(File f : files){
            if(f.getName().contains(name)){
                return f.getAbsolutePath();
            }
        }
        return null;
    }

    @SuppressWarnings("SameParameterValue")
    private void sleep(Duration duration, int cycleCounts, EventHandler<ActionEvent> whileRunning, EventHandler<ActionEvent> onFinished){
        Timeline timeline = new Timeline();
        timeline.setCycleCount(cycleCounts);
        timeline.getKeyFrames().add(new KeyFrame(duration, whileRunning));
        timeline.setOnFinished(onFinished);
        timeline.play();
    }

}
