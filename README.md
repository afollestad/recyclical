## Recyclical

*recyclical*, an easy-to-use DSL API to setup RecyclerViews from Kotlin.

[ ![jCenter](https://api.bintray.com/packages/drummer-aidan/maven/recyclical/images/download.svg) ](https://bintray.com/drummer-aidan/maven/recyclical/_latestVersion)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/bdc552fb3832423986a296a47b9ddef0)](https://www.codacy.com/app/drummeraidan_50/recyclical?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=afollestad/recyclical&amp;utm_campaign=Badge_Grade)
[![Build Status](https://travis-ci.org/afollestad/recyclical.svg)](https://travis-ci.org/afollestad/recyclical)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

<img src="https://raw.githubusercontent.com/afollestad/recyclical/master/art/showcase2.png" width="600" />

---

## Table of Contents

1. [Gradle Dependency](#gradle-dependency)
2. [The Basics](#the-basics)
3. [More Options](#more-options)
4. [Multiple Item Types](#multiple-item-types)
5. [DataSource](#datasource)
    1. [Construction](#construction)
    2. [Manipulation](#manipulation)
    4. [Diffing](#diffing)
6. [SelectableDataSource](#selectabledatasource)
    1. [Construction](#construction-1)
    2. [Manipulation](#manipulation-1)
    3. [Use in Binding](#use-in-binding)

---

## Gradle Dependency

Add this to your module's `build.gradle` file:

```gradle
dependencies {

  implementation 'com.afollestad:recyclical:0.2.1'
}
```

---

## The Basics

First, declare an Item class:

```kotlin
data class Person(
  var name: String,
  var arg: Int
)
```

Second, a layout and a View Holder:

```xml
<LinearLayout ...>

  <TextView 
     android:id="@+id/text_name"
     ... />    
     
  <TextView 
     android:id="@+id/text_age"
     ... />
     
</LinearLayout>
```

```kotlin
class PersonViewHolder(itemView: View) {
  val name: TextView = itemView.findViewById(R.id.text_name)
  val age: TextView = itemView.findViewById(R.id.text_age)
}
```

Now you can begin using the DSL API:

```kotlin
class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
      
      val dataSource = dataSourceOf(
          Person("Aidan", 24),
          Person("Nina", 24)
      )
      
      // setup{} is an extension method on RecyclerView
      recyclerView.setup {
          withDataSource(dataSource)
          withItem<Person>(R.layout.person_item_layout) {
            onBind(::PersonViewHolder) { index, item ->
              name.text = item.name
              age.text = "${item.age}"
            }
            onClick { index, item ->
              toast("Clicked $index: ${item.name}")
            }
            onLongClick { index, item -> 
              toast("Long clicked $index: ${item.name}")
            }
         }
      }
  }
}
```

---

## More Options

There are other things you can give to the setup extension:

```kotlin
recyclerView.setup {
  // Custom layout manager, rather than the default which is a vertical LinearLayoutManager
  withLayoutManager(GridLayoutManager(context, 2))
  // Assigns a view that is made visible when the data source has content, else is hidden (gone)
  withEmptyView(view)
  // Global click listener for any item type. Individual item click listeners are called first.
  withClickListener { index, item -> }
  // Global long click listener for any item type. Individual item long click listeners are called first.
  withLongClickListener { index, item -> }
}
```

---

## Multiple Item Types

You can mix different types of items - but you need to specify view holders and layouts for them too:

```kotlin
val dataSource = dataSourceOf(
  Car(2012, "Volkswagen GTI"),
  Motorcycle(2018, "Triumph", "Thruxton R"),
  Person("Aidan", 24)
)

recyclerView.setup {
  withDataSource(dataSource)
    
  withItem<Person>(R.layout.person_item_layout) {
     onBind(::PersonViewHolder) { index, item ->
        name.text = item.name
        age.text = "${item.age}"
     }
  }
  withItem<Motorcycle>(R.layout.motorcycle_item_layout) {
     onBind(::MotorcycleViewHolder) { index, item ->
        year.text = "${item.year}"
        make.text = item.make
        model.text = item.model
     }
  }
  withItem<Car>(R.layout.car_item_layout) {
     onBind(::CarViewHolder) { index, item ->
        year.text = "${item.year}"
        name.text = item.name
     } 
  }
}
```

---

## DataSource

`DataSource` is an interface which provides data and allows manipulation of the data, to display in a RecyclerView. 
Being an interface means you make your own implementations of it, you can mock it in tests, you could even provide it 
via Dagger to a presenter and manipulate the RecyclerView outside of your UI layer.

### Construction

The included implementation of data source operates on a List of objects (of any type).

```kotlin
// Empty by default, but can still add, insert, etc.
val dataSource = emptyDataSource()

// Initial data set of items from a vararg list
val dataSource = dataSourceOf(item1, item2)

// Initial data set of items from an existing list
val items = listOf(item1, item2)
val dataSource = dataSourceOf(items)
```

### Manipulation

```kotlin
val dataSource: DataSource = // ...

// getters
val item: Any = dataSource.get(5)
val contains: Boolean = dataSource.contains(item)
val size: Int = dataSource.size()
val isEmpty: Boolean = dataSource.isEmpty()
val isNotEmpty: Boolean = dataSource.isNotEmpty()
val firstIndex: Int = dataSource.indexOfFirst { }
val lastIndex: Int = dataSource.indexOfLast { }

// mutation
val person = Person("Aidan", 24)
dataSource.add(person)
dataSource.set(listOf(person))
dataSource.insert(1, person)
dataSource.removeAt(1)
dataSource.remove(person)
dataSource.swap(1, 4)
dataSource.move(1, 4)
dataSource.clear()

// iteration
for (item in dataSource) { }
dataSource.forEach { }  // emits all items
dataSource.forEachOf<Person> { }  // only emits items that are a Person

// operators
val item: Any = dataSource[5]  // get(5)
val contains: Boolean = item in dataSource  // contains(item)
dataSource += person  // add(person)
dataSource -= person  // remove(person)
```

### Diffing

When performing a `set` on the data set, you can opt to use diff utils:

```kotlin
dataSource.set(
  newItems = newItems,
  areTheSame = ::areItemsTheSame,
  areContentsTheSame = ::areItemContentsTheSame
)

// Return true if items represent the same entity, e.g. by ID or name
private fun areItemsTheSame(left: Any, right: Any): Boolean {
  return when (left) {
    is Person -> {
      right is Person && right.name == left.name
    }
    else -> false
  }
}

// Return true if all contents in the items are equal
private fun areItemContentsTheSame(left: Any, right: Any): Boolean {
  return when (left) {
    is Person -> {
      right is Person &&
        right.name == left.name &&
        right.age == left.age
    }
    else -> false
  }
}
```

This will automatically coordinate notifying of adds, moves, and insertions so that 
update of the data set is pretty and animated by the RecyclerView.

---

## SelectableDataSource

A `SelectableDataSource` is built on top of a regular [DataSource]. It provides additional APIs 
to manage the selection state of items in your list.

### Construction

Construction methods for `SelectableDataSource` are the same as the `DataSource` ones, they just 
include `selectable` in their names.

```kotlin
// Empty by default, but can still add, insert, etc.
val dataSource = emptySelectableDataSource()

// Initial data set of items from a vararg list
val dataSource = selectableDataSourceOf(item1, item2)

// Initial data set of items from an existing list
val items = listOf(item1, item2)
val dataSource = selectableDataSourceOf(items)
```

### Manipulation

There are some additional methods added on top of the `DataSource` methods:

```kotlin
val dataSource: SelectableDataSource = // ...

// Index operations
dataSource.selectAt(1)
dataSource.deselectAt(1)
dataSource.toggleSelectionAt(1)
val selected: Boolean = dataSource.isSelectedAt(1)

// Mass operations
dataSource.selectAll()
dataSource.deselectAll()

// Item operations, uses index operations under the hood
val item: Any = // ...
dataSource.select(item)
dataSource.deselect(item)
dataSource.toggleSelection(item)
val selected: Boolean = dataSource.isSelected(item)

// Misc operations
val count: Int = dataSource.selectionCount()
val hasSelection: Boolean = dataSource.hasSelection()

// Set a callback invoked when something is selected or deselected
dataSource.onSelectionChange { dataSource -> }
```

### Use in Binding

During binding of your items, you can access selection states *even if you don't have a direct 
reference to your `DataSource`.

```kotlin
recyclerView.setup {
    withEmptyView(emptyView)
    withDataSource(dataSource)
    
    withItem<MyListItem>(R.layout.my_list_item) {
      onBind(::MyViewHolder) { index, item ->
          val itemSelected = isSelectedAt(index)
          // Do something
      }
      
      onClick { index, item ->
          if (hasSelection()) {
            toggleSelectionAt(index)
          } else {
            // Normal click handling
          } 
      }
      
      onLongClick { index, _ ->
          // Generally long clicks trigger selection mode
          toggleSelectionAt(index)
      }
    }
}  
```