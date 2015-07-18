package me.enerccio.sp.types;

import me.enerccio.sp.interpret.PythonInterpret;
import me.enerccio.sp.types.base.ClassInstanceObject;
import me.enerccio.sp.types.base.ComplexObject;
import me.enerccio.sp.types.base.IntObject;
import me.enerccio.sp.types.base.RealObject;
import me.enerccio.sp.types.sequences.ListObject;
import me.enerccio.sp.types.sequences.StringObject;
import me.enerccio.sp.utils.Utils;

public final class Arithmetics {

	public static final String __ADD__ = "__add__";
	public static final String __SUB__ = "__sub__";
	public static final String __MUL__ = "__mul__";
	public static final String __DIV__ = "__div__";
	public static final String __MOD__ = "__mod__";
	public static final String __POW__ = "__pow__";
	public static final String __AND__ = "__and__";
	public static final String __OR__  = "__or__";
	public static final String __NOT__ = "__not__";
	public static final String __XOR__ = "__xor__";
	public static final String __LSHIFT__ = "__lshift__";
	public static final String __RSHIFT__ = "__rshift__";
	
	
	public static PythonObject doOperator(PythonObject a, PythonObject b, String m) {
		if (a instanceof ClassInstanceObject){
			if (b == null)
				return PythonInterpret.interpret.get().execute(false, Utils.get(a, m));
			else
				return PythonInterpret.interpret.get().execute(false, Utils.get(a, m), b);
		}
		
		if (a instanceof IntObject){
			return doOperatorInt((IntObject)a, b, m);
		}
		
		if (a instanceof RealObject){
			return doOperatorFloat((RealObject)a, b, m);
		}
		
		if (a instanceof ComplexObject){
			return doOperatorComplex((ComplexObject)a, b, m);
		}
		
		if (a instanceof StringObject){
			// TODO
		}
		
		if (a instanceof ListObject){
			// TODO
		}
		
		if (b != null)
			throw Utils.throwException("TypeError", "Unknown operation " + m + " of types '" + Utils.run("type", a) + "' and '" + Utils.run("type", b) + "'");
		else
			throw Utils.throwException("TypeError", "Unknown operation " + m + " of type '" + Utils.run("type", a) + "'");
	}
	
