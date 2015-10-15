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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Wraps all public methods wrapped by @WrapMethod
 * @author Enerccio
 *
 */
public class WrapAnnotationFactory extends WrapBaseFactory {
	private static final long serialVersionUID = -5142774589035715501L;

	@Retention(RetentionPolicy.RUNTIME)
	public static @interface WrapMethod {
		String[] value() default {};
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface WrapMethodNoConversion { }
	
	@Override
	public List<MethodData> getMethods(Object instance) {
		List<MethodData> ml = new ArrayList<>();
		for (Method m : instance.getClass().getMethods()){
			if (m.isAnnotationPresent(WrapMethod.class)) {
				if (m.getAnnotation(WrapMethod.class).value().length == 0)
					ml.add(new MethodData(m.getName(), m, false));
				else
					for (String name : m.getAnnotation(WrapMethod.class).value())
						ml.add(new MethodData(name, m, false));
			} else if (m.isAnnotationPresent(WrapMethodNoConversion.class)) {
				ml.add(new MethodData(m.getName(), m, true));
			}
		}
		return ml;
	}

}
