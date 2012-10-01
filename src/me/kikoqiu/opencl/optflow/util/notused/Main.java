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
package me.kikoqiu.opencl.optflow.util.notused;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import me.kikoqiu.opencl.CLBase;
import me.kikoqiu.opencl.filters.DecrFilter;
import me.kikoqiu.opencl.filters.GaussianBlurFilter;
import me.kikoqiu.opencl.filters.ImageFilterBase;
import me.kikoqiu.opencl.filters.IterFilter;
import me.kikoqiu.opencl.image.IImage2d;
import me.kikoqiu.opencl.image.ImageRGBA;
import me.kikoqiu.opencl.image.ImageRGBA_SNORM_INT16;
import me.kikoqiu.opencl.optflow.util.Helpers;
import me.kikoqiu.opencl.optflow.util.Profile;
import me.kikoqiu.opencl.optflow.util.ScaleFilter;

import org.jocl.Pointer;
import org.jocl.Sizeof;
import org.jocl.cl_mem;
import org.jocl.utils.Mems;

import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;
import de.humatic.dsj.DSJUtils;
import de.humatic.dsj.DSMovie;
import de.humatic.dsj.SwingMovieController;
import de.humatic.dsj.rc.RendererControls;

public class Main implements PropertyChangeListener {

	private JFrame frame;
	private DSCapture graph;
	private DSMovie movie;
	
