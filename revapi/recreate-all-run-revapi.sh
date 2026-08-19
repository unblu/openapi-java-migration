#!/bin/sh
#
# Re-creates the `run-revapi.sh` script of every artifact of every existing report
# folder, by calling `create-report-folder.sh` for each of them.
#
# Usage:
#
#   ./recreate-all-run-revapi.sh
#
# Use this after changing what the generated command looks like: the Revapi extension
# versions pinned in `create-report-folder.sh`, the options passed to `revapi.java`, or
# the layout of the generated script. The reports themselves are not refreshed, run
# `./run-all-revapi.sh` afterwards for that.
#
# Note that a change to `revapi-config.json` alone does *not* require this script: the
# configuration file is referenced by path and read at run time. `./run-all-revapi.sh`
# is enough in that case.

set -u

BASE_DIR=$(cd "$(dirname "$0")" && pwd)

pairs=$(find "$BASE_DIR" -mindepth 1 -maxdepth 1 -type d -name '*_*' -exec basename {} \; | sort -V)

if [ -z "$pairs" ]; then
    echo "No report folder found under $BASE_DIR" >&2
    exit 1
fi

total=$(echo "$pairs" | wc -l | tr -d ' ')
echo "Re-creating the run-revapi.sh scripts of $total report folders"
echo

index=0
failed=

for pair in $pairs; do
    index=$((index + 1))

    old_version=${pair%_*}
    new_version=${pair#*_}

    # Only re-create the scripts of the artifacts that are already present, so that a
    # folder is never silently extended with an artifact it was not created for.
    artifacts=$(find "$BASE_DIR/$pair" -mindepth 1 -maxdepth 1 -type d -exec basename {} \; | sort | tr '\n' ' ')

    if [ -z "$artifacts" ]; then
        echo "[$index/$total] $pair: no artifact folder, skipping"
        continue
    fi

    echo "[$index/$total] $pair"
    # shellcheck disable=SC2086
    if ! "$BASE_DIR"/create-report-folder.sh "$old_version" "$new_version" $artifacts; then
        echo "  FAILED: $pair" >&2
        failed="$failed $pair"
    fi
done

echo

if [ -n "$failed" ]; then
    echo "Failed folders:$failed" >&2
    exit 1
fi

echo "Done."
