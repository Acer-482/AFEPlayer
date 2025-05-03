package com.acer.afeplayer.desktop;

import com.acer.afeplayer.desktop.mediaPage.MediaController;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("file-page.fxml"));
        Scene scene = new Scene(fxmlLoader.load()); // 加载场景
        /* 初始化显示窗口 */
        stage.setScene(scene);
        stage.show();
    }

    // 显示媒体控制器
    public static void showMediaController(String defaultFile) {
        try {
            Stage stage = new Stage(); // 创建新窗口
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("media-page.fxml")); // 加载fxml\
            Scene scene = new Scene(loader.load());
            MediaController controller = loader.getController();
            controller.loadMedia(defaultFile); // 直接加载媒体
            controller.playMedia(); // 播放媒体
            // 显示窗口
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {
        launch();
    }
}