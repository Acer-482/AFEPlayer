package com.acer.afeplayer.desktop.filePage;

import javafx.beans.property.*;

/// 文件表数据模型
@SuppressWarnings("unused")
public class FileTableModel {
    private final StringProperty fileName; // 文件名
    private final LongProperty fileSize; // 文件大小
    private long encryptedFileSize; // 加密的文件大小
    private final DoubleProperty loadProgress; // 加载进度

    public FileTableModel() {
        fileName = new SimpleStringProperty();
        fileSize = new SimpleLongProperty();
        loadProgress = new SimpleDoubleProperty();
    }
    public FileTableModel(String fileName, long encryptedFileSize, long fileSize) {
        this();
        setFileName(fileName);
        this.encryptedFileSize = encryptedFileSize;
        setFileSize(fileSize);

        setLoadProgress(encryptedFileSize, fileSize);
    }

    /* getter & setter */
    public String getFileName() {
        return fileName.get();
    }
    public StringProperty fileNameProperty() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }
    public long getFileSize() {
        return fileSize.get();
    }
    public LongProperty fileSizeProperty() {
        return fileSize;
    }
    public void setFileSize(long fileSize) {
        this.fileSize.set(fileSize);
    }
    public long getEncryptedFileSize() {
        return encryptedFileSize;
    }
    public void setEncryptedFileSize(long encryptedFileSize) {
        this.encryptedFileSize = encryptedFileSize;
    }
    public void addEncryptedFileSize(long as) {
        this.encryptedFileSize = Math.max(this.encryptedFileSize + as, 0);
        setLoadProgress(encryptedFileSize, fileSize.get());
    }
    public double getLoadProgress() {
        return loadProgress.get();
    }
    public DoubleProperty loadProgressProperty() {
        return loadProgress;
    }
    public void setLoadProgress(double loadProgress) {
        this.loadProgress.set(loadProgress);
    }
    public void setLoadProgress(long encryptedFileSize, long fileSize) {
        setLoadProgress((double) encryptedFileSize / fileSize);
    }
}
