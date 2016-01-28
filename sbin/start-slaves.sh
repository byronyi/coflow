#!/usr/bin/env bash

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin/coflow-config.sh"

if [ -f "${COFLOW_CONF_DIR}/coflow-env.sh" ]; then
  . "${COFLOW_CONF_DIR}/coflow-env.sh"
fi

# Find the port number for the master
if [ "$COFLOW_MASTER_PORT" = "" ]; then
  COFLOW_MASTER_PORT=1606
fi

if [ "$COFLOW_MASTER_IP" = "" ]; then
  COFLOW_MASTER_IP=`hostname`
fi

echo "Master IP: $COFLOW_MASTER_IP"

# Launch the slaves
exec "$bin/slaves.sh" cd "$COFLOW_HOME" \; "$bin/start-slave.sh" $COFLOW_MASTER_IP $COFLOW_MASTER_PORT
