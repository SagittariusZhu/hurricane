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
# Start/Stop Script for the HURRICANE Server
#
# Environment Variable Prerequisites
#
#   HURRICANE_HOME   May point at your Catalina "build" directory.
#
#   HURRICANE_BASE   (Optional) Base directory for resolving dynamic portions
#                   of a Catalina installation.  If not present, resolves to
#                   the same directory that HURRICANE_HOME points to.
#
#   HURRICANE_OUT    (Optional) Full path to a file where stdout and stderr
#                   will be redirected. 
#                   Default is $HURRICANE_BASE/logs/hurricane.out
#
#   HURRICANE_OPTS   (Optional) Java runtime options used when the "start",
#                   or "run" command is executed.
#
#   HURRICANE_TMPDIR (Optional) Directory path location of temporary directory
#                   the JVM should use (java.io.tmpdir).  Defaults to
#                   $HURRICANE_BASE/temp.
#
#   JAVA_HOME       Must point at your Java Development Kit installation.
#                   Required to run the with the "debug" argument.
#
#   JRE_HOME        Must point at your Java Development Kit installation.
#                   Defaults to JAVA_HOME if empty.
#
#   JAVA_OPTS       (Optional) Java runtime options used when the "start",
#                   "stop", or "run" command is executed.
#
#   JPDA_TRANSPORT  (Optional) JPDA transport used when the "jpda start"
#                   command is executed. The default is "dt_socket".
#
#   JPDA_ADDRESS    (Optional) Java runtime options used when the "jpda start"
#                   command is executed. The default is 8000.
#
#   JPDA_SUSPEND    (Optional) Java runtime options used when the "jpda start"
#                   command is executed. Specifies whether JVM should suspend
#                   execution immediately after startup. Default is "n".
#
#   JPDA_OPTS       (Optional) Java runtime options used when the "jpda start"
#                   command is executed. If used, JPDA_TRANSPORT, JPDA_ADDRESS,
#                   and JPDA_SUSPEND are ignored. Thus, all required jpda
#                   options MUST be specified. The default is:
#
#                   -Xdebug -Xrunjdwp:transport=$JPDA_TRANSPORT,
#                       address=$JPDA_ADDRESS,server=y,suspend=$JPDA_SUSPEND
#
#   HURRICANE_PID    (Optional) Path of the file which should contains the pid
#                   of hurricane startup java process, when start (fork) is used
#
# $Id: hurricane.sh 1040548 2010-11-30 14:48:26Z markt $
# -----------------------------------------------------------------------------

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

# Only set HURRICANE_HOME if not already set
[ -z "$HURRICANE_HOME" ] && HURRICANE_HOME=`cd "$PRGDIR/.." >/dev/null; pwd`

# Ensure that any user defined CLASSPATH variables are not used on startup,
# but allow them to be specified in setenv.sh, in rare case when it is needed.
CLASSPATH=

if [ -r "$HURRICANE_BASE"/bin/setenv.sh ]; then
  . "$HURRICANE_BASE"/bin/setenv.sh
elif [ -r "$HURRICANE_HOME"/bin/setenv.sh ]; then
  . "$HURRICANE_HOME"/bin/setenv.sh
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$JRE_HOME" ] && JRE_HOME=`cygpath --unix "$JRE_HOME"`
  [ -n "$HURRICANE_HOME" ] && HURRICANE_HOME=`cygpath --unix "$HURRICANE_HOME"`
  [ -n "$HURRICANE_BASE" ] && HURRICANE_BASE=`cygpath --unix "$HURRICANE_BASE"`
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
  BASEDIR="$HURRICANE_HOME"
  . "$HURRICANE_HOME"/bin/setclasspath.sh 
else
  if [ -r "$HURRICANE_HOME"/bin/setclasspath.sh ]; then
    BASEDIR="$HURRICANE_HOME"
    . "$HURRICANE_HOME"/bin/setclasspath.sh
  else
    echo "Cannot find $HURRICANE_HOME/bin/setclasspath.sh"
    echo "This file is needed to run this program"
    exit 1
  fi
fi

# Add on extra jar files to CLASSPATH
if [ ! -z "$CLASSPATH" ] ; then
  CLASSPATH="$CLASSPATH":
fi

if [ -z "$HURRICANE_BASE" ] ; then
  HURRICANE_BASE="$HURRICANE_HOME"
fi

if [ -z "$HURRICANE_OUT" ] ; then
  HURRICANE_OUT="$HURRICANE_BASE"/logs/hurricane.out
fi

if [ -z "$HURRICANE_PID" ] ; then
  HURRICANE_PID="$HURRICANE_BASE"/logs/hurricane.pid
fi

if [ -z "$HURRICANE_TMPDIR" ] ; then
  # Define the java.io.tmpdir to use for Catalina
  HURRICANE_TMPDIR="$HURRICANE_BASE"/temp
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
  HURRICANE_HOME=`cygpath --absolute --windows "$HURRICANE_HOME"`
  HURRICANE_BASE=`cygpath --absolute --windows "$HURRICANE_BASE"`
  HURRICANE_TMPDIR=`cygpath --absolute --windows "$HURRICANE_TMPDIR"`
  CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
  JAVA_ENDORSED_DIRS=`cygpath --path --windows "$JAVA_ENDORSED_DIRS"`
fi

# Set juli LogManager if it is present
if [ -r "$HURRICANE_HOME"/bin/tomcat-juli.jar ]; then
  JAVA_OPTS="$JAVA_OPTS -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager"
  LOGGING_CONFIG="-Djava.util.logging.config.file=$HURRICANE_BASE/conf/logging.properties"
