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

### VelocityTracker

速度追踪器，用于追踪手指在屏幕上的滑动速度，这里的滑动速度应该理解成数学里的矢量速度，即包括水平方向的速度和竖直方向的速度。使用方法如下，首先是获取一个速度追踪器，添加需要追踪的事件：

```
VelocityTracker tracker = VelocityTracker.obtain();
tracker.addMovement(event);
```

接下来，通过 tracker 计算速度，然后分别获取水平速度和竖直速度：

```
tracker.computeCurrentVelocity(1000);
int vx = tracker.getXVelocity();
int vy = tracker.getYVelocity(); 
```

上述代码片段就是计算 1000ms 内，手指在屏幕水平方向和竖直方向所滑过像素点数。速度的计算公式如下：

速度 = （终点位置 - 起始位置）/ 时间段

根据屏幕坐标系的正方向可以得出：当手指沿着坐标系正方向滑动时，速度为正数；当手指逆着坐标系正方向滑动时，速度为负数。

最后，当需要清空 VelocityTracker 数据时，调用 VelocityTracker#clear() 方法；当不再需要 VelocityTracker 时，调用 VelocityTracker#recycle() 进行释放。

### GestureDetector

可以使用 GestureDetector 进行用户操作的手势检测。常见的包括单击、滑动、快速滑动、长按、双击等行为。使用方法是，首先创建一个 GestureDetector 实例，并实现 OnGestureListener 或者 onDoubleTapListener 接口：

```
GestureDetector mDetector = new GestureDetector(this);
```

> GestureDetector#setIsLongpressEnabled(false) 可以解决长按屏幕后无法拖动的现象。

接下来，接管 View#onTouchEvent 方法：

```
boolean comsumed = mDetector.onTouchEvent(event);
return comsumed;
```

当然 GestureDetector 只是为我们提供一种手势检测的简便方法，我们完全可以在 View#onTouchEvent 中实现想要的手势检测。具体采用哪种方式实现，取决于具体需求，这里给出一个准则：如果只是想监听滑动相关的事件，直接在 View#onTouchEvent 实现即可；如果需要监听类似双击这类事件的话，可以使用 GestureDetector。

## View 的滑动

实现 View 的滑动有三种方法：

- 使用 View#scrollTo/scrollBy 方法
- 使用动画
- 通过改变 View 的 LayoutParams 使 View 重新布局，达到滑动的效果

### 使用 scrollTo/scrollBy 方法

scrollTo 实现 View 的绝对滑动，scrollBy 实现 View 的相对滑动。从源码看，scrollBy 本质上也是通过调用 scrollTo 实现的。要理解 View 的滑动，首先要明白什么是 mScrollX？什么是 mScrollY？

- mScrollX
mScrollX 可以通过 View#getScrollX() 获得，它的值等于 View 左边缘和 View 内容左边缘在水平方向上的距离。
- mScrollY
mScrollY 可以通过 View#getScrollY() 获得，它的值等于 View 上边缘和 View 内容上边缘在竖直方向上的距离。

简单地说，如果从左向右滑动，mScrollX 为负值；如果从右向左滑动，mScrollX 为正值；如果从上向下滑动，mScrollY 为负值，如果从下向上滑动，mScrollY 为正值。

> 使用 scrollTo/scrollBy 方法实现 View 的滑动，只能将 View 的内容进行移动，并不能对 View 本身进行移动。

### 使用动画

Android 中的动画可以分为帧动画、View 动画和属性动画。其中 View 动画只是对 View 的影响做操作，动画结束后 View 的位置和属性并不会发生变化。例如：对一个按钮使用 View 动画，当动画结束后，按钮的点击失效。从 Android 3.0 开始引入的属性动画就没有这个问题，因为属性动画是通过反射直接操作 View 的属性。

使用动画实现 View 的滑动效果，是通过对 View 的 translationX 和 translationY 做动画实现的。由于 translationX 和 translationY 是左上角坐标相对于父容器的偏移量，因此是对整个 View 进行滑动操作。

### 改变布局参数

通过改变 LayoutParams，对 View 重新布局来实现 View 的滑动。例如：

```
MarginLayoutParams params = (MarginLayoutParams)mButton.getLayoutParams();
params.width += 100;
params.leftMargin += 100;
mButton.requestLayout();
```

## 弹性滑动

实现弹性滑动的思想是相通的，即将一次大的滑动分解成若干次小的滑动，并在一个时间段内完成。具体的实现方式有：

- 使用 Scroller

使用 Scroller 实现弹性滑动的代码如下：

```
mScroller = new Scroller(mContext);

public void smoothScrollTo(int destX, int desY) {
    int scrollX = getScrollX();
    int deltaX = destX - scrollX;
    mScroller.startScroll(scrollX, 0, deltaX, 0, 1000);
    invalidate();
}
@Override
public void computeScroll() {
    if (mScroller.computeScrollOffset()) {
        scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
        postInvalidate();
    }
}
```

Scroller 本身并不能实现 View 的滑动，它需要配合 View 的 computeScroll 方法才能完成弹性滑动效果。Scroller 不断让 View 重绘，每一次重绘距离滑动起始时间会有一个时间间隔，通过这个时间间隔计算出需要滑动的百分比，根据百分比得到 View 需要滑动到的位置，滑动直接调用 scrollTo() 方法。

- 通过动画

动画本身就是一个渐进的过程，如果需要实现弹性滑动，只需要对 View 的属性（例如 translationX）做动画即可。如果使用属性动画，我们能够使用 ValueAnimator 实现 Scroller 效果：

```
ValueAnimator animator = ValueAnimator.ofFloat(0, 1)
        .setDuration(1000);
animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        float fraction = animation.getAnimatedFraction();
        Log.d(TAG, "onAnimationUpdate, fraction is " + fraction);
        scrollTo((int) (fraction * destX), 0);
    }
});
animator.start();
```

- 使用延时策略

通过对 View 发送一系列延时消息从而达到一种渐进式的效果。发送延时消息可以使用 Handler 或者 View 的 postDelayed 方法，在接收消息的位置，使用 scrollTo() 方法对 View 进行滑动。
