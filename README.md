# skript-yaml
The proper way to do yaml in skript

Rather then checking the file each time this addon caches the yaml file to memory

## Syntax



### Effect
Loads a yaml file into memory
  - The optional adds an id name otherwise it uses the files name itself as the id

#### Syntax

`[re]load y[a]ml %string% [as [id] %-string%]`

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

`unload y[a]ml %string%`

#### Example

```
unload yaml "config"
```
---

### Effect
Gets a list of all 'cached' yaml files

#### Syntax

`(the|all [of the]) [currently] loaded y[a]ml [files]`

#### Example

```
set {_list::*} to the currently loaded yaml files
broadcast "%{_list::*}%"
```
---

### Effect
Saves the current yaml to file

#### Syntax

`save y[a]ml %string%`

#### Example

```
save yaml file "config"
```
---

### Effect
Gets, sets, removes valeus/nodes etc.. of a yaml file

#### Syntax

`[skript-]y[a]ml (1¦value|2¦node[s]|3¦node[s with] keys|4¦list) %string% (in|at|from) [file] %string%`

#### Example

```
set yaml value "test1.test2" from file "config" to "test3"

set {_test} to yaml value "test1.test2" from file "config"
broadcast "%{_test}%"

#same syntax for this expression as skQuery and skellett
```
---


