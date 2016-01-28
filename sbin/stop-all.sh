#!/usr/bin/env bash

# Start all coflow daemons.
# Run this on the master nde

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

# Load the coflow configuration
. "$bin/coflow-config.sh"

# Stop the slaves, then the master
"$bin"/stop-slaves.sh
"$bin"/stop-master.sh
