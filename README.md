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

## 1.2 Activity 启动模式

### 1.2.1 Activity 的 launchMode

在介绍 Activity 启动模式之前，需要先弄明白任务栈的概念。在默认情况下，当我们启动同一个 Activity 时，Android 会创建多个 Activity 实例，并把它们一一压入栈中，而当我们按 back 键时 Activity 就会一一出栈，当任务栈中没有任何 Activity 任务栈是典型的“后进先出”栈结构。Andriod 提供了 4 种 Activity 启动模式，分别为：standard、singleTop、singleTask 和 singleInstance。

#### standard

标准模式，这是系统默认的启动方式。每次启动 Activity 都会创建出一个新的实例，不管这个 Activity 的实例是否存在。在标准模式下，谁启动了这个 Activity，这个 Activity 就运行在启动它的 Activity 所在的任务栈中。例如：Activity A 启动了 Activity B，这里 B 的启动模式是标准模式，那么 B 就会进入到 A 所在的任务栈中。

这里，引申出一个问题。当使用 ApplicationContext 启动一个标准模式的 Activity 时，会抛出异常：

```
Calling startActivity() from outside of an Activity  context requires the  
FLAG_ACTIVITY_NEW_TASK flag. Is this really what you want?
```

原因很简单，因为 ApplicationContext 没有所谓的任务栈，当用它启动一个标准模式的 Activity 时，启动 Activity 找不到所属任务栈，因此抛出异常。解决的办法很简单，只需指定 Intent 标志位为 FLAG_ACTIVITY_NEW_TASK，这时待启动 Activity 的启动模式其实相当于是 singleTask。

#### singleTop

栈顶复用模式。在这种模式下，如果待启动 Activity 已经位于任务栈栈顶，此 Activity 不会重新创建，直接复用现有实例，并且回调 onNewIntent 提取当前请求信息。考虑另一种情况，如果待启动 Activity 已经存在于任务栈，但是不在栈顶，这时候还是会重新创建一个 Activity 实例。

#### singleTask

栈内复用模式。在介绍这种模式之前，需要先弄清楚什么是 Activity 目标任务栈？

Activity 目标任务栈是由 AndroidManifest 中 Activity 的 taskAffinity 指定的，如果不指定，默认为应用的包名。taskAffinity 指定的字符串就是该 Activity 目标任务栈。taskAffinity 必须和 singleTask 和 allowTaskReparenting 属性配对使用，单独使用的话没有意义。关于 allowTaskReparenting 属性比较抽象，简单的说，当一个 Activity 的 allowTaskReparenting 设置为 true时，它就具备任务栈转移的能力。举个例子：当应用 A 启动了应用 B 的一个 Activity（启动模式为 standard，taskAffinity为默认值，allowTaskReparenting 为 true），这个 Activity 会被压入 应用 A 所在任务栈中；接下来，当应用 B 启动后，此 Activity 会直接从应用 A 的任务栈转移到应用 B 的任务栈中。

栈内复用模式的情况下，只要待启动 Activity 在一个任务栈中存在，就不会重新创建 Activity，同样会回调 onNewIntent 方法。描述一下具体启动流程：首先，待启动 Activity 会在系统中寻找目标任务栈，如果这个任务栈中存在该 Activity 实例，则直接将 Activity 实例调到栈顶，并且把位于该 Activity 之上的所有 Activity 出栈，即 clearTop 效果，如果这个任务栈中不存在 Activity 实例，直接创建一个新的实例并且入栈；如果待启动 Activity 目标任务栈不存在，则系统会创建一个新的任务栈，并创建该 Activity 实例并入栈。

#### singleInstance

单例模式。单例模式除了具有 singleTask 的所有特性外，还具有一个特性，即单例模式 Activity 所在的任务栈中，有且只能有该 Activity 实例。

> 如果一个 Activity 同时在 AndroidManifest 文件和 Intent 中指定启动模式，以 Intent 为准。

**一个非常有用的命令**，可以用来打印任务栈信息：

```
adb shell dumpsys activity
```

### 1.2.2 Activity Flags

Activity 的 Flags 有很多，这里只列出日常开发中常用的几个。

