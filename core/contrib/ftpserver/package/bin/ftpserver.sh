#!/bin/sh

# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# -----------------------------------------------------------------------------
# Start/Stop Script for the FTP Server
#

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false
os400=false
darwin=false
case "`uname`" in
CYGWIN*) cygwin=true;;
OS400*) os400=true;;
Darwin*) darwin=true;;
esac

# resolve links - $0 may be a softlink
PRG="$0"

while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done

# Get standard environment variables
PRGDIR=`dirname "$PRG"`

# Only set FTPSERVER_HOME if not already set
[ -z "$FTPSERVER_HOME" ] && FTPSERVER_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

# Ensure that any user defined CLASSPATH variables are not used on startup,
# but allow them to be specified in setenv.sh, in rare case when it is needed.
CLASSPATH=

if [ -r "$FTPSERVER_BASE"/bin/setenv.sh ]; then
  . "$FTPSERVER_BASE"/bin/setenv.sh
elif [ -r "$FTPSERVER_HOME"/bin/setenv.sh ]; then
  . "$FTPSERVER_HOME"/bin/setenv.sh
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$JRE_HOME" ] && JRE_HOME=`cygpath --unix "$JRE_HOME"`
  [ -n "$FTPSERVER_HOME" ] && FTPSERVER_HOME=`cygpath --unix "$FTPSERVER_HOME"`
  [ -n "$FTPSERVER_BASE" ] && FTPSERVER_BASE=`cygpath --unix "$FTPSERVER_BASE"`
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
fi

# For OS400
if $os400; then
  # Set job priority to standard for interactive (interactive - 6) by using
  # the interactive priority - 6, the helper threads that respond to requests
  # will be running at the same priority as interactive jobs.
  COMMAND='chgjob job('$JOBNAME') runpty(6)'
  system $COMMAND

  # Enable multi threading
  export QIBM_MULTI_THREADED=Y
fi

# Get standard Java environment variables
if $os400; then
  # -r will Only work on the os400 if the files are:
  # 1. owned by the user
  # 2. owned by the PRIMARY group of the user
  # this will not work if the user belongs in secondary groups
  BASEDIR="$FTPSERVER_HOME"
  . "$FTPSERVER_HOME"/bin/setclasspath.sh 
else
  if [ -r "$FTPSERVER_HOME"/bin/setclasspath.sh ]; then
    BASEDIR="$FTPSERVER_HOME"
    . "$FTPSERVER_HOME"/bin/setclasspath.sh
  else
    echo "Cannot find $FTPSERVER_HOME/bin/setclasspath.sh"
    echo "This file is needed to run this program"
    exit 1
  fi
fi

# Add on extra jar files to CLASSPATH
if [ ! -z "$CLASSPATH" ] ; then
  CLASSPATH="$CLASSPATH":
fi

if [ -z "$FTPSERVER_BASE" ] ; then
  FTPSERVER_BASE="$FTPSERVER_HOME"
fi

if [ -z "$FTPSERVER_OUT" ] ; then
  FTPSERVER_OUT="$FTPSERVER_BASE"/logs/ftpserver.out
fi

if [ -z "$FTPSERVER_TMPDIR" ] ; then
  # Define the java.io.tmpdir to use for Catalina
  FTPSERVER_TMPDIR="$FTPSERVER_BASE"/temp
fi

# Bugzilla 37848: When no TTY is available, don't output to console
have_tty=0
if [ "`tty`" != "not a tty" ]; then
    have_tty=1
fi

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
  JAVA_HOME=`cygpath --absolute --windows "$JAVA_HOME"`
  JRE_HOME=`cygpath --absolute --windows "$JRE_HOME"`
  FTPSERVER_HOME=`cygpath --absolute --windows "$FTPSERVER_HOME"`
  FTPSERVER_BASE=`cygpath --absolute --windows "$FTPSERVER_BASE"`
  FTPSERVER_TMPDIR=`cygpath --absolute --windows "$FTPSERVER_TMPDIR"`
  CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
  JAVA_ENDORSED_DIRS=`cygpath --path --windows "$JAVA_ENDORSED_DIRS"`
