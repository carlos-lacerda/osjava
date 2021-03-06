/* 
 * org.osjava.threads.InvalidThreadParentException
 * 
 * $Id$
 * $Rev$
 * $Date$
 * $Author$
 * 
 * Created on Aug 01, 2002
 * 
 * Copyright (c) 2002-2004, Robert M. Zigweid
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * + Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *
 * + Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * + Neither the name of the OSJava-Threads nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
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

package org.osjava.threads;

/**
 * Exception to be thrown in the event that a Thread or ThreadGroup is added 
 * as a child to an ExtendedThreadGroup that is not its parent
 * 
 * @author Robert M. Zigweid
 * @version $Revision: 1.1 $ $Date$
 */
public class InvalidRunnableException extends Exception {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 1L;

    /** 
     * Construction of the Exception with the default message
     */
    public InvalidRunnableException() {
        super(
            "Attempted to add Thread or ThreadGroup as child to a ThreadGroup "
                + "that is not the parent.");
    }

    /** 
     * Construction of the Exception with the specified message <code>
     * message</code>
     * 
     * @param message the String that assigns the message that will be used
     *                in the exception's message.
     */
    public InvalidRunnableException(String message) {
        super(message);
    }
}
