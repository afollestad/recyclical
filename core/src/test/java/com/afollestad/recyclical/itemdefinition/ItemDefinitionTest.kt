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
package com.afollestad.recyclical.itemdefinition

import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import com.afollestad.recyclical.R.id
import com.afollestad.recyclical.RecyclicalSetup
import com.afollestad.recyclical.ViewHolder
import com.afollestad.recyclical.databinding.TestItemBinding
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.testdata.TestChildClickListener
import com.afollestad.recyclical.testdata.TestClickListener
import com.afollestad.recyclical.testdata.TestItem
import com.afollestad.recyclical.testdata.TestViewHolder
import com.afollestad.recyclical.testutil.NoManifestTestRunner
import com.afollestad.recyclical.testutil.assertEqualTo
import com.afollestad.recyclical.testutil.assertIsA
import com.afollestad.recyclical.testutil.assertNull
import com.afollestad.recyclical.testutil.assertSameAs
import com.afollestad.recyclical.testutil.assertTrue
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_ITEM_INDEX = 101

@RunWith(NoManifestTestRunner::class)
class ItemDefinitionTest {
  private val testItem = TestItem(1, "Aidan")
  private val recyclerView = mock<RecyclerView>()
  private val dataSource = mock<DataSource<*>> {
    on { get(TEST_ITEM_INDEX) } doReturn testItem
  }
  private val globalClickListener = TestClickListener<Any>()
  private val globalLongClickListener = TestClickListener<Any>()

  private val viewHolderCreator: (TestItemBinding) -> (TestViewHolder) = { itemBinding ->

    val newViewHolder = mock<TestViewHolder> {
      on { adapterPosition } doReturn TEST_ITEM_INDEX
      on { binding } doReturn itemBinding
    }
    newViewHolder.apply {
      val itemViewField = ViewHolder::class.java.getDeclaredField("itemView")
          .apply {
            isAccessible = true
          }
      itemViewField.set(this, itemBinding.root)
    }
  }

  private lateinit var testBinding: TestItemBinding

  private val setup = RecyclicalSetup(recyclerView)
      .apply {
        withDataSource(dataSource)
        withClickListener(globalClickListener.capture())
        withLongClickListener(globalLongClickListener.capture())
      }
  private val definition = RealItemDefinition<TestItem, TestViewHolder, TestItemBinding>(
      setup = setup,
      itemClassName = TestItem::class.java.name
  )

  @Before
  fun create() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    testBinding = TestItemBinding.inflate(LayoutInflater.from(context), null, false)

    definition.itemClassName
        .assertEqualTo(TestItem::class.java.name)
    definition.currentDataSource.assertSameAs(dataSource)
  }

  @Test
  fun createViewHolder() {
    val creator = ::TestViewHolder
    val binder: ViewHolder.(Int, TestItem) -> Unit = { _, _ -> }
    definition.onBind(creator, binder)
    definition.creator.assertSameAs(creator)

    definition.createViewHolder(testBinding)
        .assertIsA<TestViewHolder> {
          binding.title.assertSameAs(testBinding.title)
        }
  }

  @Test
  fun bindViewHolder() {
    val viewHolder = TestViewHolder(testBinding)

    var wasBinderCalled = false
    val binder: ViewHolder.(Int, TestItem) -> Unit = { index, item ->
      wasBinderCalled = true
      this.assertSameAs(viewHolder)
      index.assertEqualTo(TEST_ITEM_INDEX)
      item.assertSameAs(testItem)
    }

    definition.onBind(::TestViewHolder, binder)
    definition.binder.assertSameAs(binder)
    definition.bindViewHolder(
        viewHolder, testItem,
        TEST_ITEM_INDEX
    )

    wasBinderCalled.assertTrue()

    testBinding.root.getTag(id.rec_view_item_view_holder)
        .assertIsA<ViewHolder>()
    testBinding.root.getTag(id.rec_view_item_selectable_data_source)
        .assertNull()
  }

  @Test
  fun onClick() {
    val listener = TestClickListener<TestItem>()
    definition.onClick(listener.capture())

    val binder: ViewHolder.(Int, TestItem) -> Unit = { _, _ -> }
    definition.onBind(viewHolderCreator, binder)
    val viewHolder = definition.createViewHolder(testBinding)
    definition.bindViewHolder(
        viewHolder, testItem,
        TEST_ITEM_INDEX
    )

    testBinding.root.performClick()
    listener.expect(TEST_ITEM_INDEX, testItem)
    globalClickListener.expect(TEST_ITEM_INDEX, testItem)
  }

  @Test
  fun onLongClick() {
    val listener = TestClickListener<TestItem>()
    definition.onLongClick(listener.capture())

    val binder: ViewHolder.(Int, TestItem) -> Unit = { _, _ -> }
    definition.onBind(viewHolderCreator, binder)
    val viewHolder = definition.createViewHolder(testBinding)
    definition.bindViewHolder(
        viewHolder, testItem,
        TEST_ITEM_INDEX
    )

    testBinding.root.performLongClick()
    listener.expect(TEST_ITEM_INDEX, testItem)
    globalLongClickListener.expect(TEST_ITEM_INDEX, testItem)
  }

  @Test
  fun onChildViewClick() {
    val listener = TestChildClickListener<TestItem, TextView>()
    definition.onChildViewClick(TestItemBinding::title, listener.capture())

    val binder: ViewHolder.(Int, TestItem) -> Unit = { _, _ -> }
    definition.onBind(viewHolderCreator, binder)

    val viewHolder = definition.createViewHolder(testBinding) as TestViewHolder
    definition.bindViewHolder(
        viewHolder, testItem,
        TEST_ITEM_INDEX
    )

    testBinding.root.performClick()
    listener.expectNothing()

    testBinding.root.performLongClick()
    listener.expectNothing()

    viewHolder.binding.title.performClick()
    listener.expect(TEST_ITEM_INDEX, viewHolder.binding.title, testItem)
  }
}
