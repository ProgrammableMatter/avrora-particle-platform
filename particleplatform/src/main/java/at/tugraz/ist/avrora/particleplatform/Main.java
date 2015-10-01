package at.tugraz.ist.avrora.particleplatform;

import edu.ucla.cs.compilers.arora.avrora.sim.types.ParticleSimulation;
import edu.ucla.cs.compilers.avrora.avrora.Defaults;
import edu.ucla.cs.compilers.avrora.avrora.sim.platform.ParticlePlatform;

/**
 * 
 * @author Raoul Rubien
 *
 *         Wraps {@link edu.ucla.cs.compilers.avrora.avrora.Main} to ensure
 *         that extra defaults are set up correctly until original main() starts.
 *
 */
public class Main
{
    public static void main(String[] args)
    {
        Defaults.addPlatform("particle", ParticlePlatform.Factory.class);
        Defaults.addSimulation("particle-network", ParticleSimulation.class);
        // TODO register new simulator, new nodes etc....
        edu.ucla.cs.compilers.avrora.avrora.Main.main(args);
    }
}
