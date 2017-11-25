# skript-yaml
The proper way to do yaml in skript

Rather then checking the file each time this addon caches the yaml file to memory

These results speak for themselves..

<p align="center">
  <img src="https://github.com/Sashie/skript-yaml/blob/master/res/results1.png" alt="Results 1"/>
</p>
<p align="center">
  <img src="https://github.com/Sashie/skript-yaml/blob/master/res/results2.jpg" alt="Results 2""/>
</p>
<p align="center">
  <img src="https://github.com/Sashie/skript-yaml/blob/master/res/results3.png" alt="Results 3""/>
</p>

The skript used for this test is found [here](https://github.com/Sashie/skript-yaml/blob/master/res/yaml-tests.sk)

## Syntax


### Effect (Load yaml)
Loads a yaml file into memory
  - The first input is the yaml file path(ie. "plugins/MyAwesomePlugin/config.yml")
  - The second input allows you to choose your own id for this file
  - If the second input isn't used then the files name minus the extention is used as the id for example 'config.yml' becomes 'config'

#### Syntax

`[re]load [y[a]ml] %string% [as %-string%]`

#### Example

```
#Both examples produce the same id for use in other effects/expressions
load yaml "plugins/MyAwesomePlugin/config.yml"
load yaml "plugins/MyAwesomePlugin/config.yml" as "config"


#to get similar function as the other addons you would do this sort of thing with the id...
	load yaml "plugins/MyAwesomePlugin/config.yml" as "plugins/MyAwesomePlugin/config.yml"
	set yaml value "version" from "plugins/MyAwesomePlugin/config.yml" to 1.0
	broadcast "%yaml value "version" from "plugins/MyAwesomePlugin/config.yml"%"
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
  - Lists accept list variables for input
  - Using 'without string checks' optional is a tiny bit faster but doesn't check/convert strings for numbers or booleans

#### Syntax

`[[skript-]y[a]ml] (1¦value|2¦(node|path)[s]|3¦(node|path)[s with] keys|4¦list) %string% (of|in|from) %string% [without string checks]`

#### Example

```
set yaml value "test1.test2" from "config" to "test3"
set yaml list "list.name" from "config" to {_list::*}

set {_test} to yaml value "test1.test2" from "config"
broadcast "%{_test}%"

#similar syntax as skQuery and skellett
```
---

### Expression (All yaml nodes)
Gets a list of all nodes of a cached yaml file

#### Syntax

`[all] [[skript-]y[a]ml] (node|path)[s] (of|in|from) %string%`

#### Example

```
set yaml value "test1.test2" from "config" to "test3"
set yaml value "boop.beep" from "config" to "bop"

set {_list::*} to all yaml nodes of "config"
broadcast "%{_list::*}%"
```
---

### Expression (Yaml comment/header)
Gets, sets, deletes comments or the header of a cached yaml file
  - Headers don't contain '#' so add it yourself if you want it
  - Comments can only be at root level ie. 'root' not 'root.something'
  - Both header and comments accept list variables for input
  - This expression does not save to file

#### Syntax

`[the] comment[s] (of|from) [y[a]ml] node[s] %strings% (of|in|from) %string%"`

`[the] (comment[s] (at|on) [the] top of |header (of|from)) %string%`

#### Example

```
set the comments of yaml node "test" from "config" to "First line" and "Second line"
delete the comments of yaml node "test" from "config"

set {_header::*} to "First line" and "Second line"
set the comments at the top of "config" to {_header::*}
delete  the comments at the top of "config"

set the header of "config" to "First line" and "Second line"
delete  the header of "config"
set the header of "config" to {_header::*}
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

`[skript-]y[a]ml [(node|path)[s]] %strings% (of|in|from) %string% exists`

`[skript-]y[a]ml [(node|path)[s]] %strings% (of|in|from) %string% does(n't| not) exist`

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

### Condition (Does yaml path have value)
Checks if one or more values exist at a path in a cached YAML file using said ID.
  - First input is the path
  - Second input is the id
  - If multiple paths are checked at once it will return false on the first one found to not contain a value.

#### Syntax

`[skript-]y[a]ml [(node|path)[s]] %strings% (of|in|from) %string% has value`

`[skript-]y[a]ml [(node|path)[s]] %strings% (of|in|from) %string% does(n't| not) have value`

#### Example

```
set skript-yaml value "test.test" from "config" to "test"

yaml path \"test.test\" in \"config\" has value:
    broadcast "has value"
```
---

## Skripts


[ez-yaml.sk](https://github.com/Sashie/skript-yaml/blob/master/res/ez-yaml.sk)

  - Updated version thanks to @Pikachu920 of [this](https://forums.skunity.com/resources/ezyml.85/) Skript API

```
createYMLFile("plugins/MyAwesomePlugin/boop.yml", "list: listName:50;3.14;true;false;yes;no;on;off||value: valueName1:true||value: valueName2:2||value: valueName3:2.6||value: valueName4:This is a string")
```

---

[yaml-tests.sk](https://github.com/Sashie/skript-yaml/blob/master/res/yaml-tests.sk)

  - Same as above test made by @Rezz converted to skript-yaml by @Pickachu920
  
---

## Thanks!
I'd like to thank the whole Skript community, without users like you I wouldn't have bothered to make this!

A special shout out goes to @Pikachu920 for helping me with the syntax and some ideas <3
