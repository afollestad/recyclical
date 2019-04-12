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
package com.afollestad.recyclical.plugins

import androidx.recyclerview.widget.RecyclerView
import com.afollestad.recyclical.datasource.DataSource

/**
 * A base class that Recyclical plugins use to store configuration data. Consumers can override
 * [attach] to attach to the RecyclerView when the main setup process is completing.
 *
 * @author Aidan Follestad (@afollestad)
 */
abstract class PluginData {

  /**
   * Invoked when the setup extension method on [RecyclerView] is exiting, so the consumer
   * has completed setting everything up. This is the optimal time for your plugin to attach
   * to the RecyclerView.
   */
  abstract fun attach(
    toView: RecyclerView,
    dataSource: DataSource
  )
}
