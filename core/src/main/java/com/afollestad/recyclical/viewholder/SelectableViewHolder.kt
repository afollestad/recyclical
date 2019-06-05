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

package com.afollestad.recyclical.viewholder

import com.afollestad.recyclical.R
import com.afollestad.recyclical.ViewHolder
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.datasource.SelectableDataSource

/**
 * Must be called from within an onBind block. Returns true if the item is
 * currently selected.
 */
fun ViewHolder.isSelected(): Boolean {
  return selectableDataSource.isSelectedAt(adapterPosition)
}

/**
 * Must be called from within an onBind block. Selects the current item if it isn't
 * already selected.
 */
fun ViewHolder.select(): Boolean {
  return selectableDataSource.selectAt(adapterPosition)
}

/**
 * Must be called from within an onBind block. Deselects the current item if it is
 * currently selected.
 */
fun ViewHolder.deselect(): Boolean {
  return selectableDataSource.selectAt(adapterPosition)
}

/**
 * Must be called from within an onBind block. If the current item is selected, deselects it.
 * Else selects it.
 */
fun ViewHolder.toggleSelection(): Boolean {
  return selectableDataSource.toggleSelectionAt(adapterPosition)
}

/**
 * Must be called from within an onBind block. Returns true if any items in the data source
 * are selected, not necessarily the current item.
 */
fun ViewHolder.hasSelection(): Boolean {
  return selectableDataSource.hasSelection()
}

private val ViewHolder.selectableDataSource: SelectableDataSource<*>
  get() {
    return when (val dataSource =
      itemView.getTag(R.id.rec_view_item_selectable_data_source) as? DataSource<*>) {
      null -> error("isSelected() can only be called from within an onBind block.")
      !is SelectableDataSource -> error("Current data source is not a SelectableDataSource.")
      else -> dataSource
    }
  }
