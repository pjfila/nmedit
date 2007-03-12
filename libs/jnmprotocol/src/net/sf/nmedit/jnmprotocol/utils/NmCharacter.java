/*
    Nord Modular Midi Protocol 3.03 Library
    Copyright (C) 2003-2006 Marcus Andersson

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

/*
 * Created on Jan 13, 2007
 */
package net.sf.nmedit.jnmprotocol.utils;

import java.util.Arrays;

public class NmCharacter
{

    /**
     * Returns true if the specified character is supported by the nord modular synthesizer.
     */
    public static boolean isCharacter(char c)
    {
        return c<charmap.length && charmap[c];
    }
    
    /**
     * Returns true if each of the characters is supported by the nord modular synthesizer.
     */
    public static boolean isValid(CharSequence cs)
    {
        for (int i=0;i<cs.length();i++)
            if (!isCharacter(cs.charAt(i)))
                return false;
        return true;
    }
    
    private static boolean[] charmap = new boolean[256];

    static
    {
        // set all to false
        Arrays.fill(charmap, false);
        // small letters
        Arrays.fill(charmap, 'a', 'z'+1, true);
        // big letters
        Arrays.fill(charmap, 'A', 'Z'+1, true);
        // digits
        Arrays.fill(charmap, '0', '9'+1, true);
        // other characters
        charmap[' ']=true;
        charmap['!']=true;
        charmap['"']=true;
        charmap['#']=true;
        charmap['$']=true;
        charmap['%']=true;
        charmap['&']=true;
        charmap['\'']=true;
        charmap['(']=true;
        charmap[')']=true;
        charmap['*']=true;
        charmap['+']=true;
        charmap[',']=true;
        charmap['-']=true;
        charmap['.']=true;
        charmap['/']=true;
        charmap[':']=true;
        charmap[';']=true;
        charmap['<']=true;
        charmap['=']=true;
        charmap['>']=true;
        charmap['?']=true;
        charmap[0x40]=true; // strange symbol 
        charmap['[']=true;
        charmap[0x5C]=true;// looks like (extended) ascii character 157
        charmap[']']=true;
        charmap['^']=true;
        charmap['_']=true;
        charmap[0x60]=true;// ? accon - not the a ???
        charmap['{']=true;
        charmap['|']=true;
        charmap['}']=true;
    }

}