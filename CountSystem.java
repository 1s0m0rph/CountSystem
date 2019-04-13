
public abstract class CountSystem
{
	public static final char neg = '-';	 //change this if you need the '-' character in your alpha
	private static final char point = '.';  //change this if you need the '.' character in your alpha
	public String BASE;
	int BASE_INT;
	public char[] alpha;
	private String[][] tt;   //times table
	Decimal d;

	public CountSystem(String _base)
	{
		BASE = _base;
		BASE_INT = Integer.parseInt(BASE);
		if(BASE_INT != 10)
		{
			d = new Decimal();
		}
		initAlphabet();
		initTT();
	}

	/*
	useful whenever we want a non-orthodox decimal alphabet
	 */
	public CountSystem(String _base, Decimal _d)
	{
		BASE = _base;
		BASE_INT = Integer.parseInt(BASE);
		d = _d;
		initAlphabet();
		initTT();
	}

	public CountSystem(String _base, char[] _alpha)
	{
		BASE = _base;
		BASE_INT = Integer.parseInt(BASE);
		for(int i = 0; i < _alpha.length; i++)
		{
			alpha[i] = _alpha[i];
		}
		if(BASE_INT != 10)
		{
			d = new Decimal();
		}
		initTT();
	}

	public CountSystem(String _base, char[] _alpha, Decimal _d)
	{
		BASE = _base;
		BASE_INT = Integer.parseInt(BASE);
		for(int i = 0; i < _alpha.length; i++)
		{
			alpha[i] = _alpha[i];
		}
		d = _d;
		initTT();
	}

	abstract void initAlphabet();

	private void initTT()
	{
		tt = new String[BASE_INT][BASE_INT];
		for(int i = 0; i < BASE_INT; i++)
		{
			tt[0][i] = zero();
			tt[i][0] = zero();
		}

		for(int i = 1; i < BASE_INT; i++)
		{
			String J = one();
			for(int j = 1; j < BASE_INT; j++)
			{
				if(i > j)
				{
					tt[i][j] = tt[j][i];
				}
				else
				{
					tt[i][j] = add(tt[i-1][j],J);
				}
				J = increment(J);
			}
		}
	}

	private String replaceStringIndex(String original, int index, char newChar)
	{
		String r = "";
		for(int i = 0; i < original.length(); i++)
		{
			if(i == index) r += newChar;
			else r += original.charAt(i);
		}
		return r;
	}

	char alphaAt(int idx){return alpha[idx];}

	String zero(){return Character.toString(alpha[0]);}
	String one(){return Character.toString(alpha[1]);}
	
	String abs(String n)
	{
		if(n.length() > 0 && n.charAt(0) == neg)
			return n.substring(1);
		return n;
	}
	
	/*
	Removes the "point" character from the word
	 */
	String dropPoint(String n)
	{
		StringBuilder r = new StringBuilder();
		for(int i = 0; i < n.length(); i++)
		{
			char c = n.charAt(i);
			if(c != point)
				r.append(c);
		}
		return r.toString();
	}

	/*/
	This version allows the caller to pass specific shift amounts, not necessarily equal to the natural shift amount (width of fractional(a,b))
	
	fractional part calculated with: (a <<_c p) >>_c p, then converted to base c
	
	where p is precision, a is hum, b is csFrom.base, and c is base
	
	Note about precision:
	
	Because of the way we define it, "precision" is how many *base c* arbits are going to be correct in our translation. If we use a base for c like binary that's really damn tiny and needs
	a lot of arbits to express some given precision, then convert it back into our original base, we won't be very happy with the number.
	This is just a limitation of binary, not of the system. All of the arbits in the number that you see are correct and they are correct out to "precision" of them, but
	"precision" in base c might not correspond exactly to "precision" in base b
	
	example:
	convert 1.022_10 into _2:
	
	output:
	1.022_10 = 1.0000010111_2 (1010_2 arbit precision)
	1.0000010111_2 = 1.0224609375_10 (10_10 arbit precision)
	Error (prec = 10): 0.0004609375
	Error (prec = 10, left-shifted 10): 4609375 //fucking massive number. We want this to be as low as possible. If b ~= c, this will always be about 1
	/*/
	String convertWithPartials(CountSystem csFrom, String num, String precision)
	{
		if(num.indexOf(point) == -1)//this number is actually an integer
			return convert(csFrom,num);
		Decimal d = new Decimal();
		String precisionDec = d.convert(csFrom,precision);
		int precisionInt = Integer.parseInt(precisionDec);//p
		String wholePart = "";
		if(num.charAt(0) == point)
			wholePart = csFrom.zero();
		else
			wholePart = convert(csFrom,num.substring(0,num.indexOf(point)));
		if(precision.equals(csFrom.zero()))
		{
			return wholePart;
		}
		//setup/preconditions complete
		
		num = csFrom.zero() + num.substring(num.indexOf(point));
//		int leftoverShift = csFrom.countLeadingZeroes(num,2);//the zeroes will otherwise be dropped by the shifts
//		num = csFrom.leftShift(num,precisionInt);//n <<_b p //unnecessary left-right shift in base b. This is an artifact from the old "throwing shit at the wall" version
//		num = csFrom.round(num,1);//just use the integer part of this. This part is also an artifact. Can't be losing those arbits
		String BASE_IN_csf = csFrom.convertDec(BASE);
		String convNum = csFrom.multiply(csFrom.pow(BASE_IN_csf,precision),num);//n <<_c p
//		convNum = csFrom.rightShift(convNum,precisionInt);//((n <<_b p) <<_c p) >>_b p = n <<_c p //artifact from old version. This can be removed
		convNum = csFrom.round(convNum,1);//so we actually convert an integer
//		return wholePart + point + nZeroes(leftoverShift) + convert(csFrom,convNum);
		String retNum = convert(csFrom,convNum);//this number is now left-shifted too far
		retNum = rightShift(retNum,precisionInt);
		retNum = dropPoint(retNum).substring(1);//drop the point so we don't have two and don't worry about the extra stuff to its right, it is actually correct and it's free so just leave it. We also have to drop the zero at the beginning since it doesn't actually exist
		if(retNum.length() == 0)
			return wholePart;
		return wholePart + point + retNum;
	}
	
