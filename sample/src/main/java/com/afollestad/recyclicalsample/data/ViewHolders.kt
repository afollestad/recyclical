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
package com.afollestad.recyclicalsample.data

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.afollestad.recyclical.BindingViewHolder
import com.afollestad.recyclical.ViewHolder
import com.afollestad.recyclicalsample.R.id
import com.afollestad.recyclicalsample.databinding.MyListItemBinding

class MyViewHolder(binding: MyListItemBinding) : BindingViewHolder<MyListItemBinding>(binding)

class MyViewHolder2(itemView: View) : ViewHolder(itemView) {
  val icon: ImageView = itemView.findViewById(id.icon)
  val title: TextView = itemView.findViewById(id.title)
}
