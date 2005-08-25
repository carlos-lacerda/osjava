# DIR should be the location of the checked out svn.osjava.org/site/dist/ directory
DIR=$1
OUTPUT=$1/releases/multidoc-jnr

cp header-index-template.html $OUTPUT/index.html

for i in `ls -1d $OUTPUT/*/ | sort -f`
do
  i=`echo $i | sed 's/\/$//'`
  countI=`echo $i | wc -c`
  prettyI=`echo $i | sed 's/.*\///'`

  cp header-template.html $i/versions.html

  for j in `ls -1d $i/*/ | sort -dr`
  do
    j=`echo $j | sed 's/\/$//'`
    v=`echo $j | sed 's/.*\///'`
    if [ -f $j/overview-frame.html ];
    then
        u="'packageListFrame', '$v/overview-frame.html'";
    else
        f=`find $j -type f -name 'package-frame.html' | sed 's/[^\/]*\///'`;
        f=`echo $f | cut -c ${countI}-`
        u="'packageListFrame', '$f'";
    fi

    u="$u, 'packageFrame','$v/allclasses-frame.html'";

    if [ -f $j/overview-summary.html ];
    then
        u="$u, 'classFrame', '$v/overview-summary.html'";
    else
        f=`find $j -type f -name 'package-summary.html' | sed 's/[^\/]*\///'`;
        f=`echo $f | cut -c ${countI}-`
        u="$u, 'classFrame', '$f'";
    fi

    if [ $lastV ]
    then
      group=$prettyI
      initials=`echo $prettyI | cut  -c-2`
      if [ $initials = 'gj' ]
      then
        group='genjava'
      fi
      new=$DIR/maven/$group/jars/$prettyI-$lastV.jar
      old=$DIR/maven/$group/jars/$prettyI-$v.jar
      versionfile=$i/diff-report-${lastV}_${v}.html
      if [ -f $old -a -f $new ]
      then
        echo "<table>" > $versionfile
        java -jar clirr-core-0.5-uber.jar -o $old -n $new 2>&1 | sed 's/[^:]*:[^:]*: //' | sed 's/^/<tr><td>/' | sed 's/:/<\/td><td>/' | sed 's/$/<\/td><\/tr>/' >> $versionfile
        echo "</table>" >> $versionfile
        echo "<font class=\"FrameItemFont\"><a href=\"diff-report-${lastV}_${v}.html\" target="classFrame">(diff)</a></font>" >> $i/versions.html
      fi
    fi

    echo "<nobr><a name=\"$v\"><font class=\"FrameItemFont\"><a href=\"javascript:load($u)\">$prettyI $v</a></font></nobr><br/>" >> $i/versions.html

    lastV=$v
  done

  unset lastV

  echo '</body></html>' >> $i/versions.html

  cp blank-template.html $i/blank.html
  cp index-template.html $i/index.html

  echo "<li><a href=\"$prettyI/index.html\">$prettyI</a></li>" >> $OUTPUT/index.html

done

cat footer-index-template.html >> $OUTPUT/index.html
