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
package com.afollestad.recyclical.testdata

import androidx.recyclerview.widget.RecyclerView
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.plugins.PluginData
import com.afollestad.recyclical.testutil.assertNull
import com.afollestad.recyclical.testutil.assertSameAs

class TestPluginData : PluginData() {
  private var attachedView: RecyclerView? = null
  private var dataSource: DataSource<*>? = null

  override fun attach(
    toView: RecyclerView,
    dataSource: DataSource<*>
  ) {
    this.attachedView = toView
    this.dataSource = dataSource
  }

  fun expectAttached(
    recyclerView: RecyclerView,
    dataSource: DataSource<*>
  ) {
    this.attachedView.assertSameAs(recyclerView)
    this.dataSource.assertSameAs(dataSource)
  }

  fun expectNotAttached() {
    this.attachedView.assertNull()
    this.dataSource.assertNull()
  }
}
