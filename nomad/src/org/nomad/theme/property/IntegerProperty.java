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
 * Created on Mar 16, 2006
 */
package org.nomad.theme.property;

import org.nomad.theme.component.NomadComponent;
import org.nomad.theme.property.editor.Editor;
import org.nomad.theme.property.editor.TextEditor;

public abstract class IntegerProperty extends Property
{

    public IntegerProperty( String name )
    {
        super( name );
    }

    @Override
    public Editor newEditor( NomadComponent component )
    {
        return new TextEditor( this, component );
    }

}
