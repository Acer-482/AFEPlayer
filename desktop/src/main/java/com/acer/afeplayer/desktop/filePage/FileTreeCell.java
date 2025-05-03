package com.acer.afeplayer.desktop.filePage;

import javafx.scene.control.TreeCell;

import java.io.File;

/// 自定义文件表 显示图标和名称
public class FileTreeCell extends TreeCell<File> {
    @Override
    protected void updateItem(File file, boolean empty) {
        super.updateItem(file, empty);
        if (empty || file == null) {
            setText(null);
        } else {
            setText(file.getName());
        }
    }
}
