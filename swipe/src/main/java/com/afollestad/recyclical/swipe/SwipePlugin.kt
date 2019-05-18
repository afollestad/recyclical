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
@file:Suppress("unused")

package com.afollestad.recyclical.swipe

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.recyclical.RecyclicalSetup
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.plugins.PluginData
import com.afollestad.recyclical.swipe.SwipeLocation.LEFT
import com.afollestad.recyclical.swipe.SwipeLocation.RIGHT
import kotlin.reflect.KClass

internal const val PLUGIN_NAME = "swipe_plugin"

/** @author Aidan Follestad (@afollestad) */
internal data class ActionKey(
  val location: SwipeLocation,
  val itemClassName: String?
)

/** @author Aidan Follestad (@afollestad) */
internal data class SwipePluginData(
  val actions: MutableMap<ActionKey, SwipeAction<*>> = mutableMapOf()
) : PluginData() {

  override fun attach(
    toView: RecyclerView,
    dataSource: DataSource<*>
  ) {
    val callback = SwipeItemTouchListener(toView.context, this, dataSource)
    ItemTouchHelper(callback).attachToRecyclerView(toView)
  }

  fun getSwipeDirections(): Int {
    var result = 0
    if (actions.any { it.key.location == RIGHT }) {
      result = result or ItemTouchHelper.RIGHT
    }
    if (actions.any { it.key.location == LEFT }) {
      result = result or ItemTouchHelper.LEFT
    }
    return result
  }
}

/**
 * Configures a swipe action for the view.
 *
 * @param forItemClassName If specified, this action will not effect items of other types.
 */
fun RecyclicalSetup.withSwipeAction(
  vararg locations: SwipeLocation,
  forItemClassName: KClass<*>? = null,
  block: SwipeAction<Any>.() -> Unit
): RecyclicalSetup {
  val pluginData = getPluginData<SwipePluginData>(PLUGIN_NAME) ?: SwipePluginData()
  for (location in locations) {
    val key = ActionKey(location = location, itemClassName = forItemClassName?.java?.name)
    pluginData.actions[key] = SwipeAction<Any>(recyclerView.context).apply(block)
  }
  setPluginData(PLUGIN_NAME, pluginData)
  return this
}

/**
 * Configures a swipe action for the view, for a specific item type. This action will not
 * effect items of other types.
 */
inline fun <reified IT : Any> RecyclicalSetup.withSwipeActionOn(
  vararg locations: SwipeLocation,
  noinline block: SwipeAction<IT>.() -> Unit
): RecyclicalSetup {
  @Suppress("UNCHECKED_CAST")
  return withSwipeAction(
      locations = *locations,
      forItemClassName = IT::class,
      block = block as SwipeAction<Any>.() -> Unit
  )
}
