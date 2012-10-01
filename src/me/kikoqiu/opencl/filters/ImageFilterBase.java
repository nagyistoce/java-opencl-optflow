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
package me.kikoqiu.opencl.filters;

import java.io.IOException;

import me.kikoqiu.opencl.CLBase;
import me.kikoqiu.opencl.image.IImage2d;

import org.jocl.*;
import org.jocl.utils.Kernels;

public class ImageFilterBase extends CLBase{
	cl_kernel kernel;
	
	public ImageFilterBase(String file,String kernelName) throws IOException{		
		kernel=Kernels.createFromFile(context, file, kernelName, "-cl-mad-enable");
	}
	
	public void filter(IImage2d output){		
		Kernels.setArgs(kernel, output.getBuffer());
		exec(kernel,output.getWidth(),output.getHeight());		
	}	
	
	public void filter(IImage2d input,IImage2d output){		
		Kernels.setArgs(kernel, input.getBuffer(), output.getBuffer());
		exec(kernel,output.getWidth(),output.getHeight());		
	}	
	
	public void filter(IImage2d input1,IImage2d input2,org.jocl.cl_mem input3,IImage2d output){
		Kernels.setArgs(kernel, input1.getBuffer(),input2.getBuffer(), input3,output.getBuffer());
		exec(kernel,output.getWidth(),output.getHeight());		
	}	
	
	
	public void dispose(){
		super.dispose();
		Kernels.release(kernel);
		kernel=null;
	}

	public void filter(IImage2d input1, IImage2d input2, IImage2d input3,
			IImage2d output) {
		Kernels.setArgs(kernel, input1.getBuffer(),input2.getBuffer(), input3.getBuffer(), output.getBuffer());
		exec(kernel,output.getWidth(),output.getHeight());				
	}
	
	public void filter(Object ... args)
    {
		IImage2d output=(IImage2d) args[args.length-1];

		Object[] args1=new Object[args.length];
		int p=0;
		for(Object o:args){
			if(o instanceof IImage2d){
				args1[p++]=((IImage2d) o).getBuffer();				
			}else {
				args1[p++]=o;
			}
		}
		
        Kernels.setArgs(kernel, args1);
        exec(kernel,output.getWidth(),output.getHeight());			
    }
	
	public void filter(int w,int h,Object ... args)
    {
		Object[] args1=new Object[args.length];
		int p=0;
		for(Object o:args){
			if(o instanceof IImage2d){
				args1[p++]=((IImage2d) o).getBuffer();				
			}else {
				args1[p++]=o;
			}
		}
        Kernels.setArgs(kernel, args1);
        exec(kernel,w,h);			
    }
	
}
