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

package com.afollestad.recyclical.itemdefinition

import android.view.View
import androidx.annotation.VisibleForTesting
import com.afollestad.recyclical.ChildViewClickListener
import com.afollestad.recyclical.IdGetter
import com.afollestad.recyclical.ItemClickListener
import com.afollestad.recyclical.ItemDefinition
import com.afollestad.recyclical.RecyclicalMarker
import com.afollestad.recyclical.RecyclicalSetup
import com.afollestad.recyclical.ViewHolder
import com.afollestad.recyclical.ViewHolderBinder
import com.afollestad.recyclical.ViewHolderCreator
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.viewholder.SelectionStateProvider

/** @author Aidan Follestad (@afollestad) */
@RecyclicalMarker
class RealItemDefinition<IT : Any>(
  internal val setup: RecyclicalSetup,
  itemClass: Class<IT>
) : ItemDefinition<IT> {
  /** The full name of the item model class this definition binds. */
  val itemClassName: String = itemClass.name
  /** The current data source set in setup. */
  val currentDataSource: DataSource<*>?
    get() = setup.currentDataSource

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  internal var itemOnClick: ItemClickListener<Any>? = null
  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  internal var itemOnLongClick: ItemClickListener<Any>? = null

  internal var creator: ViewHolderCreator<*>? = null
  internal var binder: ViewHolderBinder<*, *>? = null
  internal var idGetter: IdGetter<Any>? = null

  var childClickDataList = mutableListOf<ChildClickData<*, *, *>>()

  override fun <VH : ViewHolder> onBind(
    viewHolderCreator: ViewHolderCreator<VH>,
    block: ViewHolderBinder<VH, IT>
  ): ItemDefinition<IT> {
    this.creator = viewHolderCreator
    this.binder = block
    return this
  }

  override fun onClick(block: ItemClickListener<IT>): ItemDefinition<IT> {
    this.itemOnClick = (block as SelectionStateProvider<Any>.(Int) -> Unit)
    return this
  }

  override fun onLongClick(block: ItemClickListener<IT>): ItemDefinition<IT> {
    this.itemOnLongClick = (block as SelectionStateProvider<Any>.(Int) -> Unit)
    return this
  }

  override fun hasStableIds(idGetter: IdGetter<IT>): ItemDefinition<IT> {
    this.idGetter = idGetter as IdGetter<Any>
    return this
  }

  internal val viewClickListener = View.OnClickListener { itemView ->
    val position = itemView.positionTag()
    getSelectionStateProvider(position).use {
      this.itemOnClick?.invoke(it, position)
      setup.globalOnClick?.invoke(it, position)
    }
  }

  internal val viewLongClickListener = View.OnLongClickListener { itemView ->
    val position = itemView.positionTag()
    getSelectionStateProvider(position)
        .use {
          this.itemOnLongClick?.invoke(it, position)
          setup.globalOnLongClick?.invoke(it, position)
        }
    true
  }

  /** @author Aidan Follestad (@afollestad) */
  data class ChildClickData<in IT : Any, VH : ViewHolder, VT : View>(
    val viewHolderType: Class<VH>,
    val child: (viewHolder: VH) -> VT,
    val callback: ChildViewClickListener<IT, VT>
  )
}
