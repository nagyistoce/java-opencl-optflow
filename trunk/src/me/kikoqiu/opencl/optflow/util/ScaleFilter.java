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

import java.io.IOException;
import java.util.ArrayList;

import me.kikoqiu.opencl.CLBase;
import me.kikoqiu.opencl.image.IImage2d;
import me.kikoqiu.opencl.image.ImageLumi;

import org.jocl.cl_kernel;
import org.jocl.utils.Kernels;

public class ScaleFilter extends CLBase {	
	cl_kernel kernel,kernel_v;	
	public ArrayList<IImage2d> outputs;
	
	public ScaleFilter() throws IOException{		
		kernel=Kernels.createFromFile(context, "data/scale.c","filter_h" , "-cl-mad-enable");
		kernel_v=Kernels.createFromFile(context, "data/scale.c","filter_v" , "-cl-mad-enable");
	}
	
	public void init(int w,int h,int minScale,int divider){
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
			if(ww*divider!=w || hh*divider!=h){
				break;
			}
			w=ww;h=hh;
			IImage2d img=new ImageLumi();
			img.create(w, h, true);
			outputs.add(img);
		}
	}
	
	public void filter(IImage2d input){
		for(IImage2d output:outputs){
			IImage2d tmp=new ImageLumi();
			tmp.create(output.getWidth(), output.getHeight()*2, true);
			
			Kernels.setArgs(kernel, input.getBuffer(), tmp.getBuffer());			
			exec(kernel,tmp.getWidth(),tmp.getHeight());			
			
			
			Kernels.setArgs(kernel_v, tmp.getBuffer(), output.getBuffer());			
			exec(kernel_v,output.getWidth(),output.getHeight());
			
			tmp.dispose();
			
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
		releaseBuffer();
		Kernels.release(kernel);
		Kernels.release(kernel_v);
		kernel=null;
		super.dispose();
	}
}
