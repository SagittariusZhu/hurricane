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
rem Start/Stop Script for the STARTER Server
rem
rem Environment Variable Prerequisites
rem
rem   STARTER_HOME   May point at your Hurricane "build" directory.
rem
rem   STARTER_BASE   (Optional) Base directory for resolving dynamic portions
rem                   of a Hurricane installation.  If not present, resolves to
rem                   the same directory that STARTER_HOME points to.
rem
rem   STARTER_OPTS   (Optional) Java runtime options used when the "start",
rem                   or "run" command is executed.
rem
rem   STARTER_CONF   (Optional) Directory path location of temporary directory
rem                   the JVM should use (java.io.tmpdir).  Defaults to
rem                   %STARTER_BASE%\temp.
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
rem $Id: starter.bat 1040548 2010-11-30 14:48:26Z markt $
rem ---------------------------------------------------------------------------

rem Guess STARTER_HOME if not defined
set CURRENT_DIR=%cd%
if not "%STARTER_HOME%" == "" goto gotHome
set STARTER_HOME=%CURRENT_DIR%
if exist "%STARTER_HOME%\sbin\run.bat" goto okHome
cd ..
set STARTER_HOME=%cd%
cd %CURRENT_DIR%
:gotHome
if exist "%STARTER_HOME%\sbin\run.bat" goto okHome
echo The STARTER_HOME environment variable is not defined correctly
echo This environment variable is needed to run this program
goto end
:okHome

rem Ensure that any user defined CLASSPATH variables are not used on startup,
rem but allow them to be specified in setenv.bat, in rare case when it is needed.
set CLASSPATH=

rem Get standard environment variables
if "%STARTER_BASE%" == "" goto gotSetenvHome
if exist "%STARTER_BASE%\sbin\setenv.bat" call "%STARTER_BASE%\sbin\setenv.bat"
goto gotSetenvBase
:gotSetenvHome
if exist "%STARTER_HOME%\sbin\setenv.bat" call "%STARTER_HOME%\sbin\setenv.bat"
:gotSetenvBase

rem Get standard Java environment variables
if exist "%STARTER_HOME%\sbin\setclasspath.bat" goto okSetclasspath
echo Cannot find %STARTER_HOME%\sbin\setclasspath.bat
echo This file is needed to run this program
goto end
:okSetclasspath
set BASEDIR=%STARTER_HOME%
call "%STARTER_HOME%\sbin\setclasspath.bat" %1
if errorlevel 1 goto end

rem Add on extra jar file to CLASSPATH
rem Note that there are no quotes as we do not want to introduce random
rem quotes into the CLASSPATH
if "%CLASSPATH%" == "" goto emptyClasspath
set CLASSPATH=%CLASSPATH%;
:emptyClasspath
set CLASSPATH=%CLASSPATH%%STARTER_HOME%\sbin\iipg-starter-0.0.1-SNAPSHOT.jar

if not "%STARTER_BASE%" == "" goto gotBase
set STARTER_BASE=%STARTER_HOME%
:gotBase

if not "%STARTER_CONF%" == "" goto gotConf
set STARTER_CONF=%STARTER_BASE%\starter.properties
:gotConf


rem ----- Execute The Requested Command ---------------------------------------

if ""%1"" == ""version"" goto display_finished

echo Using STARTER_BASE:   %STARTER_BASE%
echo Using STARTER_HOME:   %STARTER_HOME%
echo Using STARTER_CONF:   %STARTER_CONF%
if ""%1"" == ""debug"" goto use_jdk
echo Using JRE_HOME:        %JRE_HOME%
goto java_dir_displayed
:use_jdk
echo Using JAVA_HOME:       %JAVA_HOME%
:java_dir_displayed
echo Using CLASSPATH:       %CLASSPATH%
:display_finished

set _EXECJAVA=%_RUNJAVA%
set MAINCLASS=org.iipg.starter.Runjar
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

