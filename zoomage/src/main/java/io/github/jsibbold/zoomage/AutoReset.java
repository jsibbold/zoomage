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
package io.github.jsibbold.zoomage;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({AutoReset.NONE, AutoReset.UNDER, AutoReset.OVER, AutoReset.OVER_UNDER})
/**
 * Describes how the {@link ZoomageView} will reset to its original size
 * once interaction with it stops. UNDER will reset when the image is smaller
 * than its starting size, OVER when it's over, OVER_UNDER in both situations,
 * and NONE causes no reset. Note that when using NONE, the image will still animate
 * to within the screen bounds in certain situations.
 */
public @interface AutoReset {

    int UNDER = 0;
    int OVER = 1;
    int OVER_UNDER = 2;
    int NONE = 3;

    class Parser {

        @AutoReset
        public static int fromInt(final int value) {
            switch (value) {
                case OVER:
                    return OVER;
                case OVER_UNDER:
                    return OVER_UNDER;
                case NONE:
                    return NONE;
                default:
                    return UNDER;
            }
        }
    }

}
