package me.enerccio.sp.types.types;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.types.callables.ClassObject;
import me.enerccio.sp.utils.Utils;

public abstract class TypeObject extends ClassObject {
	private static final long serialVersionUID = 5891250487396458462L;
	
	public abstract String getTypeIdentificator();
	
	@Override
	public PythonObject set(String key, PythonObject localContext,
			PythonObject value) {
		if (!fields.containsKey(key))
			throw Utils.throwException("AttributeError", "'" + 
					Utils.run("str", Utils.run("type", this)) + "' object has no attribute '" + key + "'");
		throw Utils.throwException("AttributeError", "'" + 
				Utils.run("str", Utils.run("type", this)) + "' object attribute '" + key + "' is read only");
	}

	@Override
	public void create(String key, AccessRestrictions restrictions) {
		
	}
	
	@Override
	protected String doToString() {
		return "<type " + getTypeIdentificator() + ">";
	}

}
