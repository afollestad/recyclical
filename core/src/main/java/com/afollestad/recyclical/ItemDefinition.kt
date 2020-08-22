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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.viewbinding.ViewBinding
import com.afollestad.recyclical.itemdefinition.RealItemDefinition
import com.afollestad.recyclical.viewholder.SelectionStateProvider

typealias ViewHolder = androidx.recyclerview.widget.RecyclerView.ViewHolder

typealias ItemClickListener<IT> = SelectionStateProvider<IT>.(index: Int) -> Unit
typealias ChildViewClickListener<IT, VT> = SelectionStateProvider<IT>.(index: Int, view: VT) -> Unit
typealias ViewHolderCreator<VH> = (layoutBinding: ViewBinding) -> VH
typealias ViewHolderBinder<VH, IT> = VH.(index: Int, item: IT) -> Unit
typealias IdGetter<IT> = (item: IT) -> Number
typealias RecycledCallback<VH> = (viewHolder: VH) -> Unit

/**
 * Represents the association of a model class to a layout and view model. Also responsible for
 * item-specific click listeners, etc.
 *
 * @author Aidan Follestad (@afollestad)
 */
@RecyclicalMarker
interface ItemDefinition<out IT : Any, VH : ViewHolder, VB: ViewBinding> {
  /**
   * Sets a binder that binds this item to a view holder before being displayed in the
   * RecyclerView.
   */
  fun onBind(
    viewHolderCreator: (layoutBinding: VB) -> VH,
    block: ViewHolderBinder<VH, IT>
  ): ItemDefinition<IT, VH, VB>

  /**
   * Sets a callback that's invoked when items of this type are clicked.
   */
  fun onClick(block: ItemClickListener<IT>): ItemDefinition<IT, VH, VB>

  /**
   * Sets a callback that's invoked when items of this type are long clicked.
   */
  fun onLongClick(block: ItemClickListener<IT>): ItemDefinition<IT, VH, VB>

  /**
   * Sets a callback that gets a unique ID for each item of this type.
   */
  fun hasStableIds(idGetter: IdGetter<IT>): ItemDefinition<IT, VH, VB>

  /**
   * Sets a callback that's invoked when a view holder is recycled by the underlying adapter.
   */
  fun onRecycled(block: RecycledCallback<VH>)
}

/**
 * Defines an item definition which binds a model with a view model and layout.
 *
 * @param itemClassName You can override the item class name used for mapping internally by
 *  using the fully qualified class name (e.g. com.yourapp.Model). It defaults to the class
 *  name taken from your generic [IT] type.
 *
 * @author Aidan Follestad (@afollestad)
 */
inline fun <reified IT : Any, VH : ViewHolder, VB: ViewBinding> RecyclicalSetup.withItem(
  noinline layoutBinding: (LayoutInflater, ViewGroup, Boolean) -> VB,
  itemClassName: String = IT::class.java.name,
  noinline block: ItemDefinition<IT, VH, VB>.() -> Unit
): ItemDefinition<IT, VH, VB> {
  return RealItemDefinition<IT, VH, VB>(this, itemClassName)
      .apply(block)
      .also { definition ->
        registerItemDefinition(
            layoutBinding = layoutBinding,
            definition = definition
        )
      }
}
