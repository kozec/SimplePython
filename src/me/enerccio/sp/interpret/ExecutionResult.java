/*
 * SimplePython - embeddable python interpret in java
 * Copyright (c) Peter Vanusanik, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package me.enerccio.sp.interpret;

/**
 * Every interpret step will return this enum to show how interpretation ended
 * @author Enerccio
 *
 */
public enum ExecutionResult {
	
	/** No problems */
	OK, 
	/** No remaining bytecode in frame but also no frames on frame stack */
	FINISHED, 
	/** java interrupt happened */
	INTERRUPTED, 
	/** No remaining bytecode in frame */
	EOF
	
}
