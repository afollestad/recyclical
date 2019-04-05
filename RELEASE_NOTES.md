0.6.0

1. Add `onChildViewClick` method to DSL when defining an item definition. See the README for examples.
2. The signature of the callback for item click and long click listeners is changed. `item` is no longer a lambda parameter, it exists as a `val` in `this`. 