package com.acer.afeplayer.desktop.filePage;

import com.sun.source.tree.Tree;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/// 文件树节点
public class FileTreeItem extends TreeItem<File> {
    private boolean isLoaded; // 已经加载

    public FileTreeItem(File file) {
        super(file);
        // 为目录则允许加载子目录
        if (file.isDirectory()) {
            getChildren().add(new TreeItem<>(new File("正在加载..."))); // 添加临时节点
            // 展开监听器
            addEventHandler(TreeItem.branchExpandedEvent(), e -> {
                // 开始加载
                if (!isLoaded) {
                    load();
                }
            });
        }
    }

    /// 加载子节点
    private void load() {
        File dir = getValue(); // 获取当前目录
        // 加载线程
        Task<List<FileTreeItem>> task = new Task<>() {
            @Override
            protected List<FileTreeItem> call() {
                File[] files = dir.listFiles(); // 获取文件列表
                if (files != null) {
                    return Arrays.stream(files)
                            .filter(File::isDirectory)
                            .sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
                            .map(FileTreeItem::new)
                            .collect(Collectors.toList());
                } else {
                    return List.of(new FileTreeItem(new File("（空）")));
                }
            }
        };
        // 加载完毕
        task.setOnSucceeded(e -> {
            getChildren().clear();
            getChildren().setAll(task.getValue());
            isLoaded = true;
        });
        // 加载失败
        task.setOnFailed(e -> {
            getChildren().clear();
            getChildren().add(new TreeItem<>(new File("加载失败")));
        });
        // 开启线程异步加载
        new Thread(task).start();
    }
}
