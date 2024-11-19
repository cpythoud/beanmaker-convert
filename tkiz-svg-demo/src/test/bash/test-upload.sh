#!/usr/bin/env bash

curl -X POST http://localhost:8100/convert/tkiz2svg/upload \
-H "Content-Type: multipart/form-data" \
-F "file=@/home/chris/Prov/tkiz2svg/source/radar.pdf"

curl -X POST http://localhost:8100/convert/tkiz2svg/upload \
-H "Content-Type: multipart/form-data" \
-F "file=@/home/chris/Prov/tkiz2svg/source/word-cloud.pdf"
