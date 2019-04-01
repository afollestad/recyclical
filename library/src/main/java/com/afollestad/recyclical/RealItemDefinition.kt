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

package com.afollestad.recyclical

import android.view.View
import androidx.annotation.VisibleForTesting
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.datasource.SelectableDataSource
import com.afollestad.recyclical.internal.blowUp
import com.afollestad.recyclical.internal.makeBackgroundSelectable
import com.afollestad.recyclical.viewholder.NoOpSelectionStateProvider
import com.afollestad.recyclical.viewholder.RealSelectionStateProvider
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
  val currentDataSource: DataSource?
    get() = setup.currentDataSource

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  internal var itemOnClick: ItemClickListener<Any>? = null
  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  internal var itemOnLongClick: ItemClickListener<Any>? = null

  internal var creator: ViewHolderCreator<*>? = null
  internal var binder: ViewHolderBinder<*, *>? = null
  internal var idGetter: IdGetter<Any>? = null

  override fun <VH : ViewHolder> onBind(
    viewHolderCreator: ViewHolderCreator<VH>,
    block: ViewHolderBinder<VH, IT>
  ): ItemDefinition<IT> {
    this.creator = viewHolderCreator
    this.binder = block
    return this
  }

  override fun onClick(block: ItemClickListener<IT>): ItemDefinition<IT> {
    this.itemOnClick = (block as SelectionStateProvider.(Int, Any) -> Unit)
    return this
  }

  override fun onLongClick(block: ItemClickListener<IT>): ItemDefinition<IT> {
    this.itemOnLongClick = (block as SelectionStateProvider.(Int, Any) -> Unit)
    return this
  }

  override fun hasStableIds(idGetter: IdGetter<IT>): ItemDefinition<IT> {
    this.idGetter = idGetter as IdGetter<Any>
    return this
  }

  internal val viewClickListener = View.OnClickListener { itemView ->
    val position = itemView.positionTag()
    val item = setup.requireItemFromDataSource(position)

    getSelectionStateProvider(position).use {
      this.itemOnClick?.invoke(it, position, item)
      setup.globalOnClick?.invoke(it, position, item)
    }
  }

  internal val viewLongClickListener = View.OnLongClickListener { itemView ->
    val position = itemView.positionTag()
    val item = setup.requireItemFromDataSource(position)

    getSelectionStateProvider(position)
        .use {
          this.itemOnLongClick?.invoke(it, position, item)
          setup.globalOnLongClick?.invoke(it, position, item)
        }
    true
  }
}

internal fun ItemDefinition<*>.createViewHolder(itemView: View): ViewHolder {
  val realDefinition = realDefinition()
  val setup = realDefinition.setup

  if (realDefinition.itemOnClick != null || setup.globalOnClick != null) {
    itemView.setOnClickListener(realDefinition.viewClickListener)
    itemView.makeBackgroundSelectable()
  }
  if (realDefinition.itemOnLongClick != null || setup.globalOnLongClick != null) {
    itemView.setOnLongClickListener(realDefinition.viewLongClickListener)
    itemView.makeBackgroundSelectable()
  }

  val viewHolderCreator = realDefinition.creator as? ViewHolderCreator<ViewHolder>
      ?: blowUp(
          "View holder creator not provided for item definition ${realDefinition.itemClassName}"
      )
  return viewHolderCreator.invoke(itemView)
}

internal fun ItemDefinition<*>.bindViewHolder(
  viewHolder: ViewHolder,
  item: Any,
  position: Int
) {
  val realDefinition = realDefinition()
  viewHolder.itemView.run {
    setTag(R.id.rec_view_item_adapter_position, position)
    setTag(R.id.rec_view_item_selectable_data_source, realDefinition.currentDataSource)
  }

  val viewHolderBinder = realDefinition.binder as? ViewHolderBinder<ViewHolder, Any>
  viewHolderBinder?.invoke(viewHolder, position, item)

  // Make sure we cleanup this reference, the data source shouldn't be held onto in views
  viewHolder.itemView.setTag(R.id.rec_view_item_selectable_data_source, null)
}

private fun ItemDefinition<*>.realDefinition(): RealItemDefinition<*> {
  return this as? RealItemDefinition<*> ?: blowUp("$this is not a RealItemDefinition")
}

private fun ItemDefinition<*>.getSelectionStateProvider(position: Int): SelectionStateProvider {
  val dataSourceToUse = getDataSource<SelectableDataSource>()
  return if (dataSourceToUse != null) {
    RealSelectionStateProvider(dataSourceToUse, position)
  } else {
    NoOpSelectionStateProvider()
  }
}

private fun View.positionTag(): Int {
  return getTag(R.id.rec_view_item_adapter_position) as? Int ?: blowUp(
      "Didn't find position in itemView tag."
  )
}

private fun RecyclicalSetup.requireItemFromDataSource(position: Int): Any {
  return currentDataSource?.get(position) ?: blowUp("Data source unexpectedly null.")
}
