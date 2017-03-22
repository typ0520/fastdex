# fastdex
如果你忍受不了apk龟速的编译(尤其是项目中有多个dex)，fastdex可以帮助你加快apk生成过程

[![license](https://img.shields.io/hexpm/l/plug.svg)](https://raw.githubusercontent.com/typ0520/fastdex/master/LICENSE)

[ ![Download](https://api.bintray.com/packages/typ0520/maven/com.dx168.fastdex%3Agradle-plugin/images/download.svg) ](https://bintray.com/typ0520/maven/com.dx168.fastdex%3Agradle-plugin/_latestVersion)

[版本记录](https://raw.githubusercontent.com/typ0520/fastdex/master/CHANGELOG.md)

## 使用方式
- 1、关闭Instant Run功能
     点击左上角Android studio -> Preferences -> Build,Execution,Deployment -> Instant Run -> Enable Instant Run......(把对勾去掉)
     
- 2、在root project下的build.gradle中添加依赖 
    classpath 'com.dx168.fastdex:gradle-plugin:latest.release' ,详情如下
    ````
    buildscript {
        repositories {
            jcenter()
        }
        dependencies {
            classpath 'com.android.tools.build:gradle:2.1.2'
            classpath 'com.dx168.fastdex:gradle-plugin:latest.release'
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
  
  [详情](http://www.jianshu.com/p/53923d8f241c)

## 注意事项

- 1、不要把fastdex打出来的包用在生产环境，因为fastdex打出来的包项目所有的代码都在第二个dex后面，会造成5.0以
    下机器首次运行比较慢(如果是本地调试就无所谓了)；当打包生产环境apk时注释掉加入插件的代码
    //apply plugin: 'com.dx168.fastdex'
    
- 2、fastdex会忽略开启混淆的buildType

- 3、开启自定义的编译任务能获得更快的构建速度，对使用了butterknife的大型项目效果最明显，这一特性目前还不稳定0.0.3-beta3后默认关闭了，如果想尝试在build.gradle中加入下面配置
 
    ````
     fastdex {
          useCustomCompile = true
     }
     
    ````

## 打包流程
##### 全量打包时的流程:
  - 1、合并所有的class文件生成一个jar包
  - 2、扫描所有的项目代码并且在构造方法里添加对com.dx168.fastdex.runtime.antilazyload.AntilazyLoad类的依赖
     这样做的目的是为了解决class verify的问题，
     详情请看 [安卓App热补丁动态修复技术介绍](https://mp.weixin.qq.com/s?__biz=MzI1MTA1MzM2Nw==&mid=400118620&idx=1&sn=b4fdd5055731290eef12ad0d17f39d4a)
  - 3、对项目代码做快照，为了以后补丁打包时对比那些java文件发生了变化
  - 4、对当前项目的所以依赖做快照，为了以后补丁打包时对比依赖是否发生了变化，如果变化需要清除缓存
  - 5、调用真正的transform生成dex
  - 6、缓存生成的dex，并且把fastdex-runtime.dex插入到dex列表中，假如生成了两个dex，classes.dex classes2.dex 需要做一下操作
     fastdex-runtime.dex => classes.dex
     classes.dex         => classes2.dex
     classes2.dex        => classes3.dex
     然后运行期在入口Application(com.dx168.fastdex.runtime.FastdexApplication)使用MultiDex把所有的dex加载进来
  - @see [com.dx168.fastdex.build.transform.FastdexTransform](https://github.com/typ0520/fastdex/blob/master/buildSrc/src/main/groovy/com/dx168/fastdex/build/transform/FastdexTransform.groovy)
  - 7、保存资源映射表，为了保持id的值一致，详情看
  - @see [com.dx168.fastdex.build.task.FastdexResourceIdTask](https://github.com/typ0520/fastdex/blob/master/buildSrc/src/main/groovy/com/dx168/fastdex/build/task/FastdexResourceIdTask.groovy)


##### 补丁打包时的流程
  - 1、检查缓存的有效性
  - @see [com.dx168.fastdex.build.task.FastdexCustomJavacTask](https://github.com/typ0520/fastdex/blob/master/buildSrc/src/main/groovy/com/dx168/fastdex/build/task/FastdexCustomJavacTask.groovy) 的prepareEnv方法说明
  - 2、扫描所有变化的java文件并编译成class
  - @see [com.dx168.fastdex.build.task.FastdexCustomJavacTask](https://github.com/typ0520/fastdex/blob/master/buildSrc/src/main/groovy/com/dx168/fastdex/build/task/FastdexCustomJavacTask.groovy)
  - 3、合并所有变化的class并生成jar包
  - 4、生成补丁dex
  - 5、把所有的dex按照一定规律放在transformClassesWithMultidexlistFor${variantName}任务的输出目录
     fastdex-runtime.dex    => classes.dex
     patch.dex              => classes2.dex
     dex_cache.classes.dex  => classes3.dex
     dex_cache.classes2.dex => classes4.dex
     dex_cache.classesN.dex => classes(N + 2).dex

     
## 注意事项

- 1、不要把fastdex打出来的包用在生产环境，因为fastdex打出来的包项目所有的代码都在第二个dex后面，会造成5.0以
    下机器首次运行比较慢(如果是本地调试就无所谓了)；当打包生产环境apk时注释掉加入插件的代码
    //apply plugin: 'com.dx168.fastdex'
    
- 2、fastdex会忽略开启混淆的buildType

- 3、如果使用了retrolambda,需要关掉自定义的编译任务
     fastdex {
          useCustomCompile = false
     }
     

## 后续的优化计划

- 1、提高稳定性和容错性，这个是最关键的
- 2、目前补丁打包的时候，是把没有变化的类从app/build/intermediates/transforms/jarMerging/debug/jars/1/1f/combined.jar中移除，如果能hook掉transformClassesWithJarMergingForDebug这个任务，仅把发生变化的class参与combined.jar的生成，能够在IO上省出很多的时间
- 3、目前给项目源码目录做快照，使用的是文件copy的方式，如果能仅仅只把需要的信息写在文本文件里，能够在IO上省出一些时间
- 4、目前还没有对libs目录中发生变化做监控，后续需要补上这一块
- 5、apk的安装速度比较慢(尤其是ART下，安装时对应用的AOT编译，具体请参考张邵文大神的文章[Android N混合编译与对热补丁影响解析](http://mp.weixin.qq.com/s?__biz=MzAwNDY1ODY2OQ==&mid=2649286341&idx=1&sn=054d595af6e824cbe4edd79427fc2706&scene=1&srcid=0811uOHr2RBQDKF0jKEdL4Vc##))，通过socket把代码补丁和资源补丁发送给app，做到免安装

## Thanks
[Instant Run](https://developer.android.com/studio/run/index.html#instant-run)

[Tinker](https://github.com/Tencent/tinker)

[安卓App热补丁动态修复技术介绍](https://mp.weixin.qq.com/s?__biz=MzI1MTA1MzM2Nw==&mid=400118620&idx=1&sn=b4fdd5055731290eef12ad0d17f39d4a)

[Android应用程序资源的编译和打包过程分析](http://blog.csdn.net/luoshengyang/article/details/8744683)
  
  

 
