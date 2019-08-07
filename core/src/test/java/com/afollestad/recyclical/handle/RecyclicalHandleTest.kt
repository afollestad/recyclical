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
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.internal.DefinitionAdapter
import com.afollestad.recyclical.itemdefinition.ItemGraph
import com.afollestad.recyclical.testutil.NoManifestTestRunner
import com.afollestad.recyclical.testutil.assertSameAs
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(NoManifestTestRunner::class)
class RecyclicalHandleTest {
  private val emptyView = mock<View>()
  private val adapter = mock<DefinitionAdapter>()
  private val dataSource = mock<DataSource<*>>()
  private val itemGraph = mock<ItemGraph>()

  private val handle = RealRecyclicalHandle(
      emptyView = emptyView,
      adapter = adapter,
      dataSource = dataSource,
      itemGraph = itemGraph
  )

  @Test fun showOrHideEmptyView_show() {
    handle.emptyView.assertSameAs(emptyView)
    handle.showOrHideEmptyView(true)

    verify(emptyView).visibility = VISIBLE
    verify(emptyView, never()).visibility = GONE
    verify(emptyView, never()).visibility = INVISIBLE
  }

  @Test fun showOrHideEmptyView_hide() {
    handle.emptyView.assertSameAs(emptyView)
    handle.showOrHideEmptyView(false)

    verify(emptyView).visibility = GONE
    verify(emptyView, never()).visibility = VISIBLE
    verify(emptyView, never()).visibility = INVISIBLE
  }

  @Test fun getAdapter() {
    handle.getAdapter()
        .assertSameAs(adapter)
  }

  @Test fun invalidateList_dataSourceEmpty() {
    whenever(dataSource.isEmpty()).doReturn(true)
    handle.dataSource.assertSameAs(dataSource)

    handle.invalidateList {
      notifyDataSetChanged()
    }
    verify(adapter).notifyDataSetChanged()
    verify(emptyView).visibility = VISIBLE
  }

  @Test fun invalidateList_dataSourceNotEmpty() {
    whenever(dataSource.isEmpty()).doReturn(false)
    handle.dataSource.assertSameAs(dataSource)

    handle.invalidateList {
      notifyDataSetChanged()
    }
    verify(adapter).notifyDataSetChanged()
    verify(emptyView).visibility = GONE
  }

  @Test fun attachDataSource() {
    handle.attachDataSource()
    verify(dataSource).attach(handle)
    verify(adapter).attach(handle)
  }

  @Test fun detachDataSource() {
    handle.detachDataSource()
    verify(dataSource).detach()
    verify(adapter).detach()
  }
}
