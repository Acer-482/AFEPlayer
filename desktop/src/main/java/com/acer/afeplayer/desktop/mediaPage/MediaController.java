package com.acer.afeplayer.desktop.mediaPage;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;

public class MediaController {
    private Stage stage; // 窗体
    @FXML private BorderPane root;

    @FXML private Pane mediaPane; // 媒体父控件
    @FXML private MediaView mediaView; // 媒体控件
    @FXML private CheckBox cbx_playVideo; // 播放媒体复选框
    @FXML private TextField tfd_mediaPath; // 媒体路径
    @FXML private Button btn_selectMedia; // 选择媒体
    @FXML private Button btn_loadMedia; // 加载媒体
    @FXML private Label lbl_mediaPlayInfo; // 媒体信息
    @FXML private Slider sdr_mediaProgress; // 媒体进度条

    private MediaManager mediaManager; // 媒体管理器
    private Duration mediaDuration; // 媒体时长
    private boolean isDraggedProgress; // 正在拖动进度条

    // 初始化 //
    @FXML
    public void initialize() {
        mediaManager = new MediaManager(mediaView);

        // 播放结束 //
        mediaManager.setOnEnd(this::reset);

        /* 设置自动销毁并获取Stage */
        root.sceneProperty().addListener((observableValue, s1, s2) -> {
            if (s1 == null && s2 != null) {
                s2.windowProperty().addListener((obs, w1, w2) -> {
                    if (w1 == null && w2 != null) {
                        w2.setOnCloseRequest(windowEvent -> dispose());
                        stage = (Stage) w2;
                    }
                });
            }
        });
        // 组件初始化完成代码 //
        Platform.runLater(() -> {
            /* 绑定媒体自动缩放 */
            mediaView.fitWidthProperty().bind(mediaPane.widthProperty());
            mediaView.fitHeightProperty().bind(mediaPane.heightProperty());
            /* 加载媒体完毕 */
            mediaManager.setOnReady(() -> {
                mediaDuration = mediaManager.getMedia().getDuration(); // 获取媒体时长
                // 启用控件
                cbx_playVideo.setSelected(false);
                cbx_playVideo.setDisable(false);
                sdr_mediaProgress.setDisable(false);
                // 更新媒体信息
                updateMediaInfoUI();
            });
            /* 拖动进度条 */
            sdr_mediaProgress.setOnMousePressed(mouseEvent -> isDraggedProgress = true);
            sdr_mediaProgress.setOnMouseReleased(mouseEvent -> {
                if (isDraggedProgress) {
                    isDraggedProgress = false;
                    /* 设置媒体播放进度 */
                    // 如果媒体已经播放完毕 则重置后再设置
                    if (mediaManager.isFinished()) reset();
                    // 设置
                    mediaManager.seek(new Duration(mediaManager.getMedia().getDuration().toMillis() *
                            sdr_mediaProgress.getValue()));
                    updateMediaInfoUI();
                }
            });
            /* 进度条播放更新 */
            mediaManager.setCurrentTimeListener(
                    (obs, oldDuration, newDuration) ->
                            updateMediaInfoUI(newDuration));
        });
    }
    /* 事件 */
    // 播放暂停媒体
    @FXML void playPauseMedia(ActionEvent event) {
        updateMediaPlay();
    }
    // 选择媒体
    @FXML void onSelectMedia() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择媒体");
        // 设置过滤器
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("视频文件",
                "*.mp4", "*.avi", "*.avi", "*.flv"),
                new FileChooser.ExtensionFilter("音频文件",
                "*.mp3"),
                new FileChooser.ExtensionFilter("其他",
                "*.*"));
        // 显示打开对话框
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            tfd_mediaPath.setText(file.getPath());
        }
    }
    // 加载媒体
    @FXML void onLoadMedia() {
        loadMedia();
    }

    /// 格式化Duration
    private String formatDuration(Duration duration) {
        long totalMillis = (long) Math.abs(duration.toMillis());
        // 小时
        long hours = totalMillis / 3_600_000;
        long remaining = totalMillis % 3_600_000;
        // 分钟
        long minutes = remaining / 60_000;
        remaining %= 60_000;
        // 秒钟
        long seconds = remaining / 1_000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    /// 加载媒体
    public void loadMedia(String path) {
        tfd_mediaPath.setText(path);
        loadMedia();
    }
    /// 加载媒体（通过传入路径）
    public void loadMedia() {
        try {
            mediaManager.loadMedia(tfd_mediaPath.getText()); // 加载媒体
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("发生错误");
            alert.setHeaderText("无法加载媒体文件！");
            alert.setContentText(e.getMessage());
            alert.show();
        }
    }
    /// 更新媒体信息ui
    private void updateMediaInfoUI() {
        updateMediaInfoUI(mediaManager.getMediaPlayer().getCurrentTime());
    }
    private void updateMediaInfoUI(Duration newDuration) {
        // 更新进度条位置
        if (!isDraggedProgress)
            sdr_mediaProgress.valueProperty().set(newDuration.toMillis() / mediaDuration.toMillis());
        // 设置媒体信息
        lbl_mediaPlayInfo.setText("%s / %s".formatted(
                formatDuration(newDuration), formatDuration(mediaDuration)));
    }
    public void playMedia() {
        cbx_playVideo.setSelected(true);
        updateMediaPlay();
    }
    /// 更新媒体播放
    public void updateMediaPlay() {
        // 重置播放（播放完毕且选中播放）
        if (cbx_playVideo.isSelected() && mediaManager.isFinished()) reset();
        // 播放暂停
        mediaManager.playPause(cbx_playVideo.isSelected());
    }
    /// 重置播放
    public void reset() {
        cbx_playVideo.setSelected(false); // 取消播放多选框
        mediaManager.reset(); // 重置媒体管理器
        updateMediaInfoUI(Duration.ZERO); // 重置进度
    }
    /// 释放资源
    public void dispose() {
        mediaManager.dispose();
    }
}
