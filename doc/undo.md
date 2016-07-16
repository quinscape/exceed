Undo service
============

The undo service is a helper to implement stateful react or other components with undo support based on data updated with 
immutable updates.


Creating the Undo Helper
------------------------

First you need to create an undo helper instance with
    
```JavaScript
    undo = undoService.create(initial, state => this.setState(initial));
```

The state handled by the undo manager is most often not the complete state of the module using it, although it can be. 
But very often there is both component state that should be part of the undo/redo mechanism and other component state
that one would not want to be part of the undo/redo, e.g. filter settings, paging etc.

So it's common to copy only some of your state keys over the undo helper for undo/redo handling. The undo helper will
usually be created in getInitialState() with a partial copy of the initial overall component state.

The second argument is a callback that is called from the undo helper with the current state, which might be a new
one or one of the old ones. The component must make sure to transfer that current undo helper state over to the component state 
like here by setting it as partial react state.

Make sure to .destroy() the helper on componentWillUnmount

The undo helper will automatically setup the standard shortcuts for Undo and Redo 
via Mousetrap.

Creating a new undo state
-------------------------

When your component has immutably updated its state it has to let the undo helper know 
to create a new undo step. If a new state is created while there are states to reactivate
with "redo", those states will be lost.


```JavaScript
undo.newState(partial)
```

Action Methods
-------

will create a new undo state and call the undo helper state callback for it.

```JavaScript
undo.undo();
```

Will undo the latest step if possible.


```JavaScript
undo.redo();
```
Will redo an undone change if possible

```JavaScript
undo.revert();
```

Will revert to the last saved position in the undo states.

```JavaScript
undo.markSaved()
```
Marks the current state as saved.

State Check Methods
----


```JavaScript
undo.canUndo()
undo.canRedo()
```
Return true if undo/redo is possible in the current state.


```JavaScript
undo.isSaved()
```
Returns true if the current state is saved.


Misc
--
```JavaScript
undo.getSavedState()
```
Returns the state that is marked as "saved" among the undo states.

```JavaScript
undo.destroy()
```

Destroys the undo helper and derregisters events.
