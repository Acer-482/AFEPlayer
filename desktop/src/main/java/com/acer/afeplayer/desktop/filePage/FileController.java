package com.acer.afeplayer.desktop.filePage;

import com.acer.afeplayer.core.CryptoUtils;
import com.acer.afeplayer.desktop.MainApplication;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FileController {
    /* 按钮 */
    @FXML private Button btn_update;
    @FXML private Button btn_workDir_selectDir; // 工作目录选择

    /* 选项 */
    @FXML private TextField workDir; // 工作目录

    /* 主要控件 */
    @FXML private TreeView<File> fileTree; // 文件树
    private TreeItem<File> currFileTreeItem; // 当前选择文件树的节点

    @FXML private TableView<FileTableModel> fileTable; // 文件表
    private ObservableList<FileTableModel> fileTableData; // 文件表数据
    @FXML private TableColumn<FileTableModel, String> fileTable_fileName; // 表头 文件名
    @FXML private TableColumn<FileTableModel, Long> fileTable_fileSize; // 表头 文件大小
    @FXML private TableColumn<FileTableModel, Double> fileTable_loadProgress; // 表头 加载进度

    @FXML private RadioButton rbtn_customKey; // 自定义密钥单选框
    @FXML private TextArea ta_customKey; // 自定义密钥文本区
    @FXML private RadioButton rbtn_randomKey; // 随机密钥单选框
    @FXML private HBox box_randomKey; // 随机密钥的box
    @FXML private TextField tf_randomKey; // 随机密钥展示框
    @FXML private Button btn_updateRandomKey; // 更新随机密钥
    @FXML private CheckBox cbx_useSHA3; // 使用哈希3加密
    @FXML private Button btn_startEncrypt; // 开始加密按钮
    @FXML private Button btn_startDecrypt; // 开始解密按钮

    // 初始化 //
    @FXML public void initialize() {
        initFileTree(); // 初始化文件树
        initFileTable(); // 初始化文件表

        // 选择工作目录 //
        btn_workDir_selectDir.setOnMouseClicked(mouseEvent -> {
            // 初始化目录选择器 //
            DirectoryChooser fileChooser = new DirectoryChooser();
            fileChooser.setInitialDirectory(new File(getWorkDir()));
            fileChooser.setTitle("选择工作目录");
            // 选择目录 //
            File selectionDir = fileChooser.showDialog(btn_workDir_selectDir.getScene().getWindow());
            if (selectionDir != null) {
                workDir.setText(selectionDir.getPath()); // 设置工作目录
                updateFileTree(); // 更新
            }
        });
        // 刷新按钮 //
        btn_update.setOnMouseClicked(mouseEvent -> {
            updateFileTree(); // 更新
        });
        // 自定义密钥单选框 //
        rbtn_customKey.selectedProperty().addListener((
                observableValue, aBoolean, t1) -> {
            ta_customKey.setDisable(!t1);
            box_randomKey.setDisable(t1);
        });
        // 更新密钥 //
        updateRandomKey(); // 初始更新一次
        btn_updateRandomKey.setOnMouseClicked(mouseEvent -> updateRandomKey());
        // 开始加密解密按钮 //
        btn_startEncrypt.setOnMouseClicked(mouseEvent -> {
            cryptoFile(true);
        });
        btn_startDecrypt.setOnMouseClicked(mouseEvent -> {
            cryptoFile(false);
        });

        // 后添加深度检索功能 开启就可以检索目录下子目录孙目录的文件 添加到列表
        // 配置文件功能（Gson）
    }

    // 简单显示提示文本的工具类
    public static void showAlert(Alert.AlertType type, String headerText) {
        showAlert(type, headerText, "");
    }
    public static void showAlert(Alert.AlertType type, String headerText, String contentText) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setHeaderText(headerText);
            alert.setContentText(contentText);
            alert.show();
        });
    }
    // 获取工作路径
    private String getWorkDir() {
        String dir = workDir.getText();
        File dirFile = new File(dir);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            showAlert(Alert.AlertType.WARNING, "工作目录\"%s\"无效！".formatted(dir), "将使用缺省路径\".\""); // 弹出警告
            workDir.setText("."); // 使用默认路径
            return ".";
        } else return workDir.getText();
    }
    // 获取当前路径
    public String getCurrDir() {
        if (currFileTreeItem == null) return null;
        return currFileTreeItem.getValue().getPath();
    }
    // 格式化文件大小字符串
    private String getFormatFileSize(long lSize) {
        double size = lSize; // 转换为double类型
        String[] UNITS = {"B", "KB", "MB", "GB", "TB"};
        if (size <= 0) return "0 B";
        // 动态选择单位
        int unitIndex = 0; // 单位索引
        while (size >= 1024.0 && unitIndex < UNITS.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        // 根据单位调整小数位数
        return String.format((unitIndex == 0) ? "%.0f %s" : "%.2f %s", size, UNITS[unitIndex]);
    }

    /// 初始化树
    private void initFileTree() {
        /* 添加选择节点监听器 */
        fileTree.getSelectionModel().selectedItemProperty().addListener((
                observableValue, ot, nt) -> {
            currFileTreeItem = nt;
            updateFileTable();
        });
        fileTree.setCellFactory(treeView -> new FileTreeCell()); // 自定义显示
        // 初始化目的的更新文件树
        updateFileTree();
    }
    /// 更新树
    private void updateFileTree() {
        // 创建新Root节点并设置
        currFileTreeItem = new FileTreeItem(new File(getWorkDir()));
        fileTree.setRoot(currFileTreeItem);
        // 展开根节点
        Platform.runLater(() -> fileTree.getRoot().setExpanded(true));
    }

    /// 初始化表
    private void initFileTable() {
        fileTableData = FXCollections.observableArrayList(); // 初始化文件表数据
        fileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); // 允许多选
        // 初始化表头数据
        fileTable_fileName.setCellValueFactory(data ->
                data.getValue().fileNameProperty());
        fileTable_fileSize.setCellValueFactory(data ->
                data.getValue().fileSizeProperty().asObject());
        fileTable_loadProgress.setCellValueFactory(data ->
                data.getValue().loadProgressProperty().asObject());
        // 自定义文件大小显示
        fileTable_fileSize.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Long fileSizeBytes, boolean empty) {
                super.updateItem(fileSizeBytes, empty);
                if (empty || fileSizeBytes == null) {
                    setText(null);
                } else {
                    setText(getFormatFileSize(fileSizeBytes));
                }
            }
        });
        // 自定义加载进度显示
        fileTable_loadProgress.setCellFactory(column -> new TableCell<>() {
            private final ProgressBar progressBar = new ProgressBar();
            @Override
            protected void updateItem(Double progress, boolean empty) {
                super.updateItem(progress, empty);
                if (empty || progress == null) {
                    setGraphic(null);
                } else {
                    progressBar.setProgress(progress);
                    progressBar.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    setGraphic(progressBar);
                }
            }
        });
        fileTable.setRowFactory(tv -> {
            TableRow<FileTableModel> tableRow = new TableRow<>() {
                @Override
                protected void updateItem(FileTableModel fileTableModel, boolean b) {
                    super.updateItem(fileTableModel, b);
                    if (fileTableModel == null || b) {
                        setStyle("");
                        return;
                    }
                    // 获取文件名
                    String fileName = fileTableModel.getFileName();
                    if (fileName.endsWith(".afe")) {
                        // 后缀为.afe则高亮
                        setStyle("-fx-background-color: #ffdddd;");
                    } else {
                        setStyle("");
                    }
                }
            };
            // 检测是否双击
            tableRow.setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() == 2) {
                    FileTableModel tableModel = tableRow.getItem();
                    // 尝试预览
                    MainApplication.showMediaController(getCurrDir() + File.separator + tableModel.getFileName());
                }
            });
            return tableRow;
        });
        // 更新文件表
        updateFileTable();
    }
    /// 更新表数据
    public void updateFileTable() {
        fileTableData.clear(); // 清空所有目录
        String dir = getCurrDir(); // 获取当前选中目录
        if (dir != null) {
            // 获取目录下所有文件
            File root = new File(dir);
            File[] files = root.listFiles();
            if (files == null) return;
            // 遍历添加到表格
            for (File file : files) {
                if (!file.exists() || file.isDirectory()) continue;
                addFileToTable(file);
            }
        }
    }
    /// 添加表中数据
    public void addFileToTable(File file) {
        if (!file.exists() || file.isDirectory()) return; // 文件不存在 或者 为目录 直接退出
        // 解析文件信息添加到表
        fileTableData.add(new FileTableModel(
                file.getName(),
                file.getName().endsWith(".afe") ? file.length() : 0,
                file.length())); // 添加到文件表数据
        fileTable.setItems(fileTableData); // 更新表格控件
    }

    // 密码操作文件
    private void cryptoFile(boolean encryptMode) {
        // 获取输入密钥
        String keyStr = rbtn_customKey.isSelected() ? ta_customKey.getText() : tf_randomKey.getText();
        if (rbtn_randomKey.isSelected() && !encryptMode) {
            showAlert(Alert.AlertType.WARNING, "错误操作", "无法在解密模式下使用随机密钥模式");
            return;
        }
        // 获取密钥
        byte[] key = CryptoUtils.formatSHA(keyStr.getBytes(StandardCharsets.UTF_8),
                cbx_useSHA3.isSelected() ? CryptoUtils.SHAAlgorithm.SHA3_256 : CryptoUtils.SHAAlgorithm.SHA_256);
        // 开启新线程 派发任务 //
        ObservableList<FileTableModel> observableList = fileTable.getSelectionModel().getSelectedItems(); // 获取选择的文件列表
        int fileCount = observableList.size(); // 计算文件数量
        new Thread(() -> {
            long startTime = System.currentTimeMillis();
            AtomicInteger finishCount = new AtomicInteger(); // 完成计数
            CountDownLatch countDownLatch = new CountDownLatch(fileCount); // 计数
            ExecutorService executor = Executors.newFixedThreadPool(8); // 线程池
            // 获取选择文件
            for (FileTableModel fileTableModel : observableList) {
                // 获取文件类
                File file = new File((getCurrDir() + File.separator + fileTableModel.getFileName()));
                // 文件不存在或为目录
                if (!file.exists()) {
                    countDownLatch.countDown(); // 完毕计数
                    return;
                }
                // 获取文件信息
                String filePath = file.getPath();
                String inputStreamPath = encryptMode ? file.getPath() : filePath;
                String outputStreamPath = encryptMode ? filePath + ".afe" :
                        filePath.substring(0, filePath.length() - 4);
                // 开始派发操作文件任务 //
                executor.execute(() -> {
                    try (FileInputStream fis = new FileInputStream(inputStreamPath);
                         FileOutputStream fos = new FileOutputStream(outputStreamPath)) {
                        // 操作文件
                        CryptoUtils.cryptoAESStream(fis, fos, key, CryptoUtils.CryptoMode.AES_CTR,
                                1024 * 1024, encryptMode, as ->
                                        Platform.runLater(() -> fileTableModel.addEncryptedFileSize(encryptMode ? as : -as)));
                        finishCount.getAndIncrement(); // 成功计数
                    } catch (Exception e) {
                        showAlert(Alert.AlertType.WARNING, "操作失败", e.getMessage());
                    } finally {
                        countDownLatch.countDown(); // 完毕计数
                    }
                });
            }
            // 完成 提示用户
            try {
                countDownLatch.await(); // 等待任务完成
                showAlert(Alert.AlertType.INFORMATION,
                        "%s操作完成".formatted(encryptMode ? "加密" : "解密"),
                        "成功%s了%s/%s个文件，耗时%s秒".formatted(encryptMode ? "加密" : "解密", finishCount, fileCount,
                                (System.currentTimeMillis() - startTime) / 1000));
                updateFileTable(); // 更新文件表
            } catch (InterruptedException e) {
                showAlert(Alert.AlertType.WARNING, "操作中断", e.getMessage());
            } finally {
                executor.close();
            }
        }).start();
    }
    // 更新随机密码
    private void updateRandomKey() {
        String key = CryptoUtils.buildRandomString(24);
        tf_randomKey.setText(key);
    }
}
