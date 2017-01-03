#!/bin/sh
# -----------------------------------------------------------------------------
# Start/Stop Script for the STARTER Server
#
# Environment Variable Prequisites
#
#   STARTER_HOME   May point at your Catalina "build" directory.
#
#   STARTER_BASE   (Optional) Base directory for resolving dynamic portions
#                   of a Catalina installation.  If not present, resolves to
#                   the same directory that STARTER_HOME points to.
#
#   STARTER_OPTS   (Optional) Java runtime options used when the "start",
#                   or "run" command is executed.
#
#   STARTER_CONF   (Optional) Directory path location of temporary directory
#                   the JVM should use (java.io.tmpdir).  Defaults to
#                   $STARTER_BASE/temp.
#
#   JAVA_HOME       Must point at your Java Development Kit installation.
#                   Required to run the with the "debug" or "javac" argument.
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
#   JSSE_HOME       (Optional) May point at your Java Secure Sockets Extension
#                   (JSSE) installation, whose JAR files will be added to the
#                   system class path used to start Tomcat.
#
#   STARTER_PID    (Optional) Path of the file which should contains the pid
#                   of catalina startup java process, when start (fork) is used
#
# $Id: catalina.sh,v 1.4 2009/08/02 06:20:13 james Exp $
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

export LANG="zh_CN.GB18030"

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

# Only set STARTER_HOME if not already set
[ -z "$STARTER_HOME" ] && STARTER_HOME=`cd "$PRGDIR/.." ; pwd`

if [ -r "$STARTER_BASE"/sbin/setenv.sh ]; then
  . "$STARTER_BASE"/sbin/setenv.sh
elif [ -r "$STARTER_HOME"/sbin/setenv.sh ]; then
  . "$STARTER_HOME"/sbin/setenv.sh
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin; then
  [ -n "$JAVA_HOME" ] && JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
  [ -n "$JRE_HOME" ] && JRE_HOME=`cygpath --unix "$JRE_HOME"`
  [ -n "$STARTER_HOME" ] && STARTER_HOME=`cygpath --unix "$STARTER_HOME"`
  [ -n "$STARTER_BASE" ] && STARTER_BASE=`cygpath --unix "$STARTER_BASE"`
  [ -n "$CLASSPATH" ] && CLASSPATH=`cygpath --path --unix "$CLASSPATH"`
  [ -n "$JSSE_HOME" ] && JSSE_HOME=`cygpath --absolute --unix "$JSSE_HOME"`
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
  BASEDIR="$STARTER_HOME"
  . "$STARTER_HOME"/sbin/setclasspath.sh 
else
  if [ -r "$STARTER_HOME"/sbin/setclasspath.sh ]; then
    BASEDIR="$STARTER_HOME"
    . "$STARTER_HOME"/sbin/setclasspath.sh
  else
    echo "Cannot find $STARTER_HOME/sbin/setclasspath.sh"
    echo "This file is needed to run this program"
    exit 1
  fi
fi

# Add on extra jar files to CLASSPATH
if [ -n "$JSSE_HOME" ]; then
  CLASSPATH="$CLASSPATH":"$JSSE_HOME"/lib/jcert.jar:"$JSSE_HOME"/lib/jnet.jar:"$JSSE_HOME"/lib/jsse.jar
fi
CLASSPATH="$CLASSPATH":"$STARTER_HOME"/sbin/iipg-starter-0.0.1-SNAPSHOT.jar

if [ -z "$STARTER_BASE" ] ; then
  STARTER_BASE="$STARTER_HOME"
fi

if [ -z "$STARTER_CONF" ] ; then
  STARTER_CONF="$STARTER_BASE"/starter.properties
fi

if [ -z "$STARTER_TMPDIR" ] ; then
  STARTER_TMPDIR="$STARTER_BASE"/temp
fi

if [ -z "$STARTER_PID" ] ; then
if [ "-$3" = "-" ]; then
  STARTER_PID="$STARTER_HOME"/logs/starter.pid
else
  STARTER_PID="$STARTER_HOME"/logs/"$3"
fi
fi

if [ -z "$RUN_LOG" ] ; then
if [ "-$4" = "-" ]; then
  RUN_LOG="$STARTER_HOME"/logs/run.out
