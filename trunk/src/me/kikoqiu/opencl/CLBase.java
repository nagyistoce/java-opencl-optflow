/*------------------------------------------------------------------------
 *  Copyright 2012 (c) Kiko Qiu <kikoqiu@163.com>
 *
 *  This file is part of the OpenclOpticalFlow.
 *
 *  The OpenclOpticalFlow is free software; you can redistribute it
 *  and/or modify it under the terms of the GNU Lesser Public License as
 *  published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  The OpenclOpticalFlow is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser Public License
 *  along with the OpenclOpticalFlow; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 *  Boston, MA  02110-1301  USA
 *  http://code.google.com/p/java-opencl-optflow/
 *------------------------------------------------------------------------
 */
package me.kikoqiu.opencl;

import static org.jocl.CL.CL_CONTEXT_PLATFORM;
import static org.jocl.CL.CL_DEVICE_TYPE_ALL;
import static org.jocl.CL.clCreateCommandQueue;
import static org.jocl.CL.clCreateContext;
import static org.jocl.CL.clEnqueueNDRangeKernel;
import static org.jocl.CL.clGetDeviceIDs;
import static org.jocl.CL.clGetPlatformIDs;
import static org.jocl.CL.clReleaseCommandQueue;
import static org.jocl.CL.clReleaseContext;

import org.jocl.CL;
import org.jocl.cl_command_queue;
import org.jocl.cl_context;
import org.jocl.cl_context_properties;
import org.jocl.cl_device_id;
import org.jocl.cl_kernel;
import org.jocl.cl_platform_id;

public class CLBase{
    protected cl_context context;
    protected cl_command_queue commandQueue;
    
    protected CLBase(){
    	start();
    	context=_context;
    	commandQueue=_commandQueue;
    }
    
    public static cl_context get_context() {
		return _context;
	}


	public static cl_command_queue get_commandQueue() {
		return _commandQueue;
	}
	/**
     * The OpenCL context
     */
    private static cl_context _context;
    
    /**
     * The OpenCL command queue
     */
    private static cl_command_queue _commandQueue;
    
	
	private static void start(){
		if(_context!=null)return;
		
		// The platform, device type and device number
        // that will be used
		/// TODO should be configurable
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int deviceIndex = 0;

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];

        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        
        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];
        
        // Obtain a device ID 
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        // Create a context for the selected device
        _context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device}, 
            null, null, null);
        
        // Create a command-queue for the selected device
        _commandQueue = 
            clCreateCommandQueue(_context, device, 0, null);
	}
	
	
	public static void shutdown(){
		if(_commandQueue==null)return;
		clReleaseCommandQueue(_commandQueue);
		_commandQueue=null;
        clReleaseContext(_context);
        _context=null;
	}
	
	
	
	
	protected void exec(cl_kernel kernel,long gx,long gy){
		long globalWorkSize[]=new long[]{gx,gy};
		 clEnqueueNDRangeKernel(commandQueue, kernel, 2, null, 
		            globalWorkSize, null, 0, null, null);		
	}


	public void dispose() {
	}
}
