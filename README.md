# Learning Scafi-Alchemist simulations

## How-to: basics

### Project layouts

- `src/main/scala` contains code for simulation logic (aggregate programs in scafi)
- `src/main/yaml` contains simulation descriptors

### Simulation descriptor (example)

**src/main/yaml/my_simulation.yml**

```yaml
variables:
  random: &random
    min: 0
    max: 29
    step: 1
    default: 2
  commRadius: &commRadius
    min: 35
    max: 55
    step: 15.0
    default: 50.0
              
export:
  - time

seeds:
  scenario: *random
  simulation: *random
  
incarnation: scafi

pools:
  - pool: &program
    - time-distribution:
        type: ExponentialTime
        parameters: [1]
      type: Event
      actions:
        - type: RunScafiProgram
          parameters: [it.unibo.simulations.MyAggregateProgram, 20]
  - pool: &contents
    - molecule: grain
      concentration: 10

environment:
  type: OSMEnvironment
  parameters: [cesena.pbf, false, false]

positions:
  type: LatLongPosition

network-model:
  type: ConnectWithinDistance
  parameters: [*commRadius]

displacements:
  - in:
      type: Rectangle
      parameters: [200, 44.13621, 12.24045, 0.00345, 0.00706]
    programs:
      - *program
    contents: *contents
```

where the crucial parts related to scafi are merely:

```yaml
incarnation: scafi

# ...........
      actions:
        - type: RunScafiProgram
          parameters: [it.unibo.simulations.MyAggregateProgram, 20]
```

### Aggregate program: example

**src/main/scala/it/unibo/simulations/MyAggregateProgram.scala**

```scala
package it.unibo.simulations

import it.unibo.alchemist.model.scafi.ScafiIncarnationForAlchemist._

class MyAggregateProgram extends AggregateProgram
  with StandardSensors with ScafiAlchemistSupport with BlockG with BlockC with BlockS with FieldUtils {

  override type MainResult = Any

  override def main = {
    1+1
  }

}
```

### Build the project

```commandline
$ ./gradlew
```

It will create a shadow JAR: `build/libs/sim.jar`.

### Launching simulations

Simulations are started through commands like:

```commandline
$ ./gradlew --no-daemon
$ java -Xmx5024m -cp "build/libs/sim.jar" \
  it.unibo.alchemist.Alchemist \
  -b -var <VAR1> <VAR2> ... \
  -y <simulationDescriptor> -e <baseFilepathForDataFiles> \
  -t <simulationDuration> -p <numOfParallelRuns> -v &> log.txt &
```

E.g.,
```commandline
$ java -Xmx5024m -cp "build/libs/sim.jar" it.unibo.alchemist.Alchemist \
  -b -var random -y src/main/yaml/my_simulation.yml -e data/exp1 \
  -t 10 -p 3 -v &> log.txt &
```