	int countLeadingZeroes(String n, int from)
	{
		int r = 0;
		for(int i = from; i < n.length() && Character.toString(n.charAt(i)).equals(zero()); i++){r++;}
		return r;
	}

	/*/
	ALGORITHM (ignore this part. What's in the function comments is more sensible. I'm leaving for now in case there's hidden wisdom):
	convert the part left of the point (".") normally, set it aside
	get rid of everything left of the point, as well as the point itself
	[this part can be written as (numerator = that number) / (BASE ^ k), where k is width(numerator)]
	we want to find the number i such that
	numerator * BASE^n is greater than but as close as possible to csFrom.BASE^n * some indexer
	take (in csFrom base):
		numerator * BASE^(precision) / csFrom.BASE^(precision)
	and get rid of anything to the right of the point
	convert that number into BASE
	concat the whole part and that converted number with a point in between and we have the correct number
	/*/
	String convertWithPartials(CountSystem csFrom, String num)
	{
//		String wholePart = convert(csFrom,num.substring(0,num.indexOf(point)));
		int k = num.substring(num.indexOf(point)+1).length();
		return convertWithPartials(csFrom,num,csFrom.one());
//		num = csFrom.zero() + num.substring(num.indexOf(point));
//		String BASE_IN_csf = csFrom.convertDec(BASE);
//		String test = csFrom.width(num);//test = k
//		test = csFrom.pow(BASE_IN_csf,test);//c^k
//		test = csFrom.multiply(test,num);//f*c^k = f <<_c k
		
		//for these comments, f is the fractional part of num in base b (num at this point in the algorithm), b is the source base, c is the destination base, and k is the width of n in base b
		//what we've done by dropping the "." is left shift num by its width k
		//therefore if we left-shift this number by k *in base c * we will have aligned the two
		//in order for the number to then be correct we have to take off the last k arbits that we calculated, in effect right-shifting the number back into place, but in base b, and truncating it
		//what we've actually done, then, is left shift num in base b, left shift it in base c, then right shift it back in base b
		//more succinctly, we take: ((f <<_b k) <<_c k) >>_b k = f <<_c k
		//if we then convert this into base c as an integer, it will be equal to the fractional part of a in base c
		//if we right-shift this by k again in base c we get f. this is exactly what we do when we put this number after the "." in the return statement
		//it would technically be correct to keep the fractional part that results when doing the final ">>_b k", but to do so would require us to first convert with partials in order to convert with partials
		//interesting note here is that this is, in a sense, the reason we get infinite repeating base n representations. This doesn't happen if the
		//base c representation has finite length. If that's the case, the fractional part for the new number will be 0
		//example if you don't believe me: 1.5_10 => _2
		//thus, shifting then truncating is a good approximation
		
		//note: it would, you're right, make more sense to just take (c^k)_b * (f >>_b k), but the assumption with this algorithm is (and was) that we only want to deal with integers here
		//essentially, this is intended as the entry point into partial numbers, so we can't define it in terms of partial numbers
		//even if we do this we still have to do both the multiply and the pow, so we don't get any computational savings
		
//		num = csFrom.leftShift()
//
//		String convNum = csFrom.multiply(csFrom.pow(BASE_IN_csf,csFrom.width(num)),num);//(num <<_c k) <<_b k
//		convNum = convNum.substring(0,convNum.length()-num.length());//right-shift num by k to get num <<_c k
//
//		return wholePart + point + nZeroes(leftoverShift) + convert(csFrom,csFrom.round(convNum,1));//putting (num <<_c k) to the right of the point is equivalent to right shifting by k. We add the extra zeroes lost when left shifting
		//(the leading ones right after the point) in order to complete the shift right by k. It's complete in b because we drop the last k digits while still in b, but not in c unless there were no zeroes after the point
	}

	//	 String convert(long num)
	//	 {
	//		 String r = "";
	//		 double log_b = (Math.log(num)) / (Math.log(BASE));
	//		 for(int exp = (int)Math.floor(log_b); exp >= 0; exp--)
	//		 {
	//			 if(num - Math.pow(BASE,exp) >= 0)
	//			 {
	//				 double test = Math.pow(BASE,exp);
	//				 num -= Math.pow(BASE,exp);
	//				 r += alpha[1];
	//				 int c = 2;
	//				 while(num - Math.pow(BASE,exp) >= 0)
	//				 {
	//					 num -= Math.pow(BASE,exp);
	//					 r = replaceStringIndex(r,r.length()-1,alpha[c]);
	//					 c++;
	//				 }
	//			 }
	//			 else
	//			 {
	//				 r += alpha[0];
	//			 }
	//		 }
	//
	//		 return r;
	//	 }