else
  RUN_LOG="$STARTER_HOME"/logs/"$4"
fi
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
  STARTER_HOME=`cygpath --absolute --windows "$STARTER_HOME"`
  STARTER_BASE=`cygpath --absolute --windows "$STARTER_BASE"`
  STARTER_CONF=`cygpath --absolute --windows "$STARTER_CONF"`
  CLASSPATH=`cygpath --path --windows "$CLASSPATH"`
  [ -n "$JSSE_HOME" ] && JSSE_HOME=`cygpath --absolute --windows "$JSSE_HOME"`
  JAVA_ENDORSED_DIRS=`cygpath --path --windows "$JAVA_ENDORSED_DIRS"`
fi

# Set juli LogManager if it is present
if [ -r "$STARTER_HOME"/sbin/tomcat-juli.jar ]; then
  JAVA_OPTS="$JAVA_OPTS -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager"
  LOGGING_CONFIG="-Djava.util.logging.config.file=$STARTER_BASE/conf/logging.properties"
else
  # Bugzilla 45585
  LOGGING_CONFIG="-Dnop"
fi

# ----- Execute The Requested Command -----------------------------------------

# Bugzilla 37848: only output this if we have a TTY
if [ $have_tty -eq 1 ]; then
  echo "Using STARTER_BASE:   $STARTER_BASE"
  echo "Using STARTER_HOME:   $STARTER_HOME"
  echo "Using STARTER_CONF:   $STARTER_CONF"
  if [ "$1" = "debug" -o "$1" = "javac" ] ; then
    echo "Using JAVA_HOME:       $JAVA_HOME"
  else
    echo "Using JRE_HOME:       $JRE_HOME"
  fi
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
  STARTER_OPTS="$STARTER_OPTS $JPDA_OPTS"
  shift
fi

if [ "-$2" = "-" ]; then
  MAIN_CLASS="org.iipg.starter.Runjar"
else
  MAIN_CLASS="$2"
fi
if [ "$1" = "debug" ] ; then
  if $os400; then
    echo "Debug command not available on OS400"
    exit 1
  else
    shift
    if [ "$1" = "-security" ] ; then
      echo "Using Security Manager"
      shift
      exec "$_RUNJDB" "$LOGGING_CONFIG" $JAVA_OPTS  $STARTER_OPTS \
        -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
        -sourcepath "$STARTER_HOME"/../../jakarta-tomcat-catalina/catalina/src/share \
        -Djava.security.manager \
        -Djava.security.policy=="$STARTER_BASE"/conf/catalina.policy \
        -Dstarter.base="$STARTER_BASE" \
        -Dstarter.home="$STARTER_HOME" \
        -Dstarter.conf="$STARTER_CONF" \
        -Djava.io.tmpdir="$STARTER_TMPDIR" \
        $MAIN_CLASS start "$@" 
    else
      exec "$_RUNJDB" "$LOGGING_CONFIG" $JAVA_OPTS  $STARTER_OPTS \
        -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
        -sourcepath "$STARTER_HOME"/../../jakarta-tomcat-catalina/catalina/src/share \
        -Dstarter.base="$STARTER_BASE" \
        -Dstarter.home="$STARTER_HOME" \
        -Dstarter.conf="$STARTER_CONF" \
        -Djava.io.tmpdir="$STARTER_TMPDIR" \
        $MAIN_CLASS start "$@" 
    fi
  fi

elif [ "$1" = "run" ]; then

  shift
  if [ "$1" = "-security" ] ; then
    echo "Using Security Manager"
    shift
    exec "$_RUNJAVA" "$LOGGING_CONFIG" $JAVA_OPTS  $STARTER_OPTS \
      -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
      -Djava.security.manager \
      -Djava.security.policy=="$STARTER_BASE"/conf/catalina.policy \
      -Dstarter.base="$STARTER_BASE" \
      -Dstarter.home="$STARTER_HOME" \
      -Dstarter.conf="$STARTER_CONF" \
      -Djava.io.tmpdir="$STARTER_TMPDIR" \
      $MAIN_CLASS start "$@" 
  else
    exec "$_RUNJAVA" "$LOGGING_CONFIG" $JAVA_OPTS  $STARTER_OPTS \
      -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
      -Dstarter.base="$STARTER_BASE" \
      -Dstarter.home="$STARTER_HOME" \
      -Dstarter.conf="$STARTER_CONF" \
      -Djava.io.tmpdir="$STARTER_TMPDIR" \
      $MAIN_CLASS start "$@" 
  fi

