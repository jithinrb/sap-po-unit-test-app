@ECHO OFF
cls
java -cp "<Install Dir>\com.pi.ut.automation.app.jar";"<Install Dir>\xmlunit-core-2.6.2.jar " com.pi.ut.automation.controller.POUnitTestLauncher %1
