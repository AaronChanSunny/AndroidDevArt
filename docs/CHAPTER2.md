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

通过对 View 发送一系列延时消息从而达到一种渐进式的效果。发送延时消息可以使用 Handler 的 sendEmptyMessageDelkayed 方法或者 View 的 postDelayed 方法，在接收消息的位置，使用 scrollTo() 方法对 View 进行滑动。例如：

```
private int mCount = 0;
private static final int MAX_FRAME = 30;
private int destX = 200;
private Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case SCROLL_TO:
                mCount++;
                if (mCount < MAX_FRAME) {
                    float fraction = mCount / (float) MAX_FRAME;
                    scrollTo((int) (fraction * destX), 0);
                    mHandler.sendEmptyMessageDelayed(SCROLL_TO, 33);
                } else {
                    mCount = 0;
                }
                break;
            default:
                break;
        }
    }
};

public void smoothScrollTo2() {
    mHandler.sendEmptyMessage(SCROLL_TO);
}
```

## View 的事件分发机制

事件分发过程由三个方法共同完成：dispatchTouchEvent、onInterceptTouchEvent 和 onTouchEvent。现在分别介绍如下：

- public boolean dispatchTouchEvent(MotionEvent event)

用来进行事件分发。如果事件能够传递到当前 View，此方法一定会被首先调用，返回值由当前 View 的 onTouchEvent 和子 View 的 dispatchTouchEvent 共同决定，表示是否消耗当前事件。

- public boolean onInterceptTouchEvent(MotionEvent event)

只有 ViewGroup 包含此方法，View 没有该方法。在 dispatchTouchEvent 内部调用，用来判断是否拦截某个事件，如果当前 View 拦截了某个事件，那么同一事件序列中，此方法不会被再次调用，返回结果表示是否拦截当前事件。

- public boolean onTouchEvent(MotionEvent event)

同样在 dispatchTouchEvent 内部调用，用来处理点击事件，返回结果表示是否消耗当前事件。如果不消耗，在同一事件序列中，当前 View 无法再次接收到事件。

这三个方法之间的关系可以用如下伪代码表示：

如果是 ViewGroup：
```
public boolean dispatchTouchEvent(MotionEvent event) {
    boolean consumed = false;
    if (onInterceptTouchEvent(event)) {
        consumed = onTouchEvent(event);
    } else {
        consumed = child.dispatchTouchEvent(event);
    }
    
    return consumed;
}
```

如果是 View：
```
public boolean dispatchTouchEvent(MotionEvent event) {
    return onTouchEvent(event);
}
```

当一个 View 需要处理事件时，如果它设置了 OnTouchListener，那么 OnTouchListener 中的 onTouch 方法会被调用。如果 onTouch 返回 false，则当前 View 的 onTouchEvent 会被调用；如果 onTouch 返回 true，那么 onTouchEvent 将不会被调用。由此可见，如果给 View 设置 OnTouchListener，其优先级比 onTouchEvent 更高。

在 onTouchEvent 方法中，如果当前 View 设置了 OnClickListener，那么它的 onClick 方法会被调用。由此可见，OnClickListener 的优先级最低。

当一个点击事件产生后，它的传递顺序：Activity－Window－View，当事件传递到顶级 View，顶级 View 接收到事件后就按照事件分发机制去分发事件。

如果一个 View 的 onTouchEvent 返回 false，那么它的父容器的 onTouchEvent 将会被调用。依此类推，如果所有的 View 都不处理这个事件，这个事件最终会传递给 Activity 处理，即 Activity 的 onTouchEvent 方法会被调用。

### 事件传递规则

- 同一个事件序列是指从手指接触屏幕的那一刻起，到手指离开屏幕的那一刻结束。在这个过程中所产生的一系列事件，这个事件序列以 down 事件开始，中间含有数量不定的 move 事件，最终以 up 结束。
- 正常情况下，一个事件序列只能被一个 View 拦截并且消耗。只要一个 View 拦截了某一事件，那么同一事件序列内的所有事件都会交给它处理。
- 某个 View 一旦决定拦截，那么这一事件序列都只能由它来处理，并且它的 onInterceptTouchEvent 将不会被再调用。
- 某个 View 一旦开始处理事件，如果它不消耗 ACTION_DOWN 事件，那么同一事件序列的其他事件都不会再交给它处理，并且事件将重新交给它的父容器去处理，即父容器的 onTouchEvent 会被调用。
- 如果 View 不消耗除 ACTION_DOWN 以外的其他事件，这个点击事件会消失，此时父容器的 onTouchEvent 并不会被调用，并且当前 View 可以持续收到后续事件，最终这些消失的点击事件会传递给 Activity 处理。
- ViewGroup 默认不拦截任何事件。
- View 没有 onInterceptTouchEvent 方法，一旦有点击事件传递给它，它的 onTouchEvent 就会被调用。
- View 的 onTouchEvent 默认都会消耗事件，除非它是不可点击的，所谓不可点击是 clickable 和 longClickable 同时为 false。需要注意的是 View 的 longClickable 默认为 false；而 clickable 要分情况，比如 Button 默认为 true，而 TextView 默认为 false。
- View 的 enabled 属性不影响 onTouchEvent 的默认返回值。
- onClick 会触发的前提是当前 View 是可点击的，并且它收到了 down 和 up 事件。
- 事件传递的顺序是由外向内的，即事件总是先传递给父容器，然后再由父容器分发给子 View。但是通过 requestDisallowInterceptTouchEvent 方法可以在子 View 干预父容器的事件分发，ACTION_DOWN 除外。这点是处理滑动冲突，内部拦截法的基础。
