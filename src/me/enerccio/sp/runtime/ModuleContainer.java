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
package me.enerccio.sp.runtime;

import java.util.Map;
import java.util.TreeMap;

import me.enerccio.sp.types.ModuleObject;

public class ModuleContainer {
	public ModuleObject module;
	public Map<String, ModuleObject> submodules = new TreeMap<String, ModuleObject>();
	public Map<String, ModuleContainer> subpackages = new TreeMap<String, ModuleContainer>();
}
