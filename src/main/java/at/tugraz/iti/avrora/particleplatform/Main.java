/*
 * Copyright (c) 2015
 * Raoul Rubien
 */

package at.tugraz.iti.avrora.particleplatform;

import edu.ucla.cs.compilers.avrora.avrora.Defaults;
import edu.ucla.cs.compilers.avrora.avrora.monitors.ParticleCallMonitor;
import edu.ucla.cs.compilers.avrora.avrora.monitors.ParticleInterruptMonitor;
import edu.ucla.cs.compilers.avrora.avrora.monitors.ParticlePlatformMonitor;
import edu.ucla.cs.compilers.avrora.avrora.monitors.particlemonitor.ParticleLogSink;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatform;
import edu.ucla.cs.compilers.avrora.avrora.sim.types.ParticleSimulation;

/**
 * Wraps {@link edu.ucla.cs.compilers.avrora.avrora.Main} to ensure that extra defaults are set up correctly
 * until original main() starts. <br> <p>suggested cli arguments:<br> -banner=false<br>
 * -status-timing=true<br> -verbose=all<br> -seconds-precision=11<br> -action=simulate<br>
 * -simulation=particle-network<br> -rowcount=2<br> -columncount=2<br> -seconds=90000E-6<br>
 * -report-seconds=true<br> -platform=particle<br> -arch=avr<br> -clockspeed=8000000<br>
 * -monitors=calls,retaddr,particle,interrupts,memory<br> -dump-writes=true<br> -show-interrupts=true<br>
 * -invocations-only=false<br> -low-addresses=true<br> -particle-log-file=true (see  {@link
 * ParticleLogSink#ParticleLogSink()}) <br> -particle-facets=state,break,wires<br> -input=elf<br>
 * -throughput=true<br> /path_to/node_firmware.elf /optional_path_to/communication_unit_firmware.elf<br>
 */
public class Main {
    public static void main(String[] args) {
        Defaults.addPlatform("particle", ParticlePlatform.Factory.class);
        Defaults.addSimulation("particle-network", ParticleSimulation.class);
        Defaults.addMonitor("particle-calls", ParticleCallMonitor.class);
        Defaults.addMonitor("particle-interrupts", ParticleInterruptMonitor.class);
        Defaults.addMonitor("particle-states", ParticlePlatformMonitor.class);
        edu.ucla.cs.compilers.avrora.avrora.Main.main(args);
    }
}
