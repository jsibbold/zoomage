# zoomage
[![Build Status](https://travis-ci.org/jsibbold/zoomage.svg?branch=master)](https://travis-ci.org/jsibbold/zoomage) [ ![Download](https://api.bintray.com/packages/jsibbold/maven/zoomage/images/download.svg) ](https://bintray.com/jsibbold/maven/zoomage/_latestVersion) <a href="http://www.detroitlabs.com/"><img src="https://img.shields.io/badge/Sponsor-Detroit%20Labs-000000.svg" /></a>

A simple pinch-to-zoom ImageView library for Android with an emphasis
on a smooth and natural feel.



## Gradle
```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'com.jsibbold:zoomage:1.3.1'
}
```

# Using It

Simply add a ZoomageView as you would any typical ImageView in Android. The scaleType that you set on your
ZoomageView will determine the starting size and position of your ZoomageView's image. This is the inherited
ImageView.ScaleType from Android. With a ZoomageView, the fitCenter or centerInside scale types usually make
the most sense to use, fitCenter being Android's default scale type.

```xml
    <RelativeLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:app="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    
        <com.jsibbold.zoomage.ZoomageView
            android:id="@+id/myZoomageView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:src="@drawable/my_zoomable_image"
            app:zoomage_restrictBounds="false"
            app:zoomage_animateOnReset="true"
            app:zoomage_autoResetMode="UNDER"
            app:zoomage_autoCenter="true"
            app:zoomage_zoomable="true"
            app:zoomage_translatable="true"
            app:zoomage_minScale="0.6"
            app:zoomage_maxScale="8"
            />
    </RelativeLayout>
```

If using a ZoomageView with a view pager, it is recommended that [ViewPager2](https://developer.android.com/jetpack/androidx/releases/viewpager2)
is used.

## XML Attributes

```
zoomage_restrictBounds="true|false"
```
Restricts the bounds of the image so it does not wander outside the border of the ImageView when it's smaller than the frame size,
and restricts the bounds to stop at the edges of the ImageView when the image is larger than the frame size. Default value is false.

```
zoomage_animateOnReset="true|false"
```
Image will animate back to its starting size whenever it is reset if true, and will snap back to its starting size when false.
Default value is true.

```
zoomage_autoResetMode="UNDER|OVER|ALWAYS|NEVER"
```
Determines at what times the image will reset to its starting size. Note that UNDER, OVER, and ALWAYS all have the effect of
resetting the image to its starting position if its size has not changed. Default value is UNDER.

```
zoomage_autoCenter="true|false"
```
This will cause the image to pull itself into view on-screen if it is partially off-screen. Default value is true.

```
zoomage_minScale="{float greater than 0}"
```
The minimum allowed scale for the image. Ideally this should be less than 1, must be greater than 0, and must
be less than maxScale. Default value is 0.6.

```
zoomage_maxScale="{float greater than 0}"
```
The maximum allowed scale for the image. Ideally this should be greater than 1, must be greater than 0, and must
be greater than minScale. Default value is 8.

```
zoomage_zoomable="true|false"
```
Sets whether zooming is allowed. Default value is true.

```
zoomage_translatable="true|false"
```
Sets whether translation is allowed. Default value is true.

```
zoomage_doubleTapToZoom="true|false"
```
Sets whether double tap to zoom functionality is enabled. Default is true.

```
zoomage_doubleTapToZoomScaleFactor="{float within bounds of min and max scale}"
```
Sets the scale factor for double tap to zoom functionality. Default is 3.

---
**Special thanks to <a href="https://github.com/mchowning">@mchowning</a> for all his help**

# License
```
Copyright 2016 Jeffrey Sibbold

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
