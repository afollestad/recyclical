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
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.afollestad.recyclical.dataSourceOf
import com.afollestad.recyclical.setup
import com.afollestad.recyclical.withItem
import kotlinx.android.synthetic.main.activity_main.emptyView
import kotlinx.android.synthetic.main.activity_main.list

class MainActivity : AppCompatActivity() {
  private var toast: Toast? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    @Suppress("IMPLICIT_CAST_TO_ANY")
    val items = IntArray(1000) { it + 1 }
        .map {
          if (it.isEven()) {
            ItemOne("Even #$it")
          } else {
            ItemTwo(
                title = "Odd #$it",
                body = "Hello, world! $it"
            )
          }
        }

    val dataSource = dataSourceOf(items)

    list.setup {
      withLayoutManager(LinearLayoutManager(this@MainActivity))
      withEmptyView(emptyView)
      withDataSource(dataSource)

      withItem<ItemOne>(R.layout.list_item_one) {
        onBind(::ViewHolderOne) { _, item ->
          title.text = item.title
        }
        onClick { index, item ->
          toast("Clicked $index: ${item.title}")
        }
      }

      withItem<ItemTwo>(R.layout.list_item_two) {
        onBind(::ViewHolderTwo) { _, item ->
          title.text = item.title
          body.text = item.body
        }
        onClick { index, item ->
          toast("Clicked $index: ${item.title} / ${item.body}")
        }
      }
    }

    val newItems = items.toMutableList()
        .apply { removeAt(0) }
    dataSource.set(
        newItems = newItems,
        areTheSame = ::areItemsTheSame,
        areContentsTheSame = ::areItemContentsTheSame
    )
  }

  private fun areItemsTheSame(
    left: Any,
    right: Any
  ): Boolean {
    return left === right
  }

  private fun areItemContentsTheSame(
    left: Any,
    right: Any
  ): Boolean {
    return when (left) {
      is ItemOne -> {
        right is ItemOne &&
            right.title == left.title
      }
      is ItemTwo -> {
        right is ItemTwo &&
            right.title == left.title &&
            right.body == left.body
      }
      else -> false
    }
  }

  private fun toast(message: String) {
    toast?.cancel()
    toast = Toast.makeText(this, message, LENGTH_LONG)
        .apply { show() }
  }
}

private fun Int.isEven() = this.rem(2) == 0
