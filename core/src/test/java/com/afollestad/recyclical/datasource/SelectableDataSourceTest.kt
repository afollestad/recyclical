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

import androidx.recyclerview.widget.RecyclerView.Adapter
import com.afollestad.recyclical.testdata.TestItem
import com.afollestad.recyclical.testdata.mockRecyclicalHandle
import com.afollestad.recyclical.testutil.NoManifestTestRunner
import com.afollestad.recyclical.testutil.assertEqualTo
import com.afollestad.recyclical.testutil.assertFalse
import com.afollestad.recyclical.testutil.assertTrue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(NoManifestTestRunner::class)
class SelectableDataSourceTest {
  private val mockAdapter = mock<Adapter<*>>()
  private val handle = mockRecyclicalHandle(mockAdapter)

  private val defaultItems = listOf(
      TestItem(1, "Aidan"),
      TestItem(2, "Hello"),
      TestItem(3, "Three"),
      TestItem(4, "Four")
  )
  private val dataSource: SelectableDataSource = RealSelectableDataSource(defaultItems)

  @Before fun setup() {
    dataSource.attach(handle)
    verify(mockAdapter).notifyDataSetChanged()
  }

  @Test fun selectAt_deselectAt() {
    dataSource.run {
      getSelectionCount().assertEqualTo(0)
      hasSelection().assertFalse()

      isSelectedAt(1).assertFalse()
      selectAt(1)

      verify(mockAdapter).notifyItemChanged(1)
      isSelectedAt(1).assertTrue()
      getSelectionCount().assertEqualTo(1)
      hasSelection().assertTrue()

      deselectAt(1)
      isSelectedAt(1).assertFalse()

      verify(mockAdapter, times(2)).notifyItemChanged(1)
    }
  }

  @Test fun toggleSelectionAt() {
    dataSource.run {
      isSelectedAt(1).assertFalse()

      toggleSelectionAt(1)
      isSelectedAt(1).assertTrue()

      toggleSelectionAt(1)
      isSelectedAt(1).assertFalse()
    }
  }

  @Test fun selectAll_deselectAll() {
    dataSource.run {
      isSelectedAt(0).assertFalse()
      isSelectedAt(1).assertFalse()
      isSelectedAt(2).assertFalse()
      isSelectedAt(3).assertFalse()

      selectAll()
      isSelectedAt(0).assertTrue()
      isSelectedAt(1).assertTrue()
      isSelectedAt(2).assertTrue()
      isSelectedAt(3).assertTrue()

      deselectAll()
      isSelectedAt(0).assertFalse()
      isSelectedAt(1).assertFalse()
      isSelectedAt(2).assertFalse()
      isSelectedAt(3).assertFalse()
    }
  }

  @Test fun select_deselect() {
    dataSource.run {
      isSelected(defaultItems[0]).assertFalse()
      isSelected(defaultItems[1]).assertFalse()

      select(defaultItems[1])
      isSelected(defaultItems[0]).assertFalse()
      isSelected(defaultItems[1]).assertTrue()

      deselect(defaultItems[1])
      isSelected(defaultItems[0]).assertFalse()
      isSelected(defaultItems[1]).assertFalse()
    }
  }

  @Test fun toggleSelection() {
    dataSource.run {
      isSelected(defaultItems[0]).assertFalse()
      isSelected(defaultItems[1]).assertFalse()

      toggleSelection(defaultItems[1])
      isSelected(defaultItems[0]).assertFalse()
      isSelected(defaultItems[1]).assertTrue()

      toggleSelection(defaultItems[1])
      isSelected(defaultItems[0]).assertFalse()
      isSelected(defaultItems[1]).assertFalse()
    }
  }

  @Test fun onSelectionChange() {
    dataSource.run {
      var invocations = 0
      onSelectionChange { invocations++ }

      selectAt(0)
      deselectAt(0)
      invocations.assertEqualTo(2)
    }
  }

  @Test fun insert() {
    dataSource.run {
      selectAt(0)
      selectAt(2)

      isSelectedAt(0).assertTrue()
      isSelectedAt(1).assertFalse()
      isSelectedAt(2).assertTrue()
      isSelectedAt(3).assertFalse()

      insert(0, TestItem(5, "Testing!"))

      isSelectedAt(0).assertFalse()
      isSelectedAt(1).assertTrue()
      isSelectedAt(2).assertFalse()
      isSelectedAt(3).assertTrue()
      isSelectedAt(4).assertFalse()
    }
  }

  @Test fun swap() {
    dataSource.run {
      selectAt(2)
      isSelectedAt(0).assertFalse()
      isSelectedAt(2).assertTrue()
      swap(0, 2)
      isSelectedAt(0).assertTrue()
      isSelectedAt(2).assertFalse()

      deselectAll()
      selectAt(0)
      isSelectedAt(0).assertTrue()
      isSelectedAt(2).assertFalse()
      swap(0, 2)
      isSelectedAt(0).assertFalse()
      isSelectedAt(2).assertTrue()
    }
  }

  @Test fun move() {
    dataSource.run {
      selectAt(2)
      isSelectedAt(0).assertFalse()
      isSelectedAt(2).assertTrue()

      move(2, 0)
      isSelectedAt(0).assertTrue()
      isSelectedAt(2).assertFalse()
    }
  }

  @Test fun removeAt() {
    dataSource.run {
      selectAt(2)
      hasSelection().assertTrue()

      removeAt(2)
      hasSelection().assertFalse()
    }
  }

  @Test fun clear() {
    dataSource.run {
      selectAll()
      hasSelection().assertTrue()

      clear()
      isEmpty().assertTrue()
      hasSelection().assertFalse()
    }
  }

  @After fun teardown() {
    dataSource.detach()
  }
}
