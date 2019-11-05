/**
 * Designed and developed by Aidan Follestad (@afollestad)
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
package com.afollestad.recyclical.animations

import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import androidx.annotation.AnimRes
import androidx.annotation.IntegerRes
import androidx.annotation.InterpolatorRes
import com.afollestad.recyclical.RecyclicalSetup

/**
 * Sets the layout animation controller used to animate the group's children after
 * the first layout.
 *
 * @param animationRes The resource id of the animation to load.
 * @param interpolatorRes The interpolator used to interpolate the delays between the children.
 * @param delay The delay by which each child's animation must be offset.
 */
fun RecyclicalSetup.withLayoutAnimation(
  @AnimRes animationRes: Int,
  @InterpolatorRes interpolatorRes: Int? = null,
  delay: Float = 0f,
  duration: Long? = null,
  @IntegerRes durationRes: Int? = null
) {
  val resources = recyclerView.context.resources
  val actualDuration =
    duration ?: durationRes?.let {
      resources.getInteger(durationRes)
          .toLong()
    } ?: 250L
  val anim = AnimationUtils.loadAnimation(recyclerView.context, animationRes)
      .apply { this.duration = actualDuration }
  recyclerView.layoutAnimation = LayoutAnimationController(anim, delay).apply {
    interpolatorRes?.let { setInterpolator(recyclerView.context, it) }
  }
}