	/**
	 * Launch the application.
	 */
	public static void main_(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 254, 272);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmReplay = new JMenuItem("Replay");
		mntmReplay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				createGraph(true);			
			}
		});
		mnFile.add(mntmReplay);
		
		JMenuItem mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createGraph(false);			
			}
		});
		menuBar.add(mntmOpen);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		tab1 = new JPanel();
		tabbedPane.addTab("New tab", null, tab1, null);
		
		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("New tab", null, panel_1, null);
		
		lblDebug = new JLabel("debug");
		panel_1.add(lblDebug);
		
		
		try {
			preprocess=new ImageFilterBase("data/optflow.c", "preprocess");
			solve=new ImageFilterBase("data/optflow.c","solve");
			solve_k=new ImageFilterBase("data/optflow.c","solve_k");
			visual=new ImageFilterBase("data/visual.c","filter");
			display=new ImageFilterBase("data/display.c","filter");
			
			erosion=new IterFilter("data/util.c","erosion");
			bg=new GaussianBlurFilter();
			harris=new ImageFilterBase("data/harris.c","harris");
			sf1=new ScaleFilter();
			sf2=new ScaleFilter();
			df=new DecrFilter();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	private void createGraph(boolean cap){
		if(cap){
		
			DSFilterInfo[][] dsi = DSCapture.queryDevices();
			graph = new DSCapture(DSFiltergraph.DD7 | DSFiltergraph.FRAME_CALLBACK , dsi[0][0], false, DSFilterInfo.doNotRender(), this);
			tab1.add(java.awt.BorderLayout.CENTER, graph.asComponent());
			frame.pack();
		}else{
			java.awt.FileDialog fd = new java.awt.FileDialog(frame, "select movie", java.awt.FileDialog.LOAD);
			  
			fd.setVisible(true);
			 
			if (fd.getFile() == null) return;
			
			Container container=tab1;
			movie = new DSMovie(fd.getDirectory()+fd.getFile(), DSFiltergraph.DD7| DSFiltergraph.FRAME_CALLBACK, this);		
			container.add(java.awt.BorderLayout.CENTER, movie.asComponent());
			
			container.add(java.awt.BorderLayout.SOUTH, new SwingMovieController(movie));
			
			movie.setLoop(true);
			frame.pack();
		}
	}
	
	
	ImageFilterBase preprocess;	
	ImageFilterBase solve,solve_k;
	ImageFilterBase visual,display,harris;
	DecrFilter df;
	IterFilter erosion;
	me.kikoqiu.opencl.filters.GaussianBlurFilter bg;
	
	IImage2d input1;
	ScaleFilter sf1,sf2;
	
	private JLabel lblDebug;
	private JPanel tab1;

	private boolean error=false;
	@Override
	public void propertyChange(PropertyChangeEvent pe) {
		
		//System.out.println("received event or callback from "+pe.getPropagationId());
		 
	    switch(DSJUtils.getEventType(pe)) {
	    case DSFiltergraph.FRAME_NOTIFY:
	    	try{
	    		if(error)return;
	    		process((DSFiltergraph)pe.getSource());
	    	}catch(Exception e){
	    		e.printStackTrace();
	    		error=true;
	    	}
	    }
	}
	
	public void debugcb(IImage2d k){
		IImage2d visout=new ImageRGBA();
		visout.create(k.getWidth(),k.getHeight(), false);
		display.filter(k, visout);
		BufferedImage tT=new BufferedImage(k.getWidth(),k.getHeight(),BufferedImage.TYPE_4BYTE_ABGR);
		visout.readImage(tT);
		visout.dispose();
		this.lblDebug.setIcon(new ImageIcon(tT));
	}
	public void process(DSFiltergraph target) {	
		Profile.begin();
		
		BufferedImage bi=target.getImage();	
		if(bi==null)return;
		/*BufferedImage tmp2=new BufferedImage(640,480,BufferedImage.TYPE_INT_ARGB);
		Graphics g=tmp2.createGraphics();
		g.drawImage(bi, 0, 0, 640, 480, null);
		g.dispose();
		bi=tmp2;*/
		
		/*BufferedImage tmp1=new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB),
				tmp2=new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB);
		
		Graphics g=tmp2.createGraphics();
		g.drawImage(bi, 0, 0, 256, 256, null);
		g.dispose();
		
		gb1.filter(tmp2, tmp1);
		gb2.filter(tmp1, tmp2);*/
		
		IImage2d input2=new ImageRGBA();		
		input2.create(bi);
		
		/*IImage2d gout=bg.filter(input2, this);
		IImage2d goutcopy=new me.kikoqiu.opencl.image.ImageLumi();
		goutcopy.create(gout.getWidth(), gout.getHeight(), true);
		copy.filter(gout, goutcopy);
		input2.dispose();
		input2=goutcopy;*/
		
		sf2.init(input2.getWidth(), input2.getHeight(), 16, 2);
		sf2.filter(input2);
		
		
		Profile.out();
		if(input1!=null){
			IImage2d visout=new ImageRGBA();
			IImage2d prevflow=new ImageRGBA_SNORM_INT16();;
			prevflow.create(1, 1, true);
			Helpers.getInstance().clear(prevflow);
			
			int i=sf1.outputs.size()-1;
			//i=-1;
			for(;i>=0;--i){
				IImage2d i1,i2;
				if(i==-1){
					i1=input1;
					i2=input2;
				}else{
					i1=sf1.outputs.get(i);
					i2=sf2.outputs.get(i);
				}
				IImage2d ppoutput=new ImageRGBA_SNORM_INT16();
				ppoutput.create(i1.getWidth(), i1.getHeight(), true);				
				preprocess.filter(i1, ppoutput);	
				Profile.out("pre");
				
				IImage2d flowout=new ImageRGBA_SNORM_INT16();	
				flowout.create(ppoutput.getWidth(), ppoutput.getHeight(), true);				
				
				solve.filter(i2, ppoutput,prevflow,flowout);
				prevflow.dispose();
				
				
				/*IImage2d flowout1=new ImageRGBA_SNORM_INT16();	
				flowout1.create(ppoutput.getWidth(), ppoutput.getHeight(), true);
				IImage2d fin=flowout;
				for(int k=0;k<1;++k){
					IImage2d fnext=fin==flowout?flowout1:flowout;
					solve_k.filter(i2, ppoutput,fin,fnext);					
					fin=fnext;
				}
				IImage2d fnext=fin==flowout?flowout1:flowout;
				fnext.dispose();
				flowout=fin;*/
				
				
				
				prevflow=flowout;				
				Profile.out("sol");
				
				/*if(i<sf1.outputs.size()-5){
					visout.create(prevflow.getWidth(), prevflow.getHeight(), false);
					visual.filter(prevflow, visout);
					ppoutput.dispose();
					Profile.out("vis");
					break;
				}*/				
				
				ppoutput.dispose();
			}
			
			Profile.out();
			IImage2d output=prevflow;			
			//output=erosion.filter(prevflow, 8, this);	
			
						
			visout.create(input1.getWidth(), input1.getHeight(), false);
			
			/*df.init(prevflow.getWidth(), prevflow.getHeight(), 1);
			df.filter(prevflow, this);
			Helpers.getInstance().adjust(input2, df.outputs.get(df.outputs.size()-1), visout);
			Helpers.getInstance().copy(visout, input2);
			sf2.filter(input2,this);*/
			
			
			IImage2d ppoutput=new ImageRGBA_SNORM_INT16();
			ppoutput.create(input2.getWidth(), input2.getHeight(), true);	
			preprocess.filter(input2, ppoutput);
			
			int w=input2.getWidth();int h=input2.getHeight();
			float[] arr=new float[w*h];			
			cl_mem m=Mems.create(CLBase.get_context(), arr.length*Sizeof.cl_float);
			harris.filter(ppoutput.getWidth(),ppoutput.getHeight(),ppoutput,m);
			ppoutput.dispose();			
			Helpers.getInstance().readMem(m, Pointer.to(arr), arr.length*Sizeof.cl_float);
			Mems.release(m);			
			
			
			
			
			
			visual.filter(output, input2,visout);
			
			
			//int[] group=findTrackPoint(output);	
			//for(int j=0;j<group.length/10;++j)group[j]=1;
			//cl_mem mem=Mems.create(CLBase.get_context(), group);
			//visual.filter(output, input1, mem,visout);
			//Mems.release(mem);
			
			//int[] group=findTrackPoint(output);	
			//for(int j=0;j<group.length/10;++j)group[j]=1;
			//cl_mem mem=Mems.create(CLBase.get_context(), group);
			//visual.filter(output, input1, mem,visout);
			//Mems.release(mem);
			
			float[] speeds=new float[w/2*h/2*3];
			Helpers.getInstance().copy2array(prevflow, speeds);
			//oldv=this.findTrackPoints(oldv, arr, speeds, w, h);
			
			prevflow.dispose();		
			
			
			BufferedImage t=new BufferedImage(bi.getWidth(),bi.getHeight(),BufferedImage.TYPE_4BYTE_ABGR);
			visout.readImage(t);
			visout.dispose();
			
			Graphics g=t.createGraphics();
			if(oldv!=null){
				for(int y=0;y<h;y+=1){
					for(int x=0;x<w;x+=1){
						if(oldv[y*w+x]!=null){
							TrackPoint tp=oldv[y*w+x];
							g.setColor(colors[(int) (tp.id%colors.length)]);
							g.drawRect(x-2, y-2, 5, 5);
							//g.drawString(""+tp.id, x, y);
						}
					}
				}
			}
			for(int y=0;y<h/2;y+=3){
				for(int x=0;x<w/2;x+=3){
					int p0=y*w/2+x;
					p0*=3;
					float sx=speeds[p0]*2000,sy=speeds[p0+1]*2000;
					float s=(float) Math.sqrt(sx*sx+sy*sy);
					if(s>1){
						//g.drawLine(x*2, y*2,(int) (x*2+sx), (int)(y*2+sy));
						paintk(g,x*2, y*2,(int) (x*2+sx), (int)(y*2+sy));
					}
				}
			}
			g.dispose();
			RendererControls rc = target.getRendererControls();
			rc.setOverlayImage(t, null, Color.BLACK, 1);	
			Profile.out();
		
		}
		if(input1!=null){
			input1.dispose();			
		}
		input1=input2;
		
		ScaleFilter tmp=sf1;
		sf1=sf2;
		sf2=tmp;
	}
	
	/** 
     * 画带箭头的线
     *  */ 
      public   void  paintk(Graphics g,  int  x1,  int  y1,  int  x2,  int  y2)  {

         double  H  =   3 ;  // 箭头高度    
          double  L  =   1 ; // 底边的一半   
          int  x3  =   0 ;
         int  y3  =   0 ;
         int  x4  =   0 ;
         int  y4  =   0 ;
         double  awrad  =  Math.atan(L  /  H);  // 箭头角度    
          double  arraow_len  =  Math.sqrt(L  *  L  +  H  *  H); // 箭头的长度    
          double [] arrXY_1  =  rotateVec(x2  -  x1, y2  -  y1, awrad,  true , arraow_len);
         double [] arrXY_2  =  rotateVec(x2  -  x1, y2  -  y1,  - awrad,  true , arraow_len);
         double  x_3  =  x2  -  arrXY_1[ 0 ];  // (x3,y3)是第一端点    
          double  y_3  =  y2  -  arrXY_1[ 1 ];
         double  x_4  =  x2  -  arrXY_2[ 0 ]; // (x4,y4)是第二端点    
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
         // 画线 
         g.drawLine(x1, y1, x2, y2);
         // 画箭头的一半 
         g.drawLine(x2, y2, x3, y3);
         // 画箭头的另一半 
         g.drawLine(x2, y2, x4, y4);

    } 
    
     /** 
     *取得箭头的绘画范围
      */ 
 
     public   double [] rotateVec( int  px,  int  py,  double  ang,  boolean  isChLen,
             double  newLen)  {

         double  mathstr[]  =   new   double [ 2 ];
         // 矢量旋转函数，参数含义分别是x分量、y分量、旋转角、是否改变长度、新长度    
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
     
     
	TrackPoint[] oldv;
	
	
	private TrackPoint[] findTrackPoints(TrackPoint[] old,float[] corners,float[] speeds,int w,int h) {
		
		//old=null;
		TrackPoint[] newt=processPoint(corners,speeds,w,h);		
		if(old==null){
			return newt;
		}
		//old=transform(old, w, h);	
		
		//matchTry(old, w, h, newt, 2);
		matchTry(old, w, h, newt, 4);
		matchTry(old, w, h, newt, 8);
		matchTry(old, w, h, newt, 16);
		return newt;		
	}

	private void matchTry(TrackPoint[] old, int w, int h, TrackPoint[] newt,
			int findwindow) {
		for(int y=0;y<h;++y){
			for(int x=0;x<w;++x){
				int p0=y*w+x;
				TrackPoint nv=newt[p0];
				if(nv==null || nv.isSrcMatched()){
					continue;
				}
				TrackPoint bestmatch=null;
				float bestmiss=1e10f;
				for(int xx=x-findwindow;xx<=x+findwindow;++xx){
					for(int yy=y-findwindow;yy<=y+findwindow;++yy){
						if(xx>0&&xx<w&&yy>0&&yy<h){
							int p1=yy*w+xx;
							TrackPoint c=old[p1];
							if(c==null||c.isMatched()){
								continue;
							}
							if(bestmatch==null){
								bestmatch=c;
								bestmiss=c.calcMiss(nv);
								continue;
							}				
							float miss=c.calcMiss(nv);
							if(miss<bestmiss){
								bestmiss=miss;
								bestmatch=c;
							}
						}
					}
				}
				if(bestmiss<1e10){
					nv.obj=bestmatch.obj;
					nv.id=bestmatch.id;
					//bestmatch.setMatched(true);
					nv.setSrcMatched(true);
				}
			}
		}
	}
	Color[] colors=new Color[]{
			java.awt.Color.BLACK,java.awt.Color.red,java.awt.Color.blue,java.awt.Color.yellow,
			java.awt.Color.cyan,java.awt.Color.DARK_GRAY,java.awt.Color.ORANGE,
			java.awt.Color.pink,java.awt.Color.LIGHT_GRAY
	};
	private TrackPoint[] transform(TrackPoint[] tps,int w,int h){
		TrackPoint[] ret=new TrackPoint[tps.length];
		for(int y=0;y<h;++y){
			for(int x=0;x<w;++x){
				int p0=y*w+x;
				if(tps[p0]!=null){
					int nx=(int) (x+tps[p0].getSx());
					int ny=(int) (y+tps[p0].getSy());
					if(nx>=0&& nx<w&&ny>=0&&ny<h){
						int np=ny*w+nx;
						ret[np]=tps[p0];
					}
				}
			}
		}
		return ret;
	}
	private TrackPoint[] processPoint(float[] arr,float[] speeds,int w,int h) {
		int iwndowsize=10;
		for(int y=0;y<h;++y){
			for(int x=0;x<w;++x){
				float v0=arr[y*w+x];
				if(v0<=0)continue;
				for(int a=y-iwndowsize;a<=y+iwndowsize;++a){
					for(int b=x;b<=x+iwndowsize;++b){
						if(a<0 || a>=h||b>=w || (a==y && b==x)){
							continue;
						}
						float v1=arr[a*w+b];
						if(v1<=0)continue;
						if(v1>v0){
							arr[y*w+x]=0;
						}else{
							arr[a*w+b]=0;
						}
					}
				}
			}
		}
		TrackPoint[] ret= new TrackPoint[w*h];
		for(int y=0;y<h;++y){
			for(int x=0;x<w;++x){
				int p0=y*w+x;
				if(arr[p0]>0){
					TrackPoint tp=new TrackPoint();
					tp.setSx(speeds[p0*3]*2000);
					tp.setSy(speeds[p0*3+1]*2000);
					tp.setSpec(arr[p0]);
					ret[p0]=tp;
				}
			}
		}		
		return ret;
	}


	static class TrackObject{
		float sx,sy;
		public float getSx() {
			return sx;
		}

		public void setSx(float sx) {
			this.sx = sx;
		}

		public float getSy() {
			return sy;
		}

		public void setSy(float sy) {
			this.sy = sy;
		}
	}
	static class TrackPoint{
		long id=++cid;
		static int cid=0;
		float sx,sy;
		TrackObject obj;
		float spec;
		boolean matched;
		boolean srcMatched;
		
		public boolean isSrcMatched() {
			return srcMatched;
		}
		public void setSrcMatched(boolean srcMatched) {
			this.srcMatched = srcMatched;
		}
		public boolean isMatched() {
			return matched;
		}
		public void setMatched(boolean matched) {
			this.matched = matched;
		}
		public float calcMiss(TrackPoint v){
			float ds=(float) Math.sqrt((v.sx-sx)*(v.sx-sx)+(v.sy-sy)*(v.sy-sy));
			float dspec=Math.abs(spec-v.spec);
			return ds+dspec;
		}
		public float getSx() {
			return sx;
		}

		public void setSx(float sx) {
			this.sx = sx;
		}

		public float getSy() {
			return sy;
		}

		public void setSy(float sy) {
			this.sy = sy;
		}

		public TrackObject getObj() {
			return obj;
		}

		public void setObj(TrackObject obj) {
			this.obj = obj;
		}
		public void setSpec(float s){
			this.spec=s;
		}
		public float getSpec(){
			return this.spec;
		}
	}
	

}
