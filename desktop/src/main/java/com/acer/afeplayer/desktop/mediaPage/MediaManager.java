package com.acer.afeplayer.desktop.mediaPage;

import javafx.beans.value.ChangeListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.File;

/**
 * 媒体管理器
 */
@SuppressWarnings("unused")
public class MediaManager {
    private final MediaView mediaView; // 媒体显示组件
    private Media media; // 当前媒体
    private MediaPlayer player; // 媒体播放控制器

    private boolean isLoading; // 正在加载
    private boolean loadFailedRetry = true; // 加载重试
    private boolean playFinished = true; // 播放完毕
    private Runnable onReady; // 加载完成线程
    private ChangeListener<? super Duration> currentTimeListener; // 播放线程
    private Runnable onEnd; // 结束执行线程

    public MediaManager(MediaView mediaView) {
        this.mediaView = mediaView;
    }

    /**
     * 加载媒体
     * @param path 媒体路径
     */
    public void loadMedia(String path) {
        if (isLoading) throw new RuntimeException("该实例正在加载媒体！");
        loadMedia(path, 0);
    }
    private void loadMedia(String path, int failedCount) {
        /* 检测媒体是否有效 */
        File file = new File(path); // 媒体文件
        if (!file.exists()) throw new RuntimeException("媒体不存在：" + path);
        /* 释放资源 */
        if (player != null) player.dispose();
        /* 初始化播放 */
        playFinished = false;
        isLoading = true;
        media = new Media(file.toURI().toString()); // 加载媒体资源
        /* 创建播放器 */
        player = new MediaPlayer(media);
        mediaView.setMediaPlayer(player);
        // 加载完成
        player.setOnReady(() -> {
            isLoading = false;
            if (onReady != null) onReady.run();
            // 更新进度
            player.currentTimeProperty().addListener(currentTimeListener);
        });
        // 播放完毕
        player.setOnEndOfMedia(() -> {
            playFinished = true;
            if (onEnd != null) onEnd.run();
        });
        // 加载失败 //
        if (failedCount < 10) {
            player.setOnError(() -> {
                System.out.printf("Load media Failed.%n");
                playFinished = false;
            });
            if (loadFailedRetry) player.setOnError(() -> {
                // 重试
                System.out.printf("Load mediaPlayer Failed, retry... (%s)%n", failedCount + 1);
                loadMedia(path, failedCount + 1);
            });
        } else {
            isLoading = false; // 十次加载失败 放弃加载
            throw new RuntimeException("Load mediaPlayer Failed.");
        }
    }
    /// 播放暂停
    public void playPause(boolean setPlay) {
        if (setPlay) play();
        else pause();
    }
    /// 自动播放暂停
    public void playPause() {
        playPause(!isPlaying());
    }
    /// 播放媒体
    public void play() {
        player.play();
    }
    /// 重置状态
    public void reset() {
        seek(Duration.ZERO);
        pause();
    }
    /// 设置播放进度
    public void seek(Duration duration) {
        playFinished = false;
        player.seek(duration);
    }
    /// 暂停媒体
    public void pause() {
        player.pause();
    }
    /// 释放资源
    public void dispose() {
        if (player != null) player.dispose();
    }

    /* getter & setter */
    public void setOnReady(Runnable onReady) {
        this.onReady = onReady;
    }
    public void setCurrentTimeListener(ChangeListener<? super Duration> currentTimeListener) {
        this.currentTimeListener = currentTimeListener;
    }
    public void setOnEnd(Runnable onEnd) {
        this.onEnd = onEnd;
    }
    public void setLoadFailedRetry(boolean loadFailedRetry) {
        this.loadFailedRetry = loadFailedRetry;
    }
    public boolean isPlaying() {
        return player.getStatus().equals(MediaPlayer.Status.PLAYING);
    }
    public boolean isReady() {
        return player.getStatus().equals(MediaPlayer.Status.READY);
    }
    public boolean isFinished() {
        return playFinished;
    }
    public boolean isLoading() {
        return isLoading;
    }
    public boolean isLoadFailedRetry() {
        return loadFailedRetry;
    }
    public Media getMedia() {
        return media;
    }
    public MediaPlayer getMediaPlayer() {
        return player;
    }
    public Runnable getOnReady() {
        return onReady;
    }
    public ChangeListener<? super Duration> getCurrentTimeListener() {
        return currentTimeListener;
    }
    public Runnable getOnEnd() {
        return onEnd;
    }

}
