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
package me.enerccio.sp.types.base;

import java.util.Map;
import java.util.Set;

import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.JavaMethodObject;

/**
 * Represents slice object. Can be created via slice() function
 * @author Enerccio
 *
 */
public class LabelObject extends PythonObject {
	private static final long serialVersionUID = 1865187439862L;
	private String name;
	private int position = -1;
	
	public LabelObject(String name, int position){
		super(false);
		this.name = name;
		this.position = position;
	}
	
	public LabelObject(String name){
		super(false);
		this.name = name;
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		return "<label '" + this.getName() + "' at 0x" + Integer.toHexString(position) + ">";
	}
	
	@Override
	public int hashCode() {
		return 0x151 + name.hashCode() + position;
	}

	@Override
	public Set<String> getGenHandleNames() {
		return PythonObject.sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return PythonObject.sfields;
	}

	/** Returns True if valid has position set. Returns False, if label was used only in goto statement */ 
	public boolean isValid() {
		return this.position >= 0;
	}
	
	public void set(String name, int position) {
		this.name = name;
		this.position = position;
	}

	public String getName() {
		return name;
	}

	public int getPosition() {
		return position;
	}
}