	String log(CountSystem csFrom, String num)
	{
		if(BASE.equals(csFrom.BASE))
		{
			return Integer.toString(num.length());
		}
		if(num.equals(csFrom.zero()))
		{
			return csFrom.zero();
		}

		String r = csFrom.zero();
		String BASE_csf = csFrom.convertDec(BASE);
		String check = csFrom.pow(BASE_csf,r);
		while(csFrom.lessThan(csFrom.pow(BASE_csf,r),num) || csFrom.pow(BASE_csf,r).equals(num))
		{
			r = csFrom.increment(r);
		}
		return csFrom.decrement(r);
	}

	String logDec(String num)
	{
		if(BASE.equals("10"))
		{
			return Integer.toString(num.length());
		}
		if(num.equals(d.zero()))
			return d.zero();

		String r = d.zero();
		while(d.lessThan(d.pow(BASE,r),num) || d.pow(BASE,r).equals(num))
		{
			r = d.increment(r);
		}
		return d.decrement(r);
	}

	String convertDec(String num)
	{
		if(num.charAt(0) == neg)return neg + convertDec(num.substring(1));
		String r = "";
		if(BASE.equals("10"))
			return num;

		for(String exp = logDec(num); !d.lessThan(exp,d.zero()); exp = d.decrement(exp))
		{
			String current = d.pow(BASE,exp);
			num = d.subtract(num,current);
			int c = 0;
			while(num.charAt(0) != neg)
			{
				num = d.subtract(num,current);
				c++;
			}
			r += alpha[c];
			num = d.add(num,current);
		}

		return r;
	}
	
	
	String convert(CountSystem csFrom, String n)
	{
		Decimal dec = new Decimal();
		String BASE_csf = convertSub(dec,csFrom.BASE);
		String exp = convertSub(dec,Integer.toString(n.length()-1));
		int loc = 0;
		
		String r = zero();
		while(!lessThan(exp,zero()))
		{
			String xi = convertSub(dec,Integer.toString(csFrom.alphaIndex(n.charAt(loc))));
			String bcsfpow = pow(BASE_csf,exp);
			r = add(r,multiply(bcsfpow,xi));
			exp = decrement(exp);
			loc++;
		}
		return r;
	}

	/*/
	converts FROM csFrom.BASE TO BASE
	/*/
	String convertSub(CountSystem csFrom, String num)
	{
		if(num.charAt(0) == neg)return neg + convert(csFrom,num.substring(1));
		StringBuilder r = new StringBuilder();
		if(BASE.equals(csFrom.BASE))
			return num;
		if(csFrom.BASE.equals("10"))
			return convertDec(num);

		String BASE_csf = csFrom.convertDec(BASE);
		for(String exp = log(csFrom,num); !csFrom.lessThan(exp,Character.toString(csFrom.alphaAt(0))); exp = csFrom.decrement(exp))
		{
			String current = csFrom.pow(BASE_csf,exp);
			num = csFrom.subtract(num,current);
			int c = 0;
			while(num.charAt(0) != neg)
			{
				num = csFrom.subtract(num,current);
				c++;
			}
			r.append(alpha[c]);
			num = csFrom.add(num,current);
		}

		return r.toString();
	}

	String convertBinSearch_GetNext(CountSystem csFrom, String num, String exp, String ourBaseInTheirBASE)
	{
		Binary bin = new Binary();
		String upperBound = csFrom.convertDec(decrement(BASE));
		String ubBin = bin.convertDec(decrement(BASE));
		String lowerBound = csFrom.zero();
		String lbBin = bin.zero();
		String baseExp = csFrom.pow(ourBaseInTheirBASE,exp);
		while(csFrom.lessThan(lowerBound,upperBound) || lowerBound.equals(upperBound))
		{
			String midBin = bin.rightShift(bin.add(ubBin,lbBin),1);
			if(midBin.indexOf(point) != -1)
				midBin = midBin.substring(0,midBin.indexOf(point));
			String middle = csFrom.convert(bin,midBin);//special method?
			String checkVal = csFrom.subtract(num,csFrom.multiply(baseExp,middle));
			if(csFrom.lessThan(checkVal,csFrom.zero()))
			{
				//less
				upperBound = csFrom.decrement(middle);
				ubBin = bin.decrement(midBin);
			}
			else if(csFrom.lessThan(checkVal,baseExp))
			{
				//correct
				return middle;
			}
			else
			{
				//more
				lowerBound = csFrom.increment(middle);
				lbBin = bin.increment(midBin);
			}
		}
		System.out.println("ERROR CountSystem (convertBinSearch_GetNext() was probably given the wrong value for exp or csFrom)");
		System.exit(2);
		return null;
	}

