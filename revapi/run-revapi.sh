#!/bin/sh
#
# Runs the `run-revapi.sh` script of every artifact of one report folder, which
# (re-)creates the `report.txt` and `report.json` files in it.
#
# Usage:
#
#   ./run-revapi.sh <old version> <new version>
#
# Example:
#
#   ./run-revapi.sh 8.39.3 8.40.4
#
# The report folder must exist; create it with `./create-report-folder.sh` first.

set -eu

BASE_DIR=$(cd "$(dirname "$0")" && pwd)

if [ $# -ne 2 ]; then
    echo "Usage: $0 <old version> <new version>" >&2
    exit 1
fi

OLD_VERSION=$1
NEW_VERSION=$2

REPORT_DIR="$BASE_DIR/${OLD_VERSION}_${NEW_VERSION}"

if [ ! -d "$REPORT_DIR" ]; then
    echo "No such report folder: $REPORT_DIR" >&2
    echo "Create it with: $BASE_DIR/create-report-folder.sh $OLD_VERSION $NEW_VERSION" >&2
    exit 1
fi

found=false

for script in "$REPORT_DIR"/*/run-revapi.sh; do
    [ -f "$script" ] || continue
    found=true
    artifact=$(basename "$(dirname "$script")")
    echo "Analysing $artifact ${OLD_VERSION} -> ${NEW_VERSION}"
    "$script"
done

if [ "$found" = false ]; then
    echo "No run-revapi.sh script found in $REPORT_DIR" >&2
    echo "Create them with: $BASE_DIR/create-report-folder.sh $OLD_VERSION $NEW_VERSION" >&2
    exit 1
fi
