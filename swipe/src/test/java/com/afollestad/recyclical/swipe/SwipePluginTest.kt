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
package com.afollestad.recyclical.swipe

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.swipe.SwipeLocation.LEFT
import com.afollestad.recyclical.swipe.SwipeLocation.RIGHT
import com.afollestad.recyclical.swipe.testdata.TestViewHolder
import com.afollestad.recyclical.testdata.TestItem
import com.afollestad.recyclical.testutil.NoManifestTestRunner
import com.afollestad.recyclical.testutil.assertContainsKey
import com.afollestad.recyclical.testutil.assertEqualTo
import com.afollestad.recyclical.testutil.assertIsA
import com.afollestad.recyclical.testutil.assertSize
import com.afollestad.recyclical.withItem
import com.nhaarman.mockitokotlin2.mock
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(NoManifestTestRunner::class)
class SwipePluginTest {
  private lateinit var recyclerView: RecyclerView
  private val dataSource = mock<DataSource<*>>()

  @Before fun setup() {
    recyclerView = RecyclerView(ApplicationProvider.getApplicationContext())
  }

  @Test fun `attaches to recycler view`() {
    recyclerView.setup {
      withDataSource(dataSource)
      withItem<TestItem>(android.R.layout.simple_list_item_1) {
        onBind(::TestViewHolder) { _, _ -> }
      }
      withSwipeAction(LEFT, RIGHT) {}

      getPluginData<SwipePluginData>(PLUGIN_NAME)!!.run {
        actions.assertSize(2)
        actions.assertContainsKey(LEFT)
        actions.assertContainsKey(RIGHT)
      }
    }

    recyclerView.getItemDecorationAt(0)
        .assertIsA<ItemTouchHelper>()
  }

  @Test fun `left swipe direction`() {
    recyclerView.setup {
      withDataSource(dataSource)
      withItem<TestItem>(android.R.layout.simple_list_item_1) {
        onBind(::TestViewHolder) { _, _ -> }
      }
      withSwipeAction(LEFT) {}

      getPluginData<SwipePluginData>(PLUGIN_NAME)!!.run {
        actions.assertSize(1)
        actions.assertContainsKey(LEFT)
        getSwipeDirections().assertEqualTo(ItemTouchHelper.LEFT)
      }
    }
  }

  @Test fun `right swipe direction`() {
    recyclerView.setup {
      withDataSource(dataSource)
      withItem<TestItem>(android.R.layout.simple_list_item_1) {
        onBind(::TestViewHolder) { _, _ -> }
      }
      withSwipeAction(RIGHT) {}

      getPluginData<SwipePluginData>(PLUGIN_NAME)!!.run {
        actions.assertSize(1)
        actions.assertContainsKey(RIGHT)
        getSwipeDirections().assertEqualTo(ItemTouchHelper.RIGHT)
      }
    }
  }

  @Test fun `left and right swipe directions`() {
    recyclerView.setup {
      withDataSource(dataSource)
      withItem<TestItem>(android.R.layout.simple_list_item_1) {
        onBind(::TestViewHolder) { _, _ -> }
      }
      withSwipeAction(LEFT, RIGHT) {}

      getPluginData<SwipePluginData>(PLUGIN_NAME)!!.run {
        actions.assertSize(2)
        actions.assertContainsKey(LEFT)
        actions.assertContainsKey(RIGHT)
        getSwipeDirections()
            .assertEqualTo(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)
      }
    }
  }
}
