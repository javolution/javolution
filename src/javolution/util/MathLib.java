/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2004 - The Javolution Team (http://javolution.org/)
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import java.util.Random;

/**
 * <p> This utility class ensures cross-platform portability of the math 
 *     library. Functions not supported by the platform are emulated.
 *     Developers may replace the current implementation with native
 *     implementations for faster execution.<p> 
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.1, November 6, 2004
 */
public final class MathLib {

    /**
     * Default constructor.
     */
    private MathLib() {}

    /**
     * The natural logarithm.
     */
    public static final double E = 2.71828182845904523536028747135266;
    
    /**
     * The ratio of the circumference of a circle to its diameter.
     */
    public static final double PI = 3.1415926535897932384626433832795;

    /**
     * Half the ratio of the circumference of a circle to its diameter.
     */
    public static final double HALF_PI = 1.5707963267948966192313216916398;

    /**
     * Twice the ratio of the circumference of a circle to its diameter.
     */
    public static final double TWO_PI = 6.283185307179586476925286766559;
    
    /**
     * The natural logarithm of two.
     */
    public static final double LOG2 = 0.69314718055994530941723212145818;
    
    /**
     * The natural logarithm of ten.
     */
    public static final double LOG10 = 2.3025850929940456840179914546844;
    
    /**
     * The square root of two.
     */
    public static final double SQRT2 = 1.4142135623730950488016887242097;
    
    /**
     * Not-A-Number.
     */
    public static final double NaN = 0.0 / 0.0;

    /**
     * Not-A-Number.
     */
    public static final double Infinity = 1.0 / 0.0;

    /**
     * Converts an angle in degrees to radians.
     *
     * @param degrees the angle in degrees.
     * @return the specified angle in radians.
     */
    public static double toRadians(double degrees) {
	    return degrees * (PI / 180.0);
    }

    /**
     * Converts an angle in radians to degrees.
     *
     * @param radians the angle in radians.
     * @return the specified angle in degrees.
     */
    public static double toDegrees(double radians) {
	     return radians * (180.0 / PI);
    }

    /**
     * Returns the positive square root of the specified value.
     * 
     * @param x the value.
     * @return <code>java.lang.Math.sqrt(x)</code>
     */
    public static double sqrt(double x) {
	    return Math.sqrt(x); // CLDC 1.1
    }

    /**
     * Returns the remainder of the division of the specified two arguments.
     *
     * @param x the dividend.
     * @param y the divisor.
     * @return <code>x - round(x / y) * y</code>
     */
    public static double rem(double x, double y) {
        double tmp = x / y;
        if (MathLib.abs(tmp) <= Long.MAX_VALUE) { 
            return x - MathLib.round(tmp) * y;
        } else {
            return NaN;
        }
    }

    /**
     * Returns the smallest (closest to negative infinity) 
     * <code>double</code> value that is not less than the argument and is 
     * equal to a mathematical integer.
     *
     * @param x the value.
     * @return <code>java.lang.Math.ceil(x)</code>
     */
    public static double ceil(double x) {
	    return Math.ceil(x); // CLDC 1.1
    }

    /**
     * Returns the largest (closest to positive infinity) 
     * <code>double</code> value that is not greater than the argument and 
     * is equal to a mathematical integer.
     *
     * @param x the value.
     * @return <code>java.lang.Math.ceil(x)</code>
     */
    public static double floor(double x) {
	    return Math.floor(x); // CLDC 1.1
    }
    
    /**
     * Returns the trigonometric sine of the specified angle in radians.
     * 
     * @param radians the angle in radians.
     * @return <code>java.lang.Math.sin(radians)</code>
     */
    public static double sin(double radians) {
	    return Math.sin(radians); // CLDC 1.1
    }
    
    /**
     * Returns the trigonometric cosine of the specified angle in radians.
     * 
     * @param radians the angle in radians.
     * @return <code>java.lang.Math.cos(radians)</code>
     */
    public static double cos(double radians) {
	    return Math.cos(radians); // CLDC 1.1
    }
    
    /**
     * Returns the trigonometric tangent of the specified angle in radians.
     * 
     * @param radians the angle in radians.
     * @return <code>java.lang.Math.tan(radians)</code>
     */
    public static double tan(double radians) {
	    return Math.tan(radians); // CLDC 1.1
    }
    
    /**
     * Returns the arc sine of the specified value, 
     * in the range of -<i>pi</i>/2 through <i>pi</i>/2. 
     *
     * @param x the value whose arc sine is to be returned.
     * @return the arc sine in radians for the specified value.
     */
    public static double asin(double x) {
        if (x < -1.0 || x > 1.0) return MathLib.NaN;
        if (x == -1.0) return - HALF_PI;
        if (x == 1.0) return HALF_PI;
        return MathLib.atan(x / MathLib.sqrt(1.0 - x * x));
    }

    /**
     * Returns the arc cosine of the specified value,
     * in the range of 0.0 through <i>pi</i>. 
     *
     * @param x the value whose arc cosine is to be returned.
     * @return the arc cosine in radians for the specified value.
     */
    public static double acos(double x) {
	    return HALF_PI - MathLib.asin(x);
    }

    /**
     * Returns the arc tangent of the specified value,
     * in the range of -<i>pi</i>/2 through <i>pi</i>/2.  
     *
     * @param x the value whose arc tangent is to be returned.
     * @return the arc tangent in radians for the specified value.
     * @see <a href="http://mathworld.wolfram.com/InverseTangent.html">
     *      Inverse Tangent -- from MathWorld</a> 
     */
    public static double atan(double x) {
        return MathLib._atan(x);
    }
    
    /**
     * Returns the angle theta such that
     * <code>(x == cos(theta)) && (y == sin(theta))</code>. 
     *
     * @param y the y value.
     * @param x the x value.
     * @return the angle theta in radians.
     */
    public static double atan2(double y, double x) {
        final double epsilon = 1E-128;
        if (MathLib.abs(x) > epsilon) {
            double temp = MathLib.atan(MathLib.abs(y) / MathLib.abs(x));
            if( x < 0.0 ) temp = PI - temp;
            if( y < 0.0 ) temp = TWO_PI - temp;
            return temp;
        } else if( y >  epsilon) {
            return HALF_PI;
        } else if( y < -epsilon) {
            return 3 * HALF_PI;
        } else {
            return 0.0; 
        }
    }
    
    /**
     * Returns the hyperbolic sine of x.
     * 
     * @param x the value for which the hyperbolic sine is calculated.
     * @return <code>(exp(x) - exp(-x)) / 2</code>
     */
    public static double sinh(double x) {
	    return (MathLib.exp(x) - MathLib.exp(-x)) * 0.5;
    }
    
    /**
     * Returns the hyperbolic cosine of x.
     * 
     * @param x the value for which the hyperbolic cosine is calculated.
     * @return <code>(exp(x) + exp(-x)) / 2</code>
     */
    public static double cosh(double x) {
	    return (MathLib.exp(x) + MathLib.exp(-x)) * 0.5;
    }
    
    /**
     * Returns the hyperbolic tangent of x.
     * 
     * @param x the value for which the hyperbolic tangent is calculated.
     * @return <code>(exp(2 * x) - 1) / (exp(2 * x) + 1)</code>
     */
    public static double tanh(double x) {
	    return (MathLib.exp(2 * x) - 1) / (MathLib.exp(2 * x) + 1);
    }
    

    /**
     * Returns <i>{@link #E e}</i> raised to the specified power.
     *
     * @param x the exponent.
     * @return <code><i>e</i><sup>x</sup></code>
     * @see <a href="http://mathworld.wolfram.com/ExponentialFunction.html">
     *      Exponential Function -- from MathWorld</a> 
     */
    public static double exp(double x) {
        return MathLib._ieee754_exp(x);
    }

    /**
     * Returns the natural logarithm (base <i>{@link #E e}</i>) of the specified
     * value.
     *
     * @param x the value greater than <code>0.0</code>.
     * @return the value y such as <code><i>e</i><sup>y</sup> == x</code>
     */
    public static double log(double x) {
        return MathLib._ieee754_log(x);
    }
    
    /**
     * Returns the value of the first argument raised to the power of the
     * second argument. 
     *
     * @param x the base.
     * @param y the exponent.
     * @return <code>x<sup>y</sup></code>
     */
    public static double pow(double x, double y) {
        return MathLib.exp(y * MathLib.log(x));
    }

    /**
     * Returns the closest <code>int</code> to the specified argument. 
     *
     * @param f the <code>float</code> value to be rounded to a <code>int</code>
     * @return the nearest <code>int</code> value.
     */
    public static int round(float f) {
          return (int) floor(f + 0.5f);
    }

    /**
     * Returns the closest <code>long</code> to the specified argument. 
     *
     * @param d the <code>double</code> value to be rounded to a 
     *        <code>long</code>
     * @return the nearest <code>long</code> value.
     */
    public static long round(double d) {
          return (long) floor(d + 0.5d);
    }

    /**
     * Returns a random number between zero and one.
     *  
     * @return  a <code>double</code> greater than or equal 
     *          to <code>0.0</code> and less than <code>1.0</code>.
     */
    public static double random() {
        return RANDOM.nextDouble();
    }
    private static final Random RANDOM = new java.util.Random();

    /**
     * Returns the absolute value of the specified <code>int</code> argument.
     *
     * @param i the <code>int</code> value.
     * @return <code>i</code> or <code>-i</code>
     */
    public static int abs(int i) {
	    return (i < 0) ? -i : i;
    }

    /**
     * Returns the absolute value of the specified <code>long</code> argument.
     *
     * @param l the <code>long</code> value.
     * @return <code>l</code> or <code>-l</code>
     */
    public static long abs(long l) {
	    return (l < 0) ? -l : l;
    }

    /**
     * Returns the absolute value of the specified <code>float</code> argument.
     *
     * @param f the <code>float</code> value.
     * @return <code>f</code> or <code>-f</code>
     */
    public static float abs(float f) {
	    return (f < 0) ? -f : f;
    }
  
    /**
     * Returns the absolute value of the specified <code>double</code> argument.
     *
     * @param d the <code>double</code> value.
     * @return <code>d</code> or <code>-d</code>
     */
    public static double abs(double d) {
	    return (d < 0) ? -d : d;
    }
  
    /**
     * Returns the greater of two <code>int</code> values. 
     *
     * @param x the first value.
     * @param y the second value.
     * @return the larger of <code>x</code> and <code>y</code>.
     */
    public static int max(int x, int y) {
         return (x >= y) ? x : y;
    }

    /**
     * Returns the greater of two <code>long</code> values. 
     *
     * @param x the first value.
     * @param y the second value.
     * @return the larger of <code>x</code> and <code>y</code>.
     */
    public static long max(long x, long y) {
         return (x >= y) ? x : y;
    }

    /**
     * Returns the greater of two <code>float</code> values. 
     *
     * @param x the first value.
     * @param y the second value.
     * @return the larger of <code>x</code> and <code>y</code>.
     */
    public static float max(float x, float y) {
         return (x >= y) ? x : y;
    }

    /**
     * Returns the greater of two <code>double</code> values. 
     *
     * @param x the first value.
     * @param y the second value.
     * @return the larger of <code>x</code> and <code>y</code>.
     */
    public static double max(double x, double y) {
         return (x >= y) ? x : y;
    }

    /**
     * Returns the smaller of two <code>int</code> values. 
     *
     * @param x the first value.
     * @param y the second value.
     * @return the smaller of <code>x</code> and <code>y</code>.
     */
    public static int min(int x, int y) {
         return (x < y) ? x : y;
    }

    /**
     * Returns the smaller of two <code>long</code> values. 
     *
     * @param x the first value.
     * @param y the second value.
     * @return the smaller of <code>x</code> and <code>y</code>.
     */
    public static long min(long x, long y) {
         return (x < y) ? x : y;
    }

    /**
     * Returns the smaller of two <code>float</code> values. 
     *
     * @param x the first value.
     * @param y the second value.
     * @return the smaller of <code>x</code> and <code>y</code>.
     */
    public static float min(float x, float y) {
         return (x < y) ? x : y;
    }

    /**
     * Returns the smaller of two <code>double</code> values. 
     *
     * @param x the first value.
     * @param y the second value.
     * @return the smaller of <code>x</code> and <code>y</code>.
     */
    public static double min(double x, double y) {
         return (x < y) ? x : y;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    /* @(#)s_atan.c 1.3 95/01/18 */
    /*
     * ====================================================
     * Copyright (C) 1993 by Sun Microsystems, Inc. All rights reserved.
     *
     * Developed at SunSoft, a Sun Microsystems, Inc. business.
     * Permission to use, copy, modify, and distribute this
     * software is freely granted, provided that this notice 
     * is preserved.
     * ====================================================
     *
     */

    /* atan(x)
     * Method
     *   1. Reduce x to positive by atan(x) = -atan(-x).
     *   2. According to the integer k=4t+0.25 chopped, t=x, the argument
     *      is further reduced to one of the following intervals and the
     *      arctangent of t is evaluated by the corresponding formula:
     *
     *      [0,7/16]      atan(x) = t-t^3*(a1+t^2*(a2+...(a10+t^2*a11)...)
     *      [7/16,11/16]  atan(x) = atan(1/2) + atan( (t-0.5)/(1+t/2) )
     *      [11/16.19/16] atan(x) = atan( 1 ) + atan( (t-1)/(1+t) )
     *      [19/16,39/16] atan(x) = atan(3/2) + atan( (t-1.5)/(1+1.5t) )
     *      [39/16,INF]   atan(x) = atan(INF) + atan( -1/t )
     *
     * Constants:
     * The hexadecimal values are the intended ones for the following 
     * constants. The decimal values may be used, provided that the 
     * compiler will convert from decimal to binary accurately enough 
     * to produce the hexadecimal values shown.
     */
    static final double atanhi[] = {
      4.63647609000806093515e-01, /* atan(0.5)hi 0x3FDDAC67, 0x0561BB4F */
      7.85398163397448278999e-01, /* atan(1.0)hi 0x3FE921FB, 0x54442D18 */
      9.82793723247329054082e-01, /* atan(1.5)hi 0x3FEF730B, 0xD281F69B */
      1.57079632679489655800e+00, /* atan(inf)hi 0x3FF921FB, 0x54442D18 */
    };
    static final double atanlo[] = {
      2.26987774529616870924e-17, /* atan(0.5)lo 0x3C7A2B7F, 0x222F65E2 */
      3.06161699786838301793e-17, /* atan(1.0)lo 0x3C81A626, 0x33145C07 */
      1.39033110312309984516e-17, /* atan(1.5)lo 0x3C700788, 0x7AF0CBBD */
      6.12323399573676603587e-17, /* atan(inf)lo 0x3C91A626, 0x33145C07 */
    };
    static final double aT[] = {
      3.33333333333329318027e-01, /* 0x3FD55555, 0x5555550D */
     -1.99999999998764832476e-01, /* 0xBFC99999, 0x9998EBC4 */
      1.42857142725034663711e-01, /* 0x3FC24924, 0x920083FF */
     -1.11111104054623557880e-01, /* 0xBFBC71C6, 0xFE231671 */
      9.09088713343650656196e-02, /* 0x3FB745CD, 0xC54C206E */
     -7.69187620504482999495e-02, /* 0xBFB3B0F2, 0xAF749A6D */
      6.66107313738753120669e-02, /* 0x3FB10D66, 0xA0D03D51 */
     -5.83357013379057348645e-02, /* 0xBFADDE2D, 0x52DEFD9A */
      4.97687799461593236017e-02, /* 0x3FA97B4B, 0x24760DEB */
     -3.65315727442169155270e-02, /* 0xBFA2B444, 0x2C6A6C2F */
      1.62858201153657823623e-02, /* 0x3F90AD3A, 0xE322DA11 */
    };
    static final double 
    one   = 1.0,
    huge   = 1.0e300;
    static double _atan(double x)
    {
    	double w,s1,s2,z;
    	int ix,hx,id;
    	long xBits = Double.doubleToLongBits(x);
        int __HIx = (int) (xBits >> 32);
        int __LOx = (int) xBits;

    	hx = __HIx;
    	ix = hx&0x7fffffff;
    	if(ix>=0x44100000) {	/* if |x| >= 2^66 */
    	    if(ix>0x7ff00000||
    		(ix==0x7ff00000&&(__LOx!=0)))
    		return x+x;		/* NaN */
    	    if(hx>0) return  atanhi[3]+atanlo[3];
    	    else     return -atanhi[3]-atanlo[3];
    	} if (ix < 0x3fdc0000) {	/* |x| < 0.4375 */
    	    if (ix < 0x3e200000) {	/* |x| < 2^-29 */
    		if(huge+x>one) return x;	/* raise inexact */
    	    }
    	    id = -1;
    	} else {
    	x = MathLib.abs(x);
    	if (ix < 0x3ff30000) {		/* |x| < 1.1875 */
    	    if (ix < 0x3fe60000) {	/* 7/16 <=|x|<11/16 */
    		id = 0; x = (2.0*x-one)/(2.0+x); 
    	    } else {			/* 11/16<=|x|< 19/16 */
    		id = 1; x  = (x-one)/(x+one); 
    	    }
    	} else {
    	    if (ix < 0x40038000) {	/* |x| < 2.4375 */
    		id = 2; x  = (x-1.5)/(one+1.5*x);
    	    } else {			/* 2.4375 <= |x| < 2^66 */
    		id = 3; x  = -1.0/x;
    	    }
    	}}
        /* end of argument reduction */
    	z = x*x;
    	w = z*z;
        /* break sum from i=0 to 10 aT[i]z**(i+1) into odd and even poly */
    	s1 = z*(aT[0]+w*(aT[2]+w*(aT[4]+w*(aT[6]+w*(aT[8]+w*aT[10])))));
    	s2 = w*(aT[1]+w*(aT[3]+w*(aT[5]+w*(aT[7]+w*aT[9]))));
    	if (id<0) return x - x*(s1+s2);
    	else {
    	    z = atanhi[id] - ((x*(s1+s2) - atanlo[id]) - x);
    	    return (hx<0)? -z:z;
    	}
    }

    ////////////////////////////////////////////////////////////////////////////
    /* @(#)e_log.c 1.3 95/01/18 */
    /*
     * ====================================================
     * Copyright (C) 1993 by Sun Microsystems, Inc. All rights reserved.
     *
     * Developed at SunSoft, a Sun Microsystems, Inc. business.
     * Permission to use, copy, modify, and distribute this
     * software is freely granted, provided that this notice 
     * is preserved.
     * ====================================================
     */

    /* __ieee754_log(x)
     * Return the logrithm of x
     *
     * Method :                  
     *   1. Argument Reduction: find k and f such that 
     *			x = 2^k * (1+f), 
     *	   where  sqrt(2)/2 < 1+f < sqrt(2) .
     *
     *   2. Approximation of log(1+f).
     *	Let s = f/(2+f) ; based on log(1+f) = log(1+s) - log(1-s)
     *		 = 2s + 2/3 s**3 + 2/5 s**5 + .....,
     *	     	 = 2s + s*R
     *      We use a special Reme algorithm on [0,0.1716] to generate 
     * 	a polynomial of degree 14 to approximate R The maximum error 
     *	of this polynomial approximation is bounded by 2**-58.45. In
     *	other words,
     *		        2      4      6      8      10      12      14
     *	    R(z) ~ Lg1*s +Lg2*s +Lg3*s +Lg4*s +Lg5*s  +Lg6*s  +Lg7*s
     *  	(the values of Lg1 to Lg7 are listed in the program)
     *	and
     *	    |      2          14          |     -58.45
     *	    | Lg1*s +...+Lg7*s    -  R(z) | <= 2 
     *	    |                             |
     *	Note that 2s = f - s*f = f - hfsq + s*hfsq, where hfsq = f*f/2.
     *	In order to guarantee error in log below 1ulp, we compute log
     *	by
     *		log(1+f) = f - s*(f - R)	(if f is not too large)
     *		log(1+f) = f - (hfsq - s*(hfsq+R)).	(better accuracy)
     *	
     *	3. Finally,  log(x) = k*ln2 + log(1+f).  
     *			    = k*ln2_hi+(f-(hfsq-(s*(hfsq+R)+k*ln2_lo)))
     *	   Here ln2 is split into two floating point number: 
     *			ln2_hi + ln2_lo,
     *	   where n*ln2_hi is always exact for |n| < 2000.
     *
     * Special cases:
     *	log(x) is NaN with signal if x < 0 (including -INF) ; 
     *	log(+INF) is +INF; log(0) is -INF with signal;
     *	log(NaN) is that NaN with no signal.
     *
     * Accuracy:
     *	according to an error analysis, the error is always less than
     *	1 ulp (unit in the last place).
     *
     * Constants:
     * The hexadecimal values are the intended ones for the following 
     * constants. The decimal values may be used, provided that the 
     * compiler will convert from decimal to binary accurately enough 
     * to produce the hexadecimal values shown.
     */
    static final double
    ln2_hi  =  6.93147180369123816490e-01,	/* 3fe62e42 fee00000 */
    ln2_lo  =  1.90821492927058770002e-10,	/* 3dea39ef 35793c76 */
    two54   =  1.80143985094819840000e+16,  /* 43500000 00000000 */
    Lg1 = 6.666666666666735130e-01,  /* 3FE55555 55555593 */
    Lg2 = 3.999999999940941908e-01,  /* 3FD99999 9997FA04 */
    Lg3 = 2.857142874366239149e-01,  /* 3FD24924 94229359 */
    Lg4 = 2.222219843214978396e-01,  /* 3FCC71C5 1D8E78AF */
    Lg5 = 1.818357216161805012e-01,  /* 3FC74664 96CB03DE */
    Lg6 = 1.531383769920937332e-01,  /* 3FC39A09 D078C69F */
    Lg7 = 1.479819860511658591e-01;  /* 3FC2F112 DF3E5244 */
    static final double zero   =  0.0;
    static double _ieee754_log(double x)
    {
    	double hfsq,f,s,z,R,w,t1,t2,dk;
    	int k,hx,i,j;
    	int lx; // unsigned 

    	long xBits = Double.doubleToLongBits(x);
        hx = (int) (xBits >> 32);
        lx = (int) xBits;

    	k=0;
    	if (hx < 0x00100000) {			/* x < 2**-1022  */
    	    if (((hx&0x7fffffff)|lx)==0) 
    		return -two54/zero;		/* log(+-0)=-inf */
    	    if (hx<0) return (x-x)/zero;	/* log(-#) = NaN */
    	    k -= 54; x *= two54; /* subnormal number, scale up x */
        	xBits = Double.doubleToLongBits(x);
            hx = (int) (xBits >> 32); /* high word of x */
    	} 
    	if (hx >= 0x7ff00000) return x+x;
    	k += (hx>>20)-1023;
    	hx &= 0x000fffff;
    	i = (hx+0x95f64)&0x100000;
    	xBits = Double.doubleToLongBits(x);
    	int HIx = hx|(i^0x3ff00000);	/* normalize x or x/2 */
    	xBits = ((HIx & 0xFFFFFFFFL) << 32) | (xBits & 0xFFFFFFFFL);
    	x = Double.longBitsToDouble(xBits);
    	k += (i>>20);
    	f = x-1.0;
    	if((0x000fffff&(2+hx))<3) {	/* |f| < 2**-20 */
    	    if(f==zero) if(k==0) return zero;  else {dk=(double)k;
    				 return dk*ln2_hi+dk*ln2_lo;}
    	    R = f*f*(0.5-0.33333333333333333*f);
    	    if(k==0) return f-R; else {dk=(double)k;
    	    	     return dk*ln2_hi-((R-dk*ln2_lo)-f);}
    	}
     	s = f/(2.0+f); 
    	dk = (double)k;
    	z = s*s;
    	i = hx-0x6147a;
    	w = z*z;
    	j = 0x6b851-hx;
    	t1= w*(Lg2+w*(Lg4+w*Lg6)); 
    	t2= z*(Lg1+w*(Lg3+w*(Lg5+w*Lg7))); 
    	i |= j;
    	R = t2+t1;
    	if(i>0) {
    	    hfsq=0.5*f*f;
    	    if(k==0) return f-(hfsq-s*(hfsq+R)); else
    		     return dk*ln2_hi-((hfsq-(s*(hfsq+R)+dk*ln2_lo))-f);
    	} else {
    	    if(k==0) return f-s*(f-R); else
    		     return dk*ln2_hi-((s*(f-R)-dk*ln2_lo)-f);
    	}
    }    
    
    ////////////////////////////////////////////////////////////////////////////
    /* @(#)e_exp.c 1.6 04/04/22 */
    /*
     * ====================================================
     * Copyright (C) 2004 by Sun Microsystems, Inc. All rights reserved.
     *
     * Permission to use, copy, modify, and distribute this
     * software is freely granted, provided that this notice 
     * is preserved.
     * ====================================================
     */

    /* __ieee754_exp(x)
     * Returns the exponential of x.
     *
     * Method
     *   1. Argument reduction:
     *      Reduce x to an r so that |r| <= 0.5*ln2 ~ 0.34658.
     *	Given x, find r and integer k such that
     *
     *               x = k*ln2 + r,  |r| <= 0.5*ln2.  
     *
     *      Here r will be represented as r = hi-lo for better 
     *	accuracy.
     *
     *   2. Approximation of exp(r) by a special rational function on
     *	the interval [0,0.34658]:
     *	Write
     *	    R(r**2) = r*(exp(r)+1)/(exp(r)-1) = 2 + r*r/6 - r**4/360 + ...
     *      We use a special Remes algorithm on [0,0.34658] to generate 
     * 	a polynomial of degree 5 to approximate R. The maximum error 
     *	of this polynomial approximation is bounded by 2**-59. In
     *	other words,
     *	    R(z) ~ 2.0 + P1*z + P2*z**2 + P3*z**3 + P4*z**4 + P5*z**5
     *  	(where z=r*r, and the values of P1 to P5 are listed below)
     *	and
     *	    |                  5          |     -59
     *	    | 2.0+P1*z+...+P5*z   -  R(z) | <= 2 
     *	    |                             |
     *	The computation of exp(r) thus becomes
     *                             2*r
     *		exp(r) = 1 + -------
     *		              R - r
     *                                 r*R1(r)	
     *		       = 1 + r + ----------- (for better accuracy)
     *		                  2 - R1(r)
     *	where
     *			         2       4             10
     *		R1(r) = r - (P1*r  + P2*r  + ... + P5*r   ).
     *	
     *   3. Scale back to obtain exp(x):
     *	From step 1, we have
     *	   exp(x) = 2^k * exp(r)
     *
     * Special cases:
     *	exp(INF) is INF, exp(NaN) is NaN;
     *	exp(-INF) is 0, and
     *	for finite argument, only exp(0)=1 is exact.
     *
     * Accuracy:
     *	according to an error analysis, the error is always less than
     *	1 ulp (unit in the last place).
     *
     * Misc. info.
     *	For IEEE double 
     *	    if x >  7.09782712893383973096e+02 then exp(x) overflow
     *	    if x < -7.45133219101941108420e+02 then exp(x) underflow
     *
     * Constants:
     * The hexadecimal values are the intended ones for the following 
     * constants. The decimal values may be used, provided that the 
     * compiler will convert from decimal to binary accurately enough
     * to produce the hexadecimal values shown.
     */
    static final double
    halF[]	= {0.5,-0.5,},
    twom1000= 9.33263618503218878990e-302,     /* 2**-1000=0x01700000,0*/
    o_threshold=  7.09782712893383973096e+02,  /* 0x40862E42, 0xFEFA39EF */
    u_threshold= -7.45133219101941108420e+02,  /* 0xc0874910, 0xD52D3051 */
    ln2HI[]   ={ 6.93147180369123816490e-01,  /* 0x3fe62e42, 0xfee00000 */
    	     -6.93147180369123816490e-01,},/* 0xbfe62e42, 0xfee00000 */
    ln2LO[]   ={ 1.90821492927058770002e-10,  /* 0x3dea39ef, 0x35793c76 */
    	     -1.90821492927058770002e-10,},/* 0xbdea39ef, 0x35793c76 */
    invln2 =  1.44269504088896338700e+00, /* 0x3ff71547, 0x652b82fe */
    P1   =  1.66666666666666019037e-01, /* 0x3FC55555, 0x5555553E */
    P2   = -2.77777777770155933842e-03, /* 0xBF66C16C, 0x16BEBD93 */
    P3   =  6.61375632143793436117e-05, /* 0x3F11566A, 0xAF25DE2C */
    P4   = -1.65339022054652515390e-06, /* 0xBEBBBD41, 0xC5D26BF1 */
    P5   =  4.13813679705723846039e-08; /* 0x3E663769, 0x72BEA4D0 */
    static double _ieee754_exp(double x)	/* default IEEE double exp */
    {
    	double y,hi = 0,lo = 0,c,t;
    	int k = 0,xsb;
    	int hx; // Unsigned.
    	long xBits = Double.doubleToLongBits(x);
        int __HIx = (int) (xBits >> 32);
        int __LOx = (int) xBits;

    	hx  = __HIx;	/* high word of x */
    	xsb = (hx>>31)&1;		/* sign bit of x */
    	hx &= 0x7fffffff;		/* high word of |x| */

        /* filter out non-finite argument */
    	if(hx >= 0x40862E42) {			/* if |x|>=709.78... */
                if(hx>=0x7ff00000) {
    		if(((hx&0xfffff)|__LOx)!=0) 
    		     return x+x; 		/* NaN */
    		else return (xsb==0)? x:0.0;	/* exp(+-inf)={inf,0} */
    	    }
    	    if(x > o_threshold) return huge*huge; /* overflow */
    	    if(x < u_threshold) return twom1000*twom1000; /* underflow */
    	}

        /* argument reduction */
    	if(hx > 0x3fd62e42) {		/* if  |x| > 0.5 ln2 */ 
    	    if(hx < 0x3FF0A2B2) {	/* and |x| < 1.5 ln2 */
    		hi = x-ln2HI[xsb]; lo=ln2LO[xsb]; k = 1-xsb-xsb;
    	    } else {
    		k  = (int)(invln2*x+halF[xsb]);
    		t  = k;
    		hi = x - t*ln2HI[0];	/* t*ln2HI is exact here */
    		lo = t*ln2LO[0];
    	    }
    	    x  = hi - lo;
    	} 
    	else if(hx < 0x3e300000)  {	/* when |x|<2**-28 */
    	    if(huge+x>one) return one+x;/* trigger inexact */
    	}
    	else k = 0;

        /* x is now in primary range */
    	t  = x*x;
    	c  = x - t*(P1+t*(P2+t*(P3+t*(P4+t*P5))));
    	if(k==0) 	return one-((x*c)/(c-2.0)-x); 
    	else 		y = one-((lo-(x*c)/(2.0-c))-hi);
    	long yBits = Double.doubleToLongBits(y);
        int __HIy = (int) (yBits >> 32);
    	if(k >= -1021) {
    	    __HIy += (k<<20);	/* add k to y's exponent */
        	yBits = ((__HIy & 0xFFFFFFFFL) << 32) | (yBits & 0xFFFFFFFFL);
        	y = Double.longBitsToDouble(yBits);
    	    return y;
    	} else {
    	    __HIy += ((k+1000)<<20);/* add k to y's exponent */
        	yBits = ((__HIy & 0xFFFFFFFFL) << 32) | (yBits & 0xFFFFFFFFL);
        	y = Double.longBitsToDouble(yBits);
    	    return y*twom1000;
    	}
    }    
}