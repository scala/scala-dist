#!/bin/sh

for file in $@; do
  name=tour/`basename $file`;
  #echo name=$name;
  awk '/<src/||/<pre/{on=1};/<\/src>/||/<\/pre>/{off=1};{if(on){gsub("{","\\{");gsub("}","\\}")};if(off){on=0;off=0};print $0}' \
      $file > $BUILDDIR/$name;
done
