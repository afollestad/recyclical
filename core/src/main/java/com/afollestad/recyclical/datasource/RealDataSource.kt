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
package com.afollestad.recyclical.datasource

import androidx.recyclerview.widget.DiffUtil
import com.afollestad.recyclical.handle.RecyclicalHandle
import com.afollestad.recyclical.internal.ItemDiffCallback

/** @author Aidan Follestad (@afollestad) */
open class RealDataSource internal constructor(
  initialData: List<Any> = mutableListOf()
) : DataSource {
  private var items = initialData.toMutableList()
  private var handle: RecyclicalHandle? = null

  override operator fun get(index: Int): Any = items[index]

  override operator fun contains(item: Any): Boolean = items.contains(item)

  override operator fun iterator(): Iterator<Any> = items.iterator()

  override fun attach(handle: RecyclicalHandle) {
    if (this.handle != null) return
    this.handle = handle.also {
      it.invalidateList { notifyDataSetChanged() }
    }
  }

  override fun detach() {
    this.handle = null
  }

  override fun add(item: Any) {
    items.add(item)
    handle?.invalidateList {
      notifyItemInserted(items.size - 1)
    }
  }

  override fun set(
    newItems: List<Any>,
    areTheSame: LeftAndRightComparer?,
    areContentsTheSame: LeftAndRightComparer?
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
      handle?.getAdapter()
          ?.let { diffResult.dispatchUpdatesTo(it) }
    } else {
      this.items = newItems.toMutableList()
      handle?.invalidateList { notifyDataSetChanged() }
    }
  }

  override fun insert(
    index: Int,
    item: Any
  ) {
    items.add(index, item)
    handle?.invalidateList { notifyItemInserted(index) }
  }

  override fun removeAt(index: Int) {
    items.removeAt(index)
    handle?.invalidateList { notifyItemRemoved(index) }
  }

  override fun remove(item: Any) {
    val index = items.indexOf(item)
    if (index == -1) return
    removeAt(index)
  }

  override fun swap(
    left: Int,
    right: Int
  ) {
    val leftItem = items[left]
    items[left] = items[right]
    items[right] = leftItem
    handle?.invalidateList {
      notifyItemChanged(left)
      notifyItemChanged(right)
    }
  }

  override fun move(
    from: Int,
    to: Int
  ) {
    val item = items[from]
    items.removeAt(from)
    items.add(to, item)
    handle?.invalidateList { notifyItemMoved(from, to) }
  }

  override fun clear() {
    items.clear()
    handle?.invalidateList { notifyDataSetChanged() }
  }

  override fun size() = items.size

  override fun isEmpty() = items.isEmpty()

  override fun isNotEmpty() = items.isNotEmpty()

  override fun forEach(block: (Any) -> Unit) = items.forEach(block)

  override fun indexOfFirst(predicate: (Any) -> Boolean): Int = items.indexOfFirst(predicate)

  override fun indexOfLast(predicate: (Any) -> Boolean): Int = items.indexOfLast(predicate)

  override fun indexOf(item: Any): Int = items.indexOf(item)

  override fun invalidateAt(index: Int) {
    handle?.invalidateList { notifyItemChanged(index) }
  }

  override fun invalidateAll() {
    handle?.invalidateList { notifyDataSetChanged() }
  }
}
