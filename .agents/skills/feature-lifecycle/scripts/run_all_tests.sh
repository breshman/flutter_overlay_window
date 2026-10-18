#!/usr/bin/env bash
set -e

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
echo "=== Running Flutter Overlay Window Full Verification Suite ==="
echo "Repository Root: $REPO_ROOT"

echo ""
echo "[1/4] Running flutter analyze on root plugin..."
cd "$REPO_ROOT"
flutter analyze

echo ""
echo "[2/4] Running flutter analyze on example app..."
cd "$REPO_ROOT/example"
flutter analyze

echo ""
echo "[3/4] Running Flutter unit test suite..."
cd "$REPO_ROOT"
flutter test

echo ""
echo "[4/4] Running Android Kotlin native unit tests..."
cd "$REPO_ROOT/example/android"
./gradlew :flutter_overlay_window:testDebugUnitTest

echo ""
echo "=== All Quality Gates Passed Successfully! ==="
