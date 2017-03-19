#!/usr/bin/env bash
./gradlew clean build bintrayUpload -PbintrayUser=hello2mao -PbintrayKey=03ff4c867fd45bd369083759b1d56906b6765b63 -PdryRun=false