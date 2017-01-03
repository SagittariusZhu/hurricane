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
rem Start/Stop Script for the HURRICANE Server
rem
rem Environment Variable Prerequisites
rem
rem   HURRICANE_HOME   May point at your Hurricane "build" directory.
rem
rem   HURRICANE_BASE   (Optional) Base directory for resolving dynamic portions
rem                   of a Hurricane installation.  If not present, resolves to
rem                   the same directory that HURRICANE_HOME points to.
rem
rem   HURRICANE_OPTS   (Optional) Java runtime options used when the "start",
rem                   or "run" command is executed.
rem
rem   HURRICANE_TMPDIR (Optional) Directory path location of temporary directory
rem                   the JVM should use (java.io.tmpdir).  Defaults to
rem                   %HURRICANE_BASE%\temp.
rem
rem   JAVA_HOME       Must point at your Java Development Kit installation.
rem                   Required to run the with the "debug" argument.
rem
rem   JRE_HOME        Must point at your Java Runtime installation.
rem                   Defaults to JAVA_HOME if empty.
rem
rem   JAVA_OPTS       (Optional) Java runtime options used when the "start",
rem                   "stop", or "run" command is executed.
rem
rem   JPDA_TRANSPORT  (Optional) JPDA transport used when the "jpda start"
rem                   command is executed. The default is "dt_shmem".
rem
rem   JPDA_ADDRESS    (Optional) Java runtime options used when the "jpda start"
rem                   command is executed. The default is "jdbconn".
rem
rem   JPDA_SUSPEND    (Optional) Java runtime options used when the "jpda start"
rem                   command is executed. Specifies whether JVM should suspend
rem                   execution immediately after startup. Default is "n".
rem
rem   JPDA_OPTS       (Optional) Java runtime options used when the "jpda start"
rem                   command is executed. If used, JPDA_TRANSPORT, JPDA_ADDRESS,
rem                   and JPDA_SUSPEND are ignored. Thus, all required jpda
rem                   options MUST be specified. The default is:
rem
rem                   -Xdebug -Xrunjdwp:transport=%JPDA_TRANSPORT%,
rem                       address=%JPDA_ADDRESS%,server=y,suspend=%JPDA_SUSPEND%
rem
rem $Id: hurricane.bat 1040548 2010-11-30 14:48:26Z markt $
rem ---------------------------------------------------------------------------

rem Guess HURRICANE_HOME if not defined
set CURRENT_DIR=%cd%
if not "%HURRICANE_HOME%" == "" goto gotHome
set HURRICANE_HOME=%CURRENT_DIR%
if exist "%HURRICANE_HOME%\bin\hurricane.bat" goto okHome
cd ..
set HURRICANE_HOME=%cd%
cd %CURRENT_DIR%
:gotHome
if exist "%HURRICANE_HOME%\bin\hurricane.bat" goto okHome
echo The HURRICANE_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome

rem Ensure that any user defined CLASSPATH variables are not used on startup,
rem but allow them to be specified in setenv.bat, in rare case when it is needed.
set CLASSPATH=

rem Get standard environment variables
if "%HURRICANE_BASE%" == "" goto gotSetenvHome
if exist "%HURRICANE_BASE%\bin\setenv.bat" call "%HURRICANE_BASE%\bin\setenv.bat"
goto gotSetenvBase
:gotSetenvHome
if exist "%HURRICANE_HOME%\bin\setenv.bat" call "%HURRICANE_HOME%\bin\setenv.bat"
:gotSetenvBase

rem Get standard Java environment variables
if exist "%HURRICANE_HOME%\bin\setclasspath.bat" goto okSetclasspath
echo Cannot find %HURRICANE_HOME%\bin\setclasspath.bat
echo This file is needed to run this program
goto end
:okSetclasspath
set BASEDIR=%HURRICANE_HOME%
call "%HURRICANE_HOME%\bin\setclasspath.bat" %1
if errorlevel 1 goto end

