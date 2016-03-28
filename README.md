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

- 针对某一特定的 Activity，第一次启动，生命周期为：onCreate->onStart->onResume；

- 当用户打开新的 Activity 或者回到桌面，当前 Activity 的生命周期为：onPause->onStop。这里有一种特殊情况，如果新的 Activity 使用透明主题，不会回调 onStop；

- 当用户回到原 Activity，生命周期为：onRestart->onStart->onResume；

- 当用户按返回键（back）时，生命周期为：onPause->onStop->onDestroy；

几点说明：

- 从整个生命周期来看，onCreate 和 onDestroy 是配对的，分别表示 Activity 的创建和销毁；从 Activity 是否可见来说，onStart 和 onStop 是配对的；从 Activity 是否在前台或者说是否可交互来说，onResume 和 onPause 是配对的。

- 当启动一个新的 Activity ，旧 Activity 的 onPause 会先执行，然后才会启动新 Activity。举个例子，如果在 Activity A 启动 Acitivity B，那么生命周期为：A#onPause->B#onCreate->B#onStart->B#onResume->A#onStop。这点有什么指导意义呢？说明我们不能在 onPause 和 onStop 做耗时操作，尤其是 onPause，从而使新的 Activity 尽快显示到前台。

### 1.1.2 异常情况下的生命周期

当系统配置发生变化或者系统资源不足回收 Activity，都会导致 Activity 重建，这种情况下的生命周期可以归为异常情况下的生命周期。

#### 配置发生变化

onPause->onStop->onDestroy。此时由于 Activity 是在异常情况下终止的，还会调用 onSaveInstantState 来保存当前 Activity 状态；当 Activity 重建时，会调用 onRestoreInstanceState 恢复销毁时的 Activity 状态。

有一点需要说明，onSaveInstantState 的调用发生在 onStop 之前，但和 onPause 没有固定的先后关系，可能在 onPause 之前，也可能在 onPause 之后，和具体的 API 版本有关。

> Prior to Honeycomb, activities were not considered killable until after they had been paused, meaning that onSaveInstanceState() was called immediately before onPause(). 

这点有什么意义呢？如果使用 Fragment 做事务操作时，出现如下错误：

```
java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
    at android.support.v4.app.FragmentManagerImpl.checkStateLoss(FragmentManager.java:1341)
    at android.support.v4.app.FragmentManagerImpl.enqueueAction(FragmentManager.java:1352)
    at android.support.v4.app.BackStackRecord.commitInternal(BackStackRecord.java:595)
    at android.support.v4.app.BackStackRecord.commit(BackStackRecord.java:574)
```

那么，就和 onSaveInstantState 在不同 Android 版本，调用的时机具有差异性有关了。具体原因和解决方法可以参考 [Fragment Transactions & Activity State Loss](http://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html)。

当我们在 onSaveInstanceState 中保存了 Activity 状态，Activity 重建时可以从 onCreate 或者 onRestoreInstanceState 恢复之前的状态。二者的区别是，onCreate 中的 Bundle 可能为空，恢复时需要做非空校验；而 onRestoreInstanceState 一旦被调用，它的 Bundle 一定是非空的，最佳实践是通过 onRestoreInstanceState 恢复。

#### 系统资源不足导致 Activity 被回收

这个在正常交互过程中比较难重现，而且低端机型出现的概率远远高于高端机型。这里提供一种模拟方式：

- 打开应用 A，进入首页；
- 进入设置->开发者选项->应用->后台进程限制，选择不允许后台进程；
- 回到桌面，或者通过最近任务切换到应用 B；
- 重新打开 A。

当执行上述步骤 3 时，应用 A 就相当于被系统回收了，即应用 A 进程被直接杀死。可以用这种方式来模拟应用 A 由于系统资源不足被回收的场景。

如果我们实现当系统配置发生变化时 Activity 不重建的效果呢？这时候就需要给 Activity 指定 configChanges。例如，我们希望发生转屏时不重建 Activity，可以配置 configChanges 如下：

```
android:configChanges="orientation|screenSize"
```

这样，Activity 就不会重建，也没有调用 onSaveInstanceState 和 onRestoreInstanceState，而是调用了 onConfigurationChanged 方法。