#### FLAG_ACTIVITY_NEW_TASK

如果在 Intent 中指定这个标志位和在 AndroidManifest 中指定 Activity 启动模式为 singleTask 是一个效果。

#### FLAG_ACTIVITY_SINGLE_TOP

如果在 Intent 中指定这个标志位和在 AndroidManifest 中指定 Activity 启动模式为 singleTop 是一个效果。

#### FLAG_ACTIVITY_CLEAR_TOP

这个标志位一般会和 singleTask 一起出现，如果在 Intent 中指定这个标志位并且目标任务栈中已存在该 Activity 的实例，位于待启动 Activity 之上的所有 Activity 都会被出栈，并且回调 onNewIntent。 

有一种情况，当待启动 Activity 的启动模式是 standard，并且在 Intent 中使用了这个标志位，如果这时候待启动 Activity 已经在任务栈中，那么它连同它之上的所有 Activity 都会出栈，系统会创建出一个新的 Activity 入栈。

#### FLAG_ACTIVITY_EXCLUDE_FROM_RECENT

具有这个标志位的 Activity 不会出现在最近 Activity 列表中。如果我们不希望用户通过最近返回到我们的 Activity，可以使用这个标志位。

## 1.3 IntentFilter 匹配规则

启动 Activity 有两种方式，显示启动和隐式启动。显示启动的话很简单，直接指定待启动 Activity 的组件信息；隐式启动的话比较复杂，涉及到 IntentFilter 匹配过程，只有当 Intent 能够匹配目标组件 IntentFilter 设置的过滤信息时，才能启动该组件。IntentFilter 的过滤信息有三种类型：action、category 和 data。

### action 匹配规则

Intent 中的 action 必须和过滤规则中的 action 匹配。这里的匹配是指，只要 Intent 中的 action 和过滤规则中（过滤规则可以有多个 action）的其中一个 action 匹配即可。action 在这里的匹配时值的匹配，并且大小写敏感。

> 如果 Intent 中没有指定 action，则直接匹配失败。

### category 匹配规则

category 匹配要求，如果 Intent 中含有 category，那么 Intent 中每一个 category 都必须和过滤规则中的其中一个 category 匹配。

> Intent 可以不指定 category，如果不指定 category，在调用 startActivity 或者 startActivityForResult 时会自动为 Intent 加上一个 `android.intent.category.DEFAULT` 这个category。此时，为了使 Activity 能够接收隐式 Intent，需要在 IntentFilter 中加入 `android.intent.category.DEFAULT` 这个category。

### data 匹配规则

data 由两部分组成，mimeType 和 URI，它的匹配规则和 action 一样。

> 当为 Activity 指定 data 匹配规则时，可以只指定 mimeType，不指定 URI。因为，当没有指定 URI 时，会有一个默认值，默认值为 content 和 file。
> 为 Intent 指定完整 data 时，需要调用 setDataAndType 方法，不能通过调用 setData 和 setType 组合的方式，因为这两个方法在调用时会互相清除对方的值。

### 注意

如果一个 Intent 匹配 IntentFilter 失败，会有两种情况：一种是程序崩溃，例如 data 的 URI 匹配失败；另一种是没有隐式启动后没有任何效果。无论是哪一种情况，都影响到了正常的业务过程。因此，为了提高程序的健壮性，当我们隐式启动 Activity 时，应该先做一个判断。有两个方法可以选择：

- Intent#resolveActivity
- PackageManager#queryIntentActivities

```
public abstract ResolveInfo resolveActivity(Intent intent, int flags)
public abstract List<ResolveInfo> queryIntentActivities(Intent intent, int flags)
```

对于第二个参数，需要使用 MATCH_DEFAULT_ONLY 这个标志位，具体含义是仅仅匹配那些在 intent-filter 包含 `android.intent.category.DEFAULT` 的 Activity。这样就能保证，只要上述两个方法返回不是 null，startActivity 就一定能成功。因为不含有 `android.intent.category.DEFAULT` 的 Activity 是没办法接收隐式 Intent的。

另外，对于 Service 推荐使用显示启动。
