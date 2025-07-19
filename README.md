# skript-yaml
The proper way to do yaml in skript

Rather then checking the file each time this addon caches the yaml file to memory

## Contents

Effects
 - [Load yaml](#effect-load-yaml)
 - [Load all yaml from directory from directory](#effect-load-all-yaml-from-directory)
 - [Delete yaml](#effect-delete-yaml)
 - [Delete all or any loaded yaml from directory](#effect-delete-all-or-any-loaded-yaml-from-directory)
 - [Unload yaml](#effect-unload-yaml)
 - [Save yaml](#effect-save-yaml)
 
Expressions
 - [Return all cached yaml](#expression-return-all-cached-yaml)
 - [Return all cached yaml directories](#expression-return-all-cached-yaml-directories)
 - [Yaml](#expression-yaml)
 - [Yaml list value](#expression-yaml-list-value)
 - [All yaml nodes](#expression-all-yaml-nodes)
 - [Yaml comment/header](#expression-yaml-comment-or-header)
 - [Yaml loop](#expression-yaml-loop)
 
 Conditions
 - [Is yaml loaded](#condition-is-yaml-loaded)
 - [Is yaml empty](#condition-is-yaml-empty)
 - [Does yaml path have value](#condition-does-yaml-path-have-value)
 - [Does yaml path exist](#condition-does-yaml-path-exist)
 - [Is yaml path a list](#condition-does-yaml-path-have-list)
 - [Does yaml exist](#condition-does-yaml-exist)

## Effects

### Effect (Load yaml)

Loads a yaml file into memory
  - If the file doesn't exist it will be created for you, no need to use other addons to create a file!
  - Using the optional `[non[(-| )]relative]` allows for root directory access
  - The first input is the yaml file path(ie. "plugins/MyAwesomePlugin/config.yml") (see example for root directories)
  - The second input allows you to choose your own id for this file
  - If the second input isn't used then the files name minus the extension is used as the id for example 'config.yml' becomes 'config'

#### Syntax

`[re]load [(1¦non[(-| )]relative)] y[a]ml %strings%`

`[re]load [(1¦non[(-| )]relative)] y[a]ml %string% as %string%`

`[re]load [(1¦non[(-| )]relative)] y[a]ml %strings% using [the] [file] path[s] as [the] id[s]`

#### Example

```skript
# Load a YAML file and use its filename (without extension) as the ID
load yaml "plugins/MyAwesomePlugin/config.yml"

# Load a YAML file and specify a custom ID
load yaml "plugins/MyAwesomePlugin/config.yml" as "config"

# Root directory example (Windows)
load non-relative yaml "RootFolder/MyAwesomePlugin/config.yml"
# Or specify a drive
load non-relative yaml "C:/RootFolder/MyAwesomePlugin/config.yml"

# To get similar function as the other addons you would do this sort of thing with the id...
load yaml "plugins/MyAwesomePlugin/config.yml" as "plugins/MyAwesomePlugin/config.yml"
set yaml value "version" from "plugins/MyAwesomePlugin/config.yml" to 1.0
broadcast "%yaml value 'version' from 'plugins/MyAwesomePlugin/config.yml'%"
```

---

### Effect (Load all yaml from directory)
Loads a directory YAML files into memory.
  - The input is a directory (ie. "plugins/MyAwesomePlugin/").
  - If for example a file in that directory is named test.yml then the output ID would be 'plugins/MyAwesomePlugin/test.yml'
  - Using the optional filename ID would output `test.yml`"

#### Syntax

`[re]load all y[a]ml from [(1¦non[(-| )]relative)] director(y|ies) %strings% [using [the] filename as [the] id]`

#### Example

```skript
# Load all YAML files from a directory and loop through their nodes
load all yaml from directory "plugins/skript-yaml/test"
loop the loaded yaml:
    loop yaml nodes "" from loop-value-1:
        loop yaml nodes loop-value-2 from loop-value-1:
            broadcast yaml value "%loop-value-2%.%loop-value-3%" from loop-value-1
```

---

### Effect (Delete yaml)
Unloads a yaml file from memory and deletes the file

#### Syntax

`delete y[a]ml %string% and keep loaded in memory`

#### Example

```skript
# Delete the YAML file with ID "config" from memory and disk
delete yaml "config"

# Delete the YAML file with ID "config" from disk and keep it in memory
delete yaml "config" and keep loaded in memory
```

---

### Effect (Delete all or any loaded yaml from directory)
Unloads a directory of yaml files from memory and deletes them

#### Syntax

`delete (all|any) y[a]ml from [(1¦non[(-| )]relative)] director(y|ies) %strings%`

`delete (all|any) loaded y[a]ml from [(1¦non[(-| )]relative)] director(y|ies) %strings% [using [the] filename as [the] id]`

#### Example

```skript
# Delete all loaded YAML files from a directory
delete all yaml from directory "plugins/skript-yaml/test"
```

---

### Effect (Unload yaml)
Unloads one or more yaml files from memory
 - If using the `[(1¦director(y|ies))]` option the input string must be a directory instead of an id

#### Syntax

`unload y[a]ml [(1¦director(y|ies))] %strings%`

#### Example

```skript
# Unload a YAML file from memory (does not delete the file)
unload yaml "config"
```
---

### Effect (Save yaml)
Saves the current cached yaml elements to file
 - Using the `[with an indentation of %-number%]` option allows you to save the file with a different amount of spacing between 1 and 10
#### Syntax

`save y[a]ml %strings% [with an indentation of %-number%] [(1¦[and] with(out| no) extra lines between nodes)]`

#### Example

```skript
# Save the YAML file with default indentation
save yaml "config"

# Save the YAML file with an indentation of 2
save yaml "config" with an indentation of 2
```
---

## Expressions

### Expression (Return all cached yaml)
Returns a list of all 'cached' yaml file ids
  - Using `from (director(y|ies) %-strings%` option gets ids from only said directories

#### Syntax

`[(the|all [(of the|the)])] [currently] loaded y[a]ml [files] [from (director(y|ies) %-strings%|all directories)]`

#### Example

```skript
# Get all currently loaded YAML file IDs
set {_list::*} to the currently loaded yaml files
broadcast "%{_list::*}%"

# Loop through all loaded YAML files
loop the loaded yaml:
    broadcast loop-value

# Loop through loaded YAML files from a specific directory
loop the loaded yaml from directory "plugins/skript-yaml":
    broadcast loop-value
```
---

### Expression (Return all cached yaml directories)
Returns a list of directories from all 'cached' yaml file ids

#### Syntax

`[(the|all [(of the|the)])] [currently] loaded y[a]ml directories`

#### Example

```skript
# Loop through all loaded YAML directories
loop the loaded yaml directories:
    broadcast loop-value
```
---

### Expression (Yaml)
Gets, sets, removes values/node keys etc.. of a cached yaml file
  - Requires the id used/created from the load effect
  - This expression does not save to file
  - Lists accept list variables for input
  - Using 'without string checks' optional is a tiny bit faster but doesn't check/convert strings for numbers or booleans
  - Using '(node|path) list' only gets a list of nodes at that path (full names like 'rootnode.subnode' are returned)

#### Syntax

`[skript-]y[a]ml (1¦value|2¦(node|path) list|3¦(node|path)[s with] keys|4¦list) %string% (of|in|from) %string% [without string checks]`

#### Examples

```skript
# Set a value in a YAML file
set yaml value "test1.test2" from "config" to "test3"

# Set a list in a YAML file
set yaml list "list.name" from "config" to {_list::*}

# Retrieve a value from a YAML file
set {_test} to yaml value "test1.test2" from "config"
broadcast "%{_test}%"

# To delete a value you can use either of these two syntax
# Delete a value at a specific path
delete yaml value "test1.test2" in "uniqueID"
# Remove a key from a node
remove "test2" from yaml node key "test1" in "uniqueID"
```

```skript
# Example: Saving and teleporting with Skript-YAML
on script load:
    load yaml "plugins/skript-yaml/teleport.yml" as "plugins/skript-yaml/teleport.yml"
    
command /savetp:
    trigger:
        set yaml value "%player%.location" from "plugins/skript-yaml/teleport.yml" to location of player
        save yaml "plugins/skript-yaml/teleport.yml"

command /tp:
    trigger:
        teleport player to yaml value "%player%.location" from "plugins/skript-yaml/teleport.yml"
```
---

### Expression (Yaml list value)
Gets, sets, removes values from a list in a cached yaml file
  - Requires index between 1 and the size of the list
  - Requires the id used/created from the load effect
  - This expression does not save to file
  - Using 'without string checks' optional is a tiny bit faster but doesn't check/convert strings for numbers or booleans

#### Syntax

`(index|value) %number% (of|in|from) [skript-]y[a]ml list %string% (of|in|from) %string% [without string checks]`

#### Examples

```skript
# Set a value at a specific index in a YAML list
set index 1 in yaml list "test1.test2" from "config" to "test3"

# Get a value from a YAML list
set {_test} to index 1 in yaml list "test1.test2" from "config"
broadcast "%{_test}%"
```

---

### Expression (All yaml nodes)
Gets a list of all nodes of a cached yaml file

#### Syntax

`[all] [skript-]y[a]ml (node|path)[s] (of|in|from) %string%`

#### Example

```skript
# Set some values in the YAML file
set yaml value "test1.test2" from "config" to "test3"
set yaml value "boop.beep" from "config" to "bop"

# Get all nodes from the YAML file
set {_list::*} to all yaml nodes of "config"
broadcast "%{_list::*}%"
```
---

### Expression (Yaml comment or header)
Gets, sets, deletes comments or the header of a cached yaml file
  - Headers don't contain '#' so add it yourself if you want it
  - Comments can only be at root level ie. 'root' not 'root.something'
  - Both comment and header expressions can be set to multiple elements
  - This expression does not save to file

#### Syntax

`[the] comment[s] (of|from) y[a]ml node[s] %strings% (of|in|from) %string%" [(1¦with [an] extra line)]`

`[the] (comment[s] (at|on) [the] top of |header (of|from)) %string% [(1¦with [an] extra line)]`

#### Example

```skript
# Set comments on a root node
set the comments of yaml node "test" from "config" to "First line" and "Second line"
# Delete comments from a root node
delete the comments of yaml node "test" from "config"

# Set the header of a YAML file
set {_header::*} to "First line" and "Second line"
set the comments at the top of "config" to {_header::*}
delete the comments at the top of "config"

set the header of "config" to "First line" and "Second line"
delete the header of "config"
set the header of "config" to {_header::*}
```
---

### Expression (Yaml loop)
Loop expressions to use while looping a yaml expression
  - Only works while using [yaml node list](#expression-yaml), [yaml node keys](#expression-yaml) and [yaml list](#expression-yaml)
  - `loop-id` gets the id used in the yaml expression
  - `loop-val` gets a value at that node if one exists
  - `loop-list` gets a list at that node if one exists
  - `loop-node` gets the full path plus key
  - `loop-key` gets just the keys
  - `loop-subnodekeys` gets any subnode from the current node (does not work on lists)


#### Syntax

`[the] loop-(1¦id|2¦val|3¦list|4¦node|5¦key|6¦subnodekey[s]|7¦iteration)`

#### Example
Yaml file:

```yaml
settings:
  subnode1: value1
  subnode2: value2
node:
  subnode1: value1
  subnode2: value2
node2:
- listValue1
- listValue2
```

Skript file:

```skript
# Loop through node keys and broadcast their values
loop yaml node keys "node" from "config":
    broadcast yaml value "%loop-node%" from "%loop-id%"
```
---

## Conditions

### Condition (Is yaml loaded)
Checks if one or more yaml files are loaded into memory using said id

#### Syntax

`y[a]ml[s] %strings% (is|are) loaded`

`y[a]ml[s] %strings% ((are|is) not|(is|are)n[']) loaded`

#### Example

```skript
# Check if a YAML file is loaded
if yaml "config" is loaded:
    broadcast "YAML file is loaded!"
```
---

### Condition (Is yaml empty)
Only checks if there are any nodes or not

#### Syntax

`[skript-]y[a]ml %string% is[(n't| not)] empty`

#### Example

```skript
# Check if a YAML file is empty
if yaml "config" is empty:
    broadcast "YAML file is empty!"
```
---

### Condition (Does yaml path have value)
Checks if one or more values exist at a path in a cached YAML file using said ID.
  - First input is the path
  - Second input is the id
  - If multiple paths are checked at once it will return false on the first one found to not contain a value.

#### Syntax

`[skript-]y[a]ml [(node|path)[s]] %strings% (of|in|from) %string% has [a] value[s]`

`[skript-]y[a]ml [(node|path)[s]] %strings% (of|in|from) %string% does(n't| not) have [a] value[s]`

#### Example

```skript
# Set a value in the YAML file if nothing is set
if yaml path "test.test" in "config" doesn't have value:
    set yaml value "test.test" from "config" to "test"

# Check if the path has a value
if yaml path "test.test" in "config" has value:
    broadcast "Path has a value!"
```
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

```skript
# Set values in the YAML file
set yaml value "test.test" from "config" to "test"
set yaml value "test2.test2" from "config" to "test"

# Check if paths exist
if yaml path "test.test" and "test2.test2" in "config" exists:
    broadcast "Both paths exist!"
if yaml path "test.test" and "boop.boop" in "config" exists:
    broadcast "This will fail if 'boop.boop' does not exist."
```
---

### Condition (Does yaml path have list)
Checks if one or more paths contain a list in a cached yaml file using said id
  - First input is the path
  - Second input is the id
  - If multiple paths are checked at once it will return false on the first one found to not exist

#### Syntax

`[skript-]y[a]ml [(node|path)[s]] %string% (of|in|from) %string% has [a] list`

`[skript-]y[a]ml [(node|path)[s]] %string% (of|in|from) %string% does(n't| not) have [a] list`

#### Example

```skript
# Check if a node contains a list
if yaml node "listnode" from "example" has list:
    loop yaml list "listnode" from "example":
        broadcast "%loop-val%"
```
---

### Condition (Does yaml exist)
Checks if a yaml file exists
  - You really shouldn't have to use this since the [load yaml](#effect-load-yaml) effect creates one if it doesn't already exist
  - Input is the yaml file path


#### Syntax

`[(1¦non[(-| )]relative)] y[a]ml file %string% exists`

`[(1¦non[(-| )]relative)] y[a]ml file %string% does(n't| not) exist`

#### Example

```skript
# Check if a YAML file exists
if yaml file "plugins/MyAwesomePlugin/config.yml" exists:
    broadcast "YAML file exists!"
```
---

## Skripts


[ez-yaml.sk](https://github.com/Sashie/skript-yaml/blob/master/res/ez-yaml.sk)

  - Updated version thanks to @Pikachu920 of [this](https://forums.skunity.com/resources/ezyml.85/) Skript API

```skript
# Example of creating a YAML file with various values and lists
createYMLFile("plugins/MyAwesomePlugin/boop.yml", "list: listName:50;3.14;true;false;yes;no;on;off||value: valueName1:true||value: valueName2:2||value: valueName3:2.6||value: valueName4:This is a string")
```

---

[yaml-tests.sk](https://github.com/Sashie/skript-yaml/blob/master/res/yaml-tests.sk)

  - Old test made by @Rezz converted to skript-yaml by @Pickachu920
  
---

## Thanks!
I'd like to thank the whole Skript community, without users like you I wouldn't have bothered to make this!

A special shout out goes to @Pikachu920 for helping me with the syntax and some ideas <3
