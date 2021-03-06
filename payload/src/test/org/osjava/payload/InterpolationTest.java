/*
 * Copyright (c) 2003-2004, Henri Yandell
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or 
 * without modification, are permitted provided that the 
 * following conditions are met:
 * 
 * + Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * 
 * + Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 * 
 * + Neither the name of OSJava nor the names of its contributors 
 *   may be used to endorse or promote products derived from this software 
 *   without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package org.osjava.payload;

import java.util.Properties;

import junit.framework.*;

public class InterpolationTest extends TestCase {

    Interpolation interpolation = null;

    public void setUp() {
        this.interpolation = new Interpolation( PayloadConfiguration.DEFAULT );
    }

    public void tearDown() {
        this.interpolation = null;
    }

    public void testInterpolatable() {
        assertTrue( this.interpolation.interpolatable("xml") );
        assertTrue( this.interpolation.interpolatable("jcml") );
        assertTrue( this.interpolation.interpolatable("properties") );
        assertTrue( this.interpolation.interpolatable("txt") );
        assertTrue( this.interpolation.interpolatable("conf") );
        assertFalse( this.interpolation.interpolatable("sh") );
        assertFalse( this.interpolation.interpolatable("pl") );
        assertFalse( this.interpolation.interpolatable("") );
    }
    public void testInterpolate() {
        String before = "Fred ${BANG} ${FOO} Joe";
        Properties props = new Properties();
        props.setProperty("BANG", "boom");
        props.setProperty("foo", "fail");
        assertEquals( "Fred boom ${FOO} Joe", this.interpolation.interpolate(before, props) );
    }

}
