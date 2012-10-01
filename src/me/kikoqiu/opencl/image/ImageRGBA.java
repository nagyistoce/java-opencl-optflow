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


import java.awt.image.BufferedImage;

import org.jocl.CL;
import org.jocl.cl_image_format;

public class ImageRGBA extends ImageBase {
	
	@Override
	protected cl_image_format getImageFormat() {
		cl_image_format f=new cl_image_format();
	    f.image_channel_order=CL.CL_RGBA;
	    f.image_channel_data_type=CL.CL_UNORM_INT8;
		return f;
	}
	
	@Override
	protected int getBufferedImageFormat(){
		return BufferedImage.TYPE_INT_RGB;
	}
}
