# 『Android 开发艺术探索』

# 一 Activity 的生命周期和启动模式

主要包括以下内容：

- Activity 正常情况下的生命周期；
- Activity 异常退出情况下的生命周期；
- Activity 启动模式和各种 Flags；
- Intent 匹配过程

## 1.1 Activity 的生命周期

Activity 的生命周期分为两种情况，一种情况是典型情况下的生命周期；另一种情况是异常情况下的生命周期。异常情况下的生命周期是指 Activity 被系统回收或者当前设备的 Configuration 发生改变从而导致 Activity 被销毁重建。

### 1.1.1 典型情况下的生命周期

onCreate->onRestart->onStart-onResume->onPause->onStop->onDestroy

几种场景解析：

1. 针对某一特定的 Activity，第一次启动，生命周期为：onCreate->onStart->onResume；

2. 当用户打开新的 Activity 或者回到桌面，当前 Activity 的生命周期为：onPause->onStop。这里有一种特殊情况，如果新的 Activity 使用透明主题，不会回调 onStop；

3. 当用户回到原 Activity，生命周期为：onRestart->onStart->onResume；

4. 当用户按返回键（back）时，生命周期为：onPause->onStop->onDestroy；

几点说明：

1. 从整个生命周期来看，onCreate 和 onDestroy 是配对的，分别表示 Activity 的创建和销毁；从 Activity 是否可见来说，onStart 和 onStop 是配对的；从 Activity 是否在前台或者说是否可交互来说，onResume 和 onPause 是配对的。

2. 当启动一个新的 Activity ，旧 Activity 的 onPause 会先执行，然后才会启动新 Activity。举个例子，如果在 Activity A 启动 Acitivity B，那么生命周期为：A#onPause->B#onCreate->B#onStart->B#onResume->A#onStop。这点有什么指导意义呢？说明我们不能在 onPause 和 onStop 做耗时操作，尤其是 onPause，从而使新的 Activity 尽快显示到前台。


