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
import java.util.ArrayList;
import java.util.List;

/**
 * Wraps all public methods of the object
 * @author Enerccio
 *
 */
public class WrapPublicFactory extends WrapBaseFactory implements PointerFactory {
	private static final long serialVersionUID = 693487950048251692L;

	@Override
	protected List<Method> getMethods(Object instance) {
		List<Method> ml = new ArrayList<Method>();
		for (Method m : instance.getClass().getMethods()){
			ml.add(m);
		}
		return ml;
	}
}
