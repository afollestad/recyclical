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
package com.afollestad.recyclical.testdata

import com.afollestad.recyclical.ItemClickListener
import com.afollestad.recyclical.testutil.assertEqualTo
import com.afollestad.recyclical.testutil.assertSameAs
import com.afollestad.recyclical.viewholder.SelectionStateProvider

class TestClickListener<in IT : Any> {
  private var selectionStateProvider: SelectionStateProvider? = null
  private var actualIndex: Int? = null
  private var actualItem: IT? = null

  fun capture(): ItemClickListener<IT> = { index, item ->
    selectionStateProvider = this
    actualIndex = index
    actualItem = item
  }

  fun expect(
    index: Int,
    item: IT,
    selectionState: (SelectionStateProvider.() -> Unit)? = null
  ) {
    actualIndex.assertEqualTo(index)
    actualItem.assertSameAs(item)
    selectionStateProvider?.let { selectionState?.invoke(it) }
    reset()
  }

  private fun reset() {
    selectionStateProvider = null
    actualIndex = null
    actualItem = null
  }
}