	String convertBinSearch(CountSystem csFrom, String num)
	{
		String r = "";
		Decimal dec = new Decimal();
		String ourBaseInTheirBASE = csFrom.convertDec(BASE);
		for(String exp = log(csFrom,num); !csFrom.lessThan(exp,csFrom.zero()) && !exp.equals(zero()); exp = csFrom.decrement(exp))
		{
			String digitCSF = convertBinSearch_GetNext(csFrom,num,exp,ourBaseInTheirBASE);
			char digit = alpha[Integer.parseInt(dec.convert(csFrom,digitCSF))];
			num = csFrom.subtract(num,csFrom.multiply(csFrom.pow(ourBaseInTheirBASE,exp),digitCSF));
			r += digit;
		}
		r += alpha[Integer.parseInt(dec.convert(csFrom,num))];
		return r;
	}

	/*/
	asymptotically identical performance to method 1 (typically about half the time required)
	/*/
	String convertDec_Method2(String num)
	{
		if(BASE.equals("10")) return num;
		if(num.charAt(0) == neg) return neg + convertDec_Method2(num.substring(1));

		String r = "0";
		String num10 = "";
		if(BASE_INT > 10)num10 += alpha[10];
		else num10 = convertDec("10");
		int c = 0;


		for(String exp = convertDec(Integer.toString(num.length() - 1)); !exp.equals(neg + alphaAt(1)); exp = decrement(exp))
		{
			String multRight = "";
			if(BASE_INT > 10)multRight += alpha[Integer.parseInt(num.substring(c,c+1))];
			else multRight = convertDec(num.substring(c,c+1));
			r = add(r,multiply(pow(num10,exp),multRight));
			c++;
		}
		return r;
	}

	/*/
	DEPRECATED, use Decimal.convert(CountSystem,conv) instead
	/*/
	String convertTo_10(String conv)
	{
		if(conv == null || conv.length() == 0)return null;
		if(BASE.equals("10"))
			return conv;
		Decimal d = new Decimal();

		String r = "0";
		int c = 0;
		for(String exp = Integer.toString(conv.length() - 1); !exp.equals("-1"); exp = d.decrement(exp))
		{
			r = d.add(r,d.multiply(d.pow(BASE,exp),Integer.toString(alphaIndex(conv.charAt(c)))));
			c++;
		}
		return r;
	}


	String increment(String num)
	{
		return increment(num,num.length()-1);
	}

	String increment(String num, int digitPos)
	{
		if(num.charAt(digitPos) == alpha[BASE_INT - 1])
		{
			num = replaceStringIndex(num,digitPos,alpha[0]);
			if(digitPos == 0)										//string is all alpha[BASE-1]
			{
				num = alpha[1] + num;
				return num;
			}
		}
		else
		{
			num = replaceStringIndex(num,digitPos,alpha[alphaIndex(num.charAt(digitPos))+1]);
			return num;
		}

		return increment(num,digitPos - 1);
	}

	String incrementIterative(String num)
	{
		for(int arbitPos = num.length()-1; arbitPos >= 0; arbitPos--)
		{
			if(num.charAt(arbitPos) == alpha[BASE_INT - 1])
			{
				num = replaceStringIndex(num,arbitPos,alpha[0]);
				if(arbitPos == 0)
				{
					return alpha[1] + num;
				}
			}
			else
			{
				return replaceStringIndex(num,arbitPos,alpha[alphaIndex(num.charAt(arbitPos))+1]);
			}
		}
		System.out.print("ERROR CountSystem (incrementIterative() this statement should be unreachable)");
		System.exit(-1);
		return null;
	}


	String decrement(String num)
	{
		return decrement(num,num.length()-1);
	}

	String decrement(String num, int digitPos)
	{
		if(num.equals(Character.toString(alpha[0])))return Character.toString(neg) + alpha[1];
		if(num.charAt(0) == neg)return neg + increment(num.substring(1));
		if(num.charAt(digitPos) != alpha[0])
		{
			num = replaceStringIndex(num,digitPos,alpha[alphaIndex(num.charAt(digitPos)) - 1]);
			if(digitPos == 0 && num.charAt(digitPos) == alpha[0] && num.length() > 1)
			{
				num = num.substring(1);
			}
			return num;
		}
		else
		{
			if(digitPos == 0 && num.length() > 1)
			{
				num = num.substring(1);
				return num;
			}
			else
			{
				num = replaceStringIndex(num,digitPos,alpha[BASE_INT - 1]);
			}
		}

		return decrement(num,digitPos-1);
	}

	int alphaIndex(char c)
	{
		int r;
		for(r = 0; r < BASE_INT; r++)
		{
			if(alpha[r] == c) return r;
		}
		return -1;
	}

