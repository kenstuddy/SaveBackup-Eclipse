package ken;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle. This class extends the abstract base class AbstractUIPlugin.
 * This class runs on a single thread using a single thread executor and overrides the resourceChanged method of the 
 * IResourceChangeListener interface.
 * @author Ken Studdy
 * @date August 5, 2018
 * @version 1.0
 */
public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID = "SaveBackup"; //$NON-NLS-1$
    
    //This is the shared (static) instance of the plugin.
    private static Activator plugin;
    
    //This is the minimum interval for save duration.
    private int saveInterval = 1000;
    
    //This will contain a single thread executor.
    private static ExecutorService executor;
    
    //The time since the current file was last saved.
    private static long lastChanged = 0;
    
    private String src;
    private String dest;
    private String logTime;
    
    /* In this static initializer, create a new single thread executor, we will use this to submit an instance of the Runnable interface. 
     * Since we only need one thread we do not need a thread pool or a thread factory.
     */
    static {
        executor = Executors.newSingleThreadExecutor();
    }
    
    /**
     * The constructor
     */
    public Activator() {
        
        try {
            //Here we must start a new instance of the IResourceChangeListener interface.
            IResourceChangeListener rcl = new IResourceChangeListener() {
                //Override the resourceChanged method in the IResourceChangeListener interface, this method handles resource (in this case, file) changes.
                @Override
                public void resourceChanged(IResourceChangeEvent event) {
                    //We only want to save a maximum of once per second.
                    if (System.currentTimeMillis() - lastChanged < saveInterval) {
                        lastChanged = System.currentTimeMillis();
                        return;
                    }
                    lastChanged = System.currentTimeMillis();
                    //Start a new instance of the Runnable interface, we will submit this to our ExecutorService variable executor.
                    Runnable runnable = new Runnable() {
                        public void run() {
                            //Get the resource delta from the IResourceChangeEvent parameter event.
                            IResourceDelta rootDelta = event.getDelta();
                            //Start a new instance of the IResourceDeltaVisitor interface, we will use this for resource delta changes (file changes, specifically the current file in the editor being saved).
                            IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
                                public boolean visit(IResourceDelta delta) {
                                    if (delta.getResource().getLocation().toFile().isDirectory()) {
                                        return true;
                                    }
                                    //Get the resource from our IResourceDelta parameter delta.
                                    IResource resource = delta.getResource();
                                    //We only want to save the file if the file has changed, not if it has been added or removed.
                                    if (resource.getType() == IResource.FILE && delta.getKind() == IResourceDelta.CHANGED) {
                                        //Get the source path (including the filename) of the resource (file) and convert it to an operating system string. This contains the absolute path, not just a relative path to the workspace.                             
                                        src = resource.getLocation().toOSString();
                                        //This is the destination path (including the filename) of the output file.
                                        dest = System.getProperty("user.home") + File.separator + ".SaveBackup";
                                        
                                        //If we are on Windows, we need to append another file separator to the end of our destination folder.
                                        if (System.getProperty("os.name").startsWith("Windows")) {
                                            dest += File.separator;
                                        }
                                        
                                        
                                        //On Windows, we cannot have : in the folder name, and we might as well remove it for other operating systems too for cross-platform compatibility.
                                        dest +=  resource.getRawLocation().toString().replace(":", "");
                                        //Here is the date and time that the file is saved at.
                                        logTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(Calendar.getInstance().getTime());
                                        
                                        //Since the destination path (the output file) originally contains the current file name without the date and time, we must prepend the date and time to the filename.
                                        dest = dest.replace("." + resource.getFileExtension(), "-" + logTime + "." + resource.getFileExtension());
                                        
                                        try {
                                            //We don't actually do anything with this file other than use it for creating parent folders if they don't already exist.
                                            File newFile = new File(dest);
                                            
                                            //Create all the folders for the output directory if they don't exist, this also works with Linux by incrementally creating the folders one at a time.
                                            if (!newFile.getParentFile().exists()) {
                                                newFile.getParentFile().mkdirs();
                                            }
                                            //Here we call our static copy wrapper method and pass two stings to it. 
                                            copy(src, dest);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    return true;
                                }
                            };
                            try {
                                //Invoke the visitor for the resource delta rootDelta.
                                rootDelta.accept(visitor);
                            } catch (CoreException e) {
                                System.out.println(e.getStackTrace());
                            }
                        }
                    };
                    //Here we submit our instance of the Runnable interface to our ExecutorService variable executor, which uses a single thread executor, to execute this thread.
                    executor.submit(runnable);
                }
            };
            //Here we add a resource change listener, which is the instance of IResourceChangeListener, to our current workspace.
            ResourcesPlugin.getWorkspace().addResourceChangeListener(rcl);
        } catch (Exception ex) {
            System.out.println(ex.getStackTrace());
        }

    }
    /**
     * This is a wrapper for the copy method of the Java Files class. The Files class has been available since Java SE 7,
     * so it makes sense to make use of it instead of older ways of copying files.
     * @param sourcePath
     * @param destinationPath
     * @throws IOException
     */
    public static void copy(String sourcePath, String destinationPath) throws IOException {
        Files.copy(Paths.get(sourcePath), new FileOutputStream(destinationPath));
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }
    
}
