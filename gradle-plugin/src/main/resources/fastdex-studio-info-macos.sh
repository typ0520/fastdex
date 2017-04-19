#!/bin/bash

#输入gradle进程id

#输出以下信息
#是否是在studio上触发的构建

#如果是在studio上触发的构建在获取以下信息
#android studio 是否开启了instant run
#android studio版本号

debug=0

function debug_log {
    if [ $debug != 0 ];then
        echo $@
    fi
}

gradle_pid=$1
if [ "${gradle_pid}" == "" ];then
    echo "please input gradle pid"
    exit -1
fi
ps_gradle_result=$(ps -ef | grep "${gradle_pid}" | head -1)
debug_log "ps_gradle_result: ${ps_gradle_result}"

echo $ps_gradle_result | grep "$0 ${gradle_pid}" > /dev/null
if [ $? == 0 ] || [ "${ps_gradle_result}" == "" ];then
    echo "process not found id: ${gradle_pid}"
    exit -1
fi

gradle_ppid=$(echo $ps_gradle_result | awk '{print $3}')

ps_studio_result=$(ps -ef | grep 'MacOS/studio' | head -1)
debug_log "ps_studio_result: ${ps_studio_result}"
echo $ps_studio_result | grep 'Contents/MacOS/studio' > /dev/null

from_studio=true
if [ $? != 0 ] || [ "${ps_studio_result}" == "" ];then
    from_studio=false

    echo "from_studio=${from_studio}"
    if [ "$2" != "" ];then
        echo 'from_studio=false' > $2
    fi
    exit 0
fi

studio_pid=$(echo $ps_studio_result | awk '{print $2}')
studio_home="$(echo $ps_studio_result | awk '{print $8}')"
temp_dir=$(echo $ps_studio_result | awk '{print $9}')
if [ "${temp_dir}" != "" ];then
    studio_home="${studio_home} ${temp_dir}"
fi

studio_home=${studio_home%/MacOS/studio*}
debug_log $studio_home

info_plist="${studio_home}/Info.plist"
debug_log $info_plist
line_num=$(cat -n "${info_plist}" | grep 'CFBundleShortVersionString' | awk '{print $1}')
let line_num=$line_num+1
debug_log $line_num
studio_version=$(sed -n "${line_num},${line_num}p" "${info_plist}")
studio_version=${studio_version#*<string>}
studio_version=${studio_version%</string>*}

if [ "${gradle_ppid}" != "${studio_pid}" ];then
    from_studio=false
fi

instant_run_disabled=false
instant_run_config="${HOME}/Library/Preferences/AndroidStudio2.2/options/instant-run.xml"
if [ -f "${instant_run_config}" ];then
    cat ${instant_run_config} | grep 'false' > /dev/null
    if [ $? == 0 ];then
        instant_run_disabled=true
    fi
fi

echo "from_studio=${from_studio}"
#echo "gradle_pid=${gradle_pid}"
#echo "gradle_ppid=${gradle_ppid}"
#echo "studio_pid=${studio_pid}"
echo "studio_home=${studio_home}"
echo "studio_version=${studio_version}"
echo "info_plist=${info_plist}"
echo "instant_run_disabled=${instant_run_disabled}"
echo "instant_run_config=${instant_run_config}"

if [ "$2" != "" ];then
    echo "from_studio=${from_studio}"                   > $2
    echo "gradle_pid=${gradle_pid}"                     >> $2
    echo "gradle_ppid=${gradle_ppid}"                   >> $2
    echo "studio_pid=${studio_pid}"                     >> $2
    echo "studio_home=${studio_home}"                   >> $2
    echo "studio_version=${studio_version}"             >> $2
    echo "info_plist=${info_plist}"                     >> $2
    echo "instant_run_disabled=${instant_run_disabled}" >> $2
    echo "instant_run_config=${instant_run_config}"     >> $2
fi

exit 0


