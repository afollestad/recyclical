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

/** @author Aidan Follestad (@afollestad) */
open class RealSelectableDataSource(
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
