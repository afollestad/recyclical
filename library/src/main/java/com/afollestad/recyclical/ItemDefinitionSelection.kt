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

package com.afollestad.recyclical

/**
 * Returns true if the [DataSource] is a [SelectableDataSource] and the item at the given [index]
 * is currently selected.
 */
fun <IT : Any> ItemDefinition<IT>.isSelectedAt(index: Int): Boolean {
  return selectableDataSource?.isSelectedAt(index) ?: false
}

/**
 * If the [DataSource] is a [SelectableDataSource], selects the item at the given [index].
 */
fun <IT : Any> ItemDefinition<IT>.selectAt(index: Int): Boolean {
  return selectableDataSource?.selectAt(index) ?: false
}

/**
 * If the [DataSource] is a [SelectableDataSource], deselects the item at the given [index].
 */
fun <IT : Any> ItemDefinition<IT>.deselectAt(index: Int): Boolean {
  return selectableDataSource?.deselectAt(index) ?: false
}

/**
 * The [DataSource] must be a [SelectableDataSource]. If the item at the given [index] is selected,
 * it is deselected. If it's not selected, then it is selected.
 */
fun <IT : Any> ItemDefinition<IT>.toggleSelectionAt(index: Int): Boolean {
  return selectableDataSource?.toggleSelectionAt(index) ?: false
}

/**
 * Returns true if the [DataSource] is a [SelectableDataSource] and at least one item is selected.
 */
fun <IT : Any> ItemDefinition<IT>.hasSelection(): Boolean {
  return selectableDataSource?.hasSelection() ?: false
}

private val ItemDefinition<*>.selectableDataSource: SelectableDataSource?
  get() = setup.dataSource as? SelectableDataSource
