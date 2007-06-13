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
package net.sf.nmedit.jpatch;

public interface PModuleMetrics
{

    int getScreenHeight(PModuleDescriptor module);
    int getScreenHeight(PModule module);
    int getScreenWidth(PModuleDescriptor module);
    int getScreenWidth(PModule module);

    int getInternalHeight(PModuleDescriptor module);
    int getInternalHeight(PModule module);
    int getInternalWidth(PModuleDescriptor module);
    int getInternalWidth(PModule module);

    int internalToScreenX(int x);
    int internalToScreenY(int y);
    int screenToInternalX(int x);
    int screenToInternalY(int y);

    int getMaxScreenX();
    int getMaxScreenY();
    int getMaxInternalX();
    int getMaxInternalY();
    int alignScreenX(int x);
    int alignScreenY(int y);
    
}