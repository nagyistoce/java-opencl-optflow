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
import me.kikoqiu.opencl.image.ImageRGBA_SNORM_INT16;
import me.kikoqiu.opencl.optflow.Main;

import org.jocl.cl_kernel;
import org.jocl.utils.Kernels;

public class IterFilter extends CLBase {	
	cl_kernel kernel;
	IImage2d img1,img2;
	
	public IterFilter(String file,String ker) throws IOException{		
		kernel=Kernels.createFromFile(context, file,ker , "-cl-mad-enable");
	}
	
	public void init(int w,int h){
		if(img1!=null){
			if(img1.getWidth()==w && img1.getHeight()==h){
				return;
			}else{
				this.releaseBuffer();
			}
		}
		img1=new ImageRGBA_SNORM_INT16();
		img1.create(w, h, true);
		
		img2=new ImageRGBA_SNORM_INT16();
		img2.create(w, h, true);
	}
	
	public IImage2d filter(IImage2d input,int loops, Main main){
		init(input.getWidth(),input.getHeight());
		IImage2d output=img1;
		for(int i=0;i<loops;++i){
			Kernels.setArgs(kernel, input.getBuffer(), output.getBuffer());			
			exec(kernel,output.getWidth(),output.getHeight());
			input=output;
			output=input==img1?img2:img1;
		}
		return input;
	}
	
	public void releaseBuffer(){
		if(img1!=null){
			img1.dispose();
			img1=null;
		}
		if(img2!=null){
			img2.dispose();
			img2=null;
		}
	}	
	
	public void dispose(){
		super.dispose();
		releaseBuffer();
		Kernels.release(kernel);
		kernel=null;
	}
}
