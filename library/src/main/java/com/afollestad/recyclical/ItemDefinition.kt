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
@file:Suppress("UNCHECKED_CAST", "unused")

package com.afollestad.recyclical

import android.view.View
import androidx.annotation.LayoutRes
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.internal.blowUp
import com.afollestad.recyclical.viewholder.SelectionStateProvider

typealias ViewHolder = androidx.recyclerview.widget.RecyclerView.ViewHolder

typealias ItemClickListener<IT> = SelectionStateProvider.(index: Int, item: IT) -> Unit
typealias ViewHolderCreator<VH> = (itemView: View) -> VH
typealias ViewHolderBinder<VH, IT> = VH.(index: Int, item: IT) -> Unit

/**
 * Represents the association of a model class to a layout and view model. Also responsible for
 * item-specific click listeners, etc.
 *
 * @author Aidan Follestad (@afollestad)
 */
@RecyclicalMarker
interface ItemDefinition<IT : Any> {
  /**
   * Sets a binder that binds this item to a view holder before being displayed in the
   * RecyclerView.
   */
  fun <VH : ViewHolder> onBind(
    viewHolderCreator: ViewHolderCreator<VH>,
    block: ViewHolderBinder<VH, IT>
  ): ItemDefinition<IT>

  /**
   * Sets a callback that's invoked when items of this type are clicked.
   */
  fun onClick(block: ItemClickListener<IT>): ItemDefinition<IT>

  /**
   * Sets a callback that's invoked when items of this type are long clicked.
   */
  fun onLongClick(block: ItemClickListener<IT>): ItemDefinition<IT>
}

/**
 * Defines an item definition which binds a model with a view model and layout.
 *
 * @author Aidan Follestad (@afollestad)
 */
inline fun <reified IT : Any> RecyclicalSetup.withItem(
  @LayoutRes layoutRes: Int,
  noinline block: ItemDefinition<IT>.() -> Unit
): ItemDefinition<IT> {
  return RealItemDefinition(this, IT::class.java)
      .apply(block)
      .also { definition ->
        registerItemDefinition(
            className = definition.itemClassName,
            viewType = layoutRes,
            definition = definition
        )
      }
}

/** Gets the current data source, auto casting to the type [T]. */
inline fun <reified T : DataSource> ItemDefinition<*>.getDataSource(): T? {
  return if (this is RealItemDefinition) {
    currentDataSource as? T
  } else {
    blowUp("$this is not a RealItemDefinition")
  }
}
