# View 的事件体系

主要包括以下内容：

- View 基础知识
- View 的滑动
- 弹性滑动
- View 的事件分发机制
- 解决滑动冲突

## 基础知识

Android 中 View 分为两大类：View 和 ViewGroup，两者都是继承自 View。

### View 的位置参数

View 的位置由它的四个顶点来决定，分别对应 View 的四个属性：top、left、right 和 bottom，这些坐标都是相对于父容器的坐标，是一种相对坐标。根据 View 的坐标，很容易得出 View 的宽和高：

```
width = right - left;
height = bottom - top;
```

给定一个 View，可以通过如下方法获取它的坐标信息：

```
Left = getLeft();
Right = getRight();
Top = getTop();
Bottom = getBottom();
```

此外 View 还有这几个参数：x、y、translationX 和 translationY，这几个参数也是相对于父容器的相对坐标。其中 x 和 y 是 View 的左上角坐标，translationX 和 translationY 是左上角坐标相对于父容器的偏移量。因此，很容易得出如下换算关系：

```
x = left + translationX;
y = top + translationY;
```

> translationX 和 translationY 默认值是 0。

### MotionEvent

从手指触碰 Android 屏幕的那一刻开始，就会产生一系列触碰事件，这一系列触碰事件称之为事件序列。一个事件序列中包含以下三种事件类型：

- ACTION_DOWN
手指刚触碰屏幕时触发。
- ACTION_MOVE
手指触碰屏幕后，在屏幕上移动时触发。
- ACTION_UP
手指离开屏幕后触发。

在触碰事件发生时，可以通过 MotionEvent 获取事件类型和坐标。事件坐标分为两种：相对坐标和屏幕坐标。

- 相对坐标
通过 getX() 和 getY() 获取，得到的是点击位置相对于当前 View 左上角的 x 和 y坐标。
- 屏幕坐标
通过 getRawX() 和 getRawY() 获取，得到的是点击位置相对于屏幕左上角的 x 和 y 坐标。

### TouchSlop

TouchSlop 是系统所能识别的滑动最小距离。当手指在屏幕上移动，如果移动的距离小于 TouchSlop，系统不认为这是滑动，只有当移动的距离大于 TouchSlop，系统才认为这是一次滑动操作。TouchSlop 的具体取值和设备有关，可以通过 ViewConfiguration.get(getContext).getScaledTouchSlop() 获得。

在日常开发中，可以使用 TouchSlop 做一些过滤。比如我们要实现一个包含滑动操作的自定义控件，当两次滑动事件的滑动距离小于 TouchSlop 时，当作不滑动来处理。TouchSlop 的默认值可以在 framework 源码中找到：

```
<!-- Base "touch slop" value used by ViewConfiguration as a
         movement threshold where scrolling should begin. -->
    <dimen name="config_viewConfigurationTouchSlop">8dp</dimen>
```

- VelocityTracker
- GestureDetector
- Scroller
