/**
 * .
 *
 * @author Daniel Taylor (DTIII)
 * @version .._-
 */
public abstract class CountSystem
{
    private static final char neg = '-'; //change this if you need the '-' character in your alpha
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

    /*/
    see convertWithPartials(CountSystem, String) for details
    /*/
    String convertWithPartials(CountSystem csFrom, String num, String precision)
    {
        Decimal d = new Decimal();
        String precisionDec = d.convert(csFrom,precision);
        int precisionInt = Integer.parseInt(precisionDec);
        String wholePart = "";
        if(num.charAt(0) == '.')
            wholePart = Character.toString(csFrom.alphaAt(0));
        else
            wholePart = convert(csFrom,num.substring(0,num.indexOf(".")));
        if(precision.equals(csFrom.zero()))
        {
            return wholePart;
        }
        num = num.substring(num.indexOf(".")+1);
        String numLength = csFrom.convertDec(Integer.toString(num.length()));
        while(csFrom.lessThan(numLength,precision))
        {
            num += csFrom.alphaAt(0);
            numLength = csFrom.convertDec(Integer.toString(num.length()));
        }
        if(num.length() > precisionInt)
        {
            precisionInt = num.length();
            //precision = csFrom.convertDec(Integer.toString(precisionInt));
            precision = numLength;
        }
        String BASE_csf = csFrom.convertDec(BASE);
        String convNum = csFrom.multiply(csFrom.pow(BASE_csf,precision),num);
        convNum = convNum.substring(0,convNum.length()-precisionInt);
        if(wholePart.equals(""))
            wholePart = zero();
        return wholePart + "." + convert(csFrom,convNum);
    }

    /*/
    ALGORITHM:
    convert the part left of the point (".") normally, set it aside
    get rid of everything left of the point, as well as the point itself
    [this part can be written as (numerator = that number) / (BASE ^ k), where k is some constant
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
        String wholePart = convert(csFrom,num.substring(0,num.indexOf(".")));
        num = num.substring(num.indexOf(".")+1);
        String BASE_csf = csFrom.convertDec(BASE);
        String convNum = csFrom.multiply(csFrom.pow(BASE_csf,Integer.toString(num.length())),num);
        convNum = convNum.substring(0,convNum.length()-num.length());

        return wholePart + "." + convert(csFrom,convNum);
    }

    //     String convert(long num)
    //     {
    //         String r = "";
    //         double log_b = (Math.log(num)) / (Math.log(BASE));
    //         for(int exp = (int)Math.floor(log_b); exp >= 0; exp--)
    //         {
    //             if(num - Math.pow(BASE,exp) >= 0)
    //             {
    //                 double test = Math.pow(BASE,exp);
    //                 num -= Math.pow(BASE,exp);
    //                 r += alpha[1];
    //                 int c = 2;
    //                 while(num - Math.pow(BASE,exp) >= 0)
    //                 {
    //                     num -= Math.pow(BASE,exp);
    //                     r = replaceStringIndex(r,r.length()-1,alpha[c]);
    //                     c++;
    //                 }
    //             }
    //             else
    //             {
    //                 r += alpha[0];
    //             }
    //         }
    //
    //         return r;
    //     }

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

    /*/
    converts FROM csFrom.BASE TO BASE
    /*/
    String convert(CountSystem csFrom, String num)
    {
        if(num.charAt(0) == neg)return neg + convert(csFrom,num.substring(1));
        String r = "";
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
            r += alpha[c];
            num = csFrom.add(num,current);
        }

        return r;
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
            if(midBin.indexOf(".") != -1)
                midBin = midBin.substring(0,midBin.indexOf("."));
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
            if(digitPos == 0)                                        //string is all alpha[BASE-1]
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
     * O(log base BASE (n)) performance
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

        return r;
    }

    boolean lessThan(String a, String b)
    {
        if(a.equals(b))return false;
        if(a.equals(Character.toString(alpha[0])))return b.charAt(0) != neg;
        if(b.equals(Character.toString(alpha[0])))return a.charAt(0) == neg;
        if(a.length() > b.length())return false;
        if(a.length() < b.length())return true;

        int checkIndex = 0;
        while(checkIndex < a.length())
        {
            if(alphaIndex(a.charAt(checkIndex)) > alphaIndex(b.charAt(checkIndex)))return false;
            if(alphaIndex(a.charAt(checkIndex)) < alphaIndex(b.charAt(checkIndex)))return true;
            checkIndex++;
        }
        return false;
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
        if(lessThan(a,b))
        {
            return neg + subtract(b,a);
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

        return r;
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
     * a little slower, but not recursive
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
     */
    String multiply(String a, String b)
    {
        if(lessThan(a,b))return multiply(b,a);
        String r = zero();
        if(a.length() == 1 && b.length() == 1)
        {
            int aIndex = alphaIndex(a.charAt(0));
            int bIndex = alphaIndex(b.charAt(0));
            r = tt[aIndex][bIndex];
            // for(String i = Character.toString(alpha[0]); lessThan(i,b); i = increment(i))
            // {
            //     r = add(r,a);
            // }
            return r;
        }
        int posA = a.length() - 1, exp = 0;

        while(posA >= 0)
        {
            r = add(r,multiply(b,Character.toString(a.charAt(posA))) + nZeroes(exp));
            posA--;
            exp++;
        }

        return r;
    }

