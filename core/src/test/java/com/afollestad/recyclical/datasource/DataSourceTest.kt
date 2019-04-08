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

import androidx.recyclerview.widget.RecyclerView
import com.afollestad.recyclical.testdata.TestItem
import com.afollestad.recyclical.testdata.mockRecyclicalHandle
import com.afollestad.recyclical.testutil.NoManifestTestRunner
import com.afollestad.recyclical.testutil.assertEqualTo
import com.afollestad.recyclical.testutil.assertFalse
import com.afollestad.recyclical.testutil.assertSameAs
import com.afollestad.recyclical.testutil.assertTrue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(NoManifestTestRunner::class)
class DataSourceTest {
  private val mockAdapter = mock<RecyclerView.Adapter<*>>()
  private val handle = mockRecyclicalHandle(mockAdapter)

  private val defaultItems = listOf(
      TestItem(1, "Aidan"),
      TestItem(2, "Hello")
  )
  private val dataSource: DataSource = RealDataSource(defaultItems)

  @Before fun setup() {
    dataSource.size()
        .assertEqualTo(2)
    dataSource.attach(handle)

    verify(mockAdapter).notifyDataSetChanged()
  }

  @Test fun add() {
    val item = TestItem(3, "Hi")
    dataSource.add(item)

    verify(mockAdapter).notifyItemInserted(2)
  }

  @Test fun set() {
    val newItems = listOf(
        TestItem(9, "Nine"),
        TestItem(10, "Ten")
    )
    dataSource.set(newItems)

    dataSource.size()
        .assertEqualTo(2)
    dataSource[0].assertSameAs(newItems[0])
    dataSource[1].assertSameAs(newItems[1])

    verify(mockAdapter, times(2)).notifyDataSetChanged()
  }

  @Test fun contains() {
    val item = TestItem(3, "Hi")
    dataSource.contains(defaultItems[0])
        .assertTrue()
    dataSource.contains(item)
        .assertFalse()
  }

  @Test fun insert() {
    dataSource.size()
        .assertEqualTo(2)
    val item = TestItem(-1, "Test")
    dataSource.insert(0, item)

    dataSource.size()
        .assertEqualTo(3)
    dataSource[0].assertSameAs(item)
    dataSource[1].assertSameAs(defaultItems[0])
    dataSource[2].assertSameAs(defaultItems[1])

    verify(mockAdapter).notifyItemInserted(0)
  }

  @Test fun removeAt() {
    dataSource.size()
        .assertEqualTo(2)
    dataSource.removeAt(0)

    dataSource.size()
        .assertEqualTo(1)
    dataSource[0].assertSameAs(defaultItems[1])

    verify(mockAdapter).notifyItemRemoved(0)
  }

  @Test fun remove() {
    dataSource.size()
        .assertEqualTo(2)
    dataSource.remove(defaultItems[0])

    dataSource.size()
        .assertEqualTo(1)
    dataSource[0].assertSameAs(defaultItems[1])

    verify(mockAdapter).notifyItemRemoved(0)
  }

  @Test fun swap() {
    dataSource[0].assertSameAs(defaultItems[0])
    dataSource[1].assertSameAs(defaultItems[1])

    dataSource.swap(0, 1)

    dataSource[0].assertSameAs(defaultItems[1])
    dataSource[1].assertSameAs(defaultItems[0])

    verify(mockAdapter).notifyItemChanged(0)
    verify(mockAdapter).notifyItemChanged(1)
  }

  @Test fun move() {
    dataSource[0].assertSameAs(defaultItems[0])
    dataSource[1].assertSameAs(defaultItems[1])

    dataSource.move(0, 1)

    dataSource[0].assertSameAs(defaultItems[1])
    dataSource[1].assertSameAs(defaultItems[0])

    verify(mockAdapter).notifyItemMoved(0, 1)
  }

  @Test fun clear() {
    dataSource.isNotEmpty().assertTrue()
    dataSource.isEmpty().assertFalse()
    dataSource.size()
        .assertEqualTo(2)

    dataSource.clear()

    dataSource.isNotEmpty().assertFalse()
    dataSource.isEmpty().assertTrue()
    dataSource.size()
        .assertEqualTo(0)

    verify(mockAdapter, times(2)).notifyDataSetChanged()
  }

  @Test fun forEach() {
    var iterations = 0
    dataSource.forEach { iterations++ }
    iterations.assertEqualTo(2)
  }

  @Test fun indexOfFirst() {
    dataSource.indexOfFirst { it === defaultItems[1] }
        .assertEqualTo(1)
  }

  @Test fun indexOfLast() {
    dataSource.indexOfLast { it === defaultItems[1] }
        .assertEqualTo(1)
  }

  @Test fun indexOf() {
    dataSource.indexOf(defaultItems[1])
        .assertEqualTo(1)
  }

  @Test fun invalidateAt() {
    dataSource.invalidateAt(0)
    verify(mockAdapter).notifyItemChanged(0)
  }

  @Test fun invalidateAll() {
    dataSource.invalidateAll()
    verify(mockAdapter, times(2)).notifyDataSetChanged()
  }

  @After fun teardown() {
    dataSource.detach()
  }
}
