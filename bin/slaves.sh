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

# Run a shell command on all slave hosts.
#
# Environment Variables
#
#   COFLOW_SLAVES    File naming remote hosts.
#     Default is ${COFLOW_CONF_DIR}/slaves.
#   COFLOW_CONF_DIR  Alternate conf dir. Default is ${COFLOW_HOME}/conf.
#   COFLOW_SLAVE_SLEEP Seconds to sleep between spawning remote commands.
#   COFLOW_SSH_OPTS Options passed to ssh when running remote commands.
##

usage="Usage: slaves.sh [--config confdir] command..."

# if no args specified, show usage
if [ $# -le 0 ]; then
  echo $usage
  exit 1
fi

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin/coflow-config.sh"

# If the slaves file is specified in the command line,
# then it takes precedence over the definition in 
# coflow-env.sh. Save it here.
HOSTLIST=$COFLOW_SLAVES

if [ -f "${COFLOW_CONF_DIR}/coflow-env.sh" ]; then
  . "${COFLOW_CONF_DIR}/coflow-env.sh"
fi

if [ "$HOSTLIST" = "" ]; then
  if [ "$COFLOW_SLAVES" = "" ]; then
    export HOSTLIST="${COFLOW_CONF_DIR}/slaves"
  else
    export HOSTLIST="${COFLOW_SLAVES}"
  fi
fi

echo $"${@// /\\ }"

# By default disable strict host key checking
if [ "$COFLOW_SSH_OPTS" = "" ]; then
  COFLOW_SSH_OPTS="-o StrictHostKeyChecking=no"
fi

for slave in `cat "$HOSTLIST"|sed  "s/#.*$//;/^$/d"`; do
 ssh $COFLOW_SSH_OPTS $slave $"${@// /\\ }" \
   2>&1 | sed "s/^/$slave: /" &
 if [ "$COFLOW_SLAVE_SLEEP" != "" ]; then
   sleep $COFLOW_SLAVE_SLEEP
 fi
done

wait