	/*
	 * Theta(max(n,m)) performance
	 */
	String add(String a, String b)
	{
		if(a.charAt(0) == neg && b.charAt(0) != neg)
		{
			return subtract(b,a.substring(1));
		}
		if(a.charAt(0) != neg && b.charAt(0) == neg)
		{
			return subtract(a,b.substring(1));
		}
		if(a.charAt(0) == neg && b.charAt(0) == neg)
		{
			return neg + add(a.substring(1),b.substring(1));
		}

		int shift = 0;
		if(a.contains(Character.toString(point)) || b.contains(Character.toString(point)))
		{
			int np = a.length() - a.indexOf(point) - 1;
			int mp = b.length() - b.indexOf(point) - 1;
			shift = Integer.max(np,mp);
			a = leftShift(a,shift);
			b = leftShift(b,shift);
		}

		int posA = a.length() - 1, posB = b.length() - 1;
		String r = "";

		int indexSum = 0;
		while(posA >= 0 && posB >= 0)
		{
			indexSum += alphaIndex(a.charAt(posA)) + alphaIndex(b.charAt(posB));
			if(indexSum < BASE_INT)
			{
				r = Character.toString(alpha[indexSum]) + r;
				indexSum = 0;
			}
			else
			{
				indexSum = indexSum % BASE_INT;
				r = Character.toString(alpha[indexSum]) + r;
				indexSum = 1;
				if(posA == 0 && posB == 0)r = Character.toString(alpha[1]) + r;
			}
			posA--;
			posB--;
		}

		if(indexSum != 0 && posA >= 0)
		{
			int pl = a.length();
			a = increment(a,posA);
			if(pl < a.length())posA++;
		}

		if(indexSum != 0 && posB >= 0)
		{
			int pl = b.length();
			b = increment(b,posB);
			if(pl < b.length())posB++;
		}

		while(posA >= 0)
		{
			r = Character.toString(a.charAt(posA)) + r;
			posA--;
		}

		while(posB >= 0)
		{
			r = Character.toString(b.charAt(posB)) + r;
			posB--;
		}

		while(r.length() > 1 && r.charAt(0) == alpha[0])
		{
			r = r.substring(1);
		}

		return rightShift(r,shift);
	}
	
	String removeExtraneousZeroes(String n)
	{
		while(n.charAt(0) == alpha[0] && n.length() > 1 && n.charAt(2) != point) n = n.substring(1);
		while(n.contains(Character.toString(point)) && (n.charAt(n.length()-1) == alpha[0] || n.charAt(n.length() - 1) == point))
			n = n.substring(0,n.length()-1);
		if(n.equals(Character.toString(point)) || n.equals(""))n = zero();
		if(n.charAt(0) == point)n = zero() + n;
		return n;
	}
	
	int min(int x, int y)
	{
		return x < y ? x : y;
	}

	boolean lessThan(String a, String b)
	{
		a = removeExtraneousZeroes(a);
		b = removeExtraneousZeroes(b);
		if(a.equals(b))return false;
		if(a.equals(Character.toString(alpha[0])))return b.charAt(0) != neg;
		if(b.equals(Character.toString(alpha[0])))return a.charAt(0) == neg;
		if(!a.contains(Character.toString(point)))a = a + point + alpha[0];
		if(!b.contains(Character.toString(point)))b = b + point + alpha[0];
		if(a.indexOf(point) < b.indexOf(point))return true;
		if(a.indexOf(point) > b.indexOf(point))return false;

		int checkIndex = 0;
		while(checkIndex < min(a.length(),b.length()))
		{
			if(a.charAt(checkIndex) != point && b.charAt(checkIndex) == point)
			{
				return false;
			}
			if(a.charAt(checkIndex) == point && b.charAt(checkIndex) != point)
			{
				return true;
			}
			if(a.charAt(checkIndex) != point && b.charAt(checkIndex) != point)
			{
				if (alphaIndex(a.charAt(checkIndex)) > alphaIndex(b.charAt(checkIndex))) return false;
				if (alphaIndex(a.charAt(checkIndex)) < alphaIndex(b.charAt(checkIndex))) return true;
			}
			checkIndex++;
		}
		return false;//unreachable statement: we will have returned false on line 568 in this case bc a = b
	}

	/*
	 * O(log base BASE (n)) performance
	 */
	String subtract(String a, String b)
	{
		if(a.charAt(0) == neg && b.charAt(0) != neg)
		{
			return neg + add(a.substring(1),b);
		}
		if(b.charAt(0) == neg)
		{
			return add(a,b.substring(1));
		}
		int shift = 0;
		if(a.contains(Character.toString(point)) || b.contains(Character.toString(point)))
		{
			int np = a.length() - a.indexOf(point) - 1;
			int mp = b.length() - b.indexOf(point) - 1;
			shift = Integer.max(np,mp);
			a = leftShift(a,shift);
			b = leftShift(b,shift);
		}
		
		if(lessThan(a,b))
		{
			return neg + rightShift(subtract(b,a),shift);//this is to deal with numbers that contain partials in the wrong order
		}

		int posA = a.length() - 1, posB = b.length() - 1;
		String r = "";

		int indexDiff = 0;
		while(posA >= 0 && posB >= 0)
		{
			indexDiff += alphaIndex(a.charAt(posA)) - alphaIndex(b.charAt(posB));
			if(indexDiff >= 0)
			{
				r = Character.toString(alpha[indexDiff]) + r;
				indexDiff = 0;
			}
			else
			{
				indexDiff = BASE_INT + indexDiff;
				r = Character.toString(alpha[indexDiff]) + r;
				indexDiff = -1;
				if(posA == 0 && posB == 0)r = neg + r;
			}
			posA--;
			posB--;
		}

		if(indexDiff != 0 && posA >= 0)
		{
			int lO = a.length();
			a = decrement(a.substring(0,posA+1),posA) + a.substring(posA+1);
			if(a.charAt(0) == neg)
			{
				a = replaceStringIndex(a,1,alpha[BASE_INT-1]);
				a = a.substring(1);
			}
			if(lO > a.length())
			{
				posA--;
			}
		}

		if(indexDiff != 0 && posB >= 0)
		{
			int lO = b.length();
			b = decrement(b.substring(0,posB+1),posB) + b.substring(posB+1);
			if(b.charAt(0) == neg)
			{
				b = replaceStringIndex(b,1,alpha[BASE_INT-1]);
				b = b.substring(1);
			}
			if(lO > b.length())
			{
				posB--;
			}
		}

		while(posA >= 0)
		{
			r = Character.toString(a.charAt(posA)) + r;
			posA--;
		}

		while(posB >= 0)
		{
			r = Character.toString(b.charAt(posB)) + r;
			posB--;
		}

		while(r.length() > 1 && r.charAt(0) == alpha[0])
		{
			r = r.substring(1);
		}

		return rightShift(r,shift);
	}

