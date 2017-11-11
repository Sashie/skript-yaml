# skript-yaml
The proper way to do yaml in skript

Rather then checking the file each time this addon caches the yaml file to memory

## Syntax


### Effect
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

### Effect
Unloads a yaml file from memory

#### Syntax

`unload [y[a]ml] %string%`

#### Example

```
unload yaml "config"
```
---

### Effect
Saves the current cached yaml elements to file

#### Syntax

`save [y[a]ml] %string%`

#### Example

```
save yaml file "config"
```
---

### Expression
Returns a list of all 'cached' yaml file ids

#### Syntax

`[(the|all (of the|the))] [currently] loaded y[a]ml [files]`

#### Example

```
set {_list::*} to the currently loaded yaml files
broadcast "%{_list::*}%"
```
---

### Expression
Gets, sets, removes values/nodes etc.. of a cached yaml file
  - Requires the id used/created from the load effect
  - This expression does not save to file

#### Syntax

`[skript-]y[a]ml (1¦value|2¦node[s]|3¦node[s with] keys|4¦list) %string% (of|in|from) %string%`

#### Example

```
set yaml value "test1.test2" from file "config" to "test3"

set {_test} to yaml value "test1.test2" from file "config"
broadcast "%{_test}%"

#same syntax for this expression as skQuery and skellett
```
---


