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
import java.util.Map;
import java.util.Set;

import me.enerccio.sp.errors.NameError;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.base.NoneObject;
import me.enerccio.sp.types.callables.JavaMethodObject;

/**
 * Environment object represents environment. Environment is responsible for fetching variable values
 * @author Enerccio
 *
 */
public class EnvironmentObject extends PythonObject {
	private static final long serialVersionUID = -4678903433798210010L;
	private List<InternalDict> environments = new ArrayList<InternalDict>();
	
	public EnvironmentObject(){
		super(true);
	}
	
	private EnvironmentObject(boolean internal){
		super(internal);
	}
	
	private transient EnvironmentObject pyClone;
	
	public EnvironmentObject cloneAsPython(){
		if (pyClone == null){
			synchronized (this){
				if (pyClone == null){
					pyClone = new EnvironmentObject(false);
					pyClone.environments = environments;
				}
			}
		}
		return pyClone;
	}
	
	/**
	 * Adds closure maps to the environment
	 * @param closures
	 */
	public void add(InternalDict... closures){
		environments.addAll(Arrays.asList(closures));
	}
	
	/**
	 * Adds closure maps to the environment
	 * @param closures
	 */
	public void add(Collection<InternalDict> closures){
		environments.addAll(closures);
	}
	
	public void prepend(InternalDict args) {
		environments.add(1, args);
	}

	/**
	 * Returns bound value to variable
	 * @param key name of the variable
	 * @param isGlobal is asking for global variable or not
	 * @param skipFirst whether to skip first closure (skip locals)
	 * @return value or null if none exists
	 */
	public PythonObject get(String key, boolean isGlobal, boolean skipFirst){
		int it = skipFirst ? 1 : 0;
		if (isGlobal)
			it = environments.size() > 1 ? environments.size()-2 : 0;
		
		PythonObject o;
		for (int i=it; i<environments.size(); i++){
			InternalDict e = environments.get(i);
			o = e.getVariable(key);
			if (o != null)
				return o;
		}
		return null;
	}
	
	/**
	 * Sets the variable key to the value. 
	 * @param key name of the variable
	 * @param value value to set
	 * @param isGlobal is asking for global variable or not
	 * @param skipFirst whether to skip first closure (skip locals)
	 * @return value or null if none exists
	 * @return
	 */
	public PythonObject set(String key, PythonObject value, boolean isGlobal, boolean skipFirst){
		if (isGlobal){
			environments.get(environments.size()-1).putVariable(key, value);
		}
		
		PythonObject o;
		for (InternalDict e : environments){
			if (skipFirst){
				skipFirst = false;
				continue;
			}
			o = e.getVariable(key);
			if (o != null){
				e.putVariable(key, value);
				return NoneObject.NONE;
			}
		}
		
		environments.get(0).putVariable(key, value);
		return NoneObject.NONE;
	}

	@Override
	public boolean truthValue() {
		return true;
	}

	@Override
	protected String doToString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<environment 0x");
		sb.append(Integer.toHexString(hashCode()));
		sb.append("\n");
		int pad = 2;
		for (InternalDict e : environments){
			for (int i=0; i<pad; i++)
				sb.append(" ");
			for (String k : e.keySet()) {
				sb.append(k);
				sb.append(" ");
			}
			sb.append("\n");
			pad += 2;
		}
		sb.append(">");
		return sb.toString(); 
	}

	/**
	 * Returns locals in this environment
	 * @return
	 */
	public InternalDict getLocals() {
		return environments.get(0);
	}
	
	public void pushLocals(InternalDict locals){
		environments.add(0, locals);
	}
	
	public PythonObject getBuiltin(String key){
		return environments.get(environments.size()-1).getVariable(key);
	}

	public List<InternalDict> toClosure() {
		return new ArrayList<InternalDict>(environments);
	}

	public void delete(String vname, boolean isGlobal) {
		if (environments.size() == 1){
			delete(vname, false);
			return;
		} else {
			if (isGlobal) {
				if (environments.get(0).containsVariable(vname)){
					environments.get(environments.size()-2).remove(vname);
					return;
				}
			} else {
				if (environments.get(0).containsVariable(vname)){
					environments.get(0).remove(vname);
					return;
				} 
			}
		}
		throw new NameError("name '" + vname.toString() + "' is not defined");
	}

	public InternalDict getGlobals() {
		if (environments.size() == 1)
			return environments.get(0);
		else
			return environments.get(environments.size()-2);
	}

	@Override
	public Set<String> getGenHandleNames() {
		return PythonObject.sfields.keySet();
	}

	@Override
	protected Map<String, JavaMethodObject> getGenHandles() {
		return PythonObject.sfields;
	}
}
