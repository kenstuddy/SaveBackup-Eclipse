package ken;

import org.eclipse.ui.IStartup;

/**
 * The Startup class implements the IStartup interface and runs at the startup of the plugin, 
 * which just so happens to be when the Eclipse IDE is opened. This class overrides the earlyStartup method.
 * @author Ken Studdy
 * @date August 5, 2018
 * @version 1.0
 */
public class Startup implements IStartup {
    //Override the earlyStartup method in the IStartup interface, for now we just print to the console.
    @Override
    public void earlyStartup() {
        System.out.println("starting SaveBackup...;");
    }
}
