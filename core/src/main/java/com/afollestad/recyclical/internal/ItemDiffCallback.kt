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
package com.afollestad.recyclical.internal

import androidx.recyclerview.widget.DiffUtil
import com.afollestad.recyclical.datasource.LeftAndRightComparer

/** @author Aidan Follestad (@afollestad) */
class ItemDiffCallback<IT : Any>(
  private val oldItems: List<IT>,
  private val newItems: List<IT>,
  private val areTheSame: LeftAndRightComparer<IT>,
  private val areContentsTheSame: LeftAndRightComparer<IT>
) : DiffUtil.Callback() {

  override fun areItemsTheSame(
    oldItemPosition: Int,
    newItemPosition: Int
  ): Boolean {
    val oldItem = oldItems[oldItemPosition]
    val newItem = newItems[newItemPosition]
    return areTheSame(oldItem, newItem)
  }

  override fun getOldListSize(): Int = oldItems.size

  override fun getNewListSize(): Int = newItems.size

  override fun areContentsTheSame(
    oldItemPosition: Int,
    newItemPosition: Int
  ): Boolean {
    val oldItem = oldItems[oldItemPosition]
    val newItem = newItems[newItemPosition]
    return areContentsTheSame(oldItem, newItem)
  }
}
