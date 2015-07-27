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

import java.util.ArrayList;
import java.util.List;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.TupleObject;
import me.enerccio.sp.utils.Utils;

public class DictTypeObject extends TypeObject {
	private static final long serialVersionUID = -3923399715218968304L;
	public static final String DICT_CALL = "dict";

	@Override
	public String getTypeIdentificator() {
		return "dict";
	}

	@Override
	public PythonObject call(TupleObject args) {
		if (args.len() == 0)
			return new MapObject();
		
		if (args.len() == 1){
			if (args.getObjects()[0] instanceof MapObject)
				return ((MapObject) args.getObjects()[0]).cloneMap();
			
			try {
				if (args.getObjects()[0] instanceof ListObject){
					ListObject o = (ListObject)args.getObjects()[0];
					List<TupleObject> tuples = new ArrayList<TupleObject>();
					for (PythonObject oo : o.objects)
						tuples.add((TupleObject) oo);
					
					MapObject m = new MapObject();
					for (TupleObject to : tuples)
						if (to.len() != 2)
							throw Utils.throwException("TypeError", "dict(): parameter 1 must be list of tuples with two elements");
						else
							m.backingMap.put(to.getObjects()[0], to.getObjects()[1]);
					return m;
				}
			} catch (ClassCastException e){
				throw Utils.throwException("TypeError", "dict(): parameter 1 must be list of tuples with two elements");
			}
			throw Utils.throwException("TypeError", "dict(): parameter 1 must be list of tuples with two elements");
		}
		
		throw Utils.throwException("TypeError", "dict(): wrong mumber of parameters, expected 1 or 0, got " + args.len());
	}

	
}