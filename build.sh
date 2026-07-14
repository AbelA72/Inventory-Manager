#!/bin/sh
set -eu

mkdir -p bin
javac -Xlint:all -d bin src/*.java
echo "Build complete. Run: java -cp bin RestaurantManager"
