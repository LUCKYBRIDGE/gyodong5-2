#!/usr/bin/env bash
set -euo pipefail

project_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source_dir="$project_dir/src/main/java/com/knolquiz/traffic"
java_home="${TRAFFIC_JAVA_HOME:-${JAVA_HOME:-}}"
javac="javac"
java="java"
if [[ -n "$java_home" ]]; then
    javac="$java_home/bin/javac"
    java="$java_home/bin/java"
fi

if ! "$javac" -version >/dev/null 2>&1; then
    echo "Set TRAFFIC_JAVA_HOME or JAVA_HOME to a JDK 17+ installation." >&2
    exit 1
fi

classes="$(mktemp -d)"
trap 'rm -rf "$classes"' EXIT

"$javac" --release 17 -d "$classes" \
    "$source_dir/RewardSession.java" \
    "$source_dir/Proximity.java" \
    "$source_dir/DailyPoints.java" \
    "$source_dir/Rewards.java" \
    "$project_dir/tests/RewardTest.java" \
    "$project_dir/tests/ShopTest.java"
"$java" -cp "$classes" com.knolquiz.traffic.RewardTest
"$java" -cp "$classes" com.knolquiz.traffic.ShopTest
