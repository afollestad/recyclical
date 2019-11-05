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
import com.afollestad.recyclical.handle.AdapterBlock
import com.afollestad.recyclical.handle.RecyclicalHandle
import com.afollestad.recyclical.internal.ItemDiffCallback

/** @author Aidan Follestad (@afollestad) */
open class RealDataSource<IT : Any> internal constructor(
  initialData: List<IT> = mutableListOf()
) : DataSource<IT> {
  private var items = initialData.toMutableList()
  private var handle: RecyclicalHandle? = null
  private var changeListeners: MutableList<DataSourceOnChanged<IT>>? = null

  override operator fun get(index: Int): IT = items[index]

  override operator fun contains(item: IT): Boolean = items.contains(item)

  override operator fun iterator(): Iterator<IT> = items.iterator()

  override fun attach(handle: RecyclicalHandle) {
    if (this.handle != null) return
    this.handle = handle
    invalidateAll()
  }

  override fun detach() {
    this.handle = null
    this.changeListeners?.clear()
    this.changeListeners = null
  }

  override fun add(vararg newItems: IT) {
    val startPosition = this.items.size
    this.items.addAll(newItems)
    invalidateList {
      notifyItemRangeInserted(startPosition, newItems.size)
    }
  }

  override fun addAll(newItems: Collection<IT>) {
    val startPosition = this.items.size
    this.items.addAll(newItems)
    invalidateList {
      notifyItemRangeInserted(startPosition, newItems.size)
    }
  }

  override fun set(
    newItems: List<IT>,
    areTheSame: LeftAndRightComparer<IT>?,
    areContentsTheSame: LeftAndRightComparer<IT>?
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
      invalidateList {
        diffResult.dispatchUpdatesTo(this)
      }
    } else {
      this.items = newItems.toMutableList()
      invalidateAll()
    }
  }

  override fun insert(
    index: Int,
    item: IT
  ) {
    items.add(index, item)
    invalidateList { notifyItemInserted(index) }
  }

  override fun removeAt(index: Int) {
    items.removeAt(index)
    invalidateList { notifyItemRemoved(index) }
  }

  override fun remove(item: IT) {
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
    invalidateList {
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
    invalidateList { notifyItemMoved(from, to) }
  }

  override fun clear() {
    items.clear()
    invalidateAll()
  }

  override fun size(): Int = items.size

  override fun isEmpty(): Boolean = items.isEmpty()

  override fun isNotEmpty(): Boolean = items.isNotEmpty()

  override fun forEach(block: (IT) -> Unit) = items.forEach(block)

  override fun indexOfFirst(predicate: (IT) -> Boolean): Int = items.indexOfFirst(predicate)

  override fun indexOfLast(predicate: (IT) -> Boolean): Int = items.indexOfLast(predicate)

  override fun indexOf(item: IT): Int = items.indexOf(item)

  override fun toList(): List<IT> = items.toList()

  override fun addChangedListener(listener: DataSourceOnChanged<IT>) {
    (changeListeners ?: mutableListOf<DataSourceOnChanged<IT>>()
        .also { changeListeners = it }).add(listener)
  }

  override fun invalidateAt(index: Int) = invalidateList { notifyItemChanged(index) }

  override fun invalidateAll() = invalidateList { notifyDataSetChanged() }

  private fun invalidateList(block: AdapterBlock) {
    handle?.invalidateList(block)
    changeListeners?.forEach { it.onDataSourceChanged(this) }
  }
}
