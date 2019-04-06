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

import android.util.Log
import android.view.View
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import com.afollestad.recyclical.ItemDefinition
import com.afollestad.recyclical.R
import com.afollestad.recyclical.R.id
import com.afollestad.recyclical.ViewHolder
import com.afollestad.recyclical.ViewHolderBinder
import com.afollestad.recyclical.ViewHolderCreator
import com.afollestad.recyclical.datasource.SelectableDataSource
import com.afollestad.recyclical.getDataSource
import com.afollestad.recyclical.internal.blowUp
import com.afollestad.recyclical.internal.makeBackgroundSelectable
import com.afollestad.recyclical.viewholder.NoSelectionStateProvider
import com.afollestad.recyclical.viewholder.RealSelectionStateProvider
import com.afollestad.recyclical.viewholder.SelectionStateProvider

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
      .also {
        setChildClickListeners(it)
      }
}

private fun ItemDefinition<*>.setChildClickListeners(viewHolder: ViewHolder) {
  val realDefinition = realDefinition()
  if (realDefinition.childClickDatas.isEmpty()) {
    return
  }

  val clickDatas = realDefinition.childClickDatas.filter {
    it.viewHolderType == viewHolder::class.java
  }
  for (item in clickDatas) {
    val viewGetter = item.child as ((ViewHolder) -> View)
    val callback = item.callback as (SelectionStateProvider<Any>.(Int, Any) -> Unit)
    val childView = viewGetter(viewHolder)

    childView.setOnClickListener {
      val index = viewHolder.itemView.positionTag()
      getSelectionStateProvider(index).use {
        callback(it, index, childView)
      }
      Log.d("IDK", "After")
    }
  }
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
  viewHolder.itemView.setTag(id.rec_view_item_selectable_data_source, null)
}

internal fun <IT : Any> ItemDefinition<IT>.getSelectionStateProvider(
  position: Int
): SelectionStateProvider<IT> {
  val selectableSource = getDataSource<SelectableDataSource>()
  return if (selectableSource != null) {
    RealSelectionStateProvider(selectableSource, position)
  } else {
    NoSelectionStateProvider(getDataSource(), position)
  }
}

internal fun View.positionTag(): Int {
  return getTag(R.id.rec_view_item_adapter_position) as? Int ?: blowUp(
      "Didn't find position in itemView tag."
  )
}

@RestrictTo(LIBRARY)
fun ItemDefinition<*>.realDefinition(): RealItemDefinition<*> {
  return this as? RealItemDefinition<*> ?: blowUp("$this is not a RealItemDefinition")
}
