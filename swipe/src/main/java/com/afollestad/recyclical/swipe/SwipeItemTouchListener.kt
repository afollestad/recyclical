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
import android.graphics.Canvas
import android.graphics.Color.DKGRAY
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.swipe.SwipeLocation.LEFT
import com.afollestad.recyclical.swipe.SwipeLocation.LEFT_LONG
import com.afollestad.recyclical.swipe.SwipeLocation.RIGHT
import com.afollestad.recyclical.swipe.SwipeLocation.RIGHT_LONG
import kotlin.math.abs

/** @author Aidan Follestad (@afollestad) */
internal class SwipeItemTouchListener(
  private val context: Context,
  pluginData: SwipePluginData,
  private val dataSource: DataSource
) : ItemTouchHelper.SimpleCallback(0, pluginData.getSwipeDirections()) {
  private val actions = pluginData.actions

  private var leftDistance: Float = -1f
  private var leftIsLong: Boolean = false
  private var rightDistance: Float = -1f
  private var rightIsLong: Boolean = false

  private var icon: Drawable? = null
  private var background: ColorDrawable? = null

  private val textMargin: Float by lazy {
    context.resources.getDimension(R.dimen.swipe_text_margin)
  }
  private val textMarginLess: Float by lazy {
    context.resources.getDimension(R.dimen.swipe_text_margin_less)
  }
  private val textMarginHalf: Float by lazy {
    context.resources.getDimension(R.dimen.swipe_text_margin_half)
  }

  override fun onMove(
    recyclerView: RecyclerView,
    viewHolder: ViewHolder,
    target: ViewHolder
  ): Boolean = false

  override fun onSwiped(
    viewHolder: ViewHolder,
    direction: Int
  ) {
    val index = viewHolder.adapterPosition
    when (direction) {
      ItemTouchHelper.LEFT -> {
        val action = actions[if (leftIsLong) LEFT_LONG else LEFT] ?: return
        val callbackResult = action.callback?.invoke(index, dataSource[index]) ?: false
        if (callbackResult) {
          dataSource.removeAt(index)
        } else {
          dataSource.invalidateAt(index)
        }
      }
      ItemTouchHelper.RIGHT -> {
        val action = actions[if (rightIsLong) RIGHT_LONG else RIGHT] ?: return
        val callbackResult = action.callback?.invoke(index, dataSource[index]) ?: false
        if (callbackResult) {
          dataSource.removeAt(index)
        } else {
          dataSource.invalidateAt(index)
        }
      }
      else -> throw IllegalStateException("Unknown direction: $direction")
    }

    leftDistance = DEFAULT_DISTANCE
    rightDistance = DEFAULT_DISTANCE
  }

  override fun onChildDraw(
    c: Canvas,
    recyclerView: RecyclerView,
    viewHolder: ViewHolder,
    dX: Float,
    dY: Float,
    actionState: Int,
    isCurrentlyActive: Boolean
  ) {
    val itemView = viewHolder.itemView
    val action: SwipeAction?
    var textX = 0f
    when {
      dX > 0 -> {
        // Swiping to the right
        rightDistance = dX

        rightIsLong = actions.containsKey(RIGHT_LONG) &&
            rightDistance >= (LONG_THRESHOLD_PERCENT * itemView.measuredWidth)
        action = actions[if (rightIsLong) RIGHT_LONG else RIGHT] ?: return
        icon = action.iconDrawable
        background = action.backgroundDrawable ?: ColorDrawable(DKGRAY)

        if (icon != null) {
          val iconTop = itemView.top + (itemView.height - icon!!.intrinsicHeight) / 2
          val iconBottom = iconTop + icon!!.intrinsicHeight

          val iconLeft = itemView.left + textMarginLess.toInt()
          val iconRight = iconLeft + icon!!.intrinsicWidth
          icon!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)

          if (action.text != null) {
            textX = iconRight + textMarginHalf
          }
        } else if (action.text != null) {
          textX = textMargin
        }

        val actualRight = itemView.left + dX.toInt()
        background!!.setBounds(
            itemView.left,
            itemView.top,
            actualRight,
            itemView.bottom
        )
      }
      dX < 0 -> {
        // Swiping to the left
        leftDistance = dX

        leftIsLong = actions.containsKey(LEFT_LONG) &&
            abs(leftDistance) >= (LONG_THRESHOLD_PERCENT * itemView.measuredWidth)
        action = actions[if (leftIsLong) LEFT_LONG else LEFT] ?: return
        icon = action.iconDrawable
        background = action.backgroundDrawable ?: ColorDrawable(DKGRAY)

        if (icon != null) {
          val iconTop = itemView.top + (itemView.height - icon!!.intrinsicHeight) / 2
          val iconBottom = iconTop + icon!!.intrinsicHeight

          val iconRight = itemView.right - textMarginLess.toInt()
          val iconLeft = iconRight - icon!!.intrinsicWidth
          icon!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)

          if (action.text != null) {
            textX = iconLeft - textMarginHalf - action.getTextWidth()
          }
        } else if (action.text != null) {
          textX = itemView.measuredWidth.toFloat() - textMargin - action.getTextWidth()
        }

        val actualLeft = itemView.right + dX.toInt()
        background!!.setBounds(
            actualLeft,
            itemView.top,
            itemView.right,
            itemView.bottom
        )
      }
      else -> {
        // View is un-swiped
        leftDistance = DEFAULT_DISTANCE
        rightDistance = DEFAULT_DISTANCE
        icon = null
        action = null
        background?.setBounds(0, 0, 0, 0)
      }
    }

    background?.draw(c)
    icon?.draw(c)

    if (action?.text != null && !action.text.isNullOrEmpty()) {
      val halfHeight = (itemView.bottom - itemView.top) / 2
      val textTop = itemView.top + halfHeight + (action.getTextHeight() / 2f)
      c.drawText(
          action.text!!,
          textX,
          textTop,
          action.textPaint!!
      )
    }

    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
  }

  private companion object {
    private const val DEFAULT_DISTANCE = -1f
    private const val LONG_THRESHOLD_PERCENT = 45f / 100f
  }
}
