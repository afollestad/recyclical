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

import android.view.View
import com.afollestad.recyclical.ChildViewClickListener
import com.afollestad.recyclical.testutil.assertEqualTo
import com.afollestad.recyclical.testutil.assertSameAs
import com.afollestad.recyclical.viewholder.SelectionStateProvider

class TestChildClickListener<IT : Any, in VT : View> {
  private var selectionStateProvider: SelectionStateProvider<IT>? = null
  private var actualIndex: Int? = null
  private var actualView: VT? = null
  private var actualItem: IT? = null
  private var isInvoked: Boolean = false

  fun capture(): ChildViewClickListener<IT, VT> = { index, view ->
    if (isInvoked) {
      throw AssertionError("Callback invoked more than once without a expect in between.")
    }
    isInvoked = true
    selectionStateProvider = this
    actualIndex = index
    actualView = view
    actualItem = item
  }

  fun expect(
    index: Int,
    view: VT,
    item: IT,
    selectionState: (SelectionStateProvider<IT>.() -> Unit)? = null
  ) {
    actualIndex.assertEqualTo(index)
    actualView.assertSameAs(view)
    actualItem.assertSameAs(item)
    selectionStateProvider?.let { selectionState?.invoke(it) }
    reset()
  }

  fun expectNothing() {
    if (isInvoked) {
      throw AssertionError("Didn't want callback to be invoked, but it was.")
    }
  }

  private fun reset() {
    isInvoked = false
    selectionStateProvider = null
    actualIndex = null
    actualItem = null
  }
}
