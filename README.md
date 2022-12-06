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
Particularly, the program consists of the description of the self-healing gradient: an algorithm that computes a gradient (i.e.,  a field mapping each device in the system with its minimum distance from the closest *source* device) field
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
 Particularly, when you launch the command of SCR you will see something like this:

![SCR result](https://user-images.githubusercontent.com/23448811/175658741-08537743-a325-4137-be14-a4dd6532237b.gif)

Where the colour denotes the potential field (i.e., the gradient) that starts from the selected leader.
In this GIF, the leaders are the ones marked with blue colour.
#### What is inside 
|Configuration File|ScaFi Program File|
|-|-|
| [selforgCoordRegions.yml](https://github.com/scafi/learning-scafi-alchemist/blob/master/src/main/yaml/selforgCoordRegions.yml) | [SelforganisingCoordinationRegions.scala](https://github.com/scafi/learning-scafi-alchemist/blob/master/src/main/scala/it/unibo/scafi/examples/SelforganisingCoordinationRegions.scala) |


The SCR pattern consists of four main phases:
1. leader election: using block `S` the system will produce a distributed leader election that tries to divide the system equally with a certain range (in S term, it is called `grain`):
<!-- embedme ./src/main/scala/it/unibo/scafi/examples/SelforganisingCoordinationRegions.scala#L14-L15 -->
```scala
// Sparse choice (leader election) of the cluster heads
val leader = S(sense(Params.GRAIN), metric = nbrRange)
```
2. potential field definition: after the leader election process, there is another phase in which will be computed potential field from the leader. In this way, the slave node could send information to leader's
<!-- embedme ./src/main/scala/it/unibo/scafi/examples/SelforganisingCoordinationRegions.scala#L16-L17 -->
```scala
// G block to run a gradient from the leaders
val g = distanceTo(leader, metric = nbrRange)
```
3. collection phase: the slave node could collect local information (e.g., temperature) and send it to the leader. During the path, it will be an aggregation process that combines local information with area information (i.e., all the nodes that are inside the potential field of a leader) 
<!-- embedme ./src/main/scala/it/unibo/scafi/examples/SelforganisingCoordinationRegions.scala#L18-L19 -->
```scala
// C block to collect information towards the leaders
val c = C[Double,Set[ID]](g, _++_, Set(mid()), Set.empty)
```
4. leader choice and share: with the information collected inside an area, the leader could perform an area-wide decision and then send it to the whole area (using `G`)
<!-- embedme ./src/main/scala/it/unibo/scafi/examples/SelforganisingCoordinationRegions.scala#L20-L22 -->
```scala
// G block to propagate decisions or aggregated info from leaders to members
val info = G[Set[ID]](leader, c, identity, metric = nbrRange)
val head = G[ID](leader, mid(), identity, metric = nbrRange)
```   

#### Minimal changes
1. Try to change the grain (check in the configuration file). It would lead to changes in area formation
2. Try to count the number of nodes inside an area and share this information with that area---suggestion: change phase 3. of the SCR
3. As in the previous example, the areas are self-healing. Therefore try to move leaders and see what happens in the leader formation. Try to remove nodes too (see the next clip)
![ezgif com-gif-maker](https://user-images.githubusercontent.com/23448811/179356424-bd31b95a-e38a-491a-80d3-cc448059e484.gif)

### 3. Overlapping computations in space and time with aggregate processes

```bash
./gradlew runAggregateProcesses
```
#### What happened
This example shows an application of Aggregate Processes, 
 which is s a way to specify a dynamic number of collective 
 computations running on dynamic ensembles of devices (more details in [Engineering collective intelligence at the edge with aggregate processes](https://www.sciencedirect.com/science/article/abs/pii/S0952197620303389)). 
![Processes API](https://user-images.githubusercontent.com/23448811/175660568-9906a920-d701-48ce-a0de-a0d6fa146425.gif)

The processes are the bigger circle around the nodes. The colour identifies the process ID. As you can see,
during the simulation the process starts, shrink and then could disappear.
#### What is inside
|Configuration File|ScaFi Program File|
|-|-|
| [aggregateProcesses.yml](https://github.com/scafi/learning-scafi-alchemist/blob/master/src/main/yaml/aggregateProcesses.yml) | [SelforganisingCoordinationRegions.scala](https://github.com/scafi/learning-scafi-alchemist/blob/master/src/main/scala/it/unibo/scafi/examples/AggregateProcesses.scala) |

#### Minimal changes
To start the process, you can use the `spawn` operators (and their variation, `sspawn`, `cspawn`, etc.):

<!-- embedme ./src/main/scala/it/unibo/scafi/examples/AggregateProcesses.scala#L24-L24 -->
```scala
val maps = sspawn[Pid,Unit,Double](process, pids, {})
```

Particularly, `sspawn` accepts:
- the process logic, that is a function `ID => Input => Pout[Out]`
    - `ID` in this case is a `case class` that contains the `id` of a node that will start the process, the time in which it will effectively start and finally the time in which it will end.
    <!-- embedme ./src/main/scala/it/unibo/scafi/examples/AggregateProcesses.scala#L41-L42 -->
    ```scala
    case class Pid(src: ID = mid(), time: Long = alchemistTimestamp.toDouble.toLong)
                  (val terminateAt: Long = Long.MaxValue)
    ```
    - The input of the process (in this case is empty)
    - Finally, the `Pout[Double]` is the process output. `Pout` is a data structure that contains the output of the process and the status of the process (that could be `Output`, `Terminated` and `External`---more details in the paper).
- The key set of the process that will be spawn (`pids`)
    - In this case, the new `pids` associated to new processes are selected from Alchemist molecule. 
     <!-- embedme ./src/main/scala/it/unibo/scafi/examples/AggregateProcesses.scala#L11-L11 -->
     ```scala
     def processesSpec: Map[Int,(Int,Int)] = sense(MOLECULE_PROCS)
     ```
    - From this information il will be created the `pids` for the processes:
    <!-- embedme ./src/main/scala/it/unibo/scafi/examples/AggregateProcesses.scala#L18-L22 -->
    ```scala
    // Determine the processes to be generated (these are provided in a molecule "procs")
    val procs: Set[ProcessSpec] = processesSpec.map(tp => ProcessSpec.fromTuple(tp._1, tp._2)).toSet
    val t = alchemistTimestamp.toDouble.toLong
    val pids: Set[Pid] = procs.filter(tgen => tgen.device == mid() && t > tgen.startTime && (t - 5) < tgen.startTime)
      .map(tgen => Pid(time = tgen.startTime)(terminateAt = tgen.endTime))
    ```

Particularly, in this case, the process logic is quite simple:
- it produces a potential field from the processes creator
- it terminates if the `terminateAt` is reached
- the nodes that belong to nodes are the ones that are inside the `bubble`, that is the nodes inside an area of 200 units
<!-- embedme ./src/main/scala/it/unibo/scafi/examples/AggregateProcesses.scala#L44-L50 -->
```scala
def process(pid: Pid)(src: Unit = ()): POut[Double] = {
  val g = classicGradient(pid.src==mid())
  val s = if(pid.src==mid() && pid.terminateAt.toDouble <= alchemistTimestamp.toDouble){
    Terminated
  } else if(g < 200) Output else External
  POut(g, s)
}
```
#### Minimal changes
- Try to add the extension of the `bubble` as a parameter (as the start and end time)
- Even in this case, the computation is self-healing. Therefore, try to move the process center to see how the system reacts
- Try to add other processess (see the yaml configuration)
## External resources to improve your understanding

- The Alchemist metamodel: https://alchemistsimulator.github.io/explanation/
- The Alchemist Simulator reference https://alchemistsimulator.github.io/reference/yaml/
- ScaFi documentation: https://scafi.github.io/docs/
- Main scientific papers about ScaFI (and that use ScaFi): https://scafi.github.io/papers/
