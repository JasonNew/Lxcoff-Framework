package com.andymcsherry.library;

import java.util.ArrayList;

import org.jason.lxcoff.lib.ExecutionController;

public class MathHelper {
	public static OffMath math;
	
	public static double integrate(String fun, String v1, String v2, String[] bounds){
		math.setBounds(bounds);
		double result = math.integrate(fun, v1, v2);
		
		return result;
	}
}
