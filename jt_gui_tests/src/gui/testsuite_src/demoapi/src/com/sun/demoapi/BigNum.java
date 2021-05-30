/*
 * $Id$
 *
 * Copyright (c) 2002, 2009, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.sun.demoapi;

/**
 * Basic support for immutable arbitrary size integers. 
 * Numbers are held as a sign and an array of ints. The sign is
 * +1 for positive numbers, 0 for zero, and -1 for negative 
 * numbers. Each element in the array holds a group of decimal
 * digits; the array is of minimal size and is  arranged in 
 * little-endian order (thus, element 0 holds the least 
 * significant group of digits.) Because the array is 
 * always of minimal size, zero is represented by a sign of zero 
 * and an empty array.
 */

// Note: this class is purely provided to be the basis of some
// examples for writing a test suite. The code has been written with 
// simplicity in mind, rather than efficiency, and may contain 
// deliberate coding errors. For proper support for big numbers, 
// see java.math.BigInteger.

public class BigNum
{
    /**
     * Create a BigNum for a standard long integer.
     * @param n the value to be converted to a BigNum representation
     */
    public BigNum(long n) {
	if (n == Integer.MIN_VALUE) {
	    throw new IllegalArgumentException();
	}
	else if (n < 0) {
	    sign = -1;
	    n = -n;
	}
	else if (n == 0) {
	    data = new int[0];
	    sign = 0;
	    return;
	}
	else 
	    sign = 1;

	int size = 0;
	long n1 = n;
	while (n1 > 0) {
	    size++;
	    n1 = n1 / MAX_PER_CELL;
	}

	data = new int[size];
	for (int i = 0; i < size; i++) {
	    data[i] = (int) (n % MAX_PER_CELL);
	    n = n / MAX_PER_CELL;
	}
    }

    /**
     * Create a BigNum from a string. The string must be of
     * the form of an optional '-', followed by an arbitrary
     * non-empty sequence of digits.
     * @param s the string to be converted.
     * @throws NullPointerException if the argument is null.
     * @throws NumberFormatException if the argument string
     * is invalid.
     */
    public BigNum(String s) {
	if (s == null)
	    throw new NullPointerException();
	
	int slen = s.length();
	if (slen == 0)
	    throw new NumberFormatException("empty string");

	int i = 0;
	
	// check for leading - sign
	if (s.charAt(0) == '-') {
	    sign = -1;
	    i = 1;
	}
	else
	    sign = 1; // assume positive, for now

	// check for (and ignore) redundant leading zeroes
	while (i < slen - 1 && s.charAt(i) == '0')
	    i++;

	// check for 0
	if (i == slen - 1 && s.charAt(i) == '0') {
	    sign = 0; 
	    data = new int[0];
	    return;
	}
	
	// check for no significant digits
	if (i == slen)
	    throw new NumberFormatException("no digits");
	
	// allocate array of cells for number
	int numDigits = slen - i; // i currently points to the first significant digit
	int numCells = (numDigits + DIGITS_PER_CELL - 1) / DIGITS_PER_CELL;
	data = new int[numCells];

	// scan digits in string
	for ( ; i < slen; i++) {
	    char c = s.charAt(i);
	    if ( c < '0' || c > '9')
		throw new NumberFormatException("bad char");
	    int d = (slen - 1 - i) / DIGITS_PER_CELL;
	    data[d] = data[d] * 10 + (c - '0');
	}
    }

    /**
     * Add another BigNum to this one.
     * @param other The BigNum to be added
     * @return a BigNum containing the sum
     */
    public BigNum add(BigNum other) {
	if (sign == 0)
	    return other;
	else if (other.sign == 0)
	    return this;
	else if (sign == other.sign) 
	    return new BigNum(sign, add(data, other.data));
	else {
	    switch (compare(data, other.data)) {
	    case +1:
		// this is bigger in magnitude, so result is
		// the difference, with the sign of this
		return new BigNum(sign, subtract(data, other.data));
	    case 0: 
		// numbers are equal in magnitude, but different
		// in sign; result must be zero
		return new BigNum(0);
	    case -1:
		// other is bigger in magnitude, so result is 
		// the difference, with the sign of other
		return new BigNum(other.sign, subtract(other.data, data));
	    default:
		throw new Error("should not happen");
	    }
	}
    }

    /**
     * Subtract another BigNum from this one.
     * @param other The BigNum to be added
     * @return a BigNum containing the sum
     */
    public BigNum subtract(BigNum other) {
	if (sign == 0)
	    return new BigNum(-other.sign, other.data);
	else if (other.sign == 0)
	    return this;
	else if (sign == other.sign) {
	    switch (compare(data, other.data)) {
	    case +1:
		// this is bigger in magnitude, so result is
		// the difference, with the sign of this
		return new BigNum(sign, subtract(data, other.data));
	    case 0: 
		// numbers are equal in magnitude, but different
		// in sign; result must be zero
		return new BigNum(0);
	    case -1:
		// other is bigger in magnitude, so result is 
		// the difference, with the opposite sign of other
		return new BigNum(-other.sign, subtract(other.data, data));
	    default:
		throw new Error("should not happen");
	    }
	}
	else 
	    return new BigNum(sign, add(data, other.data));
    }