	String nZeroes(int n)
	{
		String r = "";
		for(int i = 0; i < n; i++)
		{
			r += alpha[0];
		}
		return r;
	}

	/*
	in base B
	 */
	String width(String n)
	{
		String r = zero();
		for(int i = 0; i < n.length(); i++)
		{
			r = increment(r);
		}
		return r;
	}

	String multiply(String a, String b)
	{
		String r;
		int shift = 0;
		if(a.contains(Character.toString(point)))
		{
			int np = a.length() - a.indexOf(point) - 1;
			a = leftShift(a,np);
			shift += np;
		}
		if(b.contains(Character.toString(point)))
		{
			int mp = b.length() - b.indexOf(point) - 1;
			b = leftShift(b,mp);
			shift += mp;
		}
		r = multiplyKaratsuba(a,b);
		return rightShift(r,shift);
	}

	/*
	WAY WAY WAY FASTER than multiply
	 */
	String multiplyKaratsuba(String a, String b)
	{
		int k = Integer.min(a.length(),b.length())/2;//this choice makes things fast
		if(k <= 1)
		{
			return multiplyRecursive(a,b);
		}
		//k = min(m,n)-1
		String x0 = a.substring(a.length()-k);
		String x1 = a.substring(0,a.length()-k);
		while(x0.charAt(0) == alpha[0] && x0.length() > 1)
		{
			x0 = x0.substring(1);
		}

		//a = x1*B^k + x0
		String y0 = b.substring(b.length()-k);
		String y1 = b.substring(0,b.length()-k);
		while(y0.charAt(0) == alpha[0] && y0.length() > 1)
		{
			y0 = y0.substring(1);
		}
		//b = y1*B^k + y0
		//get these ^^ values by somehow dividing a and b by B^k
		//this isn't a problem since that's just going to be a right shift
		//z2 = x1*y1
		String z2 = multiplyKaratsuba(x1,y1);
		//z0 = x0*y0
		String z0 = multiplyKaratsuba(x0,y0);
		//z1 = (x1+x0)*(y1+y0)-z2-z0
		String z1 = subtract(subtract(multiplyKaratsuba(add(x1,x0),add(y1,y0)),z2),z0);
		//ANS = (z2 << 2k) + (z1 << k) + z0
		return add(leftShift(z2,k<<1),add(leftShift(z1,k),z0));
	}

	/*
	 * a little slower, but not recursive
	 * this method should never be called with a non-integer (it doesn't know what to do with them)
	 */
	String multiplyMCSAP(String a, String b)
	{
		if(b.equals(one()))
		{
			return a;
		}
		if(a.equals(one()))
		{
			return b;
		}
		if(a.equals(zero()) || b.equals(zero()))
		{
			return zero();
		}

		String r = zero();
		int posA = a.length()-1, aShift = 0;
		while(posA >= 0)
		{
			int posB = b.length()-1, bShift = 0;
			char adig = a.charAt(posA);
			if(adig != alpha[0])
			{
				while(posB >= 0)
				{
					char bdig = b.charAt(posB);
					if(bdig != alpha[0])
					{
						// int product = alphaIndex(adig) * alphaIndex(bdig);
						// String temp = convertDec(Integer.toString(product));
						String product = tt[alphaIndex(adig)][alphaIndex(bdig)];
						product = leftShift(product,aShift+bShift);
						r = add(r,product);
					}
					posB--;
					bShift++;
				}
			}
			posA--;
			aShift++;
		}

		return r;
	}

	/*
	 * faster, but recursive
	 * this method should never be called with a non-integer (it doesn't know what to do with them)
	 */
	String multiplyRecursive(String a, String b)
	{
		if(lessThan(a,b))return multiply(b,a);

		//implement partial number multiplication
		String r = zero();
		if(a.length() == 1 && b.length() == 1)
		{
			int aIndex = alphaIndex(a.charAt(0));
			int bIndex = alphaIndex(b.charAt(0));
			r = tt[aIndex][bIndex];
			// for(String i = Character.toString(alpha[0]); lessThan(i,b); i = increment(i))
			// {
			//	 r = add(r,a);
			// }
			return r;
		}
		int posA = a.length() - 1, exp = 0;

		while(posA >= 0)
		{
			r = add(r,multiplyRecursive(b,Character.toString(a.charAt(posA))) + nZeroes(exp));
			posA--;
			exp++;
		}

		return r;
	}

