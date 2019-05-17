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
interface SelectableDataSource<IT : Any> : DataSource<IT> {

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
  fun select(item: IT): Boolean {
    val index = indexOf(item)
    if (index == -1) return false
    return selectAt(index)
  }

  /**
   * Finds an item in the [DataSource], and deselects if it it's found.
   */
  fun deselect(item: IT): Boolean {
    val index = indexOf(item)
    if (index == -1) return false
    return deselectAt(index)
  }

  /**
   * If the given item is selected, deselect it. Else select it.
   */
  fun toggleSelection(item: IT): Boolean {
    val index = indexOf(item)
    if (index == -1) return false
    return toggleSelectionAt(index)
  }

  /**
   * Finds an item in the [DataSource], and returns true if it's selected.
   */
  fun isSelected(item: IT): Boolean {
    val index = indexOf(item)
    if (index == -1) return false
    return isSelectedAt(index)
  }

  /** Returns how many items are currently selected. */
  fun getSelectionCount(): Int

  /** Returns true if at least one item is selected. */
  fun hasSelection(): Boolean = getSelectionCount() > 0

  /** Sets a callback that's invoked when the selection is changed. */
  fun onSelectionChange(block: (SelectableDataSource<IT>) -> Unit)

  /** Gets a list of all selected items in the data source. */
  fun getSelectedItems(): List<IT>
}

/**
 * Constructs a [DataSource] with an initial list of items of any type. This data source
 * allows you to select and deselect items.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun selectableDataSourceOf(items: List<Any>): SelectableDataSource<Any> =
  selectableDataSourceTypedOf(items)

/**
 * Constructs a [DataSource] with an initial list of items of type [IT]. This data source
 * allows you to select and deselect items.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun <IT : Any> selectableDataSourceTypedOf(items: List<IT>): SelectableDataSource<IT> =
  RealSelectableDataSource(items.toMutableList())

/**
 * Constructs a [DataSource] with an initial set of items of any type. This data source
 * allows you to select and deselect items.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun selectableDataSourceOf(vararg items: Any): SelectableDataSource<Any> =
  selectableDataSourceTypedOf(items)

/**
 * Constructs a [DataSource] with an initial set of items of type [IT]. This data source
 * allows you to select and deselect items.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun <IT : Any> selectableDataSourceTypedOf(vararg items: IT): SelectableDataSource<IT> =
  RealSelectableDataSource(items.toMutableList())

/**
 * Constructs a data source that is empty by default. This data source
 * allows you to select and deselect items.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun emptySelectableDataSource(): SelectableDataSource<Any> = emptySelectableDataSourceTyped()

/**
 * Constructs a data source that is empty by default, containing items of type [IT]. This
 * data source allows you to select and deselect items.
 *
 * @author Aidan Follestad (@afollestad)
 */
fun <IT : Any> emptySelectableDataSourceTyped(): SelectableDataSource<IT> =
  RealSelectableDataSource()
