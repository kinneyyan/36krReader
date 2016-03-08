36krReader
======

个人开发的36氪Android阅读客户端，数据通过Jsoup解析web端网页获取，由于最近网站改版，暂时不支持加载分页数据T T。

1. 个人业余时间的项目，包含了android中一些常用的工具类、代码段。

2. 设计风格希望能遵循Material Design，尽量使用google提供的原生控件，包括android design support library

3. 使用了国内的baas服务Bmob，给app加入了用户系统，实现了收藏云同步。

4. 一些知识点

	- CardView的使用：[关于使用 CardView 开发过程中要注意的细节](http://blog.feng.moe/2015/10/24/something-about-cardview-development/)

	- 在清单文件中设置android:parentActivityName的问题：[解决方案](http://blog.csdn.net/mengweiqi33/article/details/41285699)
	
	- style文件中设置android:windowIsTranslucent后activity切换动画失效（我这边直接不设置那个属性了）：[解决方案](http://blog.csdn.net/fancylovejava/article/details/39643449)

	- 当targetSdkVersion设为23以上时，处理运行时权限：[Android M 新的运行时权限开发者需要知道的一切
](http://jijiaxin89.com/2015/08/30/Android-s-Runtime-Permission/)
5. 使用的第三方库

	- 轻量级的缓存框架：[ASimpleCache](https://github.com/yangfuhai/ASimpleCache "")
	- 图片加载框架：[universal-image-loader](https://github.com/nostra13/Android-Universal-Image-Loader "")
	- orm框架：[ormlite](https://github.com/j256/ormlite-android "")
	- [StickyListHeaders](https://github.com/emilsjolander/StickyListHeaders "")
	- [PhotoView](https://github.com/chrisbanes/PhotoView "")
	- 监听可滚动View的滑动状态：[Android-ObservableScrollView](https://github.com/ksoichiro/Android-ObservableScrollView "")
	- an unofficial mirror for android volley library：[android-volley](https://github.com/mcxiaoke/android-volley "")
	- 扩展的RecyclerView，拥有添加头、底等多种操作：[ExRecyclerView](https://github.com/tianzhijiexian/ExRecyclerView "")
	- 可无限循环滑动的ViewPager（为了配合首页自动滑动稍做改动）：[InfiniteViewPager](https://github.com/antonyt/InfiniteViewPager "")

6. app截图：

![screenshot1](https://raw.githubusercontent.com/kinneyyan/36krReader/master/Screenshots/device-2015-10-29-152645.png)

![screenshot2](https://raw.githubusercontent.com/kinneyyan/36krReader/master/Screenshots/device-2015-10-29-152823.png)

![screenshot3](https://raw.githubusercontent.com/kinneyyan/36krReader/master/Screenshots/device-2015-10-29-152910.png)