	private static PythonObject doOperatorInt(IntObject a, PythonObject b,
			String m) {
		boolean isInt = b instanceof IntObject;
		boolean isFloat = b instanceof RealObject;
		boolean isComplex = b instanceof ComplexObject;
		
		if (isInt || isFloat || isComplex){
			switch (m){
			case __ADD__:
				if (isInt){
					return new IntObject(a.intValue() + ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject((double)a.intValue() + ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject((double)a.intValue() + ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __SUB__:
				if (isInt){
					return new IntObject(a.intValue() - ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject((double)a.intValue() - ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject((double)a.intValue() - ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __MUL__:
				if (isInt){
					return new IntObject(a.intValue() * ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject((double)a.intValue() * ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject((double)a.intValue() * ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __DIV__:
				if (isInt){
					return new IntObject(a.intValue() / ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject((double)a.intValue() / ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject((double)a.intValue() / ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __MOD__:
				if (isInt){
					return new IntObject(a.intValue() % ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject((double)a.intValue() % ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject((double)a.intValue() % ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __AND__:
				if (isInt){
					return new IntObject(a.intValue() & ((IntObject) b).intValue());
				}
				if (isFloat){
					
				}
				if (isComplex){
					
				}
			case __OR__:
				if (isInt){
					return new IntObject(a.intValue() | ((IntObject) b).intValue());
				}
				if (isFloat){
					
				}
				if (isComplex){
					
				}
			case __XOR__:
				if (isInt){
					return new IntObject(a.intValue() ^ ((IntObject) b).intValue());
				}
				if (isFloat){
					
				}
				if (isComplex){
					
				}
			case __NOT__:
				if (isInt){
					return new IntObject(~a.intValue());
				}
			case __POW__:
				if (isInt){
					return new IntObject((int) Math.pow(a.intValue(),  ((IntObject) b).intValue()));
				}
				if (isFloat){
					return new RealObject(Math.pow((double)a.intValue(), ((RealObject) b).doubleValue()));
				}
				if (isComplex){
					return new ComplexObject(Math.pow((double)a.intValue(), ((ComplexObject) b).getRealPart()), ((ComplexObject) b).getImagPart());
				}
			case __RSHIFT__:
				if (isInt){
					return new IntObject(a.intValue() >> ((IntObject) b).intValue());
				}
				if (isFloat){
					
				}
				if (isComplex){
					
				}
			case __LSHIFT__:
				if (isInt){
					return new IntObject(a.intValue() << ((IntObject) b).intValue());
				}
				if (isFloat){
					
				}
				if (isComplex){
					
				}
			}
		}
		
		if (b != null)
			throw Utils.throwException("TypeError", "Unknown operation " + m + " of types '" + Utils.run("type", a) + "' and '" + Utils.run("type", b) + "'");
		else
			throw Utils.throwException("TypeError", "Unknown operation " + m + " of type '" + Utils.run("type", a) + "'");
	}
 
	private static PythonObject doOperatorFloat(RealObject a, PythonObject b,
			String m) {
		boolean isInt = b instanceof IntObject;
		boolean isFloat = b instanceof RealObject;
		boolean isComplex = b instanceof ComplexObject;
		
		if (isInt || isFloat || isComplex){
			switch (m){
			case __ADD__:
				if (isInt){
					return new RealObject(a.doubleValue() + ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject(a.doubleValue() + ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject(a.doubleValue() + ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __SUB__:
				if (isInt){
					return new RealObject(a.doubleValue() - ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject(a.doubleValue() - ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject(a.doubleValue() - ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __MUL__:
				if (isInt){
					return new RealObject(a.doubleValue() * ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject(a.doubleValue() * ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject(a.doubleValue() * ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __DIV__:
				if (isInt){
					return new RealObject(a.doubleValue() / ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject(a.doubleValue() / ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject(a.doubleValue() / ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __MOD__:
				if (isInt){
					return new RealObject(a.doubleValue() % ((IntObject) b).intValue());
				}
				if (isFloat){
					return new RealObject(a.doubleValue() % ((RealObject) b).doubleValue());
				}
				if (isComplex){
					return new ComplexObject(a.doubleValue() % ((ComplexObject) b).getRealPart(), ((ComplexObject) b).getImagPart());
				}
			case __AND__:
				if (isInt){
					
				}
				if (isFloat){
					
				}
				if (isComplex){
					
				}
			case __OR__:
				if (isInt){
					
				}
				if (isFloat){
					
				}
				if (isComplex){
					
				}
			case __XOR__:
				if (isInt){
					
				}
				if (isFloat){
					
				}
				if (isComplex){
					
				}
			case __NOT__:
				if (isInt){
					
				}
				if (isFloat){
					
				}
				if (isComplex){
					
				}
			case __POW__:
				if (isInt){
					return new RealObject(Math.pow(a.doubleValue(), ((IntObject) b).intValue()));
				}
				if (isFloat){
					return new RealObject(Math.pow(a.doubleValue(), ((RealObject) b).doubleValue()));
				}
				if (isComplex){
					return new ComplexObject(Math.pow(a.doubleValue(), ((ComplexObject) b).getRealPart()), ((ComplexObject) b).getImagPart());
				}
			case __RSHIFT__:
				if (isInt){
					
				}
				if (isFloat){
					
				}
				if (isComplex){
					
				}
			case __LSHIFT__:
				if (isInt){
					
				}
				if (isFloat){
					
				}
				if (isComplex){
					
				}
			}
		}
		
		if (b != null)
			throw Utils.throwException("TypeError", "Unknown operation " + m + " of types '" + Utils.run("type", a) + "' and '" + Utils.run("type", b) + "'");
		else
			throw Utils.throwException("TypeError", "Unknown operation " + m + " of type '" + Utils.run("type", a) + "'");
	}
	
	private static PythonObject doOperatorComplex(ComplexObject a,
			PythonObject b, String m) {
		boolean isInt = b instanceof IntObject;
		boolean isFloat = b instanceof RealObject;
		boolean isComplex = b instanceof ComplexObject;
		
		if (isInt || isFloat || isComplex){
			switch (m){
			case __ADD__:
				if (isInt){
					return new ComplexObject(a.getRealPart() + ((IntObject)b).intValue(), a.getImagPart());
				}
				if (isFloat){
					return new ComplexObject(a.getRealPart() + ((RealObject)b).doubleValue(), a.getImagPart());
				}
				if (isComplex){
					return new ComplexObject(a.getRealPart() + ((ComplexObject)b).getRealPart(), 
							a.getImagPart() + ((ComplexObject)b).getImagPart());
				}
			case __SUB__:
				if (isInt){
					return new ComplexObject(a.getRealPart() - ((IntObject)b).intValue(), a.getImagPart());
				}
				if (isFloat){
					return new ComplexObject(a.getRealPart() - ((RealObject)b).doubleValue(), a.getImagPart());
				}
				if (isComplex){
					return new ComplexObject(a.getRealPart() - ((ComplexObject)b).getRealPart(), 
							 a.getImagPart() - ((ComplexObject)b).getImagPart());
				}
			case __MUL__:
				if (isInt){
					return new ComplexObject(a.getRealPart() * ((IntObject)b).intValue(), a.getImagPart());
				}
				if (isFloat){
					return new ComplexObject(a.getRealPart() * ((RealObject)b).doubleValue(), a.getImagPart());
				}
				if (isComplex){
					return new ComplexObject(a.getRealPart() * ((ComplexObject)b).getRealPart(), 
							 a.getImagPart() * ((ComplexObject)b).getImagPart());
				}
			case __DIV__:
				if (isInt){
					return new ComplexObject(a.getRealPart() / ((IntObject)b).intValue(), a.getImagPart());
				}
				if (isFloat){
					return new ComplexObject(a.getRealPart() / ((RealObject)b).doubleValue(), a.getImagPart());
				}
				if (isComplex){
					return new ComplexObject(a.getRealPart() / ((ComplexObject)b).getRealPart(), 
							 a.getImagPart() / ((ComplexObject)b).getImagPart());
				}
			case __MOD__:
				if (isInt){
					return new ComplexObject(a.getRealPart() % ((IntObject)b).intValue(), a.getImagPart());
				}
				if (isFloat){
					return new ComplexObject(a.getRealPart() % ((RealObject)b).doubleValue(), a.getImagPart());
				}
				if (isComplex){
					return new ComplexObject(a.getRealPart() % ((ComplexObject)b).getRealPart(), 
							 a.getImagPart() % ((ComplexObject)b).getImagPart());
				}
			case __AND__:
				if (isInt){
					
				}
				if (isFloat){
					
				}
				if (isComplex){
					
				}
			case __OR__:
				if (isInt){
					
				}
				if (isFloat){
					
				}
				if (isComplex){
					
				}
			case __XOR__:
				if (isInt){
					
				}
				if (isFloat){
					
				}
				if (isComplex){
					
				}
			case __NOT__:
				if (isInt){
					
				}
				if (isFloat){
					
				}
				if (isComplex){
					
				}
			case __POW__:
				if (isInt){
					return new ComplexObject(Math.pow(a.getRealPart(), ((IntObject)b).intValue()), a.getImagPart());
				}
				if (isFloat){
					return new ComplexObject(Math.pow(a.getRealPart(), ((RealObject)b).doubleValue()), a.getImagPart());
				}
				if (isComplex){
					return new ComplexObject(Math.pow(a.getRealPart(), ((ComplexObject)b).getRealPart()), 
							 Math.pow(a.getImagPart(), ((ComplexObject)b).getImagPart()));
				}
			case __RSHIFT__:
				if (isInt){
					
				}
				if (isFloat){
					
				}
				if (isComplex){
					
				}
			case __LSHIFT__:
				if (isInt){
					
				}
				if (isFloat){
					
				}
				if (isComplex){
					
				}
			}
		}
		
		if (b != null)
			throw Utils.throwException("TypeError", "Unknown operation " + m + " of types '" + Utils.run("type", a) + "' and '" + Utils.run("type", b) + "'");
		else
			throw Utils.throwException("TypeError", "Unknown operation " + m + " of type '" + Utils.run("type", a) + "'");
	}
	
}