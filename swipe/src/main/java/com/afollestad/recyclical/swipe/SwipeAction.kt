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
package com.afollestad.recyclical.swipe

import android.content.Context
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.TextPaint
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.afollestad.recyclical.RecyclicalMarker

/**
 * Return true to remove the item from the list, false to push it back
 * into place.
 */
typealias SwipedCallback = (index: Int, item: Any) -> Boolean

/**
 * Represents what position the swipe callback represents. Left means a right-to-left swipe,
 * right means left-to-right swipe. A long swipe means you swipe further vs a short swipe.
 */
enum class SwipeLocation {
  LEFT,
  RIGHT,
  LEFT_LONG,
  RIGHT_LONG
}

/** @author Aidan Follestad (@afollestad) */
@RecyclicalMarker
class SwipeAction(private val context: Context) {
  internal var iconDrawable: Drawable? = null
  internal var backgroundDrawable: ColorDrawable? = null
  internal var callback: SwipedCallback? = null

  internal var text: String? = null
  internal var textPaint: TextPaint? = null
  private var textBounds: Rect? = null

  /** Sets an optional icon that is shown in the swipe gutter. */
  fun icon(
    @DrawableRes res: Int? = null,
    literal: Drawable? = null
  ): SwipeAction {
    require(res != null || literal != null) {
      "Must provide a res or literal value to icon()"
    }
    iconDrawable = literal ?: ContextCompat.getDrawable(context, res!!)
    return this
  }

  /** Sets the color of the swipe gutter. */
  fun color(
    @ColorRes res: Int? = null,
    @ColorInt literal: Int? = null
  ): SwipeAction {
    require(res != null || literal != null) {
      "Must provide a res or literal value to color()"
    }
    val colorValue = literal ?: ContextCompat.getColor(context, res!!)
    backgroundDrawable = ColorDrawable(colorValue)
    return this
  }

  /** Sets optional text that is shown in the swipe gutter. */
  fun text(
    @StringRes res: Int? = null,
    literal: String? = null,
    @ColorRes color: Int = android.R.color.white,
    @DimenRes size: Int = R.dimen.swipe_default_text_size,
    typeface: Typeface? = null,
    @FontRes typefaceRes: Int? = null
  ) {
    require(res != null || literal != null) {
      "Must provide a res or literal value to text()"
    }
    text = literal ?: context.getString(res!!)

    val actualTypeface = when {
      typefaceRes != null -> ResourcesCompat.getFont(context, typefaceRes)
      typeface != null -> typeface
      else -> Typeface.SANS_SERIF
    }
    textPaint = TextPaint().apply {
      this.isAntiAlias = true
      this.color = ContextCompat.getColor(context, color)
      this.typeface = actualTypeface
      this.textSize = context.resources.getDimension(size)
    }
  }

  /**
   * Sets a callback that is invoked when the swipe action is fired. Return true in the callback
   * to remove the swiped item from the data source automatically. Returning false will animate
   * the item back in place.
   */
  fun callback(block: SwipedCallback): SwipeAction {
    this.callback = block
    return this
  }

  internal fun getTextWidth(): Int = getTextBounds().width()

  internal fun getTextHeight(): Int = getTextBounds().height()

  private fun getTextBounds(): Rect {
    require(text != null) { "text is null" }
    if (textBounds == null) {
      textBounds = Rect()
      textPaint!!.getTextBounds(text, 0, text!!.length, textBounds!!)
    }
    return textBounds!!
  }
}
