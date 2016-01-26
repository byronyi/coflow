#!/usr/bin/env bash

usage="Usage: coflow-daemons.sh [--config confdir] [--hosts hostlistfile] [start|stop] command args..."

if [ $# -le 1 ]; then
  echo $usage
  exit 1
fi

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin/coflow-config.sh"

exec "$bin/slaves.sh" cd "$COFLOW_HOME" \; "$bin/coflow-daemon.sh" "$@"
