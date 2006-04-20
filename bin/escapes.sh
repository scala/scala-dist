#!/bin/sh

file=$1
name=`basename $file`;
tmp=`mktemp` || exit 1

sed -e 's/% 2/\\% 2/g' \
    -e 's/<%/<\\%/g' \
    -e 's/#Node/\\#Node/g' \
    -e 's/{_/{\\_/g' \
    $file > $tmp
cp $tmp $BUILDDIR/$name;
rm -f $tmp