	String pow(String a, String b)
	{
		if(b.equals(Character.toString(alpha[0])))return one();
		if(a.equals(BASE))return leftShift(one(),b);
		String r = a;
		for(String i = Character.toString(alpha[1]); lessThan(i,b); i = increment(i))
		{
			r = multiply(r,a);
		}
		return r;
	}

	String mod(String a, String b)
	{
		if(!(a.equals(b) || lessThan(b,a)) || a.substring(0,1).equals(Character.toString(neg)))return null;

		while(a.equals(b) || lessThan(b,a))
		{
			a = subtract(a,b);
		}

		return a;
	}
	
	/*
	default division is a/b to integer
	 */
	String divide(String a, String b)
	{
		return divideGold(a,b,1);
	}
	
	String divide(String a, String b, int places)
	{
		return divideGold(a,b,places);
	}

	/*
	 * deprecated as of 2018-06-12. Use dividePartial instead
	 */
	String divideOld(String a, String b, boolean showRemainder)
	{
		String div = Character.toString(alpha[0]);
		while(!lessThan(a,b) && !a.substring(0,1).equals(Character.toString(neg)))
		{
			a = subtract(a,b);
			div = increment(div);
		}

		if(!a.equals(Character.toString(alpha[0])) && showRemainder)
		{
			return div + " R" + a;
		}

		return div;
	}

	/*
	 * deprecated as of 2018-06-12. Use dividePartial instead
	 */
	String divideOld(String a, String b)
	{
		String div = Character.toString(alpha[0]);
		while(!lessThan(a,b) && !a.substring(0,1).equals(Character.toString(neg)))
		{
			a = subtract(a,b);
			div = increment(div);
		}

		if(!a.equals(Character.toString(alpha[0])))
		{
			return div + " R" + a;
		}

		return div;
	}
	
	/*
	if place is positive, then round to integer (tens, ones, etc.)
	if it's negative, then round to decimal (tenths, hundreths, etc.)
	zero means no round
	 */
	String round(String n, int place)
	{
		if(place == 0) return n;
		if(!n.contains(Character.toString(point)))n = n + point + alpha[0];
		int last;
		last = n.indexOf(point) - place + 1;
		if(place == 1)
		{
			//remove the point and continue as normal
			n = n.substring(0,n.indexOf(point)) + n.substring(n.indexOf(point)+1);
		}
		if (last > n.length() - 1)
			return n;
		int charVal = alphaIndex(n.charAt(last));
		String sub = n.substring(0,last);
		if(charVal >= BASE_INT/2)
		{
			//round up
			sub = increment(sub);
		}
		return removeExtraneousZeroes(sub + nZeroes(place-1));
	}
	
	/*
	fastest division algorithm as of 2018-08-18, implementation of the Goldschmidt division algorithm.
	This version uses the round() method to ensure accuracy up to <place> digits. See round() for details.
	
	PREFERRED DIVISION METHOD 2018-08-18
	 */
	String divideGold(String a, String b, int place)
	{
		if(place == 0)
		{
			System.out.println("ERROR: CountSystem::divideGold: <place> was set to zero. This would cause an infinite loop!");
			System.exit(4);
		}
		if(!a.contains(Character.toString(point)))a = a + point + alpha[0];
		if(!b.contains(Character.toString(point)))b = b + point + alpha[0];
		
		int shift = b.indexOf(point);
		a = rightShift(a,shift);
		b = rightShift(b,shift);
		if(lessThan(b,zero() + point + alpha[BASE_INT/2]))
		{
			//a,b *= 2
			a = add(a,a);
			b = add(b,b);
		}
		//initialize st 0.5 <= b <= 1
//		String norm = subtract(one(),subtract(b,b.substring(0,1)));
		
		String e = subtract(one(),b);
		String q = a;
		String qprev = null;
		while(qprev == null || !round(q,place).equals(round(qprev,place)))
		{
			qprev = q;
			q = multiply(q,add(one(),e));
			e = multiply(e,e);
		}
		return round(q,place);
	}
 
	/*
	see divideGold() for more details.
	This version just calculates to a simple integer precision.
	 */
	String divideGoldPrecision(String a, String b, int precision)
	{
		if(!a.contains(Character.toString(point)))a = a + point + alpha[0];
		if(!b.contains(Character.toString(point)))b = b + point + alpha[0];
		
		int shift = b.indexOf(point)-1;
		a = rightShift(a,shift);
		b = rightShift(b,shift);
		String norm = subtract(one(),subtract(b,b.substring(0,1)));
		a = multiply(a,norm);
		b = multiply(b,norm);
		//initialize st 0.5 <= b <= 1
		
		String e = subtract(one(),b);
		String q = a;
		for(int i = 0; i < precision; i++)
		{
			q = multiply(q,add(one(),e));
			e = multiply(e,e);
		}
		return q;
	}

