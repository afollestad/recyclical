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
import android.view.HapticFeedbackConstants
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.swipe.SwipeLocation.LEFT
import com.afollestad.recyclical.swipe.SwipeLocation.RIGHT
import kotlin.math.absoluteValue

/** @author Aidan Follestad (@afollestad) */
internal class SwipeItemTouchListener(
  private val context: Context,
  pluginData: SwipePluginData,
  private val dataSource: DataSource<*>
) : ItemTouchHelper.SimpleCallback(0, pluginData.getSwipeDirections()) {
  private val actions = pluginData.actions

  private var leftDistance: Float = -1f
  private var rightDistance: Float = -1f

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

  private var shouldTriggerThresholdDecor = true

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
    val itemClass = dataSource[index].javaClass.name
    when (direction) {
      ItemTouchHelper.LEFT -> {
        val actionKeyForItem = ActionKey(location = LEFT, itemClassName = itemClass)
        val actionKeyGlobal = ActionKey(location = LEFT, itemClassName = null)
        val action = actions[actionKeyForItem] ?: actions[actionKeyGlobal] ?: return
        if (action.sendToCallback(index, dataSource[index])) {
          dataSource.removeAt(index)
        } else {
          dataSource.invalidateAt(index)
        }
      }
      ItemTouchHelper.RIGHT -> {
        val actionKeyForItem = ActionKey(location = RIGHT, itemClassName = itemClass)
        val actionKeyGlobal = ActionKey(location = RIGHT, itemClassName = null)
        val action = actions[actionKeyForItem] ?: actions[actionKeyGlobal] ?: return
        if (action.sendToCallback(index, dataSource[index])) {
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

  override fun getSwipeDirs(
    recyclerView: RecyclerView,
    viewHolder: ViewHolder
  ): Int {
    val index = viewHolder.adapterPosition
    if (index == -1) return 0
    val itemClass = dataSource[index].javaClass.name

    var directions = 0
    if (actions.containsKey(ActionKey(LEFT, itemClass)) ||
        actions.containsKey(ActionKey(LEFT, null))
    ) {
      directions = directions or ItemTouchHelper.LEFT
    }
    if (actions.containsKey(ActionKey(RIGHT, itemClass)) ||
        actions.containsKey(ActionKey(RIGHT, null))
    ) {
      directions = directions or ItemTouchHelper.RIGHT
    }
    return directions
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
    val index = viewHolder.adapterPosition
    if (index == -1) return
    val itemClass = dataSource[index].javaClass.name

    val itemView = viewHolder.itemView
    val action: SwipeAction<*>?
    var textX = 0f

    when {
      dX > 0 -> {
        // Swiping to the right
        rightDistance = dX

        val actionKeyForItemShort = ActionKey(location = RIGHT, itemClassName = itemClass)
        val actionKeyGlobalShort = ActionKey(location = RIGHT, itemClassName = null)
        action = actions[actionKeyForItemShort] ?: actions[actionKeyGlobalShort]

        icon = action?.iconDrawable ?: return
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

        val actionKeyForItemShort = ActionKey(location = LEFT, itemClassName = itemClass)
        val actionKeyGlobalShort = ActionKey(location = LEFT, itemClassName = null)
        action = actions[actionKeyForItemShort] ?: actions[actionKeyGlobalShort]

        icon = action?.iconDrawable ?: return
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

    if ((dX / itemView.measuredWidth).absoluteValue > getSwipeThreshold(viewHolder)) {
      if (shouldTriggerThresholdDecor) {
        shouldTriggerThresholdDecor = false
        if (action?.hapticFeedbackEnabled == true) {
          recyclerView.performHapticFeedback(
            HapticFeedbackConstants.VIRTUAL_KEY,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
          )
        }
      }
    } else {
      shouldTriggerThresholdDecor = true
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
  }
}
