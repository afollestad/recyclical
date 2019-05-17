0.9.0

* `withItem` requires that you specify the `ViewHolder` type as a generic parameter.
* Add `getSelectedItems(): List<IT>` method to `SelectableDataSource`.
* Add `onRecycled {}` method to `ItemDefinition`. 

### 0.8.0

1. Fix a crash caused by stored indices getting out of date as items are removed from the list.
2. `withItem` can take a custom class name string to support generated model classes, see #11.
3. Added generic `withSwipeActionOn<>` method to the swipe plugin to target specific item types, see #12.
4. Internal cleanup and dependency upgrades. 