package at.tugraz.ist.avrora.particleplatform;

import edu.ucla.cs.compilers.avrora.avrora.Defaults;
import edu.ucla.cs.compilers.avrora.avrora.monitors.ParticlePlatformMonitor;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatform;
import edu.ucla.cs.compilers.avrora.avrora.sim.types.ParticleSimulation;

/**
 * @author Raoul Rubien 20.11.2015
 *         <p>
 *         Wraps {@link edu.ucla.cs.compilers.avrora.avrora.Main} to ensure that extra defaults are set up
 *         correctly until original main() starts.
 */
public class Main {
    public static void main(String[] args) {
        Defaults.addPlatform("particle", ParticlePlatform.Factory.class);
        Defaults.addSimulation("particle-network", ParticleSimulation.class);
        Defaults.addMonitor("particle", ParticlePlatformMonitor.class);
        edu.ucla.cs.compilers.avrora.avrora.Main.main(args);
    }
}
