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
package com.afollestad.recyclicalsample.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.recyclical.datasource.emptyDataSource
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.swipe.SwipeLocation.LEFT
import com.afollestad.recyclical.swipe.SwipeLocation.RIGHT
import com.afollestad.recyclical.swipe.withSwipeActionOn
import com.afollestad.recyclical.withItem
import com.afollestad.recyclicalsample.R
import com.afollestad.recyclicalsample.data.MyListItem
import com.afollestad.recyclicalsample.data.MyListItem2
import com.afollestad.recyclicalsample.data.MyViewHolder
import com.afollestad.recyclicalsample.databinding.MyListItemBinding
import com.afollestad.recyclicalsample.util.toast

class MainFragment : Fragment() {
  private val addItem by lazy { view!!.findViewById<Button>(R.id.add_item) }
  private val emptyView by lazy { view!!.findViewById<View>(R.id.fragment_emptyView) }
  private val list by lazy { view!!.findViewById<RecyclerView>(R.id.fragment_list) }
  private val dataSource = emptyDataSource()

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? = inflater.inflate(R.layout.main_fragment, container, false)

  override fun onViewCreated(
    view: View,
    savedInstanceState: Bundle?
  ) {
    super.onViewCreated(view, savedInstanceState)

    addItem.setOnClickListener {
      val newIndex = dataSource.size() + 1
      val title = "Item #$newIndex"
      dataSource.add(
          if (newIndex % 2 == 0 || true) {
            MyListItem(newIndex, title, "Hello world #$newIndex")
          } else {
            MyListItem2(newIndex, title)
          }
      )
    }

    list.setup {
      withEmptyView(emptyView)
      withDataSource(dataSource)

      withSwipeActionOn<MyListItem2>(LEFT, RIGHT) {
        icon(R.drawable.ic_action_delete)
        text(R.string.delete)
        color(R.color.md_red)
        callback { _, item ->
          toast("Delete: $item")
          true
        }
      }

      withItem<MyListItem, MyViewHolder, MyListItemBinding>(MyListItemBinding::inflate) {
        hasStableIds { it.id }
        onBind(::MyViewHolder) { view, item ->
          (binding as? MyListItemBinding)?.run {
            icon.setImageResource(R.drawable.person)
            title.text = item.title
            body.text = item.body
          }
        }
        onClick { index ->
          toast("Clicked $index: ${item.title} / ${item.body}")
        }
        onRecycled { viewHolder ->
          toast("Recycled holder for holder: $viewHolder")
        }
      }

//      withItem<MyListItem2, MyViewHolder2>(R.layout.my_list_item_2) {
//        hasStableIds { it.id }
//        onBind(::MyViewHolder2) { _, item ->
//          icon.setImageResource(R.drawable.person)
//          title.text = item.title
//        }
//        onClick { index ->
//          toast("Clicked $index: ${item.title}")
//        }
//      }
    }
  }
}
