package me.enerccio.sp.types.base;

import me.enerccio.sp.types.PythonObject;

public class BoolObject extends NumberObject {
	private static final long serialVersionUID = 7L;
	
	public static final BoolObject TRUE = new BoolObject(true);
	public static final BoolObject FALSE = new BoolObject(false);
	
	private final boolean value;
	private BoolObject(boolean v){
		this.value = v;
	}
	
	@Override
	public boolean truthValue() {
		return value;
	}

	public static PythonObject fromBoolean(Boolean ret) {
		return ret ? TRUE : FALSE;
	}

	@Override
	protected PythonObject getIntValue() {
		return value ? new IntObject(1) : new IntObject(0);
	}

}
