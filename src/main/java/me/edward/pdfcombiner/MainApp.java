package me.edward.pdfcombiner;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

@SuppressWarnings("SpellCheckingInspection")
public class MainApp extends Application {

    public final String DESKTOP_PATH = "C:\\Users\\"+System.getProperty("user.name")+"\\Desktop\\";

    public static MainApp instance;

    @Override
    public void start(Stage primaryStage) throws IOException {
        instance = this;
        FXMLLoader mainPageFXML = new FXMLLoader(this.getClass().getResource("MainPage.fxml"));
        Pane pane = mainPageFXML.load();
        Scene scene = new Scene(pane, 1060, 580);
        scene.getStylesheets().add(Objects.requireNonNull(this.getClass().getResource("StyleSheets/MainPage.css")).toString());
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(this.getClass().getResourceAsStream("icons/icon.png"))));
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(980);
        primaryStage.setMinHeight(450);
        primaryStage.setTitle("PDF Combiner, Developed by Eduard M.; UPT, MCTR, Grupa 2.2");
        primaryStage.show();
        MainPage mainPage = mainPageFXML.getController();
        mainPage.positionElements(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}