# fastdex
加快存在多个dex项目调试时的打包速度

[![license](https://img.shields.io/hexpm/l/plug.svg)](https://raw.githubusercontent.com/typ0520/fastdex/master/LICENSE)

[ ![Download](https://api.bintray.com/packages/typ0520/maven/com.dx168.fastdex%3Agradle-plugin/images/download.svg) ](https://bintray.com/typ0520/maven/com.dx168.fastdex%3Agradle-plugin/_latestVersion)

## 适用场景
项目中有多个dex并且使用com.android.support:multidex加载dex。如果你忍受不了调试一次要好几分钟才能跑起来，fastdex可以帮助你加快apk生成过程

## 使用方式
- 1、关闭Instant Run功能
     点击左上角Android studio -> Preferences -> Build,Execution,Deployment -> Instant Run -> Enable Instant Run......(把对勾去掉)
     
- 2、在root project下的build.gradle中添加依赖 
    classpath 'com.dx168.fastdex:gradle-plugin:0.0.2-beta' ,详情如下
    ````
    buildscript {
        repositories {
            jcenter()
        }
        dependencies {
            classpath 'com.android.tools.build:gradle:2.1.2'
            classpath 'com.dx168.fastdex:gradle-plugin:0.0.2-beta'
        }
    }
    ````
    
- 3、在app的项目中的build.gradle添加插件
    ````
    apply plugin: 'com.dx168.fastdex'
    
    ````
  
## 实现原理
  gradle在执行transformClassesWithDexForDebug任务生成dex文件时会很慢(尤其是开启了multidex)，但是我们在开发中，修改的几乎全是项目代码，第三方库改动比较小。fastdex的原理就是预先把所有第三方库代码生成dex,
  当下次点击run或者assembleDebug时只会把项目目录下的代码生成dex，然后和预先生成的dex合并生成apk，这样即不影响调试，又能在生成dex的过程中省下了大量的时间。
  
##Thanks
[Instant Run](https://developer.android.com/studio/run/index.html#instant-run)

[Tinker](https://github.com/Tencent/tinker)

[Aceso](https://github.com/meili/Aceso)

[安卓App热补丁动态修复技术介绍](https://mp.weixin.qq.com/s?__biz=MzI1MTA1MzM2Nw==&mid=400118620&idx=1&sn=b4fdd5055731290eef12ad0d17f39d4a)

[Android应用程序资源的编译和打包过程分析](http://blog.csdn.net/luoshengyang/article/details/8744683)
  
  

 
