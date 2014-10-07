/*
 * MATLAB Compiler: 5.0 (R2013b)
 * Date: Wed Oct  1 02:22:24 2014
 * Arguments: "-B" "macro_default" "-W" "java:GaborClustering,GaborCluster" "-T" 
 * "link:lib" "-d" 
 * "/home/hao/work/DIVADIAGTWeb/A_Course_Experiment_DivadiaWeb/GaborClustering/for_testing" 
 * "-v" 
 * "/home/hao/work/DIVADIAGTWeb/A_Course_Experiment_DivadiaWeb/cvGaborTextureSegmentRun.m" 
 * "class{GaborCluster:/home/hao/work/DIVADIAGTWeb/A_Course_Experiment_DivadiaWeb/cvGaborTextureSegmentRun.m}" 
 */

package GaborClustering;

import com.mathworks.toolbox.javabuilder.*;
import com.mathworks.toolbox.javabuilder.internal.*;

/**
 * <i>INTERNAL USE ONLY</i>
 */
public class GaborClusteringMCRFactory
{
   
    
    /** Component's uuid */
    private static final String sComponentId = "GaborCluster_90DF5A90C93FC7FCAA0CCAAE0A57590F";
    
    /** Component name */
    private static final String sComponentName = "GaborClustering";
    
   
    /** Pointer to default component options */
    private static final MWComponentOptions sDefaultComponentOptions = 
        new MWComponentOptions(
            MWCtfExtractLocation.EXTRACT_TO_CACHE, 
            new MWCtfClassLoaderSource(GaborClusteringMCRFactory.class)
        );
    
    
    private GaborClusteringMCRFactory()
    {
        // Never called.
    }
    
    public static MWMCR newInstance(MWComponentOptions componentOptions) throws MWException
    {
        if (null == componentOptions.getCtfSource()) {
            componentOptions = new MWComponentOptions(componentOptions);
            componentOptions.setCtfSource(sDefaultComponentOptions.getCtfSource());
        }
        return MWMCR.newInstance(
            componentOptions, 
            GaborClusteringMCRFactory.class, 
            sComponentName, 
            sComponentId,
            new int[]{8,2,0}
        );
    }
    
    public static MWMCR newInstance() throws MWException
    {
        return newInstance(sDefaultComponentOptions);
    }
}
