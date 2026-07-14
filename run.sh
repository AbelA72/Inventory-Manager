#!/bin/sh
set -eu

mkdir -p bin
javac -Xlint:all -d bin src/*.java
exec java -cp bin RestaurantManager "${1:-8080}"
