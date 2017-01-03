@echo off
rem Licensed to the Apache Software Foundation (ASF) under one or more
rem contributor license agreements.  See the NOTICE file distributed with
rem this work for additional information regarding copyright ownership.
rem The ASF licenses this file to You under the Apache License, Version 2.0
rem (the "License"); you may not use this file except in compliance with
rem the License.  You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

if "%OS%" == "Windows_NT" setlocal
rem ---------------------------------------------------------------------------
rem Demo Script for the HURRICANE Client
rem ---------------------------------------------------------------------------

rem Guess HURRICANE_HOME if not defined
set CURRENT_DIR=%cd%
if not "%HURRICANE_HOME%" == "" goto gotHome
set HURRICANE_HOME=%CURRENT_DIR%
if exist "%HURRICANE_HOME%\bin\demo.bat" goto okHome
cd ..
set HURRICANE_HOME=%cd%
cd %CURRENT_DIR%
:gotHome
if exist "%HURRICANE_HOME%\bin\demo.bat" goto okHome
echo The HURRICANE_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome

set CLASSPATH=%HURRICANE_HOME%\conf;%HURRICANE_HOME%\lib\hu-client-demo-0.0.1-SNAPSHOT.jar

rem ----- Execute The Requested Command ---------------------------------------

set _EXECJAVA="%JAVA_HOME%\bin\java"
set MAINCLASS=org.iipg.hurricane.Client

:execCmd
rem Get remaining unshifted command line arguments and save them in the
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

%_EXECJAVA% %JAVA_OPTS% %HURRICANE_OPTS% %DEBUG_OPTS% -Djava.endorsed.dirs="%JAVA_ENDORSED_DIRS%" -classpath "%CLASSPATH%" -Dhurricane.base="%HURRICANE_BASE%" -Dhurricane.home="%HURRICANE_HOME%" -Djava.io.tmpdir="%HURRICANE_TMPDIR%" %MAINCLASS% %CMD_LINE_ARGS%
goto end

:end
