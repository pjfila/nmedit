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
 */package net.sf.nmedit.jtheme.clavia.nordmodular;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.nmedit.jtheme.JTContext;
import net.sf.nmedit.jtheme.clavia.nordmodular.graphics.FilterF;
import net.sf.nmedit.jtheme.component.JTControlAdapter;
import net.sf.nmedit.jtheme.component.JTDisplay;

public class JTFilterFDisplay extends JTDisplay implements ChangeListener
{
    
    private FilterF filterF;
    
    private JTControlAdapter cutoffAdapter;
    private JTControlAdapter resonanceAdapter;
    private JTControlAdapter slopeAdapter;

    public JTFilterFDisplay(JTContext context)
    {
        super(context);
        
        filterF = new FilterF();
    }

    protected void paintDynamicLayer(Graphics2D g)
    {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(JTNM1Context.GRAPH_DISPLAY_LINE);
        int y = getHeight()/2;
        g.drawLine(0, y, getWidth(), y);
        
        g.setColor(getForeground());
        filterF.setBounds(0, 0, getWidth(), getHeight());
        g.draw(filterF);
    }
    
    public float getCutoff()
    {
        return filterF.getCutOff()/127f;
    }
    
    public float getResonance()
    {
        return filterF.getResonance();
    }
    
    public int getSlope()
    {
        return filterF.getSlope();
    }
    
    public void setCutoff(float value)
    {
        if (getCutoff() != value)
        {
            filterF.setCutOff((int)(value*127f));
            repaint();
        }
    }
    
    public void setResonance(float value)
    {
        if (getResonance() != value)
        {
            filterF.setResonance(value);
            repaint();
        }
    }
    
    public void setSlope(int value)
    {
        if (getSlope() != value)
        {
            filterF.setSlope(value);
            repaint();
        }
    }
    
    public JTControlAdapter getCutoffAdapter()
    {
        return cutoffAdapter;
    }

    public JTControlAdapter getResonanceAdapter()
    {
        return resonanceAdapter;
    }

    public JTControlAdapter getSlopeAdapter()
    {
        return slopeAdapter;
    }

    public void setCutoffAdapter(JTControlAdapter adapter)
    {
        JTControlAdapter oldAdapter = this.cutoffAdapter;
        
        if (oldAdapter != adapter)
        {
            if (oldAdapter != null)
                oldAdapter.setChangeListener(null);
            this.cutoffAdapter = adapter;
            if (adapter != null)
                adapter.setChangeListener(this);
            
            updateCutoff();
        }
    }

    public void setResonanceAdapter(JTControlAdapter adapter)
    {
        JTControlAdapter oldAdapter = this.resonanceAdapter;
        
        if (oldAdapter != adapter)
        {
            if (oldAdapter != null)
                oldAdapter.setChangeListener(null);
            this.resonanceAdapter = adapter;
            if (adapter != null)
                adapter.setChangeListener(this);
            
            updateResonance();
        }
    }

    public void setSlopeAdapter(JTControlAdapter adapter)
    {
        JTControlAdapter oldAdapter = this.slopeAdapter;
        
        if (oldAdapter != adapter)
        {
            if (oldAdapter != null)
                oldAdapter.setChangeListener(null);
            this.slopeAdapter = adapter;
            if (adapter != null)
                adapter.setChangeListener(this);
            
            updateSlope();
        }
    }

    protected void updateCutoff()
    {
        if (cutoffAdapter != null)
            setCutoff((float)cutoffAdapter.getNormalizedValue());
    }

    protected void updateResonance()
    {
        if (resonanceAdapter != null)
            setResonance((float)resonanceAdapter.getNormalizedValue());
    }

    protected void updateSlope()
    {
        if (slopeAdapter != null)
            setSlope(slopeAdapter.getValue()-slopeAdapter.getMinValue());
    }

    public void stateChanged(ChangeEvent e)
    {
        if (e.getSource() == cutoffAdapter)
        {
            updateCutoff();
            return;
        }
        if (e.getSource() == resonanceAdapter)
        {
            updateResonance();
            return;
        }
        if (e.getSource() == slopeAdapter)
        {
            updateSlope();
            return;
        }
    }
}