#!/bin/bash

if [ ! -d '.git' ];then
	echo "$(pwd) not a git repository"
	exit -1
fi
git status | grep 'nothing to commit' > /dev/null
if [ $? != 0 ];then
	echo "Cannot clean: You have unstaged changes."
	exit -1
fi

IFS=$'\n'

#prepare delete directory
PRE_DEL_DIR_ARR=('buildSrc' 'fastdex-build' 'fastdex')

#return: 1: yes 0: no
is_mapping() {
	if [ "$1" == '' ];then
		return 0
	fi
	for pattern in ${PRE_DEL_DIR_ARR[@]}
	do
		echo $1 | grep "^${pattern}" > /dev/null
		if [ $? == 0 ];then
			return 1
		fi
	done
	return 0
}

processed_count=0

for row in $(git rev-list --objects --all)
do
	obj_hash=$(echo ${row} | awk '{print $1}')
	relative_path=$(echo ${row} | awk '{print $2}')
	is_mapping ${relative_path}
	if [ $? == 1 ];then
		echo "process: ${relative_path}"
		#remove object
		git filter-branch --force --index-filter "git rm --cached --ignore-unmatch ${relative_path}" --prune-empty --tag-name-filter cat -- --all

		if [ $? == 0 ];then
			let processed_count=processed_count+1
		fi
	else
		if [ "${relative_path}" == '' ];then
			echo "relative path is empty,just ignore.  hash value: ${obj_hash}"
		else
			echo "skip ${relative_path}"
		fi

		if [ "${relative_path}" != '' ];then
			echo "skip ${relative_path}"
		fi
	fi
done


echo "processed count: ${processed_count}"

if [ $processed_count > 0 ];then
	if [ ! -f '.gitignore' ];then
		touch .gitignore
	fi

	echo '' >> .gitignore
	for pattern in ${PRE_DEL_DIR_ARR[@]}
		do
		echo .gitignore | grep "^${pattern}$" > /dev/null
		if [ $? != 0 ];then
			echo $pattern >> .gitignore
		fi
		echo .gitignore | grep "^${pattern}/$" > /dev/null
		if [ $? != 0 ];then
			echo "${pattern}/" >> .gitignore
		fi
	done

	echo ''
	echo '=========Execute the following command to complete the cleanup========='
	echo "git add ."
	echo "git commit -m 'clean git repository'"
	echo "git push origin --force --all"
	echo "git push origin --force --tags"
	echo '=================='
fi