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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.mappings.MapObject;
import me.enerccio.sp.types.sequences.StringObject;

public class EnvironmentObject extends PythonObject {
	private static final long serialVersionUID = -4678903433798210010L;
	private List<MapObject> environments = new ArrayList<MapObject>();
	
	public void add(MapObject... closures){
		environments.addAll(Arrays.asList(closures));
	}
	
	public void add(Collection<MapObject> closures){
		environments.addAll(closures);
	}
	
	public PythonObject get(StringObject key, boolean isGlobal, boolean skipFirst){
		if (isGlobal){
			return environments.get(environments.size()-1).doGet(key);
		}
		
		PythonObject o;
		for (MapObject e : environments){
			if (skipFirst){
				skipFirst = false;
				continue;
			}
			o = e.doGet(key);
			if (o != null)
				return o;
		}
		return null;
	}
	
	public PythonObject set(StringObject key, PythonObject value, boolean isGlobal, boolean skipFirst){
		if (isGlobal){
			return environments.get(environments.size()-1).backingMap.put(key, value);
		}
		
		PythonObject o;
		for (MapObject e : environments){
			if (skipFirst){
				skipFirst = false;
				continue;
			}
			o = e.doGet(key);
			if (o != null){
				return e.backingMap.put(key, value);
			}
		}
		
		return environments.get(0).backingMap.put(key, value);
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<environment 0x" + Integer.toHexString(hashCode()) + ">"; 
	}

	public MapObject getLocals() {
		return environments.get(0);
	}
	
}
