0.7.1

1. Kotlin 1.3.0
2. A new `recyclical-swipe` module! Add it your app to gain access to an extension to the core library which enables swipe actions, like swipe to delete.
3. `DataSource` now accepts a generic type, to make it easier if your list only contains one type of item. (sorry if this breaks your app temporarily, you just need to use `DataSource<Any>` if you don't care about any specific type). 