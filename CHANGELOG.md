## 0.6.0  (2017-9-27)

Features:

  - 全面优化全量打包逻辑，如果>=2.3.0会在所有的build-type上触发dex缓存
  - 补丁打包时不在往dex输出目录里复制dex

## 0.5.2  (2017-9-25)

Bugfixes:

   - 修复adb连接的错误

## 0.5.0  (2017-9-15)

Features:

  - 支持dataBinding

## 0.4.2  (2017-9-14)

Bugfixes:

   - 修复发送了补丁dex后有时候没有触发重启app的问题
   - 修复依赖的library工程如果没有res目录时没有R.java所造成的错误
   - 修复2.0.0-2.1.3与ddmlib冲突的问题
   - 修复只有资源发生变化时资源版本没有重置的问题

## 0.4.1  (2017-9-11)

Features:

  - 支持多个设备连接的场景(./gradlew fastdexDebug -PDEVICE_SN=adb设备序列号)
  - 执行fastdex任务时先尝试拉起app
  - 报IncompatibleClassChangeError错误时清除补丁

Bugfixes:

   - 解决全量打包时如果没有连接设备报空指针的问题
   - 修复mac/linux下项目路径包含空格会报错的问题

## 0.3.3  (2017-8-31)

Bugfixes:

   - 修复runtime中复制marged-patch.dex有时会出错的问题
   - 修复基准包不一致时没有触发安装的bug

## 0.3.2  (2017-8-30)

Features:

  - 适配apt(butterknife、......),可以把useCustomCompile打开了
  - R和BuildConfig文件比对换成md5
  - 检测manifest文件的变化，如果变化本次编译不允许走免安装
  - 重构免安装模块
  - 调用javac和dx命令时输入日志内容

Bugfixes:

   - 修复runtime中空指针的问题
   - 修复项目快照丢失r文件映射的问题
   - 修复dex加载顺序错误的问题
   - 修复加载resource.ap_文件路径错误的问题
   - fix [issue#51](https://github.com/typ0520/fastdex/issues/51)
   - 解决开启customCompile时，修改library代码报类重复的错误
   - 修复免安装和全量安装混用时，加载过期补丁dex和资源的问题

Warn:

   - 默认把useCustomCompile设置为true了

## 0.2.0 (2017-8-17)

Features:

  - 支持kotlin

Bugfixes:

   - 修复butterknife 8.2.0以后的版本生成的class文件名变成_ViewBinding*.class的问题


## 0.1.13 (2017-8-16)

Bugfixes:

   - 修复dex loader顺序错误的逻辑
   - 杀掉应用进程修改成adb shell am force-stop 包名
   
## 0.1.11 (2017-7-18)

Bugfixes:

   - 修复flavor的源码目录没有正确扫描的问题
   
## 0.1.9 (2017-6-10)
   
Features:
   
  - 支持免安装功能
      
## 0.1.6 (2017-4-27)
 
Bugfixes:
 
   - 修复资源处理bug

## 0.1.5 (2017-4-25)

Bugfixes:

   - 修复开启multidex但丢失jarMerging任务时的容错处理([issue#24](https://github.com/typ0520/fastdex/issues/24) [issue#29](https://github.com/typ0520/fastdex/issues/29) [issue#35](https://github.com/typ0520/fastdex/issues/35) [issue#36](https://github.com/typ0520/fastdex/issues/36))

## 0.1.4 (2017-4-19)

Bugfixes:

   - 修复工程移动时恢复缓存出错的问题

Features:

   - 完善直接依赖的library工程的增量构建
   - mac系统下错误报告增加studio版本号、是否是点击run启动的构建、是否开启instant run等信息


## 0.1.1 (2017-4-17)

Bugfixes:

   - [fix-issue#32](https://github.com/typ0520/fastdex/issues/32)

Features:

   - 打包出错时生成错误日志方便大家提issue

## 0.1.0 (2017-4-16)

Bugfixes:

   - [fix-issue#30](https://github.com/typ0520/fastdex/issues/30)

Features:

   - 增加对直接依赖工程(包括间接依赖)的代码增量编译支持

## 0.0.9 (2017-4-14)

Bugfixes:

   - 修复customJavacTask没有正确依赖generateSources任务的问题

## 0.0.7 (2017-4-13)

Features:

   - 增加对R.java和BuildConfig.java增量编译的支持
   - 优化全量打包注入逻辑，忽略掉对第三方库R文件的注入


## 0.0.6 (2017-4-12)

Features:

   - 增加dex merge功能(随着变化的java文件的增多,补丁打包会越来越慢,dex merge以后当前的状态相当于全量打包以后的状态)

## 0.0.5-beta1  (2017-4-10)

Features:

   - 重写快照对比模块
   - 自定义编译任务支持加retrolambda插件使用lambda的场景

## 0.0.4-beta  (2017-3-30)

Features:

   - 增加fastdexEnable配置默认开启fastdex
   - hook掉了jar merging逻辑，补丁打包过程性能大幅度提升
   - 优化了补丁dex生成逻辑，在大部分的android gradle版本下使用标准的dex生成方式


## 0.0.3-beta7 (2017-3-28)

Bugfixes:

   - fix issue#8,android gradle2.3.0获取dex输出目录api变了，导致runtime.dex没有放进去而造成类找不到的错误


## 0.0.3-beta6 (2017-3-27)

Bugfixes:

   - fix issue#14,issue#15,存在同名的方法参数类型不一样时传null出错的问题

## 0.0.3-beta4 (2017-3-24)

Bugfixes:

   - fix issue#6,class name (*) does not match path (*)的问题
   - 解决全量打包后dex重复copy的问题
   
## 0.0.3-beta2 (2017-3-21)

Bugfixes:

   - fix issue#8,解决与tinkerpatch插件的冲突

Features:

   - 为提高稳定性,默认关闭掉自定义的compile任务,如果想使用增加了一个配置项useCustomCompile=true

   - 在重要节点添加日志方便以后排错   
   
## 0.0.3-beta2 (2017-3-21)

Bugfixes:

   - 修改通过useCustomCompile关闭自定义编译任务后,造成检查环境不执行的问题
   - fix issue#4,编译报编码GBK的不可映射字符的问题   
   
## 0.0.2-rc5 (2017-3-19)

Bugfixes:

  - fix issue#2 解决在activity中getApplication()强转失败的问题
  - fix issue#3 windows路径不能盘符加:的问题
  
## 0.0.2-rc1 (2017-3-16)

Bugfixes:

  - 修复buildType中有大写字母报错的问题
  - 修复没有注入app/build/generated/source/apt的bug
  - 修复获取依赖不完整的问题

Features:

  - 适配多个flavor的场景     


## 0.0.2-beta2 (2017-3-15)

Bugfixes:

  - 修复注入代码时仅注入默认构造方法的问题

   
## 0.0.2-beta1 (2017-3-14)

Bugfixes:

  - 修复动态生成的代码没有注入的问题

Features:

  - 支持自定义的compileJava任务
  
