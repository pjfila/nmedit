/* Copyright (C) 2006 Christian Schneider
 * 
 * This file is part of Nomad.
 * 
 * Nomad is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Nomad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Nomad; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

/*
 * Created on Jan 25, 2006
 */
package net.sf.nmedit.nomad.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class NomadUtilities {

	public static <T> void addAll(ArrayList<T> list, Iterator<T> iterator)
	{
		while (iterator.hasNext())
			list.add(iterator.next());
	}
	
	public static void scale(Rectangle rect, double factor)
	{
		scale(rect, factor, factor);
	}
	
	public static void enlargeToGrid(Rectangle rect, int grid) {
		enlargeToGrid(rect, grid, grid);
	}
	
	public final static String extractKeyFromImageString(String str) {
		// {@.+}
		final int min_len = 3;
		final int l = str.length();
		if (l<min_len) return null;
		else if(str.charAt(0)!='{') return null;
		else if(str.charAt(1)!='@') return null;
		else if(str.charAt(l-1)!='}') return null;
		else return str.substring(2, l-1);
	}
	
	public static void enlargeToGrid(Rectangle rect, int gridx, int gridy) {
		int dx = rect.x % gridx;
		int dy = rect.y % gridy;

		rect.x-=dx;
		rect.y-=dy;
		rect.width +=dx;
		rect.height+=dy;

		dx = rect.width % gridx;
		dy = rect.height % gridy;
		rect.width += gridx-dx+1;
		rect.height+= gridy-dy+1;
	}

	public static void scale(Rectangle rect, double xfactor, double yfactor)
	{
		double xx = rect.x * xfactor;
		double yy = rect.y * yfactor;

		int fx = (int) Math.floor(xx);
		int cx = (int) Math.ceil (xx);
		
		int fy = (int) Math.floor(yy);
		int cy = (int) Math.ceil (yy);

		rect.x = fx;
		rect.y = fy;
		
		rect.width  = ((int) Math.ceil(rect.width *xfactor));
		rect.height = ((int) Math.ceil(rect.height*yfactor));
		
		if (fx<cx) rect.width ++;
		if (fy<cy) rect.height++;
	}
	
	public static void enlarge(Rectangle rect, int enlargement)
	{
		int enlargement3  = enlargement+(enlargement<<1); // *3 
		rect.x 		-= enlargement;
		rect.y 		-= enlargement;
		rect.width	+= enlargement3;
		rect.height	+= enlargement3;
	}

	public static void intersect(Rectangle rect, int width, int height)
	{
		intersect(rect, 0, 0, width, height);
	}
	
	public static void intersect(Rectangle rect, int x, int y, int width, int height)
	{
		if (rect.x<x)
		{
			rect.width -= (x-rect.x+1);
			rect.x = x;
		}
		
		if (rect.y<y)
		{
			rect.height -= (y-rect.y+1);
			rect.y = y;
		}

		int r = x+width -1;
		int b = y+height-1;

		int rr= rect.x+rect.width -1;
		int rb= rect.y+rect.height-1;
		
		if (rr>r) rect.width  = Math.max(x, r-rect.x);
		if (rb>b) rect.height = Math.max(y, b-rect.y);
	}

    public static void union( Rectangle rect, int x, int y, int w, int h )
    {
        int l = Math.min(rect.x, x); // left
        int t = Math.min(rect.y, y); // top
        int r = Math.max(rect.x+rect.width, x+w); // right+1
        int b = Math.max(rect.y+rect.height, y+h); // bottom+1
        rect.setBounds(l, t, r-l, b-t);
    }
    
	public static void setupAndShow(JFrame frame, double dw, double dh) {
	    Dimension screensz  = Toolkit.getDefaultToolkit().getScreenSize();
	    setupAndShow(frame, new Dimension((int)(screensz.width*dw), (int)(screensz.height*dh)));
	}

	public static void setupAndShow(JFrame frame, Dimension size) {
	    Dimension screensz  = Toolkit.getDefaultToolkit().getScreenSize();
	    size.height = Math.min(size.height, screensz.height);
	    size.width  = Math.min(size.width,  screensz.width);
	    
		frame.setPreferredSize(size);
		frame.setSize(size);
	
	    frame.setLocation(
	      (screensz.width- size.width)/2,
	      (screensz.height-size.height)/2
	    );
		frame.validate();

	    // set close operation, then show window
	    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    frame.setVisible(true);
	}
	
	public static void center(Window w) {
	    Dimension screensz  = Toolkit.getDefaultToolkit().getScreenSize();
	    Dimension size = w.getSize();
	
	    w.setLocation(
	      (screensz.width- size.width)/2,
	      (screensz.height-size.height)/2
	    );
	}
	
	public static void infoDialog(Component c, String message) {
		JOptionPane.showMessageDialog(c, message, "Information", JOptionPane.INFORMATION_MESSAGE); // info message
	}

	public static boolean isConfirmedByUser(Component c, String text) {
		return isConfirmedByUser(c, text, "confirm");
	}
	
	public static boolean isConfirmedByUser(Component c, String text, String label) {
		return JOptionPane.showConfirmDialog(c, text, label, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
	}

	public static Color alpha(Color c, int alpha) {
		
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
		
	}
	
	public static Color neighbour(Color c1, Color c2, double f) {
		return new Color(
			c1.getRed() 	+ (int)((c2.getRed()	-c1.getRed()	)*f),
			c1.getGreen() 	+ (int)((c2.getGreen()	-c1.getGreen()	)*f),
			c1.getBlue() 	+ (int)((c2.getBlue()	-c1.getBlue()	)*f),
			c1.getAlpha() 	+ (int)((c2.getAlpha()	-c1.getAlpha()	)*f)
		);
	}

	public static String removeFileExtension(String name) {
		int index = name.lastIndexOf('.');
		return (index<=0) ? name : name.substring(0, index);
	}

      public static Properties createProperties(String fileName)
        throws FileNotFoundException,
               SecurityException,
               IOException
      {
        InputStream in = new BufferedInputStream(new FileInputStream(fileName));
        Properties properties = new Properties();
        properties.load(in);
        in.close();
        return properties;
      }

    /**
     * Rounds d until digit '-to'. For example d=123, to=2 returns 120.
     * If d=123.456 and to=-2 then 123.45 is returned.
     * 
     * @param d Value to round
     * @param to decimal position
     * @return rounded value of d
     */
    public static double roundTo(double d, int to) {
        if (to == 0)
            return Math.round(d);

        double norm = Math.pow(10.0, -to);
        return Math.round(d*norm) / norm;
    }

    /**
     * Returns the value of d as a String. If d is a natural number,
     * it is returned without decimal point.
     * @param d value that should be converted to a string representation
     * @return d as string
     */
    public static String doubleToStr(double d) {
        double truncated = (int) d;
        if (d==truncated)
            return Integer.toString((int)d);
        else
            return Double.toString(d);
    }

}
