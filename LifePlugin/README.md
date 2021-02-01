##开启开发模式
- 将`EnvUtils.java`中`isDev()`改为`true` 否则无法打印log

##修改Build.gradle
将`Build.gradle`改为以下代码 发布到本地 方便调试

```groovy
apply plugin: 'groovy'
apply plugin: 'maven'


dependencies {
    implementation 'com.android.tools.build:transform-api:1.5.0'
    implementation gradleApi()
    implementation localGroovy()
    implementation 'com.android.tools.build:gradle:4.0.2'
    implementation 'org.ow2.asm:asm:8.0.1'
}

compileGroovy {
    sourceCompatibility = 1.8
    targetCompatibility = 1.8
}

uploadArchives{
    repositories {
        mavenDeployer{
            pom.groupId = 'com.dsg.life'
            pom.artifactId = 'lifeCyclePlugin'
            pom.version = "1.0.0"
            repository(url:uri('../release'))
        }
    }
}

```

想要调试plugin可以参考这篇文章 [欢迎点赞加关注哦✈️](https://www.jianshu.com/p/4f8f07ff3e42)