/* Copyright (C) 2006-2007 Julien Pauty
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
package net.sf.nmedit.patchmodifier.randomizer;

import java.util.List;

import net.sf.nmedit.jpatch.PParameter;

public class SimpleRandomizerAlgorithm implements RandomizerAlgorithm 
{
    public void randomize(List<PParameter> parameterList)
    {
        for (PParameter p: parameterList)
        {
            double normalizedValue = p.getDoubleValue();
            normalizedValue = normalizedValue*Math.random();
            p.setDoubleValue(normalizedValue);
        }
    }	
}