else
  # Bugzilla 45585
  LOGGING_CONFIG="-Dnop"
fi

# ----- Execute The Requested Command -----------------------------------------

# Bugzilla 37848: only output this if we have a TTY
if [ $have_tty -eq 1 ]; then
  echo "Using HURRICANE_BASE:   $HURRICANE_BASE"
  echo "Using HURRICANE_HOME:   $HURRICANE_HOME"
  echo "Using HURRICANE_TMPDIR: $HURRICANE_TMPDIR"
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
  HURRICANE_OPTS="$HURRICANE_OPTS $JPDA_OPTS"
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
      exec "$_RUNJDB" "$LOGGING_CONFIG" $JAVA_OPTS  $HURRICANE_OPTS \
        -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
        -sourcepath "$HURRICANE_HOME"/../../jakarta-tomcat-hurricane/hurricane/src/share \
        -Djava.security.manager \
        -Djava.security.policy=="$HURRICANE_BASE"/conf/hurricane.policy \
        -Dhurricane.base="$HURRICANE_BASE" \
        -Dhurricane.home="$HURRICANE_HOME" \
        -Djava.io.tmpdir="$HURRICANE_TMPDIR" \
        org.iipg.hurricane.server.Server "$@" start
    else
      exec "$_RUNJDB" "$LOGGING_CONFIG" $JAVA_OPTS  $HURRICANE_OPTS \
        -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
        -sourcepath "$HURRICANE_HOME"/../../jakarta-tomcat-hurricane/hurricane/src/share \
        -Dhurricane.base="$HURRICANE_BASE" \
        -Dhurricane.home="$HURRICANE_HOME" \
        -Djava.io.tmpdir="$HURRICANE_TMPDIR" \
        org.iipg.hurricane.server.Server "$@" start
    fi
  fi

elif [ "$1" = "run" ]; then

  shift
  if [ "$1" = "-security" ] ; then
    if [ $have_tty -eq 1 ]; then
      echo "Using Security Manager"
    fi
    shift
    exec "$_RUNJAVA" "$LOGGING_CONFIG" $JAVA_OPTS  $HURRICANE_OPTS \
      -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
      -Djava.security.manager \
      -Djava.security.policy=="$HURRICANE_BASE"/conf/hurricane.policy \
      -Dhurricane.base="$HURRICANE_BASE" \
      -Dhurricane.home="$HURRICANE_HOME" \
      -Djava.io.tmpdir="$HURRICANE_TMPDIR" \
      org.iipg.hurricane.server.Server "$@" start
  else
    exec "$_RUNJAVA" "$LOGGING_CONFIG" $JAVA_OPTS  $HURRICANE_OPTS \
      -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
      -Dhurricane.base="$HURRICANE_BASE" \
      -Dhurricane.home="$HURRICANE_HOME" \
      -Djava.io.tmpdir="$HURRICANE_TMPDIR" \
      org.iipg.hurricane.server.Server "$@" start
  fi

elif [ "$1" = "start" ] ; then

  shift
  touch "$HURRICANE_OUT"
  if [ "$1" = "-security" ] ; then
    if [ $have_tty -eq 1 ]; then
      echo "Using Security Manager"
    fi
    shift
    "$_RUNJAVA" "$LOGGING_CONFIG" $JAVA_OPTS  $HURRICANE_OPTS \
      -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
      -Djava.security.manager \
      -Djava.security.policy=="$HURRICANE_BASE"/conf/hurricane.policy \
      -Dhurricane.base="$HURRICANE_BASE" \
      -Dhurricane.home="$HURRICANE_HOME" \
      -Djava.io.tmpdir="$HURRICANE_TMPDIR" \
      org.iipg.hurricane.server.Server "$@" start \
      >> "$HURRICANE_OUT" 2>&1 &

      if [ ! -z "$HURRICANE_PID" ]; then
        echo $! > $HURRICANE_PID
      fi
  else
    "$_RUNJAVA" "$LOGGING_CONFIG" $JAVA_OPTS  $HURRICANE_OPTS \
      -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
      -Dhurricane.base="$HURRICANE_BASE" \
      -Dhurricane.home="$HURRICANE_HOME" \
      -Djava.io.tmpdir="$HURRICANE_TMPDIR" \
      org.iipg.hurricane.server.Server "$@" start \
      >> "$HURRICANE_OUT" 2>&1 &

      if [ ! -z "$HURRICANE_PID" ]; then
        echo $! > $HURRICANE_PID
      fi
  fi

elif [ "$1" = "stop" ] ; then

  shift
  FORCE=1
#  if [ "$1" = "-force" ]; then
#    shift
#    FORCE=1
#  fi

#  "$_RUNJAVA" $JAVA_OPTS \
#    -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
#    -Dhurricane.base="$HURRICANE_BASE" \
#    -Dhurricane.home="$HURRICANE_HOME" \
#    -Djava.io.tmpdir="$HURRICANE_TMPDIR" \
#    org.iipg.hurricane.server.Server "$@" stop

  if [ $FORCE -eq 1 ]; then
    if [ ! -z "$HURRICANE_PID" ]; then
       echo "Killing: `cat $HURRICANE_PID`"
       kill -9 `cat $HURRICANE_PID`
    else
       echo "Kill failed: \$HURRICANE_PID not set"
    fi
  fi

elif [ "$1" = "version" ] ; then

    "$_RUNJAVA"   \
      "%HURRICANE_HOME%/lib/hu-server-1.0.0-SNAPSHOT.jar" \
      org.iipg.hurricane.util.ServerInfo

else

  echo "Usage: hurricane.sh ( commands ... )"
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