elif [ "$1" = "start" ] ; then

  shift
  if [ "$1" = "-security" ] ; then
    echo "Using Security Manager"
    echo > "$RUN_LOG"
    shift
    "$_RUNJAVA" "$LOGGING_CONFIG" $JAVA_OPTS  $STARTER_OPTS \
      -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
      -Djava.security.manager \
      -Djava.security.policy=="$STARTER_BASE"/conf/catalina.policy \
      -Dstarter.base="$STARTER_BASE" \
      -Dstarter.home="$STARTER_HOME" \
      -Dstarter.conf="$STARTER_CONF" \
      -Djava.io.tmpdir="$STARTER_TMPDIR" \
      $MAIN_CLASS start "$@" \
# >> /dev/null 2>&1 &
>>"$RUN_LOG" 2>&1 &

      if [ ! -z "$STARTER_PID" ]; then
        echo $! > $STARTER_PID
      fi
  else
    if [ -e $STARTER_PID ];then
		EXIST_PID=`cat $STARTER_PID`
    fi
    if [ ! -z "$EXIST_PID" ];then
#		pid=`ps -ef | grep $EXIST_PID | grep java | sed -n '1P' | awk  '{print   $2}'`
	pid=`ps --no-heading $EXIST_PID | sed -n '1P' | awk  '{print   $1}'`
    fi	
    if [ ! -z "$pid" ];then
	echo   "Start up failed! Please shut down the application first! pid = $pid. "
    else
        echo "Starting server... $@"	
        echo > "$RUN_LOG"
	exec "$_RUNJAVA" "$LOGGING_CONFIG" $JAVA_OPTS  $STARTER_OPTS \
		  -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
		  -Dstarter.base="$STARTER_BASE" \
		  -Dstarter.home="$STARTER_HOME" \
      -Dstarter.conf="$STARTER_CONF" \
		  -Djava.io.tmpdir="$STARTER_TMPDIR" \
		  $MAIN_CLASS start "$@"  \
>>"$RUN_LOG" 2>&1 &

      if [ ! -z "$STARTER_PID" ]; then
        echo $! > $STARTER_PID
      fi
	fi
  fi

elif [ "$1" = "stop" ] ; then

  shift
  FORCE=1

  if [ $FORCE -eq 1 ]; then
    if [ ! -z "$STARTER_PID" ]; then
       echo "Killing: `cat $STARTER_PID`"
       kill -9 `cat $STARTER_PID`
       `rm -rf $STARTER_PID`
    else
       echo "Kill failed: \$STARTER_PID not set"
    fi
  fi

elif [ "$1" = "version" ] ; then

  shift
  FORCE=1

  exec "$_RUNJAVA" "$LOGGING_CONFIG" $JAVA_OPTS  $STARTER_OPTS \
      -Djava.endorsed.dirs="$JAVA_ENDORSED_DIRS" -classpath "$CLASSPATH" \
      -Dstarter.base="$STARTER_BASE" \
      -Dstarter.home="$STARTER_HOME" \
      -Dstarter.conf="$STARTER_CONF" \
      -Djava.io.tmpdir="$STARTER_TMPDIR" \
      $MAIN_CLASS "$@" version

else

  echo "Usage: run.sh ( commands ... )"
  echo "commands:"
  if $os400; then
    echo "  debug             Start Server in a debugger (not available on OS400)"
    echo "  debug -security   Debug Server with a security manager (not available on OS400)"
  else
    echo "  debug             Start Server in a debugger"
    echo "  debug -security   Debug Server with a security manager"
  fi
  echo "  jpda start        Start Server under JPDA debugger"
  echo "  run               Start Server in the current window"
  echo "  run -security     Start in the current window with security manager"
  echo "  start             Start Server in a separate window"
  echo "  start -security   Start in a separate window with security manager"
  echo "  stop              Stop Server"
  echo "  stop -force       Stop Server (followed by kill -KILL)"
  echo "  version           What version of Server are you running?"
  exit 1

fi