fi

# Set juli LogManager if it is present
if [ -r "$FTPSERVER_HOME"/bin/tomcat-juli.jar ]; then
  JAVA_OPTS="$JAVA_OPTS -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager"
  LOGGING_CONFIG="-Djava.util.logging.config.file=$FTPSERVER_BASE/conf/logging.properties"
else
  # Bugzilla 45585
  LOGGING_CONFIG="-Dnop"
fi

MAINCLASS="org.apache.hadoop.contrib.ftp.HdfsOverFtpServer"

# ----- Execute The Requested Command -----------------------------------------

# Bugzilla 37848: only output this if we have a TTY
if [ $have_tty -eq 1 ]; then
  echo "Using FTPSERVER_BASE:   $FTPSERVER_BASE"
  echo "Using FTPSERVER_HOME:   $FTPSERVER_HOME"
  echo "Using FTPSERVER_TMPDIR: $FTPSERVER_TMPDIR"
  if [ "$1" = "debug" ] ; then
    echo "Using JAVA_HOME:       $JAVA_HOME"
  else
    echo "Using JRE_HOME:        $JRE_HOME"
  fi
  echo "Using CLASSPATH:       $CLASSPATH"
fi

if [ "$1" = "jpda" ] ; then
  if [ -z "$JPDA_TRANSPORT" ]; then
    JPDA_TRANSPORT="dt_socket"
  fi
  if [ -z "$JPDA_ADDRESS" ]; then
    JPDA_ADDRESS="8000"
  fi
  if [ -z "$JPDA_SUSPEND" ]; then
    JPDA_SUSPEND="n"
  fi
  if [ -z "$JPDA_OPTS" ]; then
    JPDA_OPTS="-Xdebug -Xrunjdwp:transport=$JPDA_TRANSPORT,address=$JPDA_ADDRESS,server=y,suspend=$JPDA_SUSPEND"
  fi
  FTPSERVER_OPTS="$FTPSERVER_OPTS $JPDA_OPTS"
  shift
fi

if [ "$1" = "debug" ] ; then
  if $os400; then
    echo "Debug command not available on OS400"
    exit 1
  else
    shift
    if [ "$1" = "-security" ] ; then
      if [ $have_tty -eq 1 ]; then
        echo "Using Security Manager"
      fi
      shift
      exec "$_RUNJDB" "$LOGGING_CONFIG" $JAVA_OPTS  $FTPSERVER_OPTS \
        -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
        -sourcepath "$FTPSERVER_HOME"/../../jakarta-tomcat-ftpserver/ftpserver/src/share \
        -Djava.security.manager \
        -Djava.security.policy=="$FTPSERVER_BASE"/conf/ftpserver.policy \
        -Dftpserver.base="$FTPSERVER_BASE" \
        -Dftpserver.home="$FTPSERVER_HOME" \
        -Djava.io.tmpdir="$FTPSERVER_TMPDIR" \
        "$MAINCLASS" "$@" start
    else
      exec "$_RUNJDB" "$LOGGING_CONFIG" $JAVA_OPTS  $FTPSERVER_OPTS \
        -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
        -sourcepath "$FTPSERVER_HOME"/../../jakarta-tomcat-ftpserver/ftpserver/src/share \
        -Dftpserver.base="$FTPSERVER_BASE" \
        -Dftpserver.home="$FTPSERVER_HOME" \
        -Djava.io.tmpdir="$FTPSERVER_TMPDIR" \
        "$MAINCLASS" "$@" start
    fi
  fi

