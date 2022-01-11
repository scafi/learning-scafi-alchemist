#!/usr/bin/env bash

if (($# != 4)); then
  echo "USE: ./showImgs <imgDir> <prefixImages> <numColumns> <outFilename>";
else
  set -x
  mkdir -p $1/summaries
  montage -tile $3x -geometry +0+0 $1/$2*.pdf $1/summaries/$4.pdf && xdg-open $1/summaries/$4.pdf
fi