@echo off
:: 项目名称
set demoName=AFEPlayer
:: 镜像路径
set appPath=.\desktop\target\app
:: 模块名称
set mainPackage=com.acer.afeplayer.desktop
:: 主类入口
set mainClass=com.acer.afeplayer.desktop.MainApplication

echo ======JavaFXPacker======
echo 运行前请执行jlink
echo 请按情况修改脚本前几行的变量
echo **BY Acer**
pause

:: 检查路径是否有效
if not exist %appPath% (
    echo jlink未执行 找不到镜像路径！
    goto end
)

:: 列出详细信息
echo ======list-modules======
echo 此处列出了所有有效模块：
%appPath%\bin\java --list-modules
pause

:: 清理上一次生成的文件
if exist %demoName% (
echo ======Cleaning======
del /F /S /Q %demoName%
rd /S /Q %demoName%
echo 清理完成...
)

echo ======JPackage======
jpackage --name %demoName% --type app-image -m %mainPackage%/%mainClass% --runtime-image %appPath%
echo 打包完成！

:end
echo ======ALL DONE======
pause