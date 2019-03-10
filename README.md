## Recyclical

*recyclical*, an easy-to-use DSL API to setup RecyclerViews from Kotlin.

[ ![jCenter](https://api.bintray.com/packages/drummer-aidan/maven/recyclical/images/download.svg) ](https://bintray.com/drummer-aidan/maven/recyclical/_latestVersion)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/bdc552fb3832423986a296a47b9ddef0)](https://www.codacy.com/app/drummeraidan_50/recyclical?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=afollestad/recyclical&amp;utm_campaign=Badge_Grade)
[![Build Status](https://travis-ci.org/afollestad/recyclical.svg)](https://travis-ci.org/afollestad/recyclical)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

<img src="https://raw.githubusercontent.com/afollestad/recyclical/master/art/showcase.png" width="600" />

---

## Gradle Dependency

*The publish to jCenter is pending, so add this to your repositories for now:*

```gradle
repositories {

  maven { url 'https://dl.bintray.com/drummer-aidan/maven' }
}
```

Add this to your module's `build.gradle` file:

```gradle
dependencies {

  implementation 'com.afollestad:recyclical:0.1.0'
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
      
      val items = listOf(
        Person("Aidan", 24),
        Person("Nina", 24)
      )
      val dataSource = DataSource(items)
      
      // setup{} is an extension method on RecyclerView
      recyclerView.setup {
          withDataSource(dataSource)
          withItem<Person>(R.layout.person_item_layout) {
            onBind(::PersonViewHolder) { _, item ->
              name.text = item.name
              age.text = "${item.age}"
            }
            onClick { index, item ->
              toast("Clicked $index: ${item.name}")
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
}
```

---

## Multiple Item Types

You can mix different types of items, but need to specify view holders and layouts for them too:

```kotlin
val items = listOf(
  Car(2012, "Volkswagen GTI"),
  Motorcycle(2018, "Triumph", "Thruxton R"),
  Person("Aidan", 24)
)
val dataSource = DataSource(items)

recyclerView.setup {
  withDataSource(dataSource)
    
  withItem<Person>(R.layout.person_item_layout) {
     onBind(::PersonViewHolder) { _, item ->
        name.text = item.name
        age.text = "${item.age}"
     }
  }
  withItem<Motorcycle>(R.layout.motorcycle_item_layout) {
     onBind(::MotorcycleViewHolder) { _, item ->
        year.text = "${item.year}"
        make.text = item.make
        model.text = item.model
     }
  }
  withItem<Car>(R.layout.car_item_layout) {
     onBind(::CarViewHolder) { _, item ->
        year.text = "${item.year}"
        name.text = item.name
     } 
  }
}
```

---

## Data Source Manipulation

```kotlin
val dataSource = DataSource()
val person = Person("Aidan", 24)

// gets item from a given index from the data source
val item = dataSource[5]
// append item to the data source
dataSource += person
// replaces all items in the data source with another list of items
dataSource.set(listOf(person))
// insert an item into the data source at a given index
dataSource.insert(1, person)
// remove an item from the data source at a given index
dataSource.removeAt(1)
// removes an item from the data source
dataSource.remove(person)
// swaps two items by their indices in the data source
dataSource.swap(1, 4)
// moves an item to another index in the data source
dataSource.move(1, 4)
// clears all items from the data source
dataSource.clear()

// iterates over the items in the data source
for (item in dataSource) { }
dataSource.forEach { }
dataSource.forEachOf<Person> { }

// collection-like methods
dataSource.size()
dataSource.isEmpty()
dataSource.isNotEmpty()
dataSource.indexOfFirst { }
dataSource.indexOfLast { }
```

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