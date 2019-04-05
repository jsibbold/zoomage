/**
 * Copyright 2016 Jeffrey Sibbold
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jsibbold.zoomage;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Describes how the {@link ZoomageView} will reset to its original size
 * once interaction with it stops. {@link #UNDER} will reset when the image is smaller
 * than or equal to its starting size, {@link #OVER} when it's larger than or equal to its starting size,
 * {@link #ALWAYS} in both situations,
 * and {@link #NEVER} causes no reset. Note that when using {@link #NEVER}, the image will still animate
 * to within the screen bounds in certain situations.
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({AutoResetMode.NEVER, AutoResetMode.UNDER, AutoResetMode.OVER, AutoResetMode.ALWAYS})
public @interface AutoResetMode {

    int UNDER = 0;
    int OVER = 1;
    int ALWAYS = 2;
    int NEVER = 3;

    class Parser {

        @AutoResetMode
        public static int fromInt(final int value) {
            switch (value) {
                case OVER:
                    return OVER;
                case ALWAYS:
                    return ALWAYS;
                case NEVER:
                    return NEVER;
                default:
                    return UNDER;
            }
        }
    }

}
