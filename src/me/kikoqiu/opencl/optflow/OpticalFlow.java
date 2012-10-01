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
package me.kikoqiu.opencl.optflow;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import me.kikoqiu.opencl.filters.ImageFilterBase;
import me.kikoqiu.opencl.image.IImage2d;
import me.kikoqiu.opencl.image.ImageRGBA;
import me.kikoqiu.opencl.image.ImageRGBA_SNORM_INT16;
import me.kikoqiu.opencl.optflow.util.Helpers;
import me.kikoqiu.opencl.optflow.util.Profile;
import me.kikoqiu.opencl.optflow.util.ScaleFilter;

public class OpticalFlow {


	/**
	 * Create the application.
	 */
	public OpticalFlow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		try {
			preprocess=new ImageFilterBase("data/optflow.c", "preprocess");
			solve=new ImageFilterBase("data/optflow.c","solve");
			solve_k=new ImageFilterBase("data/optflow.c","solve_k");
			sfPrev=new ScaleFilter();
			sfNow=new ScaleFilter();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void dispose(){
		preprocess.dispose();
		solve.dispose();
		solve_k.dispose();
		sfPrev.dispose();
		sfNow.dispose();
	}
	
	ImageFilterBase preprocess;	
	ImageFilterBase solve,solve_k;
	IImage2d inputPrev;
	ScaleFilter sfPrev,sfNow;
	private int iterateCount=0;
	
	
	public BufferedImage process(BufferedImage bi) {	
		BufferedImage ret=null;
		if(bi==null)return ret;		
		Profile.begin();
		
		//create current frame image and scale it
		IImage2d inputNow=new ImageRGBA();		
		inputNow.create(bi);		
		sfNow.init(inputNow.getWidth(), inputNow.getHeight(), 16, 2);
		sfNow.filter(inputNow);
				
		Profile.out();
		if(inputPrev!=null){
			IImage2d calcatedFlow=new ImageRGBA_SNORM_INT16();;
			calcatedFlow.create(1, 1, true);
			Helpers.getInstance().clear(calcatedFlow);
			
			//for all scaled images,from small to large
			for(int i=sfPrev.outputs.size()-1;i>=0;--i){
				IImage2d i1=sfPrev.outputs.get(i),i2=sfNow.outputs.get(i);
				
				//create preprocessed image 
				IImage2d preprocessed=new ImageRGBA_SNORM_INT16();
				preprocessed.create(i1.getWidth(), i1.getHeight(), true);				
				preprocess.filter(i1, preprocessed);	
				Profile.out("pre");
				
				IImage2d flowout=new ImageRGBA_SNORM_INT16();	
				flowout.create(preprocessed.getWidth(), preprocessed.getHeight(), true);				
				
				solve.filter(i2, preprocessed,calcatedFlow,flowout);
				calcatedFlow.dispose();
				
				//iterate current level
				if(iterateCount>0){
					IImage2d flowout1=new ImageRGBA_SNORM_INT16();	
					flowout1.create(preprocessed.getWidth(), preprocessed.getHeight(), true);
					IImage2d fin=flowout;
					for(int k=0;k<iterateCount;++k){
						IImage2d fnext=fin==flowout?flowout1:flowout;
						solve_k.filter(i2, preprocessed,fin,fnext);					
						fin=fnext;
					}
					IImage2d fnext=fin==flowout?flowout1:flowout;
					fnext.dispose();
					flowout=fin;
				}
				
				
				
				calcatedFlow=flowout;
				preprocessed.dispose();
				Profile.out("sol");
			}
			
			Profile.out();
			
			//read speed result
			int w=inputNow.getWidth(),h=inputNow.getHeight();
			float[] speeds=new float[w/2*h/2*3];
			Helpers.getInstance().copy2array(calcatedFlow, speeds);
			
			calcatedFlow.dispose();		
			
			
			ret=new BufferedImage(bi.getWidth(),bi.getHeight(),BufferedImage.TYPE_4BYTE_ABGR);
			
			Graphics g=ret.createGraphics();
			for(int y=0;y<h/2;y+=5){
				for(int x=0;x<w/2;x+=5){
					int p0=y*w/2+x;
					p0*=3;
					float sx=speeds[p0]*2000,sy=speeds[p0+1]*2000;
					float s=(float) Math.sqrt(sx*sx+sy*sy);
					if(s>3){
						//g.drawLine(x*2, y*2,(int) (x*2+sx), (int)(y*2+sy));
						drawArrow(g,x*2, y*2,(int) (x*2+sx), (int)(y*2+sy));
					}
				}
			}
			g.dispose();
			Profile.out();
		
		}
		if(inputPrev!=null){
			inputPrev.dispose();			
		}
		inputPrev=inputNow;
		
		//swap scale filter chains
		ScaleFilter tmp=sfPrev;
		sfPrev=sfNow;
		sfNow=tmp;
		
		return ret;
	}
	
	/** 
     *
     *  */ 
      public   void  drawArrow(Graphics g,  int  x1,  int  y1,  int  x2,  int  y2)  {

         double  H  =   2 ;  // arrow length  
          double  L  =   1 ; // arrow width/2   
          int  x3  =   0 ;
         int  y3  =   0 ;
         int  x4  =   0 ;
         int  y4  =   0 ;
         double  awrad  =  Math.atan(L  /  H);  //angle 
          double  arraow_len  =  Math.sqrt(L  *  L  +  H  *  H); //
          double [] arrXY_1  =  rotateVec(x2  -  x1, y2  -  y1, awrad,  true , arraow_len);
         double [] arrXY_2  =  rotateVec(x2  -  x1, y2  -  y1,  - awrad,  true , arraow_len);
         double  x_3  =  x2  -  arrXY_1[ 0 ];  // (x3,y3) first point
          double  y_3  =  y2  -  arrXY_1[ 1 ];
         double  x_4  =  x2  -  arrXY_2[ 0 ]; // (x4,y4)second point
          double  y_4  =  y2  -  arrXY_2[ 1 ];

        Double X3  =   new  Double(x_3);
        x3  =  X3.intValue();
        Double Y3  =   new  Double(y_3);
        y3  =  Y3.intValue();
        Double X4  =   new  Double(x_4);
        x4  =  X4.intValue();
        Double Y4  =   new  Double(y_4);
        y4  =  Y4.intValue();
         // g.setColor(SWT.COLOR_WHITE);
         //
         g.drawLine(x1, y1, x2, y2);
         // 
         g.drawLine(x2, y2, x3, y3);
         // 
         g.drawLine(x2, y2, x4, y4);

    } 
    
     /** 
     *取得箭头的绘画范围
      */ 
 
     public   double [] rotateVec( int  px,  int  py,  double  ang,  boolean  isChLen,
             double  newLen)  {

         double  mathstr[]  =   new   double [ 2 ];
          double  vx  =  px  *  Math.cos(ang)  -  py  *  Math.sin(ang);
         double  vy  =  px  *  Math.sin(ang)  +  py  *  Math.cos(ang);
         if  (isChLen)  {
             double  d  =  Math.sqrt(vx  *  vx  +  vy  *  vy);
            vx  =  vx  /  d  *  newLen;
            vy  =  vy  /  d  *  newLen;
            mathstr[ 0 ]  =  vx;
            mathstr[ 1 ]  =  vy;
        } 
         return  mathstr;
    }	

}
