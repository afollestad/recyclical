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
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView.ViewHolder

typealias ItemClickListener<IT> = (index: Int, item: IT) -> Unit
typealias ViewHolderCreator<VH> = (itemView: View) -> VH
typealias ViewHolderBinder<VH, IT> = VH.(index: Int, item: IT) -> Unit

@RecyclicalMarker
class ItemDefinition<out IT : Any>(
  private val setup: RecyclicalSetup,
  itemClass: Class<IT>
) {
  val itemClassName: String = itemClass.name
  private var itemOnClick: ItemClickListener<Any>? = null
  private var creator: ViewHolderCreator<*>? = null
  private var binder: ViewHolderBinder<*, *>? = null

  fun <VH : ViewHolder> onBind(
    viewHolderCreator: ViewHolderCreator<VH>,
    block: ViewHolderBinder<VH, IT>
  ): ItemDefinition<IT> {
    this.creator = viewHolderCreator
    this.binder = block
    return this
  }

  fun onClick(block: ItemClickListener<IT>): ItemDefinition<IT> {
    this.itemOnClick = (block as (Int, Any) -> Unit)
    return this
  }

  internal fun createViewHolder(itemView: View): ViewHolder {
    if (this.itemOnClick != null || setup.globalOnClick != null) {
      itemView.setOnClickListener(viewClickListener)
      if (itemView.background == null) {
        itemView.background = itemView.context.resolveDrawable(R.attr.selectableItemBackground)
      }
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
}

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
