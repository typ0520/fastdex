#!/bin/bash

#寻找包含Build Variants的idea 插件在那个jar包里
#https://android.googlesource.com/platform/tools/adt/idea

cd /Applications/Android\ Studio.app/Contents
for jar in $(find . | grep .jar$);do
	unzip -l $jar | grep META-INF/plugin.xml

	if [ $? == 0 ];then
		if [ -d TEMP-META-INF ];then
			rm -rf TEMP-META-INF
		fi
		echo "scan idea plugin jar: ${jar}"

		unzip $jar "META-INF/*.xml" -d TEMP-META-INF/ > /dev/null

		for xml in $(ls TEMP-META-INF/META-INF/ | grep .xml$);do
			echo "scan xml: ${xml}"

			cat TEMP-META-INF/META-INF/${xml} | grep 'Build Variants' > /dev/null
			if [ $? == 0 ];then
				echo "find it /Applications/Android\ Studio.app/Contents/TEMP-META-INF/META-INF/${xml}"
				echo "find it /Applications/Android\ Studio.app/Contents/${jar}"
				exit 0
			fi
		done
	fi
done