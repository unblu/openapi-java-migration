#!/bin/sh
#
# Runs the `run-revapi.sh` script of every artifact of every report folder, which
# (re-)creates all `report.txt` and `report.json` files.
#
# Usage:
#
#   ./run-all-revapi.sh
#
# Use this after changing `revapi-config.json`: the configuration file is read by the
# `run-revapi.sh` scripts at run time, so the reports have to be produced again, but
# the scripts themselves stay valid.
#
# This takes about an hour. Failures are collected and reported at the end rather than
# aborting the run.

set -u

BASE_DIR=$(cd "$(dirname "$0")" && pwd)

scripts=$(find "$BASE_DIR" -mindepth 3 -maxdepth 3 -name run-revapi.sh | sort -V)

if [ -z "$scripts" ]; then
    echo "No run-revapi.sh script found under $BASE_DIR" >&2
    exit 1
fi

total=$(echo "$scripts" | wc -l | tr -d ' ')
echo "Running $total analyses"
echo

index=0
failed=

for script in $scripts; do
    index=$((index + 1))
    artifact_dir=$(dirname "$script")
    artifact=$(basename "$artifact_dir")
    pair=$(basename "$(dirname "$artifact_dir")")

    echo "[$index/$total] $pair $artifact"
    if ! "$script"; then
        echo "  FAILED: $pair $artifact" >&2
        failed="$failed $pair/$artifact"
    fi
done

echo

if [ -n "$failed" ]; then
    echo "Failed analyses:$failed" >&2
    exit 1
fi

echo "Done."
