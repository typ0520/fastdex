# fastdex
如果你忍受不了apk龟速的编译(尤其是项目中有多个dex)，fastdex可以帮助你加快apk生成过程

[![license](https://img.shields.io/hexpm/l/plug.svg)](https://raw.githubusercontent.com/typ0520/fastdex/master/LICENSE)

[ ![Download](https://api.bintray.com/packages/typ0520/maven/com.dx168.fastdex%3Agradle-plugin/images/download.svg) ](https://bintray.com/typ0520/maven/com.dx168.fastdex%3Agradle-plugin/_latestVersion)

[版本记录](https://raw.githubusercontent.com/typ0520/fastdex/master/CHANGELOG.md)

## 使用方式
- 1、关闭Instant Run功能
     点击左上角Android studio -> Preferences -> Build,Execution,Deployment -> Instant Run -> Enable Instant Run......(把对勾去掉)
     
- 2、在root project下的build.gradle中添加依赖 
    classpath 'com.dx168.fastdex:gradle-plugin:0.0.2-rc1' ,详情如下
    ````
    buildscript {
        repositories {
            jcenter()
        }
        dependencies {
            classpath 'com.android.tools.build:gradle:2.1.2'
            classpath 'com.dx168.fastdex:gradle-plugin:0.0.2-rc1'
        }
    }
    ````
    
- 3、在app的项目中的build.gradle添加插件
    ````
    apply plugin: 'com.dx168.fastdex'
    ````
  
## 实现原理
  gradle在执行transformClassesWithDexFor${variant}任务生成dex文件时会很慢(尤其是开启了multidex)，我们在开发中，修改的几乎全是项目代码，第三方库改动比较小。fastdex的原理就是预先把所有代码生成dex,
  当下次执行assemble任务时只会把项目目录下变化的代码生成dex，然后和缓存的dex合并生成apk，这样即不影响调试，又能在生成dex的过程中省下了大量的时间。

## 打包流程
###### 全量打包时的流程:
  - 1、合并所有的class文件生成一个jar包
  - 2、扫描所有的项目代码并且在构造方法里添加对com.dx168.fastdex.runtime.antilazyload.AntilazyLoad类的依赖
     这样做的目的是为了解决class verify的问题，
     详情请看https://mp.weixin.qq.com/s?__biz=MzI1MTA1MzM2Nw==&mid=400118620&idx=1&sn=b4fdd5055731290eef12ad0d17f39d4a
  - 3、对项目代码做快照，为了以后补丁打包时对比那些java文件发生了变化
  - 4、对当前项目的所以依赖做快照，为了以后补丁打包时对比依赖是否发生了变化，如果变化需要清除缓存
  - 5、调用真正的transform生成dex
  - 6、缓存生成的dex，并且把fastdex-runtime.dex插入到dex列表中，假如生成了两个dex，classes.dex classes2.dex 需要做一下操作
     fastdex-runtime.dex => classes.dex
     classes.dex         => classes2.dex
     classes2.dex        => classes3.dex
     然后运行期在入口Application(com.dx168.fastdex.runtime.FastdexApplication)使用MultiDex把所有的dex加载进来
  - 7、保存资源映射映射表，为了保持id的值一致，详情看
  @see com.dx168.fastdex.build.task.FastdexResourceIdTask


###### 补丁打包时的流程
  - 1、检查缓存的有效性
  - @see com.dx168.fastdex.build.task.FastdexCustomJavacTask 的prepareEnv方法说明
  - 2、合并所有变化的class并生成jar包
  - 3、生成补丁dex
  - 4、把所有的dex按照一定规律放在transformClassesWithMultidexlistFor${variantName}任务的输出目录
     fastdex-runtime.dex    => classes.dex
     patch.dex              => classes2.dex
     dex_cache.classes.dex  => classes3.dex
     dex_cache.classes2.dex => classes4.dex
     dex_cache.classesN.dex => classes(N + 2).dex
     
## Thanks
[Instant Run](https://developer.android.com/studio/run/index.html#instant-run)

[Tinker](https://github.com/Tencent/tinker)

[Aceso](https://github.com/meili/Aceso)

[安卓App热补丁动态修复技术介绍](https://mp.weixin.qq.com/s?__biz=MzI1MTA1MzM2Nw==&mid=400118620&idx=1&sn=b4fdd5055731290eef12ad0d17f39d4a)

[Android应用程序资源的编译和打包过程分析](http://blog.csdn.net/luoshengyang/article/details/8744683)
  
  

 
