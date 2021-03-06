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
package me.enerccio.sp.utils;

/**
 * Thrown when method is incompatible for the arguments or number of arguments
 * @author Enerccio
 *
 */
@SuppressWarnings("serial")
public class PointerMethodIncompatibleException extends Exception {

	public PointerMethodIncompatibleException(String string) {
		super(string);
	}

	public PointerMethodIncompatibleException(String string, Throwable cause) {
		super(string, cause);
	}

}
