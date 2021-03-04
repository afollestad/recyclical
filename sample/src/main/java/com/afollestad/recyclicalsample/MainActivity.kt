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
package com.afollestad.recyclicalsample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.afollestad.recyclical.datasource.selectableDataSourceTypedOf
import com.afollestad.recyclical.itemdefinition.onChildViewClick
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.swipe.SwipeLocation.LEFT
import com.afollestad.recyclical.swipe.SwipeLocation.RIGHT
import com.afollestad.recyclical.swipe.withSwipeAction
import com.afollestad.recyclical.viewholder.SelectionStateProvider
import com.afollestad.recyclical.viewholder.isSelected
import com.afollestad.recyclical.withItem
import com.afollestad.recyclicalsample.data.MyListItem
import com.afollestad.recyclicalsample.data.MyViewHolder
import com.afollestad.recyclicalsample.databinding.MyListItemBinding
import com.afollestad.recyclicalsample.fragment.FragmentSampleActivity
import com.afollestad.recyclicalsample.util.startActivity
import com.afollestad.recyclicalsample.util.toast

class MainActivity : AppCompatActivity() {
  private val emptyView by lazy { findViewById<View>(R.id.emptyView) }
  private val list by lazy { findViewById<RecyclerView>(R.id.list) }
  private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }

  private var cab: AttachedCab? = null
  private val dataSource = selectableDataSourceTypedOf(
      IntArray(1000) { it + 1 }
          .map {
            MyListItem(
                id = it,
                title = "#$it",
                body = "Hello, world! #$it"
            )
          }
  )
      .apply {
        onSelectionChange { invalidateCab() }
      }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    toolbar.run {
      inflateMenu(R.menu.main)
      setOnMenuItemClickListener {
        when (it.itemId) {
          R.id.fragment -> startActivity<FragmentSampleActivity>()
          R.id.reverse -> reverseListOrder()
        }
        true
      }
    }

    list.setup {
      withSwipeAction(LEFT) {
        icon(R.drawable.ic_action_delete)
        text(R.string.delete)
        color(R.color.md_red)
        callback { _, item ->
          toast("Delete: $item")
          true
        }
        hapticFeedbackEnabled()
      }
      withSwipeAction(RIGHT) {
        icon(R.drawable.ic_action_archive)
        text(R.string.archive)
        color(R.color.md_green)
        callback { _, item ->
          toast("Archive: $item")
          true
        }
      }

      withEmptyView(emptyView)
      withDataSource(dataSource)

      withItem<MyListItem, MyViewHolder, MyListItemBinding>(MyListItemBinding::inflate) {
        hasStableIds { it.id }
        onBind(::MyViewHolder) { _, item ->
          binding.icon.setImageResource(
              if (isSelected()) {
                R.drawable.check_circle
              } else {
                R.drawable.person
              }
          )
          binding.title.text = item.title
          binding.body.text = item.body
        }

        onChildViewClick(MyListItemBinding::body) { _, _ ->
          toast("Clicked icon of ${item.title}!")
          toggleSelection()
        }

        onClick { clickMyListItem(it) }
        onLongClick { toggleSelection() }
      }
    }
  }

  private fun SelectionStateProvider<MyListItem>.clickMyListItem(index: Int) {
    if (hasSelection()) {
      // If we are in selection mode, click should toggle selection
      toggleSelection()
    } else {
      // Else just show a toast
      toast("Clicked $index: ${item.title} / ${item.body}")
    }
  }

  private fun invalidateCab() {
    if (dataSource.hasSelection()) {
      if (cab.isActive()) {
        cab?.apply {
          title(literal = getString(R.string.x_items, dataSource.getSelectionCount()))
        }
      } else {
        cab = createCab(R.id.cab_stub) {
          title(literal = getString(R.string.x_items, dataSource.getSelectionCount()))
          slideDown()
          onDestroy {
            dataSource.deselectAll()
            true
          }
        }
      }
    } else {
      cab.destroy()
    }
  }

  private fun reverseListOrder() {
    val reversedList = dataSource.toList()
        .asReversed()
    dataSource.set(
        newItems = reversedList,
        areTheSame = MyListItem.Companion::areTheSame,
        areContentsTheSame = MyListItem.Companion::areContentsTheSame
    )
    list.scrollToPosition(0)
  }

  override fun onBackPressed() {
    if (dataSource.hasSelection()) {
      dataSource.deselectAll()
    } else {
      super.onBackPressed()
    }
  }
}