rem Add on extra jar file to CLASSPATH
rem Note that there are no quotes as we do not want to introduce random
rem quotes into the CLASSPATH
if "%CLASSPATH%" == "" goto emptyClasspath
set CLASSPATH=%CLASSPATH%;
:emptyClasspath
set CLASSPATH=%CLASSPATH%%HURRICANE_HOME%\lib\hu-server-0.0.1-SNAPSHOT.jar

if not "%HURRICANE_BASE%" == "" goto gotBase
set HURRICANE_BASE=%HURRICANE_HOME%
:gotBase

if not "%HURRICANE_TMPDIR%" == "" goto gotTmpdir
set HURRICANE_TMPDIR=%HURRICANE_BASE%\temp
:gotTmpdir

if not exist "%HURRICANE_HOME%\bin\hurricane-juli.jar" goto noJuli
set JAVA_OPTS=%JAVA_OPTS% -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager -Djava.util.logging.config.file="%HURRICANE_BASE%\conf\logging.properties"
:noJuli

rem ----- Execute The Requested Command ---------------------------------------

echo Using HURRICANE_BASE:   %HURRICANE_BASE%
echo Using HURRICANE_HOME:   %HURRICANE_HOME%
echo Using HURRICANE_TMPDIR: %HURRICANE_TMPDIR%
if ""%1"" == ""debug"" goto use_jdk
echo Using JRE_HOME:        %JRE_HOME%
goto java_dir_displayed
:use_jdk
echo Using JAVA_HOME:       %JAVA_HOME%
:java_dir_displayed
rem echo Using CLASSPATH:       %CLASSPATH%

set _EXECJAVA=%_RUNJAVA%
set MAINCLASS=org.iipg.hurricane.server.Server
set ACTION=start
set SECURITY_POLICY_FILE=
set DEBUG_OPTS=
set JPDA=

if not ""%1"" == ""jpda"" goto noJpda
set JPDA=jpda
if not "%JPDA_TRANSPORT%" == "" goto gotJpdaTransport
set JPDA_TRANSPORT=dt_shmem
:gotJpdaTransport
if not "%JPDA_ADDRESS%" == "" goto gotJpdaAddress
set JPDA_ADDRESS=jdbconn
:gotJpdaAddress
if not "%JPDA_SUSPEND%" == "" goto gotJpdaSuspend
set JPDA_SUSPEND=n
:gotJpdaSuspend
if not "%JPDA_OPTS%" == "" goto gotJpdaOpts
set JPDA_OPTS=-Xdebug -Xrunjdwp:transport=%JPDA_TRANSPORT%,address=%JPDA_ADDRESS%,server=y,suspend=%JPDA_SUSPEND%
:gotJpdaOpts
shift
:noJpda

if ""%1"" == ""debug"" goto doDebug
if ""%1"" == ""run"" goto doRun
if ""%1"" == ""start"" goto doStart
if ""%1"" == ""stop"" goto doStop
if ""%1"" == ""version"" goto doVersion

echo Usage:  hurricane ( commands ... )
echo commands:
echo   debug             Start Hurricane in a debugger
echo   debug -security   Debug Hurricane with a security manager
echo   jpda start        Start Hurricane under JPDA debugger
echo   run               Start Hurricane in the current window
echo   run -security     Start in the current window with security manager
echo   start             Start Hurricane in a separate window
echo   start -security   Start in a separate window with security manager
echo   stop              Stop Hurricane
echo   version           What version of hurricane are you running?
goto end

:doDebug
shift
set _EXECJAVA=%_RUNJDB%
set DEBUG_OPTS=-sourcepath "%HURRICANE_HOME%\..\..\iipg-hurricane\hurricane\src\share"
if not ""%1"" == ""-security"" goto execCmd
shift
echo Using Security Manager
set SECURITY_POLICY_FILE=%HURRICANE_BASE%\conf\hurricane.policy
goto execCmd

