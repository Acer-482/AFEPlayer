@echo off
:: ��Ŀ����
set demoName=AFEPlayer
:: ����·��
set appPath=.\desktop\target\app
:: ģ������
set mainPackage=com.acer.afeplayer.desktop
:: �������
set mainClass=com.acer.afeplayer.desktop.MainApplication

echo ======JavaFXPacker======
echo ����ǰ��ִ��jlink
echo �밴����޸Ľű�ǰ���еı���
echo **BY Acer**
pause

:: ���·���Ƿ���Ч
if not exist %appPath% (
    echo jlinkδִ�� �Ҳ�������·����
    goto end
)

:: �г���ϸ��Ϣ
echo ======list-modules======
echo �˴��г���������Чģ�飺
%appPath%\bin\java --list-modules
pause

:: ������һ�����ɵ��ļ�
if exist %demoName% (
echo ======Cleaning======
del /F /S /Q %demoName%
rd /S /Q %demoName%
echo �������...
)

echo ======JPackage======
jpackage --name %demoName% --type app-image -m %mainPackage%/%mainClass% --runtime-image %appPath%
echo �����ɣ�

:end
echo ======ALL DONE======
pause