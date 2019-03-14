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
@file:Suppress("unused")

package com.afollestad.recyclical.testutil

fun <T : Any?> T.assertNull() {
  if (this != null) {
    throw AssertionError("Expected value to be null, actual: $this")
  }
}

fun <T> T?.assertNotNull(): T {
  if (this == null) {
    throw AssertionError("Expected value to be not null, but it was")
  }
  return this
}

fun <T : Any> T?.assertEqualTo(value: T?) {
  if (this != value) {
    throw AssertionError("Expected value: \"$value\"\nActual: \"$this\"")
  }
}

fun <T : Any> T?.assertSameAs(value: T?) {
  if (this !== value) {
    throw AssertionError("Expected reference: \"$value\"\nActual: \"$this\"")
  }
}

fun <T : Any> T?.assertNotEqualTo(value: T?) {
  if (this == value) {
    throw AssertionError("Expected value to not be \"$value\", but it was")
  }
}

inline fun <reified T : Any> Any.assertType(): T {
  if (this !is T) {
    val actual = this.javaClass.simpleName
    throw AssertionError("Expected type: ${T::class.java.simpleName}, actual: $actual")
  }
  return this
}

fun Boolean.assertTrue() = assertEqualTo(true)

fun Boolean.assertFalse() = assertEqualTo(false)

fun Collection<*>?.assertEmpty() {
  if (this != null && this.isNotEmpty()) {
    val stringRepresentation = joinToString(
        prefix = "[ ",
        postfix = " ]",
        transform = { "\"$it\"" }
    )
    throw AssertionError("Expected list to be empty, but actual: $stringRepresentation")
  }
}

fun Collection<*>.assertNotEmpty() {
  if (this.isEmpty()) {
    throw AssertionError("Expected list to not be empty, but it was")
  }
}

fun Collection<*>.assertSize(expected: Int) {
  if (this.size != expected) {
    throw AssertionError("Expected list size to be $expected, actual: ${this.size}")
  }
}

fun <T> Iterable<T>.second(): T {
  return when (this) {
    is List -> this[1]
    else -> {
      val iterator = iterator()
      if (!iterator.hasNext())
        throw NoSuchElementException("Collection is empty.")
      if (!iterator.hasNext())
        throw NoSuchElementException("There is no second element.")
      iterator.next()
    }
  }
}

fun <T> Iterable<T>.third(): T {
  return when (this) {
    is List -> this[2]
    else -> {
      val iterator = iterator()
      if (!iterator.hasNext())
        throw NoSuchElementException("Collection is empty.")
      if (!iterator.hasNext())
        throw NoSuchElementException("There is no second element.")
      if (!iterator.hasNext())
        throw NoSuchElementException("There is no third element.")
      iterator.next()
    }
  }
}

fun <T> Collection<T>.assertValues(vararg values: T) {
  val expectedRepresentation = values.joinToString(
      prefix = "[ ",
      postfix = " ]",
      transform = { "\"$it\"" }
  )
  val actualRepresentation = joinToString(
      prefix = "[ ",
      postfix = " ]",
      transform = { "\"$it\"" }
  )
  if (expectedRepresentation != actualRepresentation) {
    throw AssertionError("Expected content: $expectedRepresentation\nActual: $actualRepresentation")
  }
}

fun CharSequence.assertEmpty() {
  if (isNotEmpty()) {
    throw AssertionError("Expected string to be empty but actual: $this")
  }
}

fun CharSequence.assertNotEmpty() {
  if (isEmpty()) {
    throw AssertionError("Expected string to be not empty but it is")
  }
}

inline fun <reified T : Any> Any?.assertIsA(noinline actOn: (T.() -> Unit)? = null) {
  if (this == null) {
    throw AssertionError("Receiver is null, not a ${T::class.java.name}.")
  } else if (this !is T) {
    throw AssertionError("Object of ${this::class} is NOT an instance of ${T::class}")
  }
  actOn?.invoke(this)
}

inline fun <reified T : Any> Any?.assertIsNotA() {
  if (this == null) {
    throw AssertionError("Receiver is null.")
  } else if (this !is T) {
    throw AssertionError("Object of ${this::class} IS an instance of ${T::class}")
  }
}

inline fun <reified T : Exception> expectException(
  message: String? = null,
  block: () -> Unit
) {
  try {
    block()
    throw AssertionError("Expected exception of ${T::class}, but nothing was thrown!")
  } catch (e: Exception) {
    if (e !is T) {
      throw AssertionError("Expected exception of ${T::class}, but got ${e::class}")
    }
    if (message != null && message != e.message) {
      throw AssertionError("Expected exception message: \"$message\"\nActual: ${e.message}")
    }
  }
}
