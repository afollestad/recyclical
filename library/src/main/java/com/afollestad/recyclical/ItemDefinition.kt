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
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.datasource.SelectableDataSource
import com.afollestad.recyclical.internal.makeBackgroundSelectable
import com.afollestad.recyclical.viewholder.NoOpSelectionStateProvider
import com.afollestad.recyclical.viewholder.RealSelectionStateProvider
import com.afollestad.recyclical.viewholder.SelectionStateProvider

typealias ViewHolder = androidx.recyclerview.widget.RecyclerView.ViewHolder

typealias ItemClickListener<IT> = SelectionStateProvider.(index: Int, item: IT) -> Unit
typealias ViewHolderCreator<VH> = (itemView: View) -> VH
typealias ViewHolderBinder<VH, IT> = VH.(index: Int, item: IT) -> Unit

/** @author Aidan Follestad (@afollestad) */
@RecyclicalMarker
class ItemDefinition<IT : Any>(
  private val setup: RecyclicalSetup,
  itemClass: Class<IT>
) {
  /** The full name of the item model class this definition binds. */
  val itemClassName: String = itemClass.name
  /** The current data source set in setup. */
  val currentDataSource: DataSource?
    get() = setup.currentDataSource

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
    this.itemOnClick = (block as SelectionStateProvider.(Int, Any) -> Unit)
    return this
  }

  /**
   * Sets a callback that's invoked when items of this type are long clicked.
   */
  fun onLongClick(block: ItemClickListener<IT>): ItemDefinition<IT> {
    this.itemOnLongClick = (block as SelectionStateProvider.(Int, Any) -> Unit)
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
    viewHolder.itemView.run {
      setTag(R.id.rec_view_item_adapter_position, position)
      setTag(R.id.rec_view_item_selectable_data_source, currentDataSource)
    }

    val viewHolderBinder = binder as? ViewHolderBinder<ViewHolder, Any>
    viewHolderBinder?.invoke(viewHolder, position, castedItem)

    // Make sure we cleanup this reference, the data source shouldn't be held onto in views
    viewHolder.itemView.setTag(R.id.rec_view_item_selectable_data_source, null)
  }

  private val viewClickListener = OnClickListener { itemView ->
    val position = itemView.getTag(R.id.rec_view_item_adapter_position) as? Int
        ?: throw IllegalStateException("Didn't find viewType in itemView tag.")
    val item = setup.currentDataSource?.get(position)
        ?: throw IllegalStateException("Data source unexpectedly null.")

    getSelectionStateProvider(position).use {
      this.itemOnClick?.invoke(it, position, item)
      setup.globalOnClick?.invoke(it, position, item)
    }
  }

  private val viewLongClickListener = OnLongClickListener { itemView ->
    val position = itemView.getTag(R.id.rec_view_item_adapter_position) as? Int
        ?: throw IllegalStateException("Didn't find viewType in itemView tag.")
    val item = setup.currentDataSource?.get(position)
        ?: throw IllegalStateException("Data source unexpectedly null.")

    getSelectionStateProvider(position).use {
      this.itemOnLongClick?.invoke(it, position, item)
      setup.globalOnLongClick?.invoke(it, position, item)
    }
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
  return ItemDefinition(this, IT::class.java)
      .apply { block() }
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
  return currentDataSource as? T
}

private fun ItemDefinition<*>.getSelectionStateProvider(position: Int): SelectionStateProvider {
  val dataSourceToUse = currentDataSource as? SelectableDataSource
  return if (dataSourceToUse != null) {
    RealSelectionStateProvider(dataSourceToUse, position)
  } else {
    NoOpSelectionStateProvider()
  }
}