    /**
     * Compare this BigNum with another.
     * @param other the BigNum to be compared against
     * @return -1 is this BigNum is smaller than <i>other</i>, 
     *	0 if they are equal, 
     * and +1 if this BigNum is greater than <i>other</i>
     */
    public int compare(BigNum other) {
	if (sign == other.sign) {
	    int cmp = compare(data, other.data);
	    return (sign < 0 ? -cmp : cmp);
	}
	else {
	    return (sign > other.sign ? 1 : -1);
	}	   
    }

    /**
     * Check this BigNum for equality with another object.
     * @param other the object to be compared against
     * @return true if and only if <i>other</i> is a BigNum
     * representing the same value as this one.
     */
    public boolean equals(Object other) {
	if (!(other instanceof BigNum))
	    return false;

	BigNum o = (BigNum) other;

	if (sign != o.sign)
	    return false;

	if (data.length != o.data.length)
	    return false;

	for (int i = 0; i < data.length; i++) {
	    if (data[i] != o.data[i])
		return false;
	}

	return true;
    }

    /**
     * Convert this BigNum to a printable representation.
     * @return a string containing a negative sign if necessary, followed
     * by the decimal digits of the number
     */
    public String toString() {
	if (sign == 0)
	    return "0";

	StringBuffer sb = new StringBuffer();
	if (sign < 0)
	    sb.append('-');
	
	boolean suppressZero = true;
	for (int i = data.length - 1; i >= 0; i--) {
	    int d = data[i];
	    for (int j = DIGITS_PER_CELL - 1; j >= 0; j--) {
		int digit = (d / POWERS_OF_10[j]) % 10;
		if (digit > 0) {
		    suppressZero = false;
		    sb.append((char) ('0' + digit));
		}
		else if (!suppressZero)
		    sb.append('0');
	    }
	}

	return sb.toString();
    }

    private BigNum(int sign, int[] data) {
	this.sign = sign;
	this.data = data;
    }

    private int[] add(int[] d1, int[] d2) {
	int[] sum = new int[Math.max(d1.length, d2.length)];

	int carry = 0;
	for (int i = 0; i < sum.length; i++) {
	    int s = carry;
	    if (i < d1.length)
		s += d1[i];
	    if (i < d2.length)
		s += d2[i];
	    sum[i] = s % MAX_PER_CELL;
	    carry = s / MAX_PER_CELL;
	}

	if (carry > 0) {
	    int[] x = new int[sum.length + 1];
	    System.arraycopy(sum, 0, x, 0, sum.length);
	    x[sum.length] = carry;
	    sum = x;
	}

	return sum;
    }

    // d1 is assumed to be greater than d2
    private int[] subtract(int[] d1, int[] d2) {
	if (d1.length < d2.length)
	    throw new IllegalArgumentException();

	int[] diff = new int[d1.length];

	int borrow = 0;
	for (int i = 0; i < diff.length; i++) {
	    int d = d1[i] - borrow;
	    if (i < d2.length)
		d = d - d2[i];
	    if (d < 0) {
		d = d + MAX_PER_CELL;
		borrow = 1;
	    }
	    else
		borrow = 0;
	    diff[i] = d;
	}

	if (borrow != 0)
	    throw new IllegalArgumentException();

	int newSize = diff.length;
	while (newSize > 0 && diff[newSize - 1] == 0)
	    newSize--;

	if (newSize < diff.length) {
	    int[] newDiff = new int[newSize];
	    System.arraycopy(diff, 0, newDiff, 0, newDiff.length);
	    diff = newDiff;
	}

	return diff;
    }

    private int compare(int[] d1, int[] d2) {
	if (d1.length > d2.length)
	    return 1;
	
	if (d1.length < d2.length)
	    return -1;

	// d1.length == d2.length
	for (int i = d1.length - 1; i >= 0; i--) {
	    int n1 = d1[i];
	    int n2 = d2[i];
	    if (n1 > n2)
		return 1;
	    if (n1 < n2)
		return -1;
	}
	
	return 0;
    }

    private int sign;
    private int[] data;

    private static final int[] POWERS_OF_10 = {1, 10, 100, 1000, 10000, 100000, 1000000};
    private static final int DIGITS_PER_CELL = 6;
    private static final int MAX_PER_CELL = POWERS_OF_10[DIGITS_PER_CELL];
}
