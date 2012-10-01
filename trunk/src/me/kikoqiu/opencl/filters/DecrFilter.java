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
import java.util.ArrayList;

import me.kikoqiu.opencl.CLBase;
import me.kikoqiu.opencl.image.IImage2d;
import me.kikoqiu.opencl.image.ImageRGBA_SNORM_INT16;
import me.kikoqiu.opencl.optflow.Main;

import org.jocl.cl_kernel;
import org.jocl.utils.Kernels;

public class DecrFilter extends CLBase {	
	cl_kernel kernel;
	public ArrayList<IImage2d> outputs;
	
	public DecrFilter() throws IOException{		
		kernel=Kernels.createFromFile(context, "data/decr.c","filter" , "-cl-mad-enable");
	}
	
	public void init(int w,int h,int minScale){
		int divider=2;
		if(outputs!=null){
			return;
		}
		outputs=new ArrayList<IImage2d>();
		while(true){
			int ww=w/divider;
			int hh=h/divider;
			if(ww < minScale || hh < minScale){
				break;
			}
			if(ww*divider<w ){
				++ww;
			}
			if( hh*divider<h){
				++hh;
			}
			w=ww;h=hh;
			IImage2d img=new ImageRGBA_SNORM_INT16();
			img.create(w, h, true);
			outputs.add(img);
		}
	}
	
	public void filter(IImage2d input, Main main){
		for(IImage2d output:outputs){			
			Kernels.setArgs(kernel, input.getBuffer(), output.getBuffer());			
			exec(kernel,output.getWidth(),output.getHeight());
			input=output;
		}
	}	
	
	public void releaseBuffer(){
		for(IImage2d output:outputs){
			output.dispose();
		}
		outputs=null;
	}	
	
	public void dispose(){
		super.dispose();
		releaseBuffer();
		Kernels.release(kernel);
		kernel=null;
	}
}
