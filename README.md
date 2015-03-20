# Flint Receiver SDK(Java version)

标签（空格分隔）： Flint,SDK

---
包含以下内容：

1. flingd/flint-jni：flint service库以及测试程序。
a. Flint.java：与Flint service的接口，包含启动／停止Flint service，获得当前运行状态以及启动／关闭应用等接口。
b. libflint-android.so：Flint service库
c. MainActivity.java: jni测试程序,示范了各个接口的使用。

2. receiver/：receiver应用程序，使用了Java Receiver SDK ，Vitamio以及Crosswalk。
a. SimpleMediaPlayerActivity.java:采用Android标准MediaPlayer播放的Flint媒体类receiver应用。
b. MediaPlayerActivity.java: 使用vitamio VideoView控件播放视频的Flint媒体类receiver应用。
c. FlintContainerActivity.java: 使用web(crosswalk)做为容器以运行所有Flint应用(部分多媒体应用使用"a","b"方式)。

3. sdk/：Flint Java Receiver SDK，主要解析Flint sender端发送过来的各种媒体控制消息。

4. sender/：sender端测试程序，可以使用该测试程序来验证Java Receiver SDK，receiver端Flint service以及receiver程序。

---
参考：
1.https://crosswalk-project.org
2.https://www.vitamio.org/en/

