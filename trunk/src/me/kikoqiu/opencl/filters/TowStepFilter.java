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
import org.jocl.cl_program;
import org.jocl.utils.Kernels;
import org.jocl.utils.Programs;

public class TowStepFilter extends CLBase {	
	cl_kernel kernel,kernel1;
	IImage2d img1,img2;
	
	public TowStepFilter(String file,String ker1,String ker2) throws IOException{	
		cl_program prog=Programs.createFromFile(context, file, "-cl-mad-enable");
		kernel=Kernels.create(prog,ker1);
		kernel1=Kernels.create(prog, ker2);
		Programs.release(prog);
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
	
	public IImage2d filter(IImage2d input, Main main){
		init(input.getWidth(),input.getHeight());
			
		Kernels.setArgs(kernel, input.getBuffer(), img1.getBuffer());			
		exec(kernel,img1.getWidth(),img1.getHeight());
		
		Kernels.setArgs(kernel, img1.getBuffer(), img2.getBuffer());			
		exec(kernel,img2.getWidth(),img2.getHeight());
		
		return img2;
	}
	
	public void releaseBuffer(){
		super.dispose();
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
		releaseBuffer();
		Kernels.release(kernel,kernel1);
		kernel=null;
		kernel1=null;
	}
}
