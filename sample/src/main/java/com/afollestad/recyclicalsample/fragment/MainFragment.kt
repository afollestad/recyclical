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
import androidx.fragment.app.Fragment
import com.afollestad.recyclical.datasource.DataSource
import com.afollestad.recyclical.datasource.emptyDataSource
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import com.afollestad.recyclicalsample.R
import com.afollestad.recyclicalsample.data.MyListItem
import com.afollestad.recyclicalsample.data.MyViewHolder
import com.afollestad.recyclicalsample.util.toast
import kotlinx.android.synthetic.main.main_fragment.add_item as addItem
import kotlinx.android.synthetic.main.main_fragment.fragment_emptyView as emptyView
import kotlinx.android.synthetic.main.main_fragment.fragment_list as list

class MainFragment : Fragment() {
  private lateinit var dataSource: DataSource

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
      dataSource.add(MyListItem(newIndex, title, "Hello world #$newIndex"))
    }

    dataSource = emptyDataSource()

    list.setup {
      withEmptyView(emptyView)
      withDataSource(dataSource)

      withItem<MyListItem>(R.layout.my_list_item) {
        hasStableIds { it.id }

        onBind(::MyViewHolder) { _, item ->
          icon.setImageResource(R.drawable.person)
          title.text = item.title
          body.text = item.body
        }

        onClick { index ->
          toast("Clicked $index: ${item.title} / ${item.body}")
        }
      }
    }
  }
}
