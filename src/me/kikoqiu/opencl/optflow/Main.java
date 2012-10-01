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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

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
	private OpticalFlow of;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
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
		
		JMenuItem mntmReplay = new JMenuItem("Open Camera");
		mntmReplay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {				
				createGraph(true);			
			}
		});
		mnFile.add(mntmReplay);
		
		JMenuItem mntmOpen = new JMenuItem("Open Video File");
		mnFile.add(mntmOpen);
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createGraph(false);			
			}
		});
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		tab1 = new JPanel();
		tabbedPane.addTab("New tab", null, tab1, null);
		
		JPanel panel_1 = new JPanel();
		tabbedPane.addTab("New tab", null, panel_1, null);
		
		lblDebug = new JLabel("debug");
		panel_1.add(lblDebug);
	}
	
	
	private void createGraph(boolean cap){
		if(graph!=null){
			graph.dispose();
			graph=null;
		}
		if(movie!=null){
			movie.dispose();
			movie=null;
		}
		tab1.removeAll();
		
		if(of!=null){
			of.dispose();
			of=null;
		}
		try{
			of=new OpticalFlow();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
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
	
	
	
	
	private JLabel lblDebug;
	private JPanel tab1;

	private boolean error=false;
	@Override
	public void propertyChange(PropertyChangeEvent pe) {		 
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
	
	public void process(DSFiltergraph target) {			
		BufferedImage bi=target.getImage();	
		if(bi==null)return;
		if(of==null)return;
		
		BufferedImage t=of.process(bi);
		RendererControls rc = target.getRendererControls();
		rc.setOverlayImage(t, null, Color.BLACK, 1);
		
	}
	

}
