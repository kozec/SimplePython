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
 * fixnum represented by big integer
 * @author Enerccio
 *
 */
public class IntObject extends NumberObject {
	private static final long serialVersionUID = 6L;
	/** 
	 * If false, BigInteger-based integers are used, allowing greater range at cost of
	 * slower execution.
	 */ 
	public static boolean USE_JAVAINT = true;	
	public static int BASE_MAP_SIZE = 1024;
	
	private static NumberObject[] baseMap = new NumberObject[BASE_MAP_SIZE];
	public static int HALF_BASE_MAP_SIZE = BASE_MAP_SIZE / 2;
	static {
		if (USE_JAVAINT)
			for (int i=0; i<BASE_MAP_SIZE; i++)
				baseMap[i] = new JavaIntObject(i-HALF_BASE_MAP_SIZE);
		else
			for (int i=0; i<BASE_MAP_SIZE; i++)
				baseMap[i] = new IntObject(i-HALF_BASE_MAP_SIZE);
	}
	
	public static NumberObject valueOf(int v){
		if (v+HALF_BASE_MAP_SIZE < BASE_MAP_SIZE && v+HALF_BASE_MAP_SIZE > 0)
			return baseMap[v+HALF_BASE_MAP_SIZE];
		if (USE_JAVAINT)
			return new JavaIntObject(v);
		return new IntObject(v);
	}
	
	public static NumberObject valueOf(long v){
		if (v+HALF_BASE_MAP_SIZE < BASE_MAP_SIZE && v+HALF_BASE_MAP_SIZE > 0)
			return baseMap[(int) (v+HALF_BASE_MAP_SIZE)];
		if (USE_JAVAINT)
			return new JavaIntObject(v);
		return new IntObject(v);
	}
	
	public static NumberObject valueOf(BigInteger v){
		if (USE_JAVAINT)
			return new JavaIntObject(v.intValue());
		return new IntObject(v);
	}
	
	private IntObject(int v){
		value = BigInteger.valueOf(v);
		newObject();
	}
	
	private IntObject(long v){
		value = BigInteger.valueOf(v);
		newObject();
	}
	
	private IntObject(BigInteger v){
		value = v;
		newObject();
	}
	
	private BigInteger value;

	@Override
	public boolean truthValue() {
		return !value.equals(BigInteger.ZERO);
	}
	
	@Override 
	public int intValue() {
		return value.intValue();
	}
	
	@Override
	public double doubleValue() {
		return value.doubleValue();
	}
	
	@Override
	public int getId(){
		return value.hashCode();
	}

	public long longValue() {
		return value.longValue();
	}

	@Override
	protected PythonObject getIntValue() {
		return this;
	}
	
	@Override
	public int hashCode(){
		return value.hashCode();
	}
	
	@Override
	public boolean equals(Object o){
		if (o instanceof IntObject)
			return value.equals(((IntObject)o).value);
		return false;
	}

	@Override
	protected String doToString() {
		return value.toString();
	}

	public static boolean isInt(PythonObject b) {
		if (USE_JAVAINT) 
			return b instanceof JavaIntObject;
		return b instanceof IntObject;
	}
}
