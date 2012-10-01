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
package me.kikoqiu.opencl.image;

import static org.jocl.CL.CL_MEM_READ_ONLY;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.SinglePixelPackedSampleModel;

import me.kikoqiu.opencl.CLBase;

import org.jocl.CL;
import org.jocl.Pointer;
import org.jocl.cl_image_format;
import org.jocl.cl_mem;
import org.jocl.utils.Mems;

public abstract class ImageBase extends CLBase implements IImage2d {

	protected class ImageDataInfo{
		public int scanlineStride=0;
		public Pointer data=null;
	};
	
	cl_mem buffer;

	@Override
	public BufferedImage readImage() {
		int format=this.getBufferedImageFormat();
		BufferedImage dst=new BufferedImage(w,h,format);
		
		ImageDataInfo info = getImageDataInfo(dst);
	
	    
	    CL.clEnqueueReadImage(
	            commandQueue, this.getBuffer(), true, new long[3],
	            new long[]{w,h, 1},
	            info.scanlineStride, 0,
	            info.data, 0, null, null);
	    return dst;
	}
	private ImageDataInfo getImageDataInfo(BufferedImage dst) {
		ImageDataInfo info=new ImageDataInfo();		
		if(dst.getRaster().getSampleModel() instanceof SinglePixelPackedSampleModel){
			SinglePixelPackedSampleModel ssm=(SinglePixelPackedSampleModel)dst.getRaster().getSampleModel();
			info.scanlineStride=ssm.getScanlineStride();
		}else{
			ComponentSampleModel csm=(ComponentSampleModel) dst.getRaster().getSampleModel();
			info.scanlineStride=csm.getScanlineStride();
		}		
		DataBuffer db=dst.getRaster().getDataBuffer();
		if(db instanceof DataBufferInt){
			 DataBufferInt dataBufferDst =           (DataBufferInt)db;
	        int dataDst[] = dataBufferDst.getData();
	        info.data=Pointer.to(dataDst);
	        info.scanlineStride*=4;
		}else if(db instanceof DataBufferByte){
			DataBufferByte dataBufferDst =             (DataBufferByte)db;
		    byte dataDst[] = dataBufferDst.getData();
		    info.data=Pointer.to(dataDst);
		}else{
			throw new RuntimeException("unkown datatype");
		}
		return info;
	}
	@Override
	public BufferedImage readImage(BufferedImage dst) {
		BufferedImage bi=this.readImage();
		if(bi.getType()==dst.getType()){
			return bi;
		}
		Graphics g=dst.createGraphics();
		g.drawImage(bi, 0, 0, null, null);
		g.dispose();
	    return dst; 
	}


	@Override
	public void dispose() {
		Mems.release(this.getBuffer());
		this.setBuffer(null);
		super.dispose();
	}

	@Override
	public void create(BufferedImage bi) {
		this.dispose();
		if(bi.getType()!=this.getBufferedImageFormat()){
			 BufferedImage convertedImg = new BufferedImage(bi.getWidth(), bi.getHeight(), this.getBufferedImageFormat());
			 Graphics g=convertedImg.getGraphics();
			 g.drawImage(bi, 0, 0, null);
			 g.dispose();
			 bi=convertedImg;
		}
		this.w=bi.getWidth();
		this.h=bi.getHeight();
		
		cl_image_format f=this.getImageFormat();
		ImageDataInfo info = getImageDataInfo(bi);
	        
	
	
		cl_mem img= CL.clCreateImage2D(
				context,
				CL_MEM_READ_ONLY | CL.CL_MEM_USE_HOST_PTR, 
				new cl_image_format[]{f},
				bi.getWidth(), 
				bi.getHeight(), 
				info.scanlineStride,
				info.data, 
				null);
		
		this.setBuffer(img);
	}

	@Override
	public cl_mem getBuffer() {
		return buffer;
	}

	public void setBuffer(cl_mem buffer) {
		this.buffer=buffer;
	}
	

	@Override
	public void create(int w, int h, boolean readable) {
		this.dispose();
		this.w=w;
		this.h=h;
		cl_image_format f = getImageFormat();
		
		this.setBuffer( 
				CL.clCreateImage2D(
					context,
					readable? CL.CL_MEM_READ_WRITE:CL.CL_MEM_WRITE_ONLY , 
					new cl_image_format[]{f},
					w, 
					h, 
					0,
					null, 
					null)
		);
	}



	protected int h;
	protected int w;
	
	
	
	@Override
	public int getWidth() {		
		return w;
	}
	@Override
	public int getHeight() {
		return h;
	}
	public boolean isCreated(){
		return this.getBuffer()!=null;
	}

	public ImageBase() {
		super();
	}
	
	
	
	
	
	
	
	
	protected abstract int getBufferedImageFormat();

	protected abstract cl_image_format getImageFormat();

}