elif [ "$1" = "run" ]; then

  shift
  if [ "$1" = "-security" ] ; then
    if [ $have_tty -eq 1 ]; then
      echo "Using Security Manager"
    fi
    shift
    exec "$_RUNJAVA" "$LOGGING_CONFIG" $JAVA_OPTS  $FTPSERVER_OPTS \
      -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
      -Djava.security.manager \
      -Djava.security.policy=="$FTPSERVER_BASE"/conf/ftpserver.policy \
      -Dftpserver.base="$FTPSERVER_BASE" \
      -Dftpserver.home="$FTPSERVER_HOME" \
      -Djava.io.tmpdir="$FTPSERVER_TMPDIR" \
      "$MAINCLASS" "$@" start
  else
    exec "$_RUNJAVA" "$LOGGING_CONFIG" $JAVA_OPTS  $FTPSERVER_OPTS \
      -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
      -Dftpserver.base="$FTPSERVER_BASE" \
      -Dftpserver.home="$FTPSERVER_HOME" \
      -Djava.io.tmpdir="$FTPSERVER_TMPDIR" \
      "$MAINCLASS" "$@" start
  fi

elif [ "$1" = "start" ] ; then

  shift
  touch "$FTPSERVER_OUT"
  if [ "$1" = "-security" ] ; then
    if [ $have_tty -eq 1 ]; then
      echo "Using Security Manager"
    fi
    shift
    "$_RUNJAVA" "$LOGGING_CONFIG" $JAVA_OPTS  $FTPSERVER_OPTS \
      -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
      -Djava.security.manager \
      -Djava.security.policy=="$FTPSERVER_BASE"/conf/ftpserver.policy \
      -Dftpserver.base="$FTPSERVER_BASE" \
      -Dftpserver.home="$FTPSERVER_HOME" \
      -Djava.io.tmpdir="$FTPSERVER_TMPDIR" \
      "$MAINCLASS" "$@" start \
      >> "$FTPSERVER_OUT" 2>&1 &

      if [ ! -z "$FTPSERVER_PID" ]; then
        echo $! > $FTPSERVER_PID
      fi
  else
    "$_RUNJAVA" "$LOGGING_CONFIG" $JAVA_OPTS  $FTPSERVER_OPTS \
      -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
      -Dftpserver.base="$FTPSERVER_BASE" \
      -Dftpserver.home="$FTPSERVER_HOME" \
      -Djava.io.tmpdir="$FTPSERVER_TMPDIR" \
      "$MAINCLASS" "$@" start \
      >> "$FTPSERVER_OUT" 2>&1 &

      if [ ! -z "$FTPSERVER_PID" ]; then
        echo $! > $FTPSERVER_PID
      fi
  fi

elif [ "$1" = "stop" ] ; then

  shift
  FORCE=0
  if [ "$1" = "-force" ]; then
    shift
    FORCE=1
  fi

  "$_RUNJAVA" $JAVA_OPTS \
    -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
    -Dftpserver.base="$FTPSERVER_BASE" \
    -Dftpserver.home="$FTPSERVER_HOME" \
    -Djava.io.tmpdir="$FTPSERVER_TMPDIR" \
    "$MAINCLASS" "$@" stop

  if [ $FORCE -eq 1 ]; then
    if [ ! -z "$FTPSERVER_PID" ]; then
       echo "Killing: `cat $FTPSERVER_PID`"
       kill -9 `cat $FTPSERVER_PID`
    else
       echo "Kill failed: \$FTPSERVER_PID not set"
    fi
  fi

elif [ "$1" = "version" ] ; then

    "$_RUNJAVA"   \
      "%FTPSERVER_HOME%/lib/hu-server-0.0.1-SNAPSHOT.jar" \
      org.iipg.ftpserver.util.ServerInfo

else

  echo "Usage: ftpserver.sh ( commands ... )"
  echo "commands:"
  if $os400; then
    echo "  debug             Start Catalina in a debugger (not available on OS400)"
    echo "  debug -security   Debug Catalina with a security manager (not available on OS400)"
  else
    echo "  debug             Start Catalina in a debugger"
    echo "  debug -security   Debug Catalina with a security manager"
  fi
  echo "  jpda start        Start Catalina under JPDA debugger"
  echo "  run               Start Catalina in the current window"
  echo "  run -security     Start in the current window with security manager"
  echo "  start             Start Catalina in a separate window"
  echo "  start -security   Start in a separate window with security manager"
  echo "  stop              Stop Catalina"
  echo "  stop -force       Stop Catalina (followed by kill -KILL)"
  echo "  version           What version of tomcat are you running?"
  exit 1

fi
