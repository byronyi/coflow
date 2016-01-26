#!/usr/bin/env bash

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

"$bin"/coflow-daemon.sh start coflow.CoflowSlave $1
