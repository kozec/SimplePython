package me.enerccio.sp.types.base;

import me.enerccio.sp.types.AccessRestrictions;
import me.enerccio.sp.types.AugumentedPythonObject;
import me.enerccio.sp.types.PythonObject;
import me.enerccio.sp.utils.Utils;

public class ComplexObject extends NumberObject {
	private static final long serialVersionUID = 9L;
	private static final String REAL_ACCESSOR = "real";
	private static final String IMAG_ACCESSOR = "imag";
	
	public ComplexObject(double r, double i){
		this(new RealObject(r), new RealObject(i));
	}
	
	public ComplexObject(RealObject r, RealObject i) {
		fields.put(REAL_ACCESSOR, new AugumentedPythonObject(r, AccessRestrictions.PUBLIC));
		fields.put(IMAG_ACCESSOR, new AugumentedPythonObject(i, AccessRestrictions.PUBLIC));
	}

	@Override
	public boolean truthValue() {
		return Utils.get(this, REAL_ACCESSOR).truthValue();
	}

	@Override
	protected PythonObject getIntValue() {
		return Utils.get(this, REAL_ACCESSOR);
	}

}
