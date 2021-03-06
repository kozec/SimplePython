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
package me.enerccio.sp.types.pointer;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.enerccio.sp.types.callables.JavaCongruentAggregatorObject;
import me.enerccio.sp.types.callables.JavaMethodObject;
import me.enerccio.sp.utils.Utils;

/**
 * Base wrapping factory. Handles wrapping methods into JavaCongruentAggregatorObjects. 
 * Which methods to wrap are decided by the getMethods method.
 * @author Enerccio
 *
 */
public abstract class WrapBaseFactory implements PointerFactory {
	private static final long serialVersionUID = -4111009373007823950L;
	
	private static Map<String, List<MethodData>> cache = Collections.synchronizedMap(new HashMap<String, List<MethodData>>());

	public static class MethodData {
		final String name;
		final Method method;
		final boolean noTypeConversion;
		
		public MethodData(String name, Method method, boolean noTypeConversion) {
			this.name = name;
			this.method = method;
			this.noTypeConversion = noTypeConversion;
		}
	}
	
	@Override
	public final PointerObject doInitialize(Object instance) {
		PointerObject o = new PointerObject(instance);
		
		if (!cache.containsKey(instance.getClass().getCanonicalName()))
			synchronized (cache){
				if (!cache.containsKey(instance.getClass().getCanonicalName())){
					List<MethodData> ml = getMethods(instance);
					cache.put(instance.getClass().getCanonicalName(), ml);
				}
			}
		
		synchronized (cache){
			Map<String, JavaCongruentAggregatorObject> mm = new HashMap<String, JavaCongruentAggregatorObject>();
			
			for (MethodData m : cache.get(instance.getClass().getCanonicalName())){
				if (!mm.containsKey(m.name)){
					JavaCongruentAggregatorObject co = new JavaCongruentAggregatorObject(m.name);
					mm.put(m.name, co);
				}
				JavaMethodObject jm = new JavaMethodObject(instance, m.method, m.noTypeConversion);
				mm.get(m.name).methods.add(jm);
			}
			
			for (String name : mm.keySet()){
				Utils.putPublic(o, name, mm.get(name));
			}
		}
		
		return o;
	}

	protected abstract List<MethodData> getMethods(Object instance);
}
