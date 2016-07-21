# fastdex
加快存在多个dex项目调试时的打包速度

## 适用场景
项目中有多个dex并且使用com.android.support:multidex加载dex。如果你忍受不了调试一次要好几分钟才能跑起来，fastdex可以帮助你加快apk生成过程

## 使用方式
- 1、关闭Instant Run功能(有多个dex时也不起作用)
     点击左上角Android studio -> Preferences -> Build,Execution,Deployment -> Instant Run -> Enable Instant Run......(把对勾去掉)
     
- 2、在root project下的build.gradle中添加依赖 
    classpath 'com.tong.tools.build:fastdex:1.0.4' ,详情如下
    ````
    buildscript {
        repositories {
            jcenter()
        }
        dependencies {
            classpath 'com.android.tools.build:gradle:2.1.2'
            classpath 'com.tong.tools.build:fastdex:1.0.4'
        }
    }
    ````
    
- 3、在app的项目中的build.gradle添加插件
    ````
    apply plugin: 'com.tong.fastdex'
    
    ````
- 4、第一次使用或者第三方库有变化时需要首先在app目录下执行
    ````
     ./gradlew cacheDex
    ````
    执行成功后app/build-fastdex目录下会出现缓存下来的dex文件；下次点击run或者执行assembleDebug会使用这几个文件
    
## 常见问题
* **为什么点击run的时候还是那么慢**
  - 确认是否把关闭掉了Instant Run
  - 确认是否执行了./gradlew cacheDex
  - 确认当前的Build Variants是否选择的是debug(目前只对debug做了处理)
 
* **为什么运行起来后，报找不到类直接crash**
  - **找不到的类是项目目录下的类:
    如果项目代码最外层包名和build.gradle中applicationId不一致需要做以下的配置 (例如app/build.gradle中applicationId是example.myapp2,但是代码的最外层的包是example.myapp，那么就要把rootPackage设置成example.myapp)
         
        fastdex {
            rootPackage = '项目代码最外层包名'
        }
             
     
         
     
  - **找不到的类是第三方库下的类:
     出现这种情况是因为app启动时在执行Multidex.install(Context context)前使用了第三方库的代码，由于其他几个dex还没有安装所以会找不到类，这时候需要把对应的库代码放在第一个dex中
         例如找不到的是support v4包里的内容，可以在项目目录下添加fastdex_keep_main_dex.txt，把配置写进去
         
         android.support.v4.**
         
         如果还有别的配置换行继续写
         android.support.v7.**
         
 
* **为什么添加了依赖后，执行起来找不到类**
  - 每次添加第三方库的依赖或者修改了版本号，需要重新执行
    ````
     ./gradlew cacheDex
    ````
  
## 实现原理
  gradle在执行transformClassesWithDexForDebug任务生成dex文件时会很慢(尤其是开启了multidex)，但是我们在开发中，修改的几乎全是项目代码，第三方库改动比较小。fastdex的原理就是预先把所有第三方库代码生成dex,
  当下次点击run或者assembleDebug时只会把项目目录下的代码生成dex，然后和预先生成的dex合并生成apk，这样即不影响调试，又能在生成dex的过程中省下了大量的时间。我用公司的项目做测试，使用后的打包速度是正常打包的1.5倍左右
  
  
  

 