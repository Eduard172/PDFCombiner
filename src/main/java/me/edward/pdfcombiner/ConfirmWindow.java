package me.edward.pdfcombiner;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class ConfirmWindow implements Initializable {

    public enum StateBeforeStartingProcess {
        ALREADY_EXISTING, INVALID_PATH, NOT_A_DIRECTORY, OK
    }

    @FXML private AnchorPane anchorPane;
    @FXML private Label titleLabel;
    @FXML private TextField targetPDFName, targetPDFPath;
    @FXML private Button targetPDFPathButton, giveUpButton, combinePDFButton;
    @FXML public CheckBox openInExplorer;

    private Stage stage;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    public void setupComponentsFunctionality(Stage stage){
        this.stage = stage;
        this.giveUpButton.setOnAction(event -> this.stage.close());
        this.targetPDFPathButton.setOnAction(event -> {
            DirectorySelector selector = new DirectorySelector()
                    .setInitialDirectory(new File(MainApp.instance.DESKTOP_PATH))
                    .setTitle("Selecteaza destinatia PDF-ului final");
            selector.launch();
            File file = selector.getResult();
            if(file == null){
                return;
            }
            this.targetPDFPath.setText(file.getAbsolutePath());
        });
        this.combinePDFButton.setOnAction(event -> {
            MainPage mp = MainPage.instance;
            StateBeforeStartingProcess state = this.getStateBeforeStartingProcess();
            if(state == StateBeforeStartingProcess.OK){
                PDFMergerUtility mergerUtility = new PDFMergerUtility();
                System.out.println("Combining "+mp.pdfList.getItems().size()+" pdfs...");
                new Thread(() -> {
                    try {
                        for(int i = 0; i < mp.pdfList.getItems().size(); i++){
                            System.out.println(mp.PDFs.get(i).getKey()+" -- "+mp.PDFs.get(i).getValue());
                            mergerUtility.addSource(mp.PDFs.get(i).getValue());
                        }
                        final String destPath = this.targetPDFPath.getText()+"\\"+(
                                this.targetPDFName.getText().endsWith(".pdf") ?
                                        this.targetPDFName.getText() : this.targetPDFName.getText()+".pdf"
                        );
                        mergerUtility.setDestinationFileName(destPath);
                        mergerUtility.mergeDocuments(null);
                        Platform.runLater(() -> mp.setStatusLabel("PDF-ul final a fost salvat"));
                        if(this.openInExplorer.isSelected()){
                            try {
                                Desktop.getDesktop().open(new File(this.targetPDFPath.getText()));
                            }catch (IOException ex){
                                ex.printStackTrace(System.err);
                            }
                        }
                        Platform.runLater(() -> this.stage.close());
                    }catch (IOException ex){
                        Platform.runLater(() -> this.stage.close());
                        mp.setStatusLabel("Ceva nu a mers bine... Fisierul fie este deteriorat sau a fost mutat din locatia sa originala. ("+ex.getClass()+")");
                        ex.printStackTrace(System.err);
                    }
                }).start();
            }else{
                this.stage.close();
                mp.setStatusLabel(this.getMessageBasedOnStatus(state));
            }
        });
        Pair<String, String> suggestion = this.suggestPDFNameAndPath();
        this.targetPDFName.setText(suggestion.getKey());
        this.targetPDFPath.setText(suggestion.getValue());
    }

    private StateBeforeStartingProcess getStateBeforeStartingProcess(){
        File destinationDir = new File(this.targetPDFPath.getText());
        if(!destinationDir.exists())
            return StateBeforeStartingProcess.INVALID_PATH;
        if(!destinationDir.isDirectory())
            return StateBeforeStartingProcess.NOT_A_DIRECTORY;
        File finalFile = new File(destinationDir.getAbsolutePath()+"\\"+this.targetPDFName.getText()+".pdf");
        if(finalFile.exists())
            return StateBeforeStartingProcess.ALREADY_EXISTING;
        return StateBeforeStartingProcess.OK;
    }

    private Pair<String, String> suggestPDFNameAndPath(){
        MainPage mp = MainPage.instance;
        String path = mp.pdfSelectedPath.get();
        String initialSuggest = "Combined_PDF.pdf";
        String name = initialSuggest;
        File[] files = new File(path).listFiles();
        assert files != null;
        int index = 1;
        while (true){
            boolean ok = true;
            for(File f : files){
                if(f.getName().equals(name)){
                    ok = false;
                    name = initialSuggest.replace(".pdf", "").concat("_"+index+".pdf");
                    index++;
                }
            }
            if(ok) break;
        }
        //Key - Target PDF Name; Value - Target PDF Path
        return Pair.of(name.replace(".pdf", ""), path);
    }

    private String getMessageBasedOnStatus(StateBeforeStartingProcess state){
        return switch (state){
            case NOT_A_DIRECTORY -> "Path-ul selectat nu constituie un folder.";
            case ALREADY_EXISTING -> "Un fisier cu acest nume exista deja.";
            case INVALID_PATH -> "Path-ul selectat este invalid.";
            default -> "";
        };
    }

    public void setNumberOfPDFs(int numberOfPDFs){
        this.titleLabel.setText(this.titleLabel.getText().replace("[N]", String.valueOf(numberOfPDFs)));
    }



}
