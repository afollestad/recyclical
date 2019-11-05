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
@file:Suppress("SpellCheckingInspection", "unused")

package com.afollestad.recyclical

import android.view.View
import androidx.annotation.LayoutRes
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.handle.RealRecyclicalHandle
import com.afollestad.recyclical.handle.RecyclicalHandle
import com.afollestad.recyclical.handle.getDataSource
import com.afollestad.recyclical.internal.DefinitionAdapter
import com.afollestad.recyclical.internal.onAttach
import com.afollestad.recyclical.internal.onDetach
import com.afollestad.recyclical.itemdefinition.ItemGraph
import com.afollestad.recyclical.plugins.PluginData

/** @author Aidan Follestad (@afollestad) */
@DslMarker
annotation class RecyclicalMarker

/** @author Aidan Follestad (@afollestad) */
@RecyclicalMarker
class RecyclicalSetup internal constructor(
  val recyclerView: RecyclerView
) {
  private val itemGraph = ItemGraph()
  private var emptyView: View? = null

  internal var pluginData: MutableMap<String, PluginData>? = null

  internal var globalOnClick: ItemClickListener<Any>? = null
  internal var globalOnLongClick: ItemClickListener<Any>? = null
  internal var currentDataSource: DataSource<*>? = null
  internal var adapterCreator: () -> DefinitionAdapter = { DefinitionAdapter() }

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
  fun withDataSource(dataSource: DataSource<*>): RecyclicalSetup {
    this.currentDataSource = dataSource
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

  /**
   * Sets a global long click listener that is invoked when any type of item is long clicked in the
   * list. Gets called after any item-specific long click listeners that may be set.
   */
  fun withLongClickListener(block: ItemClickListener<Any>): RecyclicalSetup {
    this.globalOnLongClick = block
    return this
  }

  /** Persists a [PluginData] instance and allosw it to receive the attach signal. */
  fun setPluginData(
    name: String,
    data: PluginData
  ) {
    if (this.pluginData == null) {
      this.pluginData = mutableMapOf(name to data)
    } else {
      this.pluginData!![name] = data
    }
  }

  /** Retrieves persisted plugin data and auto casts it. */
  @Suppress("UNCHECKED_CAST") fun <T> getPluginData(name: String): T? {
    return this.pluginData?.get(name) as? T
  }

  /** This should not be called directly. */
  @RestrictTo(LIBRARY) fun registerItemDefinition(
    @LayoutRes layoutRes: Int,
    definition: ItemDefinition<*, *>
  ) = itemGraph.register(layoutRes, definition)

  internal fun toAttached(): RecyclicalHandle {
    val dataSource = currentDataSource ?: error("Must set a data source.")
    return RealRecyclicalHandle(
        emptyView = emptyView,
        adapter = adapterCreator(),
        dataSource = dataSource,
        itemGraph = itemGraph.validate()
    ).also {
      it.getAdapter()
          .setHasStableIds(itemGraph.hasStableIds())
      dataSource.attach(it)
    }
  }
}

/**
 * Setups a RecyclerView, accepts methods of [RecyclicalSetup] in its [block].
 *
 * @author Aidan Follestad (@afollestad)
 */
fun RecyclerView.setup(block: RecyclicalSetup.() -> Unit): RecyclicalHandle {
  val setup = RecyclicalSetup(this).apply(block)
  if (layoutManager == null) {
    layoutManager = LinearLayoutManager(context)
  }

  return setup.toAttached()
      .also { handle ->
        adapter = handle.getAdapter()

        if (handle is RealRecyclicalHandle) {
          onAttach { handle.attachDataSource() }
          onDetach { handle.detachDataSource() }
        }

        setup.pluginData?.values?.forEach { data ->
          data.attach(this, handle.getDataSource())
        }
      }
}
