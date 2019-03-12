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
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.internal.showOrHide

/**
 * Represents a handle to Recyclical as it is setup and manipulating
 * a RecyclerView. Provides utility functions to be used by [DataSource]'s.
 *
 * @author Aidan Follestad (@afollestad)
 */
data class RecyclicalHandle(
  private val emptyView: View?,
  private val adapter: RecyclerView.Adapter<*>,
  val dataSource: DataSource,
  val itemClassToType: Map<String, Int>,
  val bindingsToTypes: Map<Int, ItemDefinition<*>>
) {
  /** Shows the empty view. */
  fun showEmptyView() = emptyView.showOrHide(true)

  /** Hides the empty view. */
  fun hideEmptyView() = emptyView.showOrHide(true)

  /** Shows the empty view if [show] is true, else hides it. */
  fun showOrHideEmptyView(show: Boolean) = emptyView.showOrHide(show)

  /** Gets the underlying adapter for the RecyclerView. */
  fun getAdapter(): RecyclerView.Adapter<*> = adapter

  /**
   * Executes code in the given [block] on the current adapter,
   * then invalidates whether the empty view is visible or not
   * based on [DataSource.isEmpty].
   */
  fun invalidateList(block: Adapter<*>.() -> Unit) {
    getAdapter().block()
    showOrHideEmptyView(dataSource.isEmpty())
  }

  internal fun attachDataSource() = dataSource.attach(this)

  internal fun detachDataSource() = dataSource.detach()
}

/** Gets the current data source, auto casting it to [T]. */
inline fun <reified T : DataSource> RecyclicalHandle.getDataSource(): T {
  return dataSource as T
}
