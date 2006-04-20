#!/bin/sh

for file in $@; do
  name=`basename $file`;
  [ $name = "index.xml" ] || name=intro/$name;
  #echo name=$name;
  awk '/<src/{on=1};/<\/src>/{off=1};{if(on){gsub("{","\\{");gsub("}","\\}")};if(off){on=0;off=0};print $0}' \
      $file > $BUILDDIR/$name;
done
