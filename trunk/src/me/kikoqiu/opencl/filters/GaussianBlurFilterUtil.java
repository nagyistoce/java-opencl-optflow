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
 
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
 
public class GaussianBlurFilterUtil
{
	/*public static void main(String[] argv){
		int r=2;
		float[] t=table(r,r*2+1);
		for(float i:t){
			System.out.println(i+"f,");			
		}
	}*/
	public static ConvolveOp getGaussianBlurFilter(int radius, boolean horizontal){
		if (radius < 1){
			throw new IllegalArgumentException("Radius must be >= 1");
		}
 
		int size = radius * 2 + 1;
		float[] data = table(radius, size);
 
		Kernel kernel = null;
		if(horizontal){
			kernel = new Kernel(size,1,data);
		}
		else{
			kernel = new Kernel(1,size,data);
		}
 
		return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
	}
	private static float[] table(int radius, int size) {
		float[] data = new float[size];
 
		float sigma = radius / 3.0f;
		float twoSigmaSquare = 2.0f * sigma * sigma;
		float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
		float total = 0.0f;
 
		for(int i = -radius; i <= radius; i++){
			float distance = i * i;
			int index = i + radius;
			data[index] = (float) Math.exp(-distance/twoSigmaSquare) / sigmaRoot;
			total += data[index];
		}
 
		for(int i = 0; i < data.length; i++){
			data[i] /= total;
		}
		return data;
	}
 
	private GaussianBlurFilterUtil(){}
}