:doRun
shift
if not ""%1"" == ""-security"" goto execCmd
shift
echo Using Security Manager
set SECURITY_POLICY_FILE=%HURRICANE_BASE%\conf\hurricane.policy
goto execCmd

:doStart
shift
if not "%OS%" == "Windows_NT" goto noTitle
set _EXECJAVA=start "Hurricane" %_RUNJAVA%
goto gotTitle
:noTitle
set _EXECJAVA=start %_RUNJAVA%
:gotTitle
if not ""%1"" == ""-security"" goto execCmd
shift
echo Using Security Manager
set SECURITY_POLICY_FILE=%HURRICANE_BASE%\conf\hurricane.policy
goto execCmd

:doStop
shift
set ACTION=stop
set HURRICANE_OPTS=
goto execCmd

:doVersion
%_EXECJAVA% -classpath "%HURRICANE_HOME%\lib\hurricane\hu-server-0.0.1-SNAPSHOT.jar" org.iipg.hurricane.util.ServerInfo
goto end


:execCmd
rem Get remaining unshifted command line arguments and save them in the
set CMD_LINE_ARGS=
:setArgs
if ""%1""=="""" goto doneSetArgs
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setArgs
:doneSetArgs

rem Execute Java with the applicable properties
if not "%JPDA%" == "" goto doJpda
if not "%SECURITY_POLICY_FILE%" == "" goto doSecurity
%_EXECJAVA% %JAVA_OPTS% %HURRICANE_OPTS% %DEBUG_OPTS% -Djava.endorsed.dirs="%JAVA_ENDORSED_DIRS%" -classpath "%CLASSPATH%" -Dhurricane.base="%HURRICANE_BASE%" -Dhurricane.home="%HURRICANE_HOME%" -Djava.io.tmpdir="%HURRICANE_TMPDIR%" %MAINCLASS% %CMD_LINE_ARGS% %ACTION%
goto end
:doSecurity
%_EXECJAVA% %JAVA_OPTS% %HURRICANE_OPTS% %DEBUG_OPTS% -Djava.endorsed.dirs="%JAVA_ENDORSED_DIRS%" -classpath "%CLASSPATH%" -Djava.security.manager -Djava.security.policy=="%SECURITY_POLICY_FILE%" -Dhurricane.base="%HURRICANE_BASE%" -Dhurricane.home="%HURRICANE_HOME%" -Djava.io.tmpdir="%HURRICANE_TMPDIR%" %MAINCLASS% %CMD_LINE_ARGS% %ACTION%
goto end
:doJpda
if not "%SECURITY_POLICY_FILE%" == "" goto doSecurityJpda
%_EXECJAVA% %JAVA_OPTS% %HURRICANE_OPTS% %JPDA_OPTS% %DEBUG_OPTS% -Djava.endorsed.dirs="%JAVA_ENDORSED_DIRS%" -classpath "%CLASSPATH%" -Dhurricane.base="%HURRICANE_BASE%" -Dhurricane.home="%HURRICANE_HOME%" -Djava.io.tmpdir="%HURRICANE_TMPDIR%" %MAINCLASS% %CMD_LINE_ARGS% %ACTION%
goto end
:doSecurityJpda
%_EXECJAVA% %JAVA_OPTS% %HURRICANE_OPTS% %JPDA_OPTS% %DEBUG_OPTS% -Djava.endorsed.dirs="%JAVA_ENDORSED_DIRS%" -classpath "%CLASSPATH%" -Djava.security.manager -Djava.security.policy=="%SECURITY_POLICY_FILE%" -Dhurricane.base="%HURRICANE_BASE%" -Dhurricane.home="%HURRICANE_HOME%" -Djava.io.tmpdir="%HURRICANE_TMPDIR%" %MAINCLASS% %CMD_LINE_ARGS% %ACTION%
goto end

:end
