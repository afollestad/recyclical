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
package com.afollestad.recyclical.itemdefinition

import androidx.annotation.CheckResult
import androidx.annotation.LayoutRes
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import androidx.annotation.VisibleForTesting
import com.afollestad.recyclical.ItemDefinition

/**
 * Manages type, layout, and definition associations for items.
 *
 * @author Aidan Follestad (@afollestad)
 */
@RestrictTo(LIBRARY)
class ItemGraph {
  @VisibleForTesting
  internal val itemTypeToLayout = mutableMapOf<Int, Int>()
  @VisibleForTesting
  internal var itemTypeToDefinition = mutableMapOf<Int, ItemDefinition<*, *>>()

  private var itemClassToType = mutableMapOf<String, Int>()

  fun register(
    @LayoutRes layoutRes: Int,
    definition: ItemDefinition<*, *>
  ) {
    val itemClassName = definition.realDefinition()
        .itemClassName
    val itemType = (itemTypeToLayout.keys.max() ?: 0) + 1
    this.itemTypeToLayout[itemType] = layoutRes
    this.itemClassToType[itemClassName] = itemType
    this.itemTypeToDefinition[itemType] = definition
  }

  @CheckResult @LayoutRes
  fun layoutForType(type: Int): Int {
    return itemTypeToLayout[type] ?: error(
        "Didn't find layout for type $type"
    )
  }

  @CheckResult fun definitionForType(type: Int): ItemDefinition<*, *> {
    return itemTypeToDefinition[type] ?: error(
        "Didn't find any definitions for type $type"
    )
  }

  @CheckResult fun definitionForName(name: String): ItemDefinition<*, *> {
    val type = itemClassToType[name] ?: error(
        "Didn't find item type for class $name"
    )
    return definitionForType(type)
  }

  @CheckResult fun typeForName(name: String): Int {
    return itemClassToType[name] ?: error(
        "Didn't find item type for class $name"
    )
  }

  fun hasStableIds(): Boolean {
    val definitions = itemTypeToDefinition.values
    val anyHaveStableIds = definitions.any {
      (it as? RealItemDefinition)?.idGetter != null
    }
    val allHaveStableIds = definitions.all { (it as? RealItemDefinition)?.idGetter != null }
    require(!(anyHaveStableIds && !allHaveStableIds)) {
      "If you specify that one item type has stable IDs, all item types must."
    }
    return anyHaveStableIds
  }

  fun validate(): ItemGraph {
    check(itemTypeToDefinition.isNotEmpty()) { "No bindings defined." }
    check(itemTypeToLayout.size == itemClassToType.size)
    check(itemTypeToDefinition.size == itemClassToType.size)
    return this
  }
}