echo Usage:  Runjar ( commands ... )
echo commands:
echo   debug             Start Server in a debugger
echo   debug -security   Debug Server with a security manager
echo   jpda start        Start Server under JPDA debugger
echo   run               Start Server in the current window
echo   run -security     Start in the current window with security manager
echo   start             Start Server in a separate window
echo   start -security   Start in a separate window with security manager
echo   stop              Stop Server
echo   version           What version of wrapper server are you running?
goto end

:doDebug
shift
set _EXECJAVA=%_RUNJDB%
set DEBUG_OPTS=-sourcepath "%STARTER_HOME%\src\share"
if not ""%1"" == ""-security"" goto execCmd
shift
echo Using Security Manager
set SECURITY_POLICY_FILE=%STARTER_BASE%\conf\starter.policy
goto execCmd

:doRun
shift
if not ""%1"" == ""-security"" goto execCmd
shift
echo Using Security Manager
set SECURITY_POLICY_FILE=%STARTER_BASE%\conf\starter.policy
goto execCmd

:doStart
shift
if not "%OS%" == "Windows_NT" goto noTitle
set _EXECJAVA=start "SERVER" %_RUNJAVA%
goto gotTitle
:noTitle
set _EXECJAVA=start %_RUNJAVA%
:gotTitle
if not ""%1"" == ""-security"" goto execCmd
shift
echo Using Security Manager
set SECURITY_POLICY_FILE=%STARTER_BASE%\conf\starter.policy
goto execCmd

:doStop
shift
set ACTION=stop
set STARTER_OPTS=
goto execCmd

:doVersion
shift
set ACTION=version
set STARTER_OPTS=
goto execCmd


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
%_EXECJAVA% %JAVA_OPTS% %STARTER_OPTS% %DEBUG_OPTS% -classpath "%CLASSPATH%" -Dstarter.base="%STARTER_BASE%" -Dstarter.home="%STARTER_HOME%" -Dstarter.conf="%STARTER_CONF%" -Djava.io.tmpdir="%STARTER_TMPDIR%" %MAINCLASS% %CMD_LINE_ARGS% %ACTION%
goto end
:doSecurity
%_EXECJAVA% %JAVA_OPTS% %STARTER_OPTS% %DEBUG_OPTS% -classpath "%CLASSPATH%" -Djava.security.manager -Djava.security.policy=="%SECURITY_POLICY_FILE%" -Dstarter.base="%STARTER_BASE%" -Dstarter.home="%STARTER_HOME%" -Dstarter.conf="%STARTER_CONF%" -Djava.io.tmpdir="%STARTER_TMPDIR%" %MAINCLASS% %CMD_LINE_ARGS% %ACTION%
goto end
:doJpda
if not "%SECURITY_POLICY_FILE%" == "" goto doSecurityJpda
%_EXECJAVA% %JAVA_OPTS% %STARTER_OPTS% %JPDA_OPTS% %DEBUG_OPTS% -classpath "%CLASSPATH%" -Dstarter.base="%STARTER_BASE%" -Dstarter.home="%STARTER_HOME%" -Dstarter.conf="%STARTER_CONF%" -Djava.io.tmpdir="%STARTER_TMPDIR%" %MAINCLASS% %CMD_LINE_ARGS% %ACTION%
goto end
:doSecurityJpda
%_EXECJAVA% %JAVA_OPTS% %STARTER_OPTS% %JPDA_OPTS% %DEBUG_OPTS% -classpath "%CLASSPATH%" -Djava.security.manager -Djava.security.policy=="%SECURITY_POLICY_FILE%" -Dstarter.base="%STARTER_BASE%" -Dstarter.home="%STARTER_HOME%" -Dstarter.conf="%STARTER_CONF%" -Djava.io.tmpdir="%STARTER_TMPDIR%" %MAINCLASS% %CMD_LINE_ARGS% %ACTION%
goto end

:end
