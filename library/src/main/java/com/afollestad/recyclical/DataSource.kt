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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

interface DataSource {
  val underlyingItems: List<Any>

  fun attach(
    setup: RecyclicalSetup,
    adapter: DefinitionAdapter
  )

  fun invalidateEmptyView()

  operator fun get(index: Int): Any

  operator fun plusAssign(item: Any)

  operator fun iterator(): Iterator<Any>

  fun add(item: Any)

  fun set(
    newItems: List<Any>,
    areTheSame: LeftAndRightComparer? = null,
    areContentsTheSame: LeftAndRightComparer? = null
  )

  fun insert(
    index: Int,
    item: Any
  )

  fun removeAt(index: Int)

  fun remove(item: Any)

  fun swap(
    left: Int,
    right: Int
  )

  fun move(
    from: Int,
    to: Int
  )

  fun clear()

  fun size(): Int

  fun isEmpty(): Boolean

  fun isNotEmpty(): Boolean

  fun forEach(block: (Any) -> Unit)

  fun indexOfFirst(predicate: (Any) -> Boolean): Int

  fun indexOfLast(predicate: (Any) -> Boolean): Int
}

class RealDataSource internal constructor(
  initialData: List<Any> = mutableListOf()
) : DataSource {
  private var items = if (initialData is MutableList) {
    initialData
  } else {
    initialData.toMutableList()
  }
  private var adapter: DefinitionAdapter? = null
  private var recyclerView: RecyclerView? = null
  private var emptyView: View? = null

  override val underlyingItems: List<Any>
    get() = items

  override fun attach(
    setup: RecyclicalSetup,
    adapter: DefinitionAdapter
  ) {
    this.adapter = adapter
    this.emptyView = setup.emptyView
  }

  override fun invalidateEmptyView() = emptyView.showOrHide(isEmpty())

  override operator fun get(index: Int): Any = items[index]

  override operator fun plusAssign(item: Any) {
    add(item)
  }

  override operator fun iterator(): Iterator<Any> = items.iterator()

  override fun add(item: Any) {
    items.add(item)
    ensureAttached().notifyItemInserted(items.size - 1)
    invalidateEmptyView()
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
      adapter?.let { diffResult.dispatchUpdatesTo(it) }
    } else {
      this.items = newItems.toMutableList()
      ensureAttached().notifyDataSetChanged()
    }
    invalidateEmptyView()
  }

  override fun insert(
    index: Int,
    item: Any
  ) {
    items.add(index, item)
    ensureAttached().notifyItemInserted(index)
    invalidateEmptyView()
  }

  override fun removeAt(index: Int) {
    items.removeAt(index)
    ensureAttached().notifyItemRemoved(index)
    invalidateEmptyView()
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
    ensureAttached().notifyItemChanged(left)
    ensureAttached().notifyItemChanged(right)
  }

  override fun move(
    from: Int,
    to: Int
  ) {
    val item = items[from]
    items.removeAt(from)
    items.add(to, item)
    ensureAttached().notifyItemMoved(from, to)
  }

  override fun clear() {
    items.clear()
    ensureAttached().notifyDataSetChanged()
    invalidateEmptyView()
  }

  override fun size() = items.size

  override fun isEmpty() = items.isEmpty()

  override fun isNotEmpty() = items.isNotEmpty()

  override fun forEach(block: (Any) -> Unit) = items.forEach(block)

  override fun indexOfFirst(predicate: (Any) -> Boolean): Int = items.indexOfFirst(predicate)

  override fun indexOfLast(predicate: (Any) -> Boolean): Int = items.indexOfLast(predicate)

  private fun ensureAttached() = adapter ?: throw IllegalStateException("Not attached")
}

fun dataSourceOf(items: List<Any>): DataSource = RealDataSource(items.toMutableList())

fun dataSourceOf(vararg items: Any): DataSource = RealDataSource(items.toMutableList())

fun emptyDataSource(): DataSource = RealDataSource()

inline fun <reified T : Any> DataSource.forEachOf(block: (T) -> Unit) {
  underlyingItems.filter { it is T }
      .forEach { block(it as T) }
}
