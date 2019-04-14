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
package com.afollestad.recyclical.handle

import android.view.View
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.afollestad.recyclical.ItemDefinition
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.internal.DefinitionAdapter
import com.afollestad.recyclical.internal.blowUp

/** @author Aidan Follestad (@afollestad) */
class RealRecyclicalHandle internal constructor(
  internal val emptyView: View?,
  private val adapter: DefinitionAdapter,
  private val itemClassToType: MutableMap<String, Int>,
  private val bindingsToTypes: MutableMap<Int, ItemDefinition<*>>,
  val dataSource: DataSource<*>
) : RecyclicalHandle {

  override fun showOrHideEmptyView(show: Boolean) {
    emptyView?.visibility = if (show) View.VISIBLE else View.GONE
  }

  override fun getAdapter(): Adapter<*> = adapter

  override fun invalidateList(block: Adapter<*>.() -> Unit) {
    getAdapter().block()
    showOrHideEmptyView(dataSource.isEmpty())
  }

  override fun getViewTypeForClass(name: String): Int {
    return itemClassToType[name] ?: blowUp("Didn't find type for class $name")
  }

  override fun getDefinitionForClass(name: String): ItemDefinition<*> {
    val viewType = getViewTypeForClass(name)
    return getDefinitionForType(viewType)
  }

  override fun getDefinitionForType(type: Int): ItemDefinition<*> {
    return bindingsToTypes[type] ?: blowUp("Unable to view item definition for viewType $type")
  }

  internal fun attachDataSource() {
    dataSource.attach(this)
    adapter.attach(this)
  }

  internal fun detachDataSource() {
    dataSource.detach()
    adapter.detach()
  }
}
