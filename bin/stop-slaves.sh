#!/usr/bin/env bash

# Starts the master on the machine this script is executed on.

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

. "$bin/coflow-config.sh"

"$bin"/coflow-daemons.sh stop coflow.CoflowSlave
