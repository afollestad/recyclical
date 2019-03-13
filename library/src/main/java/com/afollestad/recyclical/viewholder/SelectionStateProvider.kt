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
package com.afollestad.recyclical.viewholder

import com.afollestad.recyclical.datasource.SelectableDataSource
import java.io.Closeable

/**
 * Provides methods to be called within blocks, like onClick, to check if the current item
 * is selected or not.
 *
 * @author Aidan Follestad (@afollestad)
 */
interface SelectionStateProvider : Closeable {
  /**
   * Must be called from within an onBind block. Returns true if the item is
   * currently selected.
   */
  fun isSelected(): Boolean

  /**
   * Must be called from within an onBind block. Selects the current item if it isn't
   * already selected.
   */
  fun select(): Boolean

  /**
   * Deselects the current item if it is
   * currently selected.
   */
  fun deselect(): Boolean

  /**
   * If the current item is selected, deselects it. Else selects it.
   */
  fun toggleSelection(): Boolean

  /**
   * Returns true if any item in the data source is selected, not necessarily the
   * current item.
   */
  fun hasSelection(): Boolean
}

/** @author Aidan Follestad (@afollestad) */
class NoOpSelectionStateProvider : SelectionStateProvider {
  override fun isSelected(): Boolean = false

  override fun select(): Boolean = false

  override fun deselect(): Boolean = false

  override fun toggleSelection(): Boolean = false

  override fun hasSelection(): Boolean = false

  override fun close() = Unit
}

/** @author Aidan Follestad (@afollestad) */
class RealSelectionStateProvider(
  dataSource: SelectableDataSource,
  private val index: Int
) : SelectionStateProvider {
  private var selectableDataSource: SelectableDataSource? = dataSource

  override fun isSelected(): Boolean = ensureActive().isSelectedAt(index)

  override fun select(): Boolean = ensureActive().selectAt(index)

  override fun deselect(): Boolean = ensureActive().deselectAt(index)

  override fun toggleSelection(): Boolean = ensureActive().toggleSelectionAt(index)

  override fun hasSelection(): Boolean = ensureActive().hasSelection()

  override fun close() {
    selectableDataSource = null
  }

  private fun ensureActive(): SelectableDataSource {
    return selectableDataSource ?: throw IllegalStateException("Already disposed.")
  }
}
