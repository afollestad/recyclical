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
@file:Suppress("unused")

package com.afollestad.recyclical.datasource

/**
 * A [DataSource] which provides an interface to manage selected items in the list.
 *
 * @author Aidan Follestad (@afollestad)
 */
interface SelectableDataSource : DataSource {

  /** Selects the item at the given [index]. */
  fun selectAt(index: Int): Boolean

  /** De-selects the item at the given [index]. */
  fun deselectAt(index: Int): Boolean

  /** If the item at the given [index] is selected, deselect it. Else select it. */
  fun toggleSelectionAt(index: Int): Boolean {
    return if (isSelectedAt(index)) {
      deselectAt(index)
    } else {
      selectAt(index)
    }
  }

  /** Returns true if the item at [index] is selected. */
  fun isSelectedAt(index: Int): Boolean

  /** Selects all items currently in the [DataSource]. */
  fun selectAll(): Boolean

  /** Deselects all items. */
  fun deselectAll(): Boolean

  /**
   * Finds an item in the [DataSource], and selects if it it's found.
   */
  fun select(item: Any): Boolean {
    val index = indexOf(item)
    if (index == -1) return false
    return selectAt(index)
  }

  /**
   * Finds an item in the [DataSource], and deselects if it it's found.
   */
  fun deselect(item: Any): Boolean {
    val index = indexOf(item)
    if (index == -1) return false
    return deselectAt(index)
  }

  /**
   * If the given item is selected, deselect it. Else select it.
   */
  fun toggleSelection(item: Any): Boolean {
    val index = indexOf(item)
    if (index == -1) return false
    return toggleSelectionAt(index)
  }

  /**
   * Finds an item in the [DataSource], and returns true if it's selected.
   */
  fun isSelected(item: Any): Boolean {
    val index = indexOf(item)
    if (index == -1) return false
    return isSelectedAt(index)
  }

  /** Returns how many items are currently selected. */
  fun getSelectionCount(): Int

  /** Returns true if at least one item is selected. */
  fun hasSelection(): Boolean = getSelectionCount() > 0

  /** Sets a callback that's invoked when the selection is changed. */
  fun onSelectionChange(block: (SelectableDataSource) -> Unit)
}

class RealSelectableDataSource(
  initialData: List<Any> = mutableListOf()
) : RealDataSource(initialData),
    SelectableDataSource {
  private val selectedIndices = mutableSetOf<Int>()
  private var onSelectionChange: ((SelectableDataSource) -> Unit)? = null

  override fun selectAt(index: Int): Boolean {
    if (index < 0 || index >= size()) {
      return false
    }
    return maybeNotifyCallback {
      selectedIndices.add(index)
      invalidateAt(index)
    }
  }

  override fun deselectAt(index: Int): Boolean {
    if (index < 0 || index >= size()) {
      return false
    }
    return maybeNotifyCallback {
      selectedIndices.remove(index)
      invalidateAt(index)
    }
  }

  override fun isSelectedAt(index: Int): Boolean = selectedIndices.contains(index)

  override fun selectAll(): Boolean {
    return maybeNotifyCallback {
      selectedIndices.addAll(0 until size())
      invalidateAll()
    }
  }

  override fun deselectAll(): Boolean {
    return maybeNotifyCallback {
      selectedIndices.clear()
      invalidateAll()
    }
  }

  override fun getSelectionCount(): Int = selectedIndices.size

  override fun onSelectionChange(block: (SelectableDataSource) -> Unit) {
    this.onSelectionChange = block
  }

  override fun insert(
    index: Int,
    item: Any
  ) {
    val greaterIndices = selectedIndices
        .filter { it >= index }
        .sortedByDescending { it }
    greaterIndices.forEach { selectedIndices.remove(it) }
    greaterIndices.map { it + 1 }
        .filter { it < size() }
        .forEach { selectedIndices.add(it) }
    super.insert(index, item)
  }

  override fun swap(
    left: Int,
    right: Int
  ) {
    val leftSelected = isSelectedAt(left)
    val rightSelected = isSelectedAt(right)
    if (leftSelected) {
      selectAt(right)
    } else {
      deselectAt(right)
    }
    if (rightSelected) {
      selectAt(left)
    } else {
      deselectAt(left)
    }
    super.swap(left, right)
  }

  override fun move(
    from: Int,
    to: Int
  ) {
    if (isSelectedAt(from)) {
      deselectAt(from)
      selectAt(to)
    }
    super.move(from, to)
  }

  override fun removeAt(index: Int) {
    deselectAt(index)
    super.removeAt(index)
  }

  override fun clear() {
    deselectAll()
    super.clear()
  }

  private fun maybeNotifyCallback(block: () -> Unit): Boolean {
    val before = selectedIndices.size
    block()
    if (selectedIndices.size != before) {
      onSelectionChange?.invoke(this)
      return true
    }
    return false
  }
}

/**
 * Constructs a [DataSource] with an initial list of items of any type. This data source
 * allows you to select and deselect items.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun selectableDataSourceOf(items: List<Any>): SelectableDataSource =
  RealSelectableDataSource(items.toMutableList())

/**
 * Constructs a [DataSource] with an initial set of items of any type. This data source
 * allows you to select and deselect items.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun selectableDataSourceOf(vararg items: Any): SelectableDataSource =
  RealSelectableDataSource(items.toMutableList())

/**
 * Constructs a data source that is empty by default. This data source
 * allows you to select and deselect items.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun emptySelectableDataSource(): SelectableDataSource =
  RealSelectableDataSource()
