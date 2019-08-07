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
@file:Suppress("UNCHECKED_CAST")

package com.afollestad.recyclical.internal

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.handle.RecyclicalHandle
import com.afollestad.recyclical.handle.getDataSource
import com.afollestad.recyclical.itemdefinition.ItemGraph
import com.afollestad.recyclical.itemdefinition.RealItemDefinition
import com.afollestad.recyclical.itemdefinition.bindViewHolder
import com.afollestad.recyclical.itemdefinition.createViewHolder
import com.afollestad.recyclical.itemdefinition.recycleViewHolder

/**
 * The core [RecyclerView] adapter of the library that manages showing
 * items in your data source using registered item definitions.
 *
 * @author Aidan Follestad (@afollestad)
 */
internal open class DefinitionAdapter : RecyclerView.Adapter<ViewHolder>() {
  private var handle: RecyclicalHandle? = null
  private var dataSource: DataSource<*>? = null

  private val itemGraph: ItemGraph
    get() = handle?.itemGraph() ?: error("Not attached!")

  /** Attaches the adapter to a handle which provides a data source, etc. */
  open fun attach(handle: RecyclicalHandle) {
    this.handle = handle.also {
      this.dataSource = it.getDataSource()
    }
  }

  /** Clears references to avoid memory leaks. */
  open fun detach() {
    this.dataSource = null
    this.handle = null
  }

  override fun getItemId(position: Int): Long {
    val item = dataSource!![position]
    val definition = itemGraph.definitionForName(item.className)
    val idGetter = (definition as? RealItemDefinition)?.idGetter
    return idGetter?.invoke(item)?.toLong() ?: super.getItemId(position)
  }

  override fun getItemViewType(position: Int): Int {
    val itemClassName = dataSource?.get(position)?.className
        ?: error("No data source available.")
    return itemGraph.typeForName(itemClassName)
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): ViewHolder {
    val layoutRes = itemGraph.layoutForType(viewType)
    val view = LayoutInflater.from(parent.context)
        .inflate(layoutRes, parent, false)
    return itemGraph.definitionForType(viewType)
        .createViewHolder(view)
  }

  override fun getItemCount() = dataSource?.size() ?: 0

  override fun onBindViewHolder(
    holder: ViewHolder,
    position: Int
  ) {
    val item = dataSource!![position]
    itemGraph.definitionForName(item.className)
        .bindViewHolder(holder, item, position)
  }

  override fun onViewRecycled(holder: ViewHolder) {
    val index = holder.adapterPosition
    if (index > -1) {
      dataSource?.get(index)
          ?.className
          ?.let { itemGraph.definitionForName(it) }
          ?.recycleViewHolder(holder)
    }
    super.onViewRecycled(holder)
  }

  private val Any.className: String get() = this::class.java.name
}
