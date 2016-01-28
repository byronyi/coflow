#!/usr/bin/env bash

# Starts the master on the machine this script is executed on.

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin/coflow-config.sh"

if [ -f "${COFLOW_CONF_DIR}/coflow-env.sh" ]; then
  . "${COFLOW_CONF_DIR}/coflow-env.sh"
fi

if [ "$COFLOW_MASTER_PORT" = "" ]; then
  COFLOW_MASTER_PORT=1606
fi

if [ "$COFLOW_MASTER_IP" = "" ]; then
  COFLOW_MASTER_IP=`hostname`
fi

"$bin"/coflow-daemon.sh start coflow.CoflowMaster $COFLOW_MASTER_IP $COFLOW_MASTER_PORT
