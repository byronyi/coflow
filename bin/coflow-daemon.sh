#!/usr/bin/env bash

# This coflow framework script is a modified version of the Apache Hadoop framework
# script, available under the Apache 2 license:
#
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

# Runs a coflow command as a daemon.
#
# Environment Variables
#
#   COFLOW_CONF_DIR  Alternate conf dir. Default is ${COFLOW_PREFIX}/conf.
#   COFLOW_LOG_DIR   Where log files are stored.  PWD by default.
#   COFLOW_MASTER    host:path where coflow code should be rsync'd from
#   COFLOW_PID_DIR   The pid files are stored. /tmp by default.
#   COFLOW_IDENT_STRING   A string representing this instance of coflow. $USER by default
#   COFLOW_NICENESS The scheduling priority for daemons. Defaults to 0.
##

usage="Usage: $(basename $0) [--config <conf-dir>] [--hosts hostlistfile] (start|stop) <coflow-command> <args...>"

if [ $# -le 1 ]; then
  echo $usage
  exit 1
fi

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin/coflow-config.sh"

startStop=$1
shift
command=$1
shift

coflow_rotate_log ()
{
    log=$1;
    num=5;
    if [ -n "$2" ]; then
  num=$2
    fi
    if [ -f "$log" ]; then
  while [ $num -gt 1 ]; do
      prev=`expr $num - 1`
      [ -f "$log.$prev" ] && mv "$log.$prev" "$log.$num"
      num=$prev
  done
  mv "$log" "$log.$num";
    fi
}

if [ -f "${COFLOW_CONF_DIR}/coflow-env.sh" ]; then
  . "${COFLOW_CONF_DIR}/coflow-env.sh"
fi

if [ "$COFLOW_IDENT_STRING" = "" ]; then
  export COFLOW_IDENT_STRING="$USER"
fi

# get log directory
if [ "$COFLOW_LOG_DIR" = "" ]; then
  export COFLOW_LOG_DIR="$COFLOW_HOME/logs"
fi
mkdir -p "$COFLOW_LOG_DIR"
touch $COFLOW_LOG_DIR/.coflow_test > /dev/null 2>&1
TEST_LOG_DIR=$?
if [ "${TEST_LOG_DIR}" = "0" ]; then
  rm -f $COFLOW_LOG_DIR/.coflow_test
else
  chown $COFLOW_IDENT_STRING $COFLOW_LOG_DIR
fi

if [ "$COFLOW_PID_DIR" = "" ]; then
  COFLOW_PID_DIR=/tmp/coflow
fi

# some variables
export COFLOW_LOGFILE=coflow-$COFLOW_IDENT_STRING-$command-$HOSTNAME.log
export COFLOW_ROOT_LOGGER="INFO,DRFA"
log=$COFLOW_LOG_DIR/coflow-$COFLOW_IDENT_STRING-$command-$HOSTNAME.out
pid=$COFLOW_PID_DIR/coflow-$COFLOW_IDENT_STRING-$command.pid

# Set default scheduling priority
if [ "$COFLOW_NICENESS" = "" ]; then
    export COFLOW_NICENESS=0
fi


case $startStop in

  (start)

    mkdir -p "$COFLOW_PID_DIR"

    if [ -f $pid ]; then
      if kill -0 `cat $pid` > /dev/null 2>&1; then
        echo $command running as process `cat $pid`.  Stop it first.
        exit 1
      fi
    fi

    if [ "$COFLOW_MASTER" != "" ]; then
      echo rsync from $COFLOW_MASTER
      rsync -a -e ssh --delete --exclude=.git --exclude='logs/*' $COFLOW_MASTER/ "$COFLOW_HOME"
    fi

    coflow_rotate_log $log
    echo Starting $command, logging to $log
    cd "$COFLOW_PREFIX"
    nohup nice -n $COFLOW_NICENESS "$COFLOW_PREFIX"/run $command "$@" > "$log" 2>&1 < /dev/null &
    echo $! > $pid
    sleep 1; head "$log"
    ;;

  (stop)

    if [ -f $pid ]; then
      if kill -0 `cat $pid` > /dev/null 2>&1; then
        echo Stopping $command
        kill `cat $pid`
      else
        echo No $command to stop
      fi
    else
      echo No $command to stop
    fi
    ;;

  (*)
    echo $usage
    exit 1
    ;;

esac
