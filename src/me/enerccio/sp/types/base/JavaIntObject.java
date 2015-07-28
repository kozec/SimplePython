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

import java.math.BigInteger;

import me.enerccio.sp.types.PythonObject;

/**
 * fixnum represented by standard integer.
 * Used only if IntObject.USE_JAVAINT is set to true.
 */
public class JavaIntObject extends NumberObject {
	private static final long serialVersionUID = 42L;
	
	JavaIntObject(int v){
		value = v;
		newObject();
	}
	
	JavaIntObject(long v){
		value = (int) v;
		newObject();
	}
	
	JavaIntObject(BigInteger v){
		value = v.intValue();
		newObject();
	}
	
	private int value;

	@Override
	public boolean truthValue() {
		return value != 0;
	}
	
	@Override
	public int getId(){
		return value;
	}

	@Override 
	public int intValue() {
		return value;
	}

	@Override
	public double doubleValue() {
		return value;
	}

	@Override
	protected PythonObject getIntValue() {
		return this;
	}
	
	@Override
	public int hashCode(){
		return value;
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof JavaIntObject)
			return value == ((JavaIntObject)o).value;
		return false;
	}

	@Override
	protected String doToString() {
		return Integer.toString(value);
	}
}
