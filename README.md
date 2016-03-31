# zoomage
A simple pinch-to-zoom ImageView library for Android with an emphasis
on a smooth and natural feel.

[![Build Status](https://travis-ci.org/jsibbold/zoomage.svg?branch=master)](https://travis-ci.org/jsibbold/zoomage)

## Gradle
```groovy
repositories {
    jcenter()
}

dependencies {
    compile 'com.jsibbold:zoomage:1.0.0'
}
```

# Using It

```xml
    <RelativeLayout
        xmlns:android="http://schemas.android.com/apk/res/android"
        xmlns:zoom="http://schemas.android.com/apk/res-auto"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    
        <com.jsibbold.zoomage.ZoomageView
            android:id="@+id/myZoomageView"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:src="@drawable/my_zoomable_image"
            zoom:restrictBounds="false"
            zoom:animateOnReset="true"
            zoom:autoReset="UNDER"
            zoom:zoomable="true"
            zoom:translatable="true"
            />
    </RelativeLayout>
```

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
