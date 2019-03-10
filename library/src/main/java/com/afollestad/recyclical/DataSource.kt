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
@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.afollestad.recyclical

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class DataSource(initialData: List<Any> = mutableListOf()) {
  private var items = if (initialData is MutableList) {
    initialData
  } else {
    initialData.toMutableList()
  }
  private var adapter: DefinitionAdapter? = null
  private var recyclerView: RecyclerView? = null
  private var emptyView: View? = null

  internal fun attachAdapter(adapter: DefinitionAdapter) {
    this.adapter = adapter
  }

  internal fun finishSetup(setup: RecyclicalSetup) {
    this.emptyView = setup.emptyView
  }

  operator fun get(index: Int): Any = items[index]

  operator fun plusAssign(item: Any) {
    items.add(item)
    ensureAttached().notifyItemInserted(items.size - 1)
    invalidateEmptyView()
  }

  operator fun iterator(): Iterator<Any> = items.iterator()

  fun set(
    newItems: List<Any>,
    areTheSame: LeftAndRightComparer? = null,
    areContentsTheSame: LeftAndRightComparer? = null
  ) {
    if (this.items.isNotEmpty() && areTheSame != null && areContentsTheSame != null) {
      val oldItems = this.items
      this.items = newItems.toMutableList()

      val diffCallback = ItemDiffCallback(
          oldItems = oldItems,
          newItems = items,
          areTheSame = areTheSame,
          areContentsTheSame = areContentsTheSame
      )
      val diffResult = DiffUtil.calculateDiff(diffCallback)
      adapter?.let { diffResult.dispatchUpdatesTo(it) }
    } else {
      this.items = newItems.toMutableList()
      ensureAttached().notifyDataSetChanged()
    }
    invalidateEmptyView()
  }

  fun insert(
    index: Int,
    item: Any
  ) {
    items.add(index, item)
    ensureAttached().notifyItemInserted(index)
    invalidateEmptyView()
  }

  fun removeAt(index: Int) {
    items.removeAt(index)
    ensureAttached().notifyItemRemoved(index)
    invalidateEmptyView()
  }

  fun remove(item: Any) {
    val index = items.indexOf(item)
    if (index == -1) return
    removeAt(index)
  }

  fun swap(
    left: Int,
    right: Int
  ) {
    val leftItem = items[left]
    items[left] = items[right]
    items[right] = leftItem
    ensureAttached().notifyItemChanged(left)
    ensureAttached().notifyItemChanged(right)
  }

  fun move(
    from: Int,
    to: Int
  ) {
    val item = items[from]
    items.removeAt(from)
    items.add(to, item)
    ensureAttached().notifyItemMoved(from, to)
  }

  fun clear() {
    items.clear()
    ensureAttached().notifyDataSetChanged()
    invalidateEmptyView()
  }

  fun size() = items.size

  fun isEmpty() = items.isEmpty()

  fun isNotEmpty() = items.isNotEmpty()

  internal fun invalidateEmptyView() = emptyView.showOrHide(isEmpty())

  private fun ensureAttached() = adapter ?: throw IllegalStateException("Not attached")
}

private fun View?.showOrHide(show: Boolean) {
  this?.visibility = if (show) VISIBLE else GONE
}
