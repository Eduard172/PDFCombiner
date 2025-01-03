module me.edward.pdfconverter {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires org.apache.pdfbox;
    requires org.apache.pdfbox.io;


    opens me.edward.pdfcombiner to javafx.fxml;
    exports me.edward.pdfcombiner;
}