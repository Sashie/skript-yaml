# skript-yaml
The proper way to do yaml in skript

Rather then checking the file each time this addon caches the yaml file to memory

## Syntax



### Effect


#### Syntax

`[re]load y[a]ml %string% [as %-string%]`

#### Example

```
load yaml "plugins/test/config.yml"
load yaml "plugins/test/config.yml" as "config"
```

---


### Effect


#### Syntax

`unload y[a]ml %string%`

#### Example

```
unload yaml "config"
```
---

### Effect


#### Syntax

`(the|all [of the]) [currently] loaded y[a]ml [files]`

#### Example

```
set {_list::*} to the currently loaded yaml files
broadcast "%{_list::*}%"
```
---

### Effect


#### Syntax

`save y[a]ml %string%`

#### Example

```
save yaml file "config"
```
---

### Effect


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


