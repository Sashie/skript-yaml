# skript-yaml
The proper way to do yaml in skript

Rather then checking the file each time this addon caches the yaml file to memory

## Syntax


### Effect (Load yaml)
Loads a yaml file into memory
  - The first input is the yaml file path(ie. "plugins/MyAwesomePlugin/config.yml")
  - The second input allows you to choose your own id for this file
  - If the second input isn't used then the files name minus the extention is used as the id

#### Syntax

`[re]load [y[a]ml] %string% [as %-string%]`

#### Example

```
#Both examples produce the same id for use in other effects/expressions
load yaml "plugins/test/config.yml"
load yaml "plugins/test/config.yml" as "config"
```

---

### Effect (Unload yaml)
Unloads a yaml file from memory

#### Syntax

`unload [y[a]ml] %string%`

#### Example

```
unload yaml "config"
```
---

### Effect (Save yaml)
Saves the current cached yaml elements to file

#### Syntax

`save [y[a]ml] %string%`

#### Example

```
save yaml "config"
```
---

### Expression (Return all cached yaml)
Returns a list of all 'cached' yaml file ids

#### Syntax

`[(the|all (of the|the))] [currently] loaded y[a]ml [files]`

#### Example

```
set {_list::*} to the currently loaded yaml files
broadcast "%{_list::*}%"
```
---

### Expression (Yaml)
Gets, sets, removes values/nodes etc.. of a cached yaml file
  - Requires the id used/created from the load effect
  - This expression does not save to file

#### Syntax

`[[skript-]y[a]ml] (1¦value|2¦node[s]|3¦node[s with] keys|4¦list) %string% (of|in|from) %string%`

#### Example

```
set yaml value "test1.test2" from file "config" to "test3"

set {_test} to yaml value "test1.test2" from file "config"
broadcast "%{_test}%"

#similar syntax as skQuery and skellett
```
---

### Expression (All yaml nodes)
Gets a list of all nodes of a cached yaml file

#### Syntax

`[all] [[skript-]y[a]ml] node[s] (of|in|from) %string%`

#### Example

```
set yaml value "test1.test2" from file "config" to "test3"
set yaml value "boop.beep" from file "config" to "bop"

set {_list::*} to all yaml nodes of "config"
broadcast "%{_list::*}%"

```
---

### Condition (Is yaml loaded)
Checks if one or more yaml files are loaded into memory using said id

#### Syntax

`y[a]ml[s] %strings% (is|are) loaded`

`y[a]ml[s] %strings% ((are|is) not|(is|are)n[']t) loaded`

---

### Condition (Does yaml path exist)
Checks if one or more paths exist in a cached yaml file using said id
  - First input is the path
  - Second input is the id
  - If multiple paths are checked at once it will return false on the first one found to not exist

#### Syntax

`[skript-]y[a]ml [path[s]] %strings% (of|in|from) %string% exists`

`[skript-]y[a]ml [path[s]] %strings% (of|in|from) %string% does(n't| not) exist`

#### Example

```
set skript-yaml value "test.test" from "config" to "test"
set skript-yaml value "test2.test2" from "config" to "test"

yaml path "test.test" and "test2.test2" in "config" exists:
    broadcast "this works"
yaml path "test.test" and "boop.boop" in "config" exists:
    broadcast "this will fail"
```
---

## Thanks!
I'd like to thank the whole Skript community, without users like you I wouldn't have bothered to make this!

A special shout out goes to @Pikachu for helping me with the syntax and some ideas <3
