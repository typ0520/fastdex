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
  