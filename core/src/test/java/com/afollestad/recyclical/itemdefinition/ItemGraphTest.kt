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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.afollestad.recyclical.databinding.TestItemBinding
import com.afollestad.recyclical.testutil.assertEqualTo
import com.afollestad.recyclical.testutil.assertFalse
import com.afollestad.recyclical.testutil.assertSameAs
import com.afollestad.recyclical.testutil.assertTrue
import com.afollestad.recyclical.testutil.expectException
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import java.lang.IllegalArgumentException
import org.junit.Test

private val REAL_LAYOUT_RES_1: ((LayoutInflater, ViewGroup, Boolean) -> ViewBinding) = TestItemBinding::inflate
private const val REAL_CLASS_NAME_1 = "com.afollestad.SomeClass1"
private val REAL_LAYOUT_RES_2: ((LayoutInflater, ViewGroup, Boolean) -> ViewBinding) = TestItemBinding::inflate
private const val REAL_CLASS_NAME_2 = "com.afollestad.SomeClass2"

class ItemGraphTest {
  private val graph = ItemGraph()
  private val itemDefinitionNoIdGetter = mock<RealItemDefinition<*, *, *>> {
    on { itemClassName } doReturn REAL_CLASS_NAME_1
  }
  private val itemDefinitionWithIdGetter = mock<RealItemDefinition<*, *, *>> {
    on { itemClassName } doReturn REAL_CLASS_NAME_2
    on { idGetter } doReturn { 5 }
  }

  @Test fun `hasStableIds - true`() {
    graph.register(
        REAL_LAYOUT_RES_1,
        itemDefinitionWithIdGetter
    )

    graph.itemTypeToLayout.keys
        .assertEqualTo(setOf(1))
    graph.hasStableIds()
        .assertTrue()
  }

  @Test fun `hasStableIds - false`() {
    graph.register(
        REAL_LAYOUT_RES_2,
        itemDefinitionNoIdGetter
    )

    graph.itemTypeToLayout.keys
        .assertEqualTo(setOf(1))
    graph.hasStableIds()
        .assertFalse()
  }

  @Test fun `hasStableIds - error from a mix`() {
    graph.register(
        REAL_LAYOUT_RES_1,
        itemDefinitionNoIdGetter
    )
    graph.register(
        REAL_LAYOUT_RES_2,
        itemDefinitionWithIdGetter
    )
    graph.itemTypeToLayout.keys
        .assertEqualTo(setOf(1, 2))

    expectException<IllegalArgumentException>(
        "If you specify that one item type has stable IDs, all item types must."
    ) {
      graph.hasStableIds()
    }
  }

  @Test fun layoutForType() {
    graph.register(
        REAL_LAYOUT_RES_1,
        itemDefinitionNoIdGetter
    )
    graph.register(
        REAL_LAYOUT_RES_2,
        itemDefinitionWithIdGetter
    )

    graph.layoutForType(1)
        .assertEqualTo(REAL_LAYOUT_RES_1)
    graph.layoutForType(2)
        .assertEqualTo(REAL_LAYOUT_RES_2)
    graph.validate()
        .assertSameAs(graph)

    expectException<IllegalStateException>(
        "Didn't find layout for type 4"
    ) {
      graph.layoutForType(4)
    }
  }

  @Test fun definitionForName() {
    graph.register(
        REAL_LAYOUT_RES_1,
        itemDefinitionNoIdGetter
    )
    graph.register(
        REAL_LAYOUT_RES_2,
        itemDefinitionWithIdGetter
    )

    graph.definitionForName(REAL_CLASS_NAME_1)
        .assertSameAs(itemDefinitionNoIdGetter)
    graph.definitionForName(REAL_CLASS_NAME_2)
        .assertSameAs(itemDefinitionWithIdGetter)
    graph.validate()
        .assertSameAs(graph)

    expectException<IllegalStateException>(
        "Didn't find item type for class idk"
    ) {
      graph.definitionForName("idk")
    }
  }

  @Test fun definitionForType() {
    graph.register(
        REAL_LAYOUT_RES_1,
        itemDefinitionNoIdGetter
    )
    graph.register(
        REAL_LAYOUT_RES_2,
        itemDefinitionWithIdGetter
    )

    graph.definitionForType(1)
        .assertSameAs(itemDefinitionNoIdGetter)
    graph.definitionForType(2)
        .assertSameAs(itemDefinitionWithIdGetter)
    graph.validate()
        .assertSameAs(graph)

    expectException<IllegalStateException>(
        "Didn't find any definitions for type 4"
    ) {
      graph.definitionForType(4)
    }
  }

  @Test fun typeForName() {
    graph.register(
        REAL_LAYOUT_RES_1,
        itemDefinitionNoIdGetter
    )
    graph.register(
        REAL_LAYOUT_RES_2,
        itemDefinitionWithIdGetter
    )

    graph.typeForName(REAL_CLASS_NAME_1)
        .assertEqualTo(1)
    graph.typeForName(REAL_CLASS_NAME_2)
        .assertEqualTo(2)
    graph.validate()
        .assertSameAs(graph)

    expectException<IllegalStateException>(
        "Didn't find item type for class idk"
    ) {
      graph.typeForName("idk")
    }
  }

  @Test fun `can register layout to multiple definitions`() {
    graph.register(
        REAL_LAYOUT_RES_1,
        itemDefinitionNoIdGetter
    )
    graph.register(
        REAL_LAYOUT_RES_1,
        itemDefinitionWithIdGetter
    )

    graph.typeForName(REAL_CLASS_NAME_1)
        .assertEqualTo(1)
    graph.typeForName(REAL_CLASS_NAME_2)
        .assertEqualTo(2)
    graph.definitionForName(REAL_CLASS_NAME_1)
        .assertSameAs(itemDefinitionNoIdGetter)
    graph.definitionForName(REAL_CLASS_NAME_2)
        .assertSameAs(itemDefinitionWithIdGetter)
    graph.layoutForType(1)
        .assertEqualTo(REAL_LAYOUT_RES_1)
    graph.layoutForType(2)
        .assertEqualTo(REAL_LAYOUT_RES_1)
  }
}
