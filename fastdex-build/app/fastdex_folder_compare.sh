#!/bin/bash

#sh compare.sh src/main/java build-fastdex/java
# 对比文件夹的变化执行结果如下(diff -qr build-fastdex/java src/main/java | grep '\.java')
# Only in src/main/java/com/dx168/epmyg/activity: AboutWebviewActivity.java
# Only in build-fastdex/java/com/dx168/epmyg/activity: BindPhoneActivity.java
# Files build-fastdex/java/com/dx168/epmyg/activity/BuyActivity.java and src/main/java/com/dx168/epmyg/activity/BuyActivity.java differ

#
#内容有变化的会被列出来
#dir1中新增的文件也会被列出来
#

IFS=$'\n'
dir1=$1
dir2=$2
for item in $(diff -qr ${dir1} ${dir2} | grep '\.java')
do 
	desc=$(echo ${item} | awk '{print $1}')
	if [ ${desc} == 'Files' ] ;then
		res=$(echo ${item} | awk '{print $2}')
		echo ${res##*src/main/java/}
	fi

	if [ ${desc} == 'Only' ]; then
		dir=$(echo ${item} | awk '{print $3}')
		filename=$(echo ${item} | awk '{print $4}')
		if [[ ${dir} =~ ${dir1} ]];then
			res="${dir%:}/${filename}"
			echo ${res##*src/main/java/}
		fi
	fi
done
