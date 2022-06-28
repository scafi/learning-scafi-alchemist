# Simulations with Alchemist and Scafi: tutorial

This tutorial presents a sequence of increasingly rich examples using the [Scafi aggregate programming DSL](https://scafi.github.io) and [the Alchemist Simulator](https://alchemistsimulator.github.io/).

## Requirements

- A [Gradle-compatible Java version](https://docs.gradle.org/current/userguide/compatibility.html) e.g., [temurin](https://adoptium.net/temurin/releases/)
- A local installation of [Git](https://git-scm.com/)
- [Optional] A working version of Python 3 for the plotting part

**Check if it works** 

Open a terminal and type
```
java -version
git --version
```

Now you are ready to launch Alchemist & ScaFi simulations
## Quickstart

Open a terminal and run:

**Windows**
```powershell
curl https://raw.githubusercontent.com/scafi/learning-scafi-alchemist/master/launch.ps1 | Select-Object -ExpandProperty Content | powershell.exe
```
**Linux & Mac**
```bash
curl https://raw.githubusercontent.com/scafi/learning-scafi-alchemist/master/launch.sh | bash
```

It will take some time for the system to download all the required dependencies,
at the end of the process you will be presented
the Alchemist default GUI
([here](https://alchemistsimulator.github.io/reference/default-ui/) are instructions on how to interact with the simulator).
At this point, the simulation should be looking like this:
![Alchemist simulation start](https://user-images.githubusercontent.com/23448811/175499943-f346221b-9308-4cf0-8402-d90ad3bc56c6.png)

Click <kbd>P</kbd> to start the simulation.
The nodes will compute the ScaFi program described
[here](https://github.com/scafi/learning-scafi-alchemist/blob/master/src/main/scala/it/unibo/scafi/examples/HelloWorld.scala))
in rounds,
producing node colour changes.
![Alchemist simulation evolution](https://user-images.githubusercontent.com/23448811/175502234-a2c5ae1a-c909-4545-ba5e-8cea0441cbd3.gif)

### What happened

Issuing the one-liner command, you have:
1. downloaded this repository using Git
2. created a folder called `learning-scafi-alchemist` that contains the simulations 
3. executed the command `./gradlew runHelloScafi` inside the `learning-scafi-alchemist` created above.

The last command produces the execution of the simulation called `helloScafi` described using a `yml` [file](https://github.com/scafi/learning-scafi-alchemist/blob/master/src/main/yaml/helloScafi.yml).
Particularly an Alchemist simulation typically consists of a network of devices that could communicate with each other by means of a neighbourhood relationship (you can see the connections by clicking <kbd>L</kbd>)
![Alchemist Neibourhood Relationship](https://user-images.githubusercontent.com/23448811/175505269-cb2f6281-d7f2-40ff-9a11-fe7301166092.png)
In this case, the nodes' positions are configured through real GPS traces from the Vienna marathon app.
The simulation effects (i.e., node shapes and colours) are highly configurable through JSON [configuration](https://github.com/scafi/learning-scafi-alchemist/blob/master/effects/helloScafi.json). Here the node colour depends on the output of the ScaFi program, which is executed in each device every 1 second. Particularly, the execution of a ScaFi program deals with local computations and interaction among neighbours through a distributed data structure called *computational field*. This distributed and repeated execution of *rounds* eventually produces a collective result (you can find more details about the execution model of ScaFi programs in the [documentation](https://scafi.github.io/docs/#execution-model)).

In this case, the program consists of the evaluation of the distance from the node with the ID 100 (in Aggregate Computing literature called "gradient").
<!--
* download repo
* creation folder xxx
* command uiiuiii executed inside xxx

* Network of devices
* colors based on ...
* the simulator runs the program every x seconds
* the interaction among devices builds the distributed data structure
-->
**Something wrong?**

Try the following:
1. clone manually using `git clone https://github.com/scafi/learning-scafi-alchemist.git`
    1. alternatively: download the repository zip ([<kbd>Download!</kbd>](https://github.com/scafi/learning-scafi-alchemist/archive/refs/heads/master.zip))  
    3. then unzip the repository to a local folder
4. open a terminal inside the cloned/downloaded folder
5. run `./gradlew runHelloScafi`

If you still have problems executing the experiments, please consider opening an issue! [<kbd> New issues</kbd>](https://github.com/scafi/learning-scafi-alchemist/issues/new)

## Guided examples

from now on, we will assume all commands have been issued inside `learning-scafi-alchemist`

### 1. Hello, ScaFi! A gradient in space and time

#### Launch command
```bash
./gradlew runHelloScafi
```

#### What happened
This is the example described in [Quickstart](#quickstart) section.
Particularly, the program consists in the description of the self-healing gradient: an algorithm that computes a gradient (i.e.,  a field mapping each device in the system with its minimum distance from the closest *source* device) field
and automatically adjusts it after changes in the source set and the connectivity network (more details about gradients can be found in [Compositional Blocks for Optimal Self-Healing Gradients](https://ieeexplore-ieee-org.ezproxy.unibo.it/document/8064033)).
#### What is inside
|Configuration File|ScaFi Program File|
|-|-|
| [helloScafi.yml](https://github.com/scafi/learning-scafi-alchemist/blob/master/src/main/yaml/helloScafi.yml) | [HelloScafi.scala](https://github.com/scafi/learning-scafi-alchemist/blob/master/src/main/scala/it/unibo/scafi/examples/HelloScafi.scala) |

An Alchemist simulation could be described through yml configurations.
In order to execute ScaFi script, you should at least define:

- the Scafi incarnation:
<!-- embedme ./src/main/yaml/helloScafi.yml#L1-L1 -->
```yml
incarnation: scafi
```
- a `Reaction` that contains the `Action` `RunScafiProgram` with the full class name of the program chosen 
<!-- embedme ./src/main/yaml/helloScafi.yml#L37-L46 -->
```yml
_reactions:
  - program: &program
      - time-distribution:
          type: ExponentialTime
          parameters: [*programRate]
        type: Event
        actions:
          - type: RunScafiProgram
            parameters: [it.unibo.scafi.examples.HelloScafi, *retentionTime]
      - program: send
```
- a deployment that contains in the programs the Action specified above
<!-- embedme ./src/main/yaml/helloScafi.yml#L56-L60 -->
```yml
deployments: ## i.e, how to place nodes
  type: FromGPSTrace ## place nodes from gps traces
  parameters: [*totalNodes, *gpsTraceFile, true, "AlignToTime", *timeToAlign, false, false]
  programs: ## the reactions installed in each nodes
    - *program
```
More details about the Alchemist configuration could be found in the [official guide](http://alchemistsimulator.github.io/reference/yaml/).

The main logic of the node behaviour is described through the Scafi program file.
Particularly, a valid ScaFi program must:
1. choose an incarnation
<!-- embedme ./src/main/scala/it/unibo/scafi/examples/HelloScafi.scala#L3-L3 -->
```scala
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._
```
2. extend the `AggregateProgram` trait
<!-- embedme ./src/main/scala/it/unibo/scafi/examples/HelloScafi.scala#L7-L7 -->
```scala
class HelloScafi extends AggregateProgram
```
3. mix-in the libraries required for the application
<!-- embedme ./src/main/scala/it/unibo/scafi/examples/HelloScafi.scala#L8-L8 -->
```scala
with StandardSensors with ScafiAlchemistSupport with BlockG with Gradients with FieldUtils {
```

4. define the behaviour inside the `main` method.

A ScaFi program typically deals with environment information through **sensors**. 
`sense[Type](name)` is the built-in operator used to query the sensors attached to each node. Each *molecule* expressed in the yaml (i.e., the Alchemist variable concept) can be queried from the ScaFi program. For instance, in helloScafi, we write:
<!-- embedme ./src/main/yaml/helloScafi.yml#L63-L64 -->
```yaml
- molecule: test
  concentration: *source # anchor to "source" value, check line 17
```
Therefore, in the program, we can get the `test` value as:
<!-- embedme ./src/main/scala/it/unibo/scafi/examples/HelloScafi.scala#L11-L12 -->
```scala
// Access to node state through "molecule"
val source = sense[Int]("test") // Alchemist API => node.get("test")
```
There are several built-sensors (in `checkSensors` there are examples of local sensors and neighbouring sensors).
For more details, please check the [Scaladoc](http://scafi-docs.surge.sh/it/unibo/scafi/index.html).

The main logic of the program is expressed in the following line:
<!-- embedme ./src/main/scala/it/unibo/scafi/examples/HelloScafi.scala#L13-L14 -->
```scala
// An aggregate operation
val g = classicGradient(mid() == source)
```
Where `classicGradient` is a function defined in `BlockG` that implements the self-healing gradient described above. The first argument is a `Boolean` field that defines which part of the system could be considered a *source* zone. In this case, nodes are marked as source when the field of ids (i.e., `mid()`)  is equal to the value passed through the variable `test`. This can be expressed as `mid() == source`.

The value produced by Scafi definitions could be used to express *actuation*. In the Scafi incarnation, you can update the Alchemist variables through `node.put`
<!-- embedme ./src/main/scala/it/unibo/scafi/examples/HelloScafi.scala#L15-L16 -->
```scala
// Write access to node state (i.e., Actuation => it changes the node state)
node.put("g", g)
```
In the Alchemist default GUI you can inspect the node variables (i.e., molecules) by double-clicking a nodes
![Alchemist Molecule panel](https://user-images.githubusercontent.com/23448811/176140035-dcc4078a-b71d-4eef-a8f2-5cdbc81b0c60.gif)


Finally, the last instruction of the main is the returned value of the Scafi program (in scala `return` is optional)
<!-- embedme ./src/main/scala/it/unibo/scafi/examples/HelloScafi.scala#L17-L18 -->
```scala
// Return value of the program
g
```
#### Minimal changes


1. As described above, the program is *self-healing*, so try to move node and see how the system eventually reaches a stable condition:
    - click <kbd>S</kbd> to enter into selection mode
    - start a selection by clicking the mouse left button and dragging it into the environment
    - once your selection is over, click <kbd>O</kbd> to enter into move mode
    - click over the selection and drag the element into another position
![Alchemist Move elements](https://user-images.githubusercontent.com/23448811/175920712-e193cf6d-6797-46e9-bc02-c0218f3bf583.gif)
2. Try to modify the source node (via yml configuration) and check the program output differences
3. Try to change the source node (i.e. with the ID == 10) after 10 seconds (check `BlockT` library, or you can implement the time progression with `rep(0 seconds)(time => time + deltaTime)`)
#### Using the generated data with the embedded plotting script
You can produce plots from the data generated by Alchemist simulations. 
Indeed, each Alchemist simulation produces aggregated data as expressed in the `export` configuration [section](https://github.com/scafi/learning-scafi-alchemist/blob/master/src/main/yaml/helloScafi.yml#L68-L83). For more details about data exporting, please refer to the official Alchemist [guide](http://alchemistsimulator.github.io/howtos/simulation/export/).   
Particularly, this command:
```bash
./gradlew runHelloScafi -Pbatch=true -Pvariables=random
```
will run several simulations in batch, one for each possible value of the random variables (six in this case, as expressed in the [helloScafi.yml](https://github.com/scafi/learning-scafi-alchemist/blob/master/src/main/yaml/helloScafi.yml#L4-L8)). Each simulation, will produce a csv file in `$exportPath/$fileNameRoot-randomValue.$fileExtension` (in this case, build/exports/helloScafi/experiment-x.txt, the values starting with $ are gathered from the simulation configuration file). 

Typically, we use these data to produce charts that express the dynamics of the collective system.
This repository contains a highly configurable script (please look at the configuration defined in [plots](/plots)).

To run the script for this experiment, you should run:
```bash
$ python plotter.py plots/helloScafi.yml ./build/exports/helloScafi ".*" "result" plots/ 
```

Where:
- the first argument is the plot configuration (expressed using a yaml file)
- the second argument is where the files are located 
- the third argument is a regex used to select the simulations file
- the fourth argument defines the initial names of the plot
- the last argument devises the folder in which the plots will be stored


### 2. A richer pattern: Self-organizing Coordination Regions
#### Launch command
```bash
./gradlew runSelforgCoordRegions
```

#### What happened
This example shows an interesting pattern developed with ScaFi, the so-called Self-Organising Coordination Regions (SCR).
(more details in [Self-organising Coordination Regions: A Pattern for
Edge Computing](https://hal.inria.fr/hal-02365498/document))

The idea of SCR is to organize a distributed activity
 into multiple spatial **regions** (inducing a partition of the system),
 each one controlled by a **leader** device,
 which collects data from the area members
 and spreads decisions to enact some area-wide policy.

![SCR result](https://user-images.githubusercontent.com/23448811/175658741-08537743-a325-4137-be14-a4dd6532237b.gif)

#### What is inside 
|Configuration File|ScaFi Program File|
|-|-|
| [selforgCoordRegions.yml](https://github.com/scafi/learning-scafi-alchemist/blob/master/src/main/yaml/selforgCoordRegions.yml) | [SelforganisingCoordinationRegions.scala](https://github.com/scafi/learning-scafi-alchemist/blob/master/src/main/scala/it/unibo/scafi/examples/SelforganisingCoordinationRegions.scala) |


#### Minimal changes

### 3. Overlapping computations in space and time with aggregate processes

```bash
./gradlew runAggregateProcesses
```
#### What happened
![Processes API](https://user-images.githubusercontent.com/23448811/175660568-9906a920-d701-48ce-a0de-a0d6fa146425.gif)

#### What is inside
|Configuration File|ScaFi Program File|
|-|-|
| [aggregateProcesses.yml](https://github.com/scafi/learning-scafi-alchemist/blob/master/src/main/yaml/aggregateProcesses.yml) | [SelforganisingCoordinationRegions.scala](https://github.com/scafi/learning-scafi-alchemist/blob/master/src/main/scala/it/unibo/scafi/examples/AggregateProcesses.scala) |

#### Minimal changes

## External resources to improve your understanding

- The Alchemist metamodel: https://alchemistsimulator.github.io/explanation/
- The Alchemist Simulator reference https://alchemistsimulator.github.io/reference/yaml/
- ScaFi documentation: https://scafi.github.io/docs/
- Main scientific papers about ScaFI (and that use ScaFi): https://scafi.github.io/papers/

<!--

### Conceptual model

- **Molecule**: name of a data item
- **Concentration**: value associated to a particular molecule
- **Node**: a container of molecules/reactions, living inside an environment
- **Environment**: the Alchemist abstration for the space.
    - It is a container for nodes, and it is able to tell:
  a) Where the nodes are in the space - i.e. their position
  b) How distant are two nodes
  c) Optionally, it may provide support for moving nodes
- **Linking rule**: a function of the current status of the environment that associates to each node a
  neighborhood
    - **Neighborhood**: an entity composed by a node (centre) + a set of nodes (neighbors)
- **Reaction**: any event that can change (through an **action**) the state of the environment
    - Consists of 0+ **conditions**, 1+ **actions**, and a **time distribution**
    - Conditions, time distribution, static rate, and rate equation affect the **frequency** of the reaction
- Alchemist implements an optimised version (NRM) of Gillespie's Stochastic Simulation Algorithm (SSA)

So

- The **system state** depends on the configuration of molecules floating in it
- The **system evolution** depends on the kinds of chemical reactions applicable over time

Another key concept is the **dependency graph**

- Actions are outputs
- Conditions are inputs

### Running simulations




Finally, you can produce plots from the data generated by Alchemist simulation.
This repository contains a highly configurable script (please look to the configuration defined in [plots](/plots)).

To run the script, you should run:
```bash
$ python plotter.py plots/helloworld.yml ./build/exports/helloScafi ".*" "result" plots/ 
```

Where:
- the first argument is the plot configuration (expressed using a yaml file)
- the second argument is where the files are located 
- the third argument is a regex used to select the simulations file
- the fourth argument defines the initial names of the plot
- the last argument devises the folder in which the plots will be stored
### Scafi

The simulation descriptor must indicate `incarnation: scafi`
 and configure a `RunScafiProgram` action
 pointing to the class of some class implementing `AggregateProgram`.

```yaml
incarnation: scafi

_reactions:
  - program: &program
    - time-distribution:
        type: ExponentialTime
        parameters: [1]
      type: Event
      actions:
        - type: RunScafiProgram
          parameters: [it.unibo.casestudy.HelloWorld, 5.0] # second argument is retention time
    - program: send
```
An example ScaFi program is the following

```scala
package it.unibo.casestudy

import it.unibo.alchemist.model.implementations.molecules.SimpleMolecule
import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

class HelloWorld extends AggregateProgram with StandardSensors with ScafiAlchemistSupport
  with Gradients {
  override def main(): Any = {
    // Access to node state through "molecule"
    val source = if(node.has("test")) node.get[Int]("test") else 1
    // An aggregate operation
    val g = classicGradient(mid == program)
    // Write access to node state
    node.put("g", g)
    // Return value of the program
    g
  }
}
```

## On this version and past versions

I will try to keep this repository aligned with the latest versions of scafi and Alchemist.

However, I have also set up different branches to "freeze" particular project configurations that have proven to work.
These can be checked out through the usual git branch mechanism, e.g.:

`git checkout scafi-0.3.2-alchemist-9.2.1`

Available branches/configurations:

- `scafi-0.3.2-alchemist-9.2.1`
- `scafi-0.3.3-alchemist-11.3.0`

-->
