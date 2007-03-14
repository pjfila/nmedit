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
 * Created on Dec 29, 2006
 */
package net.sf.nmedit.jsynth;

import java.beans.PropertyChangeListener;

import net.sf.nmedit.jsynth.event.SlotListener;
import net.sf.nmedit.jsynth.worker.SendPatchWorker;

public interface Slot
{

    Synthesizer getSynthesizer();
    
    public int getSlotIndex();

    String getName();
    
    String getPatchName();
    
    SendPatchWorker createSendPatchWorker();
    
   //Object getPatch();
    
    void addSlotListener(SlotListener l);
    void removeSlotListener(SlotListener l);

    void addPropertyChangeListener(PropertyChangeListener l);
    void addPropertyChangeListener(String propertyName, PropertyChangeListener l);
    void removePropertyChangeListener(PropertyChangeListener l);
    void removePropertyChangeListener(String propertyName, PropertyChangeListener l);
    
}
