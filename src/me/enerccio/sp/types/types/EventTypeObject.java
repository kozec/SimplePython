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
package me.enerccio.sp.types.types;

import java.util.Arrays;

import me.enerccio.sp.errors.TypeError;
import me.enerccio.sp.interpret.KwArgs;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.PythonEventObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.CastFailedException;
import me.enerccio.sp.utils.Coerce;

public class EventTypeObject extends TypeObject {
	private static final long serialVersionUID = -8515148185486184841L;
	public static final String EVENT_CALL = "event";
	
	@Override
	public String getTypeIdentificator() {
		return "event";
	}

	@Override
	public PythonObject call(TupleObject args, KwArgs kwargs){
		if (kwargs != null)
			kwargs.notExpectingKWArgs();	// Throws exception if there is kwarg defined 
		if (args.len() != 2)
			throw new TypeError("event() takes exactly 2 arguments");
		
		if (!(args.valueAt(0) instanceof StringObject))
			throw new TypeError("event() 1st argument has to be string");
		
		try {
			return new PythonEventObject(
				args.valueAt(0).toString(),
				Arrays.asList(Coerce.toJava(args.valueAt(1), String[].class))
			);
		} catch (CastFailedException e) {
			throw new TypeError("event() 2nd argument has to be list, tuple or iterable");
		}
	}
}
