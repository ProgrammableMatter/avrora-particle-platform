Simulation
==========
The particle platform simulation is built on top of the **Avrora framework**.
It simulates nodes of a network of fold-able chains, that can be used for
shape shifting displays or to build programmable matter in general as
presented by **Matteo Lasagni** in **Force-Guiding Particle Chains for Shape-Shifting Displays** ([http://arxiv.org/abs/1402.2507](http://arxiv.org/abs/1402.2507)).
The simulation constructs and connects multiple nodes to a network that allows bidirectional
communication in between nodes. Each node in the simulation is abstracted by

+ a particle platform,
+ the micro controller (Atmega16), and 
+ the micro controller firmware.

Among others the simulation allows to 

+ inspect registers at runtime, 
+ monitor actions,
+ collect lots of statistics, and
+ log network/node interactions for later usage such as replay for 
visualization or other simulation tools.



Build JAR with dependencies
===========================

    mvn assembly:assembly

Start simulation
================

    java -jar ./target/particleplatform-0.0.1-SNAPSHOT-jar-with-dependencies.jar <arguments>
    
**Recommended arguments:**

    -banner=false 
    -status-timing=true 
    -verbose=all 
    -seconds-precision=11 
    -action=simulate 
    -simulation=particle-network 
    -rowcount=2 -columncount=2 
    -seconds=1000E-6 
    -report-seconds=true 
    -platform=particle 
    -arch=avr 
    -clockspeed=8000000 
    -monitors=calls,retaddr,particle,interrupts,memory 
    -dump-writes=true 
    -show-interrupts=true 
    -invocations-only=false 
    -low-addresses=true 
    -particle-log-file=true 
    -particle-facets=state,break,wires 
    -input=elf 
    -throughput=true
    /path_to/node_firmware.elf /optional_path_to/communication_unit_firmware.elf

Network Geometry
================
The network geomertry is defined by *-rowcount=***<rows\>** 
*-columncount=***<columns\>**. With respect to the recommended arguments a simulation
would simulate a **(2 x 2)** network.

**Network** by a (5 x 3) example
---------------------------------

(0,0)<br>
&nbsp;&nbsp;&nbsp;|<br>
(1,1) -- (1,2) -- (1,3)<br>
&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|<br>
(2,1) &nbsp;&nbsp; (2,2) &nbsp;&nbsp; (2,3)<br>
&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|<br>
(3,1) &nbsp;&nbsp; (3,2) &nbsp;&nbsp; (3,3)<br>
&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|<br>
(4,1) &nbsp;&nbsp; (4,2) &nbsp;&nbsp; (4,3)<br>
&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;|<br>
(5,1) &nbsp;&nbsp; (5,2) &nbsp;&nbsp; (5,3)<br>

The node at address **(0,0)** is the communication unit and optional. It is 
instanciated whenever the arguments provide a second firmware. With the recommended
arguments the simulation would run **/optional_path_to/communication_unit_firmware.elf** program
on the communication unit. Nodes at any address **row > 0 && col > 0** will
run the **/path_to/node_firmware.elf** program according to the recommended arguments.

Logging
=======
**Development logging** an be configured in *log4j.properties". The logfile 
in **/var/tmp/particle-platform-simulation.log** is meant for development purpose.

**Particle logging** is activated by **-particle-log-file=true** and can be found in 
**/tmp/particle-states.log**:

       0  0:00:00.00000037585  WIRE[rx-north] <- high
       0  0:00:00.00000037585  WIRE[rx-south] <- high
       1  0:00:00.00000037585  WIRE[rx-north] <- high
       2  0:00:00.00000037585  WIRE[rx-north] <- high
       2  0:00:00.00000037585  WIRE[rx-south] <- high
       3  0:00:00.00000037585  WIRE[rx-north] <- high
       4  0:00:00.00000037585  WIRE[rx-south] <- high
       0  0:00:00.00000050057  SRAM[SREG.(I | T | H | S | V | N | Z | C)] <- (0b00000000)
       1  0:00:00.00000050057  SRAM[SREG.(I | T | H | S | V | N | Z | C)] <- (0b00000000)
       2  0:00:00.00000050057  SRAM[SREG.(I | T | H | S | V | N | Z | C)] <- (0b00000000)

The register monitored for the output in the example above can be configured in **ParticleStateDescriptoin.json**.

ParticleStateDescriptoin.json
------------------------------

The json object is a description of registers to be monitored. 
It defines 

+ enums, 
+ structures and 
+ type sizes on the microcontroller.

**Enums:** "enums" object describes one or multiple types (i.e. *MyEnum*) that can
be used as type later on. Each list entry corresponds to one enum value: 
1st entry == 0, 2nd == 1 ...

**Structs consists of:**
* properties (name or text to be displayed after the value),
* propertiyTypes (how the value is to be interpreted/printed),
* propertyAddresses (start address of property)

Properties are generally 1 byte width except of enums. Structures cannot be
nested. Nested structures must be resolved byte by byte.

**sizeofTypes:** not implemented/used yet

### Short example (see full [example](https://github.com/ProgrammableMatter/avrora-particle-platform/blob/master/particleplatform/src/main/resources/ParticleStateDescription.json))
        {
          // define enums
          "enums": {
            // some enum definistions
            "<MyEnum>": [
              // name of value 0
              "STATE_TYPE_START",
              // name of value 1
              "STATE_TYPE_ACTIVE"
            ]
          },

          // define structures
          "structs": {
            "<an-arbitrary-struct-name>": {
              "properties": [
                // the text following the value
                "(TP | STH_SW | A5 | STH_TX | LED | A2 | NRTH_TX | NRTH_SW)"
              ],
              "propertyTypes": [
                // how the value is printed: 
                // int, char, unsigned char, hex, bit or enums, i.e. MyEnum
                "bit"
              ],
              "propertyAddresses": [
                // address as found in hexdump minus the offset: 80058-8000=58
                58
              ]
            },
        }

          // define data type size in byte on microcontroller
          "sizeofTypes": {
            "MyEnum": 2,
            "MyEnum2": 2,
            "char": 1
          }
        }


Obtaining property address for ParticleStateDescriptoin.json 
------------------------------------------------------------

The property address corresponds to the symbol address obtained with 
**readelf -a /path_to/node_firmware.elf** in the symtab minus the offset.
As an example address of the symbol **ParticleAttributes**

    38: 00800061    17 OBJECT  GLOBAL DEFAULT    2 ParticleAttributes

is: 

    0x00800061 - 0x00800000 = 97

Future Work
===========

* For communication from/to particle nodes the Avrora's [serial monitor](http://compilers.cs.ucla.edu/avrora/help/serial.html)
(*edu.ucla.cs.compilers.avrora.avrora.monitors.SerialMonitor*) may be utilized.
* [Energy profiling](http://compilers.cs.ucla.edu/avrora/help/energy-profile.html) is to be implemented for the platform's off chip devices.
