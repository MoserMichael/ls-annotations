#!/bin/bash

set -x

add_tmp_to_readme() {
  cat tmp-file | sed -e 's/</\&lt;/g' | sed -e 's/>/\&gt;/g' >>README.md
}

make_usage() {

  cat <<EOF >>README.md
## Command line options
<pre>
EOF

  $JAVA_HOME/bin/java -jar ./ls-annotations/build/libs/ls-annotations.jar -h >tmp-file 2>&1
  add_tmp_to_readme

  cat <<EOF >>README.md

</pre>
EOF

}

function run_it {
  cmd="$1"
  help="$2"

  set -e
  $JAVA_HOME/bin/java -jar ./ls-annotations/build/libs/ls-annotations.jar $cmd >tmp-file 2>&1
  set +e

  error=$(grep -ic error tmp-file)
  if [[ "$error" != "0" ]]; then
    echo "has error"
    cat tmp-file
  fi

  size=$(stat -f '%z' tmp-file)
  if [[ "$size" == "0" ]]; then
    echo "Error: empty result"  
    exit 1
  fi

  cat <<EOF >>README.md  
    
<hr>
${help}
<details>
<summary>${help}</summary>
Command line: java -jar ls-annotations.jar ${cmd}

```java
EOF

  sed -i -e 's/<b>/@b@/g'      tmp-file
  sed -i -e 's/<\/b>/@\/b@/g'  tmp-file
  sed -i -e 's/</\&lt;/g'      tmp-file
  sed -i -e 's/>/\&gt;/g'      tmp-file
  sed -i -e 's/@b@/<b>/g'      tmp-file
  sed -i -e 's/@\/b@/<\/b>/g'  tmp-file
 #sed -i -e 's/^/    /g'       tmp-file
  cat tmp-file >>README.md

  cat <<EOF >>README.md
```
</details>
EOF
}

cp README.md.template README.md

make_usage

run_it "-l ./ls-annotations/build/libs/ls-annotations.jar" "show all annotations in jar file"

run_it "-l ./ls-annotations/build/classes" "show all annotations in classes within directory (recursive)"

run_it "-e @lsann.attrib.TestAttrib4 ./ls-annotations/build/classes" "show all annotation definitions that are used in definition of annotation @lsann.attrib.TestAttrib4"

run_it "-a @lsann.attrib.TestAttrib ./ls-annotations/build/classes" "show all annotation definitions that extend annotation @lsann.attrib.TestAttrib"

run_it "-u @lsann.attrib.TestAttrib4 ./ls-annotations/build/classes" "show all uses of @lsann.attrib.TestAttrib4 with highlight"

#LATEST_SPRING_JAR=$(find $HOME/.gradle/caches -name 'spring-context*.RELEASE.jar' -type f | perl -ne '/.*-(\d\.\d*.\d*).RELEASE.jar/ && print "$1 $_"'  | sort -n -k 1 | tail -1 | cut -d " " -f 2)

#run_it "-u @org.springframework.context.annotation.Bean ${LATEST_SPRING_JAR}" "show all spring bean types defined in ${LATEST_SPRING_JAR}"

run_it "-w @lsann.attrib.TestAttrib ./ls-annotations/build/classes" "show all uses of @lsann.attrib.TestAttrib4, including use of derived annotations "

run_it "-l ./ls-annotations/build/classes -d java.lang.Object" "show all derived classes of java.lang.Object"

run_it "-l ./ls-annotations/build/classes -b lsann.attrib.TestDerived2" "show all base classes of lsann.attrib.TestDerived2"

echo "** test ok **"
