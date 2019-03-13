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
import android.os.PersistableBundle
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import com.afollestad.materialcab.MaterialCab
import com.afollestad.recyclical.datasource.emptySelectableDataSource
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.viewholder.isSelected
import com.afollestad.recyclical.withItem
import kotlinx.android.synthetic.main.activity_main.emptyView
import kotlinx.android.synthetic.main.activity_main.list

class MainActivity : AppCompatActivity() {
  private var toast: Toast? = null
  private val dataSource = emptySelectableDataSource()
      .apply {
        onSelectionChange { invalidateCab() }
      }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    MaterialCab.tryRestore(this, savedInstanceState)

    dataSource.set(
        IntArray(1000) { it + 1 }
            .map {
              MyListItem(
                  title = "#$it",
                  body = "Hello, world! #$it"
              )
            }
    )

    list.setup {
      withEmptyView(emptyView)
      withDataSource(dataSource)

      withItem<MyListItem>(R.layout.my_list_item) {
        onBind(::MyViewHolder) { _, item ->
          icon.setImageResource(
              if (isSelected()) {
                R.drawable.check_circle
              } else {
                R.drawable.person
              }
          )
          title.text = item.title
          body.text = item.body
        }

        onClick { index, item ->
          if (hasSelection()) {
            // If we are in selection mode, click should toggle selection
            toggleSelection()
          } else {
            // Else just show a toast
            toast("Clicked $index: ${item.title} / ${item.body}")
          }
        }

        onLongClick { _, _ ->
          // Long clicking starts selection mode, or ends it
          toggleSelection()
        }
      }
    }
  }

  private fun invalidateCab() {
    if (dataSource.hasSelection()) {
      MaterialCab.attach(this, R.id.cab_stub) {
        title = getString(R.string.x_items, dataSource.getSelectionCount())
        onDestroy {
          dataSource.deselectAll()
          true
        }
      }
    } else {
      MaterialCab.destroy()
    }
  }

  override fun onSaveInstanceState(
    outState: Bundle?,
    outPersistentState: PersistableBundle?
  ) {
    MaterialCab.saveState(outState)
    super.onSaveInstanceState(outState, outPersistentState)
  }

  override fun onBackPressed() {
    if (dataSource.hasSelection()) {
      dataSource.deselectAll()
    } else {
      super.onBackPressed()
    }
  }

  private fun toast(message: String) {
    toast?.cancel()
    toast = Toast.makeText(this, message, LENGTH_LONG)
        .apply { show() }
  }
}
