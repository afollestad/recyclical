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

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.recyclical.RecyclicalSetup
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.plugins.PluginData
import com.afollestad.recyclical.swipe.SwipeLocation.LEFT
import com.afollestad.recyclical.swipe.SwipeLocation.LEFT_LONG
import com.afollestad.recyclical.swipe.SwipeLocation.RIGHT
import com.afollestad.recyclical.swipe.SwipeLocation.RIGHT_LONG

internal const val PLUGIN_NAME = "swipe_plugin"

/** @author Aidan Follestad (@afollestad) */
internal data class SwipePluginData(
  val actions: MutableMap<SwipeLocation, SwipeAction> = mutableMapOf()
) : PluginData() {

  override fun attach(
    toView: RecyclerView,
    dataSource: DataSource
  ) {
    val callback = SwipeItemTouchListener(toView.context, this, dataSource)
    ItemTouchHelper(callback).attachToRecyclerView(toView)
  }

  fun getSwipeDirections(): Int {
    var result = 0
    if (actions.any { it.key == RIGHT || it.key == RIGHT_LONG }) {
      result = result or ItemTouchHelper.RIGHT
    }
    if (actions.any { it.key == LEFT || it.key == LEFT_LONG }) {
      result = result or ItemTouchHelper.LEFT
    }
    return result
  }
}

/**
 * Configures a swipe action for the view.
 */
fun RecyclicalSetup.withSwipeAction(
  vararg locations: SwipeLocation,
  block: SwipeAction.() -> Unit
): RecyclicalSetup {
  val pluginData = getPluginData<SwipePluginData>(PLUGIN_NAME) ?: SwipePluginData()
  for (location in locations) {
    pluginData.actions[location] = SwipeAction(recyclerView.context).apply(block)
  }
  setPluginData(PLUGIN_NAME, pluginData)
  return this
}
