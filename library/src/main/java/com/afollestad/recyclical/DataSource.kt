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
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * Provides a data set for a RecyclerView to bind and display.
 *
 * @author Aidan Follestad (@afollestad)
 */
interface DataSource {
  /** The items at the foundation of the data source. */
  val underlyingItems: List<Any>

  /**
   * Attaches the data source to a setup object and adapter. This doesn't need to be manually
   * called.
   */
  @RestrictTo(LIBRARY)
  fun attach(
    setup: RecyclicalSetup,
    adapter: DefinitionAdapter
  )

  /**
   * Invalidates the empty view given by the user based on the content of the source. This
   * doesn't need to be manually called.
   */
  @RestrictTo(LIBRARY)
  fun invalidateEmptyView()

  /** Retrieves an item at a given index from the data source */
  operator fun get(index: Int): Any = underlyingItems[index]

  /** Appends an item to the data source. */
  operator fun plusAssign(item: Any) {
    add(item)
  }

  /** Returns an iterator to loop over all items in the data source. */
  operator fun iterator(): Iterator<Any> = underlyingItems.iterator()

  /** Returns true if the data source contains the given item. */
  fun contains(item: Any): Boolean

  /** Appends an item to the data source. */
  fun add(item: Any)

  /**
   * Replaces the whole contents of the data source. If [areTheSame] AND [areContentsTheSame] are
   * both provided, [DiffUtil] will be used.
   */
  fun set(
    newItems: List<Any>,
    areTheSame: LeftAndRightComparer? = null,
    areContentsTheSame: LeftAndRightComparer? = null
  )

  /** Inserts an item into the dats source at a given index. */
  fun insert(
    index: Int,
    item: Any
  )

  /** Removes an item from the data source at a given index. */
  fun removeAt(index: Int)

  /** Removes a given item from the data source, if it exists. */
  fun remove(item: Any)

  /** Swaps two items at given indices in the data source. */
  fun swap(
    left: Int,
    right: Int
  )

  /** Moves an item to another index in the data source. */
  fun move(
    from: Int,
    to: Int
  )

  /** Clears all items from the data source, making it empty. */
  fun clear()

  /** Returns how many items are in the data source. */
  fun size(): Int

  /** Returns true if the data source is empty. */
  fun isEmpty(): Boolean

  /** Returns true if the data source is NOT empty. */
  fun isNotEmpty(): Boolean

  /** Calls [block] for each item in the data source. */
  fun forEach(block: (Any) -> Unit)

  /** Returns the index of the first item matching the given predicate. */
  fun indexOfFirst(predicate: (Any) -> Boolean): Int

  /** Returns the index of the last item matching the given predicate. */
  fun indexOfLast(predicate: (Any) -> Boolean): Int
}

/** @author Aidan Follestad (@afollestad) */
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

  override fun contains(item: Any) = items.contains(item)

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

/**
 * Constructs a [DataSource] with an initial list of items of any type.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun dataSourceOf(items: List<Any>): DataSource = RealDataSource(items.toMutableList())

/**
 * Constructs a [DataSource] with an initial set of items of any type.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun dataSourceOf(vararg items: Any): DataSource = RealDataSource(items.toMutableList())

/**
 * Constructs a data source that is empty by default.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun emptyDataSource(): DataSource = RealDataSource()

/**
 * Same as [DataSource.forEach] but only emits items of a certain type.
 *
 * @author Aidan Follestad (@afollestad)
 */
inline fun <reified T : Any> DataSource.forEachOf(block: (T) -> Unit) {
  underlyingItems.filter { it is T }
      .forEach { block(it as T) }
}
