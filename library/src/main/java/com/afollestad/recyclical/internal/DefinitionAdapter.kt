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
import com.afollestad.recyclical.ItemDefinition
import com.afollestad.recyclical.RecyclicalSetup

/** @author Aidan Follestad (@afollestad) */
internal class DefinitionAdapter(
  setup: RecyclicalSetup
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
  private val dataSource = setup.currentDataSource
      ?: throw IllegalStateException("Must set a data source.")
  private val itemClassToType = setup.itemClassToType
  private val bindingsToTypes = setup.bindingsToTypes

  override fun getItemViewType(position: Int): Int {
    return dataSource[position].getItemType()
  }

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): ViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(viewType, parent, false)
    return viewType.getItemDefinition()
        .createViewHolder(view)
  }

  override fun getItemCount() = dataSource.size()

  override fun onBindViewHolder(
    holder: ViewHolder,
    position: Int
  ) {
    val item = dataSource[position]
    val viewType = item.getItemType()
    viewType.getItemDefinition()
        .bindViewHolder(holder, item, position)
  }

  private fun Any.getItemType(): Int {
    val itemClassName = this::class.java.name
    return itemClassToType[itemClassName] ?: throw IllegalStateException(
        "Didn't find type for class $itemClassName"
    )
  }

  private fun Int.getItemDefinition(): ItemDefinition<*> {
    return bindingsToTypes[this] ?: throw IllegalStateException(
        "Unable to view item definition for viewType $this"
    )
  }
}
