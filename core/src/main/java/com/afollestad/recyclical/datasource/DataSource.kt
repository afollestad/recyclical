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

package com.afollestad.recyclical.datasource

import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import androidx.recyclerview.widget.DiffUtil
import com.afollestad.recyclical.handle.RecyclicalHandle

typealias LeftAndRightComparer = (left: Any, right: Any) -> Boolean

/**
 * Provides a data set for a RecyclerView to bind and display.
 *
 * @author Aidan Follestad (@afollestad)
 */
interface DataSource {

  /** Retrieves an item at a given index from the data source */
  operator fun get(index: Int): Any

  /** Appends an item to the data source; calls [add] in its default implementation. */
  operator fun plusAssign(item: Any) {
    add(item)
  }

  /** Removes an item from the data source; calls [remove] in its default implementation. */
  operator fun minusAssign(item: Any) {
    remove(item)
  }

  /** Returns true if the data source contains the given item. */
  operator fun contains(item: Any): Boolean

  /** Returns an iterator to loop over all items in the data source. */
  operator fun iterator(): Iterator<Any>

  /**
   * Attaches the data source to an empty view and adapter. This doesn't need to be manually
   * called.
   */
  @RestrictTo(LIBRARY)
  fun attach(handle: RecyclicalHandle)

  /**
   * Detaches the data source, clearing up references to anything that can leak.
   */
  @RestrictTo(LIBRARY)
  fun detach()

  /**
   * Replaces the whole contents of the data source. If [areTheSame] AND [areContentsTheSame] are
   * both provided, [DiffUtil] will be used.
   */
  fun set(
    newItems: List<Any>,
    areTheSame: LeftAndRightComparer? = null,
    areContentsTheSame: LeftAndRightComparer? = null
  )

  /** Appends an item to the data source. */
  fun add(item: Any)

  /** Inserts an item into the data source at a given index. */
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
  fun isEmpty(): Boolean = size() == 0

  /** Returns true if the data source is NOT empty. */
  fun isNotEmpty(): Boolean = !isEmpty()

  /** Calls [block] for each item in the data source. */
  fun forEach(block: (Any) -> Unit)

  /** Returns the index of the first item matching the given predicate. -1 if none. */
  fun indexOfFirst(predicate: (Any) -> Boolean): Int

  /** Returns the index of the last item matching the given predicate. -1 if none. */
  fun indexOfLast(predicate: (Any) -> Boolean): Int

  /** Returns the index of the first item that equals the given. -1 if none. */
  fun indexOf(item: Any): Int = indexOfFirst { it == item }

  /** Used by other [DataSource] implementations to notify that an item has changed state. */
  fun invalidateAt(index: Int)

  /** Used by other [DataSource] implementations to notify that the whole data set has changed state. */
  fun invalidateAll()
}

/**
 * Constructs a [DataSource] with an initial list of items of any type.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun dataSourceOf(items: List<Any>): DataSource =
  RealDataSource(items.toMutableList())

/**
 * Constructs a [DataSource] with an initial set of items of any type.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun dataSourceOf(vararg items: Any): DataSource =
  RealDataSource(items.toMutableList())

/**
 * Constructs a data source that is empty by default.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun emptyDataSource(): DataSource =
  RealDataSource()

/**
 * Same as [DataSource.forEach] but only emits items of a certain type.
 *
 * @author Aidan Follestad (@afollestad)
 */
inline fun <reified T : Any> DataSource.forEachOf(block: (T) -> Unit) {
  iterator()
      .asSequence()
      .filter { it is T }
      .map { it as T }
      .forEach { block(it) }
}
