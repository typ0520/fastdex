#!/bin/bash

rm -rf ~/.m2/repository/com/dx168/fastdex

sh gradlew :runtime:generateRuntimeDexForRelease
sh gradlew install