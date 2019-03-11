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
import android.view.View.OnClickListener
import android.view.View.OnLongClickListener
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView.ViewHolder

typealias ItemClickListener<IT> = (index: Int, item: IT) -> Unit
typealias ViewHolderCreator<VH> = (itemView: View) -> VH
typealias ViewHolderBinder<VH, IT> = VH.(index: Int, item: IT) -> Unit

/** @author Aidan Follestad (@afollestad) */
@RecyclicalMarker
class ItemDefinition<IT : Any>(
  internal val setup: RecyclicalSetup,
  itemClass: Class<IT>
) {
  val itemClassName: String = itemClass.name
  private var itemOnClick: ItemClickListener<Any>? = null
  private var itemOnLongClick: ItemClickListener<Any>? = null
  private var creator: ViewHolderCreator<*>? = null
  private var binder: ViewHolderBinder<*, *>? = null

  /**
   * Sets a binder that binds this item to a view holder before being displayed in the
   * RecyclerView.
   */
  fun <VH : ViewHolder> onBind(
    viewHolderCreator: ViewHolderCreator<VH>,
    block: ViewHolderBinder<VH, IT>
  ): ItemDefinition<IT> {
    this.creator = viewHolderCreator
    this.binder = block
    return this
  }

  /**
   * Sets a callback that's invoked when items of this type are clicked.
   */
  fun onClick(block: ItemClickListener<IT>): ItemDefinition<IT> {
    this.itemOnClick = (block as (Int, Any) -> Unit)
    return this
  }

  /**
   * Sets a callback that's invoked when items of this type are long clicked.
   */
  fun onLongClick(block: ItemClickListener<IT>): ItemDefinition<IT> {
    this.itemOnLongClick = (block as (Int, Any) -> Unit)
    return this
  }

  internal fun createViewHolder(itemView: View): ViewHolder {
    if (this.itemOnClick != null || setup.globalOnClick != null) {
      itemView.setOnClickListener(viewClickListener)
      itemView.makeBackgroundSelectable()
    }
    if (this.itemOnLongClick != null || setup.globalOnLongClick != null) {
      itemView.setOnLongClickListener(viewLongClickListener)
      itemView.makeBackgroundSelectable()
    }

    val viewHolderCreator = creator as? ViewHolderCreator<ViewHolder>
        ?: throw IllegalStateException(
            "View holder creator not provided for item definition $itemClassName"
        )
    return viewHolderCreator.invoke(itemView)
  }

  internal fun bindViewHolder(
    viewHolder: ViewHolder,
    item: Any,
    position: Int
  ) {
    val castedItem = item as? IT ?: throw IllegalStateException(
        "Unable to cast ${item.javaClass.name} to $itemClassName"
    )
    viewHolder.itemView.setTag(R.id.rec_view_item_adapter_position, position)

    val viewHolderBinder = binder as? ViewHolderBinder<ViewHolder, Any>
    viewHolderBinder?.invoke(viewHolder, position, castedItem)
  }

  private val viewClickListener = OnClickListener { itemView ->
    val position = itemView.getTag(R.id.rec_view_item_adapter_position) as? Int
        ?: throw IllegalStateException("Didn't find viewType in itemView tag.")
    val item = setup.dataSource?.get(position)
        ?: throw IllegalStateException("Data source unexpectedly null.")

    this.itemOnClick?.invoke(position, item)
    setup.globalOnClick?.invoke(position, item)
  }

  private val viewLongClickListener = OnLongClickListener { itemView ->
    val position = itemView.getTag(R.id.rec_view_item_adapter_position) as? Int
        ?: throw IllegalStateException("Didn't find viewType in itemView tag.")
    val item = setup.dataSource?.get(position)
        ?: throw IllegalStateException("Data source unexpectedly null.")

    this.itemOnLongClick?.invoke(position, item)
    setup.globalOnLongClick?.invoke(position, item)
    true
  }
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
  val definition = ItemDefinition(this, IT::class.java)
      .apply { block() }

  itemClassToType[definition.itemClassName] = layoutRes
  bindingsToTypes[layoutRes] = definition
  return definition
}
