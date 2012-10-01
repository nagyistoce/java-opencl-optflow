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
package me.kikoqiu.opencl.optflow.util;
import static org.jocl.CL.CL_TRUE;
import static org.jocl.CL.clEnqueueReadBuffer;

import java.io.IOException;

import me.kikoqiu.opencl.filters.ImageFilter;
import me.kikoqiu.opencl.image.IImage2d;

import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_kernel;
import org.jocl.cl_mem;
import org.jocl.utils.Kernels;
import org.jocl.utils.Mems;

public class Helpers extends me.kikoqiu.opencl.CLBase {
	ImageFilter clear,copy;
	cl_kernel _copy2array;
	private cl_kernel _adjust;
	
	public void init() throws IOException{
		if(clear!=null){
			return;
		}
		clear=new ImageFilter("data/helpers.c","clear");
		copy=new me.kikoqiu.opencl.filters.CopyFilter();
		_copy2array=Kernels.createFromFile(context, "data/helpers.c", "copy2array", "-cl-enable-mad");
		_adjust=Kernels.createFromFile(context, "data/helpers.c", "adjust", "-cl-enable-mad");		
	}
	
	public void adjust(IImage2d input,IImage2d off,IImage2d output){
		Kernels.setArgs(_adjust,input.getBuffer(),off.getBuffer(),output.getBuffer());
		this.exec(_adjust, output.getWidth(), output.getHeight());
	}
	
	public void copy(IImage2d input,IImage2d output){
		copy.filter(input,output);
	}
	
	public void clear(IImage2d output){
		clear.filter(output);
	}
	
	public void copy2array(IImage2d input,float[] data){
		cl_mem output=Mems.create(context, data.length * Sizeof.cl_float);
		try{
			Kernels.setArgs(_copy2array, input.getBuffer(), output);
			this.exec(_copy2array, input.getWidth(), input.getHeight());
			clEnqueueReadBuffer(commandQueue, output, 
		            CL_TRUE, 0, data.length * Sizeof.cl_float, 
		            Pointer.to(data), 0, null, null);			
		}finally{
			Mems.release(output);
		}
	}
	public void readMem(cl_mem mem,Pointer p,int sizeByte){
		clEnqueueReadBuffer(commandQueue, mem, 
	            CL_TRUE, 0, sizeByte, 
	            p, 0, null, null);	
	}
	
	static Helpers inst=new Helpers();
	
	@Override
	public void dispose() {
		super.dispose();
		clear.dispose();
		copy.dispose();
		clear=null;
		copy=null;
		Kernels.release(_copy2array);
		_copy2array=null;
	}

	public static Helpers getInstance(){
		try {
			inst.init();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return inst;
	}
	
}
