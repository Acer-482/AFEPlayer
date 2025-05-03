module desktop {
    requires com.acer.afeplayer.core;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires jdk.compiler;

    opens com.acer.afeplayer.desktop to javafx.fxml;
    exports com.acer.afeplayer.desktop;
    exports com.acer.afeplayer.desktop.filePage;
    opens com.acer.afeplayer.desktop.filePage to javafx.fxml;
    exports com.acer.afeplayer.desktop.mediaPage;
    opens com.acer.afeplayer.desktop.mediaPage to javafx.fxml;
}