	String rightShift(String n, int places)
	{
		if(n.equals(zero()))
		{
			return n;
		}
		int pointPos = n.indexOf(point);
		if(pointPos != -1)
		{
			n = n.replace(Character.toString(point),"");
			pointPos--;
		}
		else
		{
			pointPos = n.length() - 1;
		}

		//point is sitting *after* the current pointPos index
		for(int i = 0; i < places; i++)
		{
			if(--pointPos < 0)
			{
				pointPos = 0;
				if(n.charAt(0) != neg) n = alpha[0] + n;
				else n = n.substring(0,1) + alpha[0] + n.substring(1);
			}
		}

		n =  n.substring(0,pointPos + 1) + point + n.substring(pointPos + 1);
		int pos = n.length()-1;
		for(; pos > 0 && (n.charAt(pos) == alpha[0]); pos--)
		{
			if(n.charAt(pos) == point) break;
		}
		if(n.charAt(pos) == point) pos--;
		return n.substring(0,pos+1);
	}

	/*
	 * places is in base BASE
	 */
	String leftShift(String n, String places)
	{
		Decimal dec = new Decimal();
		int placesInt = Integer.parseInt(dec.convert(this,places));
		return leftShift(n,placesInt);
	}

	String leftShift(String n, int places)
	{
		if(n.equals(zero()) || places == 0)
		{
			return n;
		}
		int pointPos = n.indexOf(point);
		if(pointPos != -1)
		{
			n = n.replace(Character.toString(point),"");
			pointPos--;
		}
		else
		{
			pointPos = n.length()-1;
		}

		for(int i = 0; i < places; i++)
		{
			if(++pointPos >= n.length())
			{
				n = n + alpha[0];
			}
		}


		n = n.substring(0,pointPos + 1) + point + n.substring(pointPos + 1);
		int posEnd = 0;
		if(n.charAt(0) == neg)posEnd++;
		for(; posEnd <= n.length() && n.charAt(posEnd) == alpha[0]; posEnd++){}
		if(n.indexOf(point) == 0) n = alpha[0] + n;
		if(n.indexOf(point) == 1 && n.charAt(0) == neg) n = n.substring(0,1) + alpha[0] + n.substring(1);
		int posBeg = n.length()-1;
		for(; posBeg > 0 && (n.charAt(posBeg) == alpha[0]); posBeg--)
		{
			if(n.charAt(posBeg) == point) break;
		}
		if(n.charAt(posBeg) == point) posBeg--;

		return n.substring(posEnd,posBeg+1);
	}

	/*
	This algorithm is nowhere near as fast as Goldschmidt (see divideGold()), but it's still really interesting.
	 */
	String dividePartial(String a, String b)
	{
		//step 1: convert to whole numbers
		int aArpoint = a.indexOf(point);
		if(aArpoint != -1 && aArpoint != a.length()-1)
		{
			//left-shift both a and b far enough to make a a whole number
			//correct amount to shift is a.length() - aArpoint + 1
			int shiftAmt = a.length()-aArpoint-1;
			a = leftShift(a,shiftAmt);
			b = leftShift(b,shiftAmt);
		}
		int bArpoint = b.indexOf(point);
		if(bArpoint != -1 && bArpoint != a.length()-1)
		{
			int shiftAmt = b.length()-bArpoint-1;
			a = leftShift(a,shiftAmt);
			b = leftShift(b,shiftAmt);
		}

		//convert a to base b
		//use native countsystem base to form the MCSAP
		//mcsap needs a countsystem object with our number base, so we just send it this object
		MetaCountSystemArbitraryPrecision aMCSAP = new MetaCountSystemArbitraryPrecision(b,this);
		aMCSAP.convert(this,a);

		//right-shift a by 1
		aMCSAP.rightShift(1);

		//convert aMCSAP to BASE
		
		return aMCSAP.convertOutWithPartials(this);
	}

	String dividePartial(String a, String b, int precision)
	{
		//step 1: convert to whole numbers
		int aArpoint = a.indexOf(point);
		if(aArpoint != -1 && aArpoint != a.length()-1)
		{
			//left-shift both a and b far enough to make a whole
			//correct amount to shift is a.length() - aArpoint + 1
			int shiftAmt = a.length()-aArpoint-1;
			a = leftShift(a,shiftAmt);
			b = leftShift(b,shiftAmt);
		}//O(n)
		int bArpoint = b.indexOf(point);
		if(bArpoint != -1 && bArpoint != a.length()-1)
		{
			int shiftAmt = b.length()-bArpoint-1;
			a = leftShift(a,shiftAmt);
			b = leftShift(b,shiftAmt);
		}//O(m)

		//convert a to base b
		//use native countsystem base to form the MCSAP
		MetaCountSystemArbitraryPrecision aMCSAP = new MetaCountSystemArbitraryPrecision(b,this);//O(1)
		aMCSAP.convert(this,a);//O(n^3*m)

		//right-shift a by 1
		aMCSAP.rightShift(1);//O(1)

		//convert aMCSAP to BASE
		
		return aMCSAP.convertOutWithPartials(this,precision);
	}
	
	String factorial(String n)
	{
		String acc = n;
		for(String i = increment(one()); lessThan(i,n); i = increment(i))
		{
			acc = multiply(acc,i);
		}
		return acc;
	}
	
	/*
	Does a divide b?
	 */
	boolean divides(String a, String b)
	{
		String q = divide(b,a);//take q = b/a
		String p = multiply(q,a);//if a divides b, then this equals b
		return p.equals(b);
	}
}
