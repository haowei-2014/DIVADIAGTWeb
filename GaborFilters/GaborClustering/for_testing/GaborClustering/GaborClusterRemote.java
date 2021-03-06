/*
 * MATLAB Compiler: 5.0 (R2013b)
 * Date: Wed Oct 22 06:02:34 2014
 * Arguments: "-B" "macro_default" "-W" "java:GaborClustering,GaborCluster" "-T" 
 * "link:lib" "-d" 
 * "/home/hao/work/DIVADIAGTWeb/A_Course_Experiment_DivadiaWeb/GaborClustering/for_testing" 
 * "-v" 
 * "/home/hao/work/DIVADIAGTWeb/A_Course_Experiment_DivadiaWeb/cvGaborTextureSegmentRun.m" 
 * "class{GaborCluster:/home/hao/work/DIVADIAGTWeb/A_Course_Experiment_DivadiaWeb/cvGaborTextureSegmentRun.m}" 
 */

package GaborClustering;

import com.mathworks.toolbox.javabuilder.pooling.Poolable;
import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * The <code>GaborClusterRemote</code> class provides a Java RMI-compliant interface to 
 * the M-functions from the files:
 * <pre>
 *  /home/hao/work/DIVADIAGTWeb/A_Course_Experiment_DivadiaWeb/cvGaborTextureSegmentRun.m
 * </pre>
 * The {@link #dispose} method <b>must</b> be called on a <code>GaborClusterRemote</code> 
 * instance when it is no longer needed to ensure that native resources allocated by this 
 * class are properly freed, and the server-side proxy is unexported.  (Failure to call 
 * dispose may result in server-side threads not being properly shut down, which often 
 * appears as a hang.)  
 *
 * This interface is designed to be used together with 
 * <code>com.mathworks.toolbox.javabuilder.remoting.RemoteProxy</code> to automatically 
 * generate RMI server proxy objects for instances of GaborClustering.GaborCluster.
 */
public interface GaborClusterRemote extends Poolable
{
    /**
     * Provides the standard interface for calling the 
     * <code>cvGaborTextureSegmentRun</code> M-function with 2 input arguments.  
     *
     * Input arguments to standard interface methods may be passed as sub-classes of 
     * <code>com.mathworks.toolbox.javabuilder.MWArray</code>, or as arrays of any 
     * supported Java type (i.e. scalars and multidimensional arrays of any numeric, 
     * boolean, or character type, or String). Arguments passed as Java types are 
     * converted to MATLAB arrays according to default conversion rules.
     *
     * All inputs to this method must implement either Serializable (pass-by-value) or 
     * Remote (pass-by-reference) as per the RMI specification.
     *
     * M-documentation as provided by the author of the M function:
     * <pre>
     * % added by Hao
     * </pre>
     *
     * @param rhs The inputs to the M function.
     *
     * @return Array of length nargout containing the function outputs. Outputs are 
     * returned as sub-classes of <code>com.mathworks.toolbox.javabuilder.MWArray</code>. 
     * Each output array should be freed by calling its <code>dispose()</code> method.
     *
     * @throws java.jmi.RemoteException An error has occurred during the function call or 
     * in communication with the server.
     */
    public Object[] cvGaborTextureSegmentRun(Object... rhs) throws RemoteException;
  
    /** Frees native resources associated with the remote server object */
    void dispose() throws RemoteException;
}
