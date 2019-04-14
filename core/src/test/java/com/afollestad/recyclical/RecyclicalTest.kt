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
package com.afollestad.recyclical

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.handle.RealRecyclicalHandle
import com.afollestad.recyclical.handle.getDataSource
import com.afollestad.recyclical.internal.DefinitionAdapter
import com.afollestad.recyclical.testdata.TestItem
import com.afollestad.recyclical.testdata.TestItem2
import com.afollestad.recyclical.testdata.TestPluginData
import com.afollestad.recyclical.testdata.TestViewHolder
import com.afollestad.recyclical.testdata.TestViewHolder2
import com.afollestad.recyclical.testutil.assertEqualTo
import com.afollestad.recyclical.testutil.assertIsA
import com.afollestad.recyclical.testutil.assertSameAs
import com.afollestad.recyclical.testutil.expectException
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.isA
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.same
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test

const val EMPTY_VIEW_ID = 2
const val RECYCLER_VIEW_ID = 3
const val INFLATE_ITEM_LAYOUT_RES = 10
const val INFLATE_ITEM_LAYOUT_RES_2 = 11

class RecyclicalTest {
  private val binder = mock<ViewHolderBinder<TestViewHolder, TestItem>>()
  private val binder2 = mock<ViewHolderBinder<TestViewHolder2, TestItem2>>()
  private val testGlobalClickListener = mock<ItemClickListener<Any>>()
  private val testGlobalLongClickListener = mock<ItemClickListener<Any>>()
  private val testItemClickListener = mock<ItemClickListener<Any>>()
  private val testItemLongClickListener = mock<ItemClickListener<Any>>()

  private val dataSource = mock<DataSource<*>>()

  private val emptyView = mock<View> {
    on { id } doReturn EMPTY_VIEW_ID
  }
  private val recyclerView = mock<RecyclerView> {
    on { id } doReturn RECYCLER_VIEW_ID
  }
  private val adapter = mock<DefinitionAdapter>()

  @Test fun `no bindings defined throws exception`() {
    expectException<IllegalStateException>(
        "No bindings defined."
    ) {
      recyclerView.setup {
        adapterCreator = { adapter }
        withEmptyView(emptyView)
        withDataSource(dataSource)
        withClickListener(testGlobalClickListener)
        withLongClickListener(testGlobalLongClickListener)
      }
    }
  }

  @Test fun `no data source provided throws exception`() {
    expectException<IllegalStateException>(
        "Must set a data source."
    ) {
      recyclerView.setup {
        adapterCreator = { adapter }
        withEmptyView(emptyView)

        val testLayoutManager = mock<LayoutManager>()
        withLayoutManager(testLayoutManager)
        verify(recyclerView).layoutManager = same(testLayoutManager)

        withItem<TestItem>(INFLATE_ITEM_LAYOUT_RES) {
          onBind(::TestViewHolder, binder)
          onClick(testItemClickListener)
          onLongClick(testItemLongClickListener)
        }
      }
    }
  }

  @Test fun `setup succeeds with required information`() {
    var itemDefinition: ItemDefinition<*>? = null
    val handle = recyclerView.setup {
      adapterCreator = { adapter }
      withEmptyView(emptyView)
      withDataSource(dataSource)
      withClickListener(testGlobalClickListener)
      withLongClickListener(testGlobalLongClickListener)

      itemDefinition = withItem<TestItem>(INFLATE_ITEM_LAYOUT_RES) {
        onBind(::TestViewHolder, binder)
        onClick(testItemClickListener)
        onLongClick(testItemLongClickListener)
      }

      this.globalOnClick.assertSameAs(testGlobalClickListener)
      this.globalOnLongClick.assertSameAs(testGlobalLongClickListener)
      this.currentDataSource.assertSameAs(dataSource)
    }

    verify(recyclerView).adapter = isA<DefinitionAdapter>()
    verify(recyclerView).layoutManager = isA<LinearLayoutManager>()
    verify(adapter).setHasStableIds(false)

    handle.assertIsA<RealRecyclicalHandle> {
      emptyView.assertSameAs(emptyView)
    }
    handle.getViewTypeForClass(TestItem::class.java.name)
        .assertEqualTo(INFLATE_ITEM_LAYOUT_RES)
    handle.getDefinitionForType(INFLATE_ITEM_LAYOUT_RES)
        .assertSameAs(itemDefinition)
    handle.getDefinitionForClass(TestItem::class.java.name)
        .assertSameAs(itemDefinition)
    handle.getDataSource<DataSource<*>>()
        .assertSameAs(dataSource)

    verify(dataSource).attach(handle)
  }

  @Test fun `has stable IDs when provided for item definitions`() {
    val handle = recyclerView.setup {
      adapterCreator = { adapter }
      withDataSource(dataSource)

      withItem<TestItem>(INFLATE_ITEM_LAYOUT_RES) {
        hasStableIds { it.id }
        onBind(::TestViewHolder, binder)
      }
    }

    verify(adapter).setHasStableIds(true)
    verify(dataSource).attach(handle)
  }

  @Test fun `only some items having stable IDs throws exception`() {
    expectException<IllegalStateException>(
        "If you specify that one item type has stable IDs, all item types must."
    ) {
      recyclerView.setup {
        adapterCreator = { adapter }
        withDataSource(dataSource)

        withItem<TestItem>(INFLATE_ITEM_LAYOUT_RES) {
          onBind(::TestViewHolder, binder)
        }

        withItem<TestItem2>(INFLATE_ITEM_LAYOUT_RES_2) {
          hasStableIds { it.id }
          onBind(::TestViewHolder2, binder2)
        }
      }
    }

    verify(dataSource, never()).attach(any())
    verify(adapter, never()).setHasStableIds(any())
  }

  @Test fun `plugin data is attached after setup`() {
    val testPluginData = TestPluginData()
    recyclerView.setup {
      adapterCreator = { adapter }
      withDataSource(dataSource)
      withItem<TestItem>(INFLATE_ITEM_LAYOUT_RES) {
        onBind(::TestViewHolder, binder)
      }

      setPluginData("test_data", testPluginData)
      testPluginData.expectNotAttached()

      getPluginData<TestPluginData>("test_data")
          .assertSameAs(testPluginData)
    }

    testPluginData.expectAttached(recyclerView, dataSource)
  }
}
