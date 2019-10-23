# AutoField插件

*版本号：V1.0*

**本开源库包含四个部分：注解、APT、plugin和一个APP demo。**


![朴新](http://pxjy.com/images/logo.png "朴新")

开源库的使用APT、javassist动态生成代码实现，解决重复写activity、fragment数据传递和存储的代码痛点。

# 关于使用
Project目录下的build.gradle：
~~~gradle
buildscript {
    repositories {
        ...
        maven { url 'http://test.nexus.pxjy.com/repository/maven-releases/' }
    }
    dependencies {
        ...
        classpath "com.pxjy.plugin:auto-field:1.1"
    }
}
allprojects {
    repositories {
        ...
        maven { url 'http://test.nexus.pxjy.com/repository/maven-releases/' }
    }
}
~~~
Module目录下的build.gradle：
~~~gradle
apply plugin: 'auto-field'
~~~


## 在field使用

~~~kotlin
    @AutoField
    var a = 0
    @AutoField
    var b = false
    @AutoField("自定义key")
    lateinit var list: ArrayList<String>
~~~~


## 构建Main2Activity的intent

~~~kotlin
    val intent = IntentMain2ActivityBuilder(activity)
        .setB(true)
        .setC(1)
        .setList(arrayListOf("A", "B"))
        .build()
    intent.putStringArrayListExtra("SadaA", list)
    activity.startActivity(intent)
~~~~


## 构建Fragment的Bundle

~~~java
    public static WorkFragment newInstance(boolean isMove, Date ctime, String cid) {
        WorkFragment fragment = new WorkFragment();
        fragment.setArguments(new BundleWorkFragmentBuilder()
                .setCid(cid)
                .setCtime(ctime)
                .setIsMove(isMove)
                .build());
        return fragment;
    }
~~~~

**一切OK，对于赋值和存储的代码生成交给插件吧，就这么简单。**
