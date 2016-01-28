#!/usr/bin/env bash

export JAVA_HOME="/usr/lib/jvm/default-java"
export COFLOW_LIBRARY_PATH="/home/byronyi/coflow/core/build/libs"
export COFLOW_MASTER_IP="192.168.2.2"
export COFLOW_LOG_DIR="/var/log/coflow"
export COFLOW_PID_DIR="/var/run/coflow"

export COFLOW_SYNC_PERIOD_MS=1000
export COFLOW_TC_INTERFACE="eth0"
export COFLOW_TC_PARENT_CLASS="1:1"
# TODO: get the bandwidth from netlink so user don't need to specify it here
export COFLOW_TC_BANDWIDTH_BYTES="13107200" # 100Mbps
