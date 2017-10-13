#!/bin/sh
git add .
date=`date`
git commit -m "Daily push: $date"
git push
