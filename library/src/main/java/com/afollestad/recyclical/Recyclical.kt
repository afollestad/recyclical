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
@file:Suppress("SpellCheckingInspection", "MemberVisibilityCanBePrivate", "unused")

package com.afollestad.recyclical

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager

@DslMarker
annotation class RecyclicalMarker

/** @author Aidan Follestad (@afollestad) */
@RecyclicalMarker
class RecyclicalSetup internal constructor(
  private val recyclerView: RecyclerView
) {
  var itemClassToType = mutableMapOf<String, Int>()
  var bindingsToTypes = mutableMapOf<Int, ItemDefinition<*>>()

  internal var emptyView: View? = null
  internal var dataSource: DataSource? = null
  internal var globalOnClick: ItemClickListener<Any>? = null

  /**
   * Sets a layout manaher for the RecyclerView. The default is a vertical LinearLayoutManager,
   * so this method is optional.
   */
  fun withLayoutManager(layoutManager: LayoutManager): RecyclicalSetup {
    recyclerView.layoutManager = layoutManager
    return this
  }

  /**
   * Sets an empty view that is shown if the data source is empty. Else the view is hidden
   * (its visibility to gone).
   */
  fun withEmptyView(emptyView: View): RecyclicalSetup {
    this.emptyView = emptyView
    return this
  }

  /**
   * Sets a [DataSource] that provides the content which is displayed in the RecyclerView.
   * You must use [withItem] to link model definitions to layouts and view holders, of any item
   * types that are added to the data source.
   */
  fun withDataSource(dataSource: DataSource): RecyclicalSetup {
    this.dataSource = dataSource
    return this
  }

  /**
   * Sets a global click listener that is invoked when any type of item is clicked in the list.
   * Gets called after any item-specific click listeners that may be set.
   */
  fun withClickListener(block: ItemClickListener<Any>): RecyclicalSetup {
    this.globalOnClick = block
    return this
  }
}

/**
 * Setups a RecyclerView, accepts methods of [RecyclicalSetup] in its [block].
 *
 * @author Aidan Follestad (@afollestad)
 */
fun RecyclerView.setup(block: RecyclicalSetup.() -> Unit): RecyclicalSetup {
  val setup = RecyclicalSetup(this)
      .apply { block() }

  setup.apply {
    check(itemClassToType.isNotEmpty()) { "No bindings defined." }
    check(bindingsToTypes.size == itemClassToType.size) {
      "Something is very wrong - binding maps don't have matching sizes."
    }
  }

  if (layoutManager == null) {
    layoutManager = LinearLayoutManager(context)
  }

  val dataSource = setup.dataSource
      ?: throw IllegalStateException("Must set a data source.")
  adapter = DefinitionAdapter(setup, dataSource).also {
    dataSource.attach(setup.emptyView, it)
  }
  onAttach { dataSource.attach(setup.emptyView, adapter as? DefinitionAdapter) }
  onDetach { dataSource.detach() }

  return setup
}