    String pow(String a, String b)
    {
        if(b.equals(Character.toString(alpha[0])))return "1";
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
     * deprecated as of 2018-06-12. Use dividePartial instead
     */
    String divide(String a, String b, boolean showRemainder)
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
    String divide(String a, String b)
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

    String rightShift(String n, int places)
    {
        int pointPos = n.indexOf(".");
        if(pointPos != -1)
        {
            n = n.replace(".","");
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

        n =  n.substring(0,pointPos + 1) + "." + n.substring(pointPos + 1);
        int pos = n.length()-1;
        for(; pos > 0 && (n.charAt(pos) == alpha[0]); pos--)
        {
            if(n.charAt(pos) == '.') break;
        }
        if(n.charAt(pos) == '.') pos--;
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
        int pointPos = n.indexOf(".");
        if(pointPos != -1)
        {
            n = n.replace(".","");
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


        n = n.substring(0,pointPos + 1) + "." + n.substring(pointPos + 1);
        int posEnd = 0;
        if(n.charAt(0) == neg)posEnd++;
        for(; posEnd <= n.length() && n.charAt(posEnd) == alpha[0]; posEnd++){}
        if(n.indexOf(".") == 0) n = alpha[0] + n;
        if(n.indexOf(".") == 1 && n.charAt(0) == neg) n = n.substring(0,1) + alpha[0] + n.substring(1);
        int posBeg = n.length()-1;
        for(; posBeg > 0 && (n.charAt(posBeg) == alpha[0]); posBeg--)
        {
            if(n.charAt(posBeg) == '.') break;
        }
        if(n.charAt(posBeg) == '.') posBeg--;

        return n.substring(posEnd,posBeg+1);
    }

    //MetaCountSystemArbitraryPrecision

    String dividePartial(String a, String b)
    {
        //step 1: convert to whole numbers
        int aArpoint = a.indexOf(".");
        if(aArpoint != -1 && aArpoint != a.length()-1)
        {
            //left-shift both a and b far enough to make a a whole number
            //correct amount to shift is a.length() - aArpoint + 1
            int shiftAmt = a.length()-aArpoint-1;
            a = leftShift(a,shiftAmt);
            b = leftShift(b,shiftAmt);
        }
        int bArpoint = b.indexOf(".");
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
        String r = aMCSAP.convertOutWithPartials(this);

        return r;
    }

    String dividePartial(String a, String b, int precision)
    {
        //step 1: convert to whole numbers
        int aArpoint = a.indexOf(".");
        if(aArpoint != -1 && aArpoint != a.length()-1)
        {
            //left-shift both a and b far enough to make a whole
            //correct amount to shift is a.length() - aArpoint + 1
            int shiftAmt = a.length()-aArpoint-1;
            a = leftShift(a,shiftAmt);
            b = leftShift(b,shiftAmt);
        }//O(n)
        int bArpoint = b.indexOf(".");
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
        String r = aMCSAP.convertOutWithPartials(this,precision);

        return r;
    }

    //     String dividePartial(String a, String b)
    //     {
    //         String wholePart = divide(a,b);
    //         String numerator = null;
    //         if(wholePart.indexOf(" ") != -1)
    //         {
    //             numerator = wholePart.substring(wholePart.indexOf(" ") + 2);
    //         }
    //
    //         if(numerator == null)
    //         return wholePart;
    //
    //         //think of the rest of the division as a conversion of the number (0.[numerator]) in base b into base BASE
    //         //can use base_n for that representation
    //         //base_n won't (necessarily) need a way to handle partial numbers
    //         //but it will need a way to handle arbitrary in-bases for conversion
    //         return "";
    //     }
}
