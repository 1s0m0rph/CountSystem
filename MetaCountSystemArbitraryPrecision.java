/*
NOTE 2018-08-18: This whole class's purpose is to serve as a helper for conversion-based division. The implementation of Goldschmidt division renders it essentially obselete, except as a mathematical and algorithmic exercise.
 */
import java.util.ArrayList;
public class MetaCountSystemArbitraryPrecision
{
    static String BASE;
    static CountSystem cs;
    ArrayList<String> n;
    int point;//the index of the point. If there's only an implicit point, this is -1
    boolean isNegative;

    public MetaCountSystemArbitraryPrecision(String base, CountSystem _cs, ArrayList<String> _n, boolean _isNegative)
    {
        BASE = base;
        cs = _cs;
        n = new ArrayList<String>(_n);
        point = -1;
        isNegative = _isNegative;
    }

    /*
     * value of -2 in _n means skip the partial part
     */
    public MetaCountSystemArbitraryPrecision(String base, CountSystem _cs, ArrayList<String> _n, int _point)
    {
        BASE = base;
        cs = _cs;
        n = null;
        if(_point != -2)
        {
            point = _point;
            n = new ArrayList<String>(_n);
        }
        else
        {
            point = -1;
            n = new ArrayList<String>();
            for(int i = 0; i < _n.size() && !_n.get(i).equals(""); i++)
            {
                n.add(_n.get(i));
            }
        }
        isNegative = false;
    }

    public MetaCountSystemArbitraryPrecision(String base, CountSystem _cs, ArrayList<String> _n)
    {
        BASE = base;
        cs = _cs;
        n = new ArrayList<String>(_n);
        point = -1;
        isNegative = false;
    }

    public MetaCountSystemArbitraryPrecision(String base, CountSystem _cs)
    {
        BASE = base;
        cs = _cs;
        n = new ArrayList<String>();
        n.add(cs.zero());
        point = -1;
        isNegative = false;
    }

    MetaCountSystemArbitraryPrecision widthInternal()
    {
        Decimal d = new Decimal();
        MetaCountSystemArbitraryPrecision r = new MetaCountSystemArbitraryPrecision(BASE,cs);
        r.convert(d,Integer.toString(width()));
        return r;
    }

    int width(){return n.size();}
    String at(int index){return n.get(index);}

    MetaCountSystemArbitraryPrecision zero(){return new MetaCountSystemArbitraryPrecision(BASE,cs);}
    MetaCountSystemArbitraryPrecision one()
    {
        MetaCountSystemArbitraryPrecision r = new MetaCountSystemArbitraryPrecision(BASE,cs);
        r.increment();
        return r;
    }

    void set(ArrayList<String> _n, int _point)
    {
        point = _point;
        n.clear();
        for(String s : _n)
        {
            n.add(s);
        }
    }

    void print()
    {
        if(isNegative)
        {
            System.out.print("NEG ");
        }
        System.out.print(n.get(0));
        for(int i = 1; i < width(); i++)
        {
            System.out.print("-" + n.get(i));
        }
        //System.out.println();
    }

    /*
     * digIT - decimal
     * bIT - binary
     * arbIT - arbitrary
     */
    void increment(int arbitPos)
    {
        for(; arbitPos >= 0; arbitPos--)
        {
            String set = cs.increment(n.get(arbitPos));
            if(!set.equals(BASE))
            {
                n.set(arbitPos,set);
                break;
            }
            set = cs.zero();
            n.set(arbitPos,set);
            if(arbitPos == 0)
            {
                n.add(0,cs.one());
            }
        }
    }

    void increment()
    {
        if(isNegative)
        {
            isNegative = false;
            decrement();
            isNegative = true;
        }
        for(int arbitPos = width()-1; arbitPos >= 0; arbitPos--)
        {
            String set = cs.increment(n.get(arbitPos));
            if(!set.equals(BASE))
            {
                n.set(arbitPos,set);
                break;
            }
            set = cs.zero();
            n.set(arbitPos,set);
            if(arbitPos == 0)
            {
                n.add(0,cs.one());
            }
        }
    }

    void decrement(int arbitPos)
    {
        ArrayList<String> initState = new ArrayList<String>(n);
        for(; arbitPos >= 0; arbitPos--)
        {
            if(at(arbitPos).equals(cs.zero()))
            {
                n.set(arbitPos,cs.decrement(BASE));
                if(arbitPos == 0)
                {
                    n.clear();
                    n.add(cs.zero());
                    break;
                }
            }
            else
            {
                n.set(arbitPos,cs.decrement(at(arbitPos)));
                break;
            }
        }
        removeLeadingZeroes();
    }

    void decrement()
    {
        if(isNegative)
        {
            isNegative = false;
            increment();
            isNegative = true;
        }
        if(equals(zero()))
        {
            isNegative = true;
            n.clear();
            n.add(cs.one());
            return;
        }
        ArrayList<String> initState = new ArrayList<String>(n);
        for(int arbitPos = width()-1; arbitPos >= 0; arbitPos--)
        {
            if(at(arbitPos).equals(cs.zero()))
            {
                n.set(arbitPos,cs.decrement(BASE));
                if(arbitPos == 0)
                {
                    n.clear();
                    n.add(cs.zero());
                    break;
                }
            }
            else
            {
                n.set(arbitPos,cs.decrement(at(arbitPos)));
                break;
            }
        }
        removeLeadingZeroes();
    }

    void removeLeadingZeroes()
    {
        while(width() > 1 && at(0).equals(cs.zero()))
        {
            n.remove(0);
        }
    }

    String log(CountSystem csFrom, String num)
    {
        String ourBaseInCsFrom = csFrom.convert(cs,BASE);
        String checkNum = Character.toString(csFrom.alpha[1]);
        String pow = csFrom.zero();
        while(csFrom.lessThan(checkNum, num) || checkNum.equals(num))
        {
            pow = csFrom.increment(pow);
            checkNum = csFrom.pow(ourBaseInCsFrom,pow);
        }
        return csFrom.decrement(pow);
    }

    /*
     * convert2 helper method
     */
    String convert2BinSearch(CountSystem csFrom, String num, String exp, String ourBaseInTheirBASE)
    {
        Binary bin = new Binary();
        String upperBound = csFrom.convert(cs,cs.decrement(BASE));
        String ubBin = bin.convert(cs,cs.decrement(BASE));
        String lowerBound = csFrom.zero();
        String lbBin = bin.zero();
        String baseExp = bin.convert(csFrom,csFrom.pow(ourBaseInTheirBASE,exp));
        String binum = bin.convert(csFrom,num);
        while(csFrom.lessThan(lowerBound,upperBound) || lowerBound.equals(upperBound))
        {
            //converting to base 2 first speeds up the division
            String midBin = bin.rightShift(bin.add(ubBin,lbBin),1);
            if(midBin.contains("."))
                midBin = midBin.substring(0,midBin.indexOf("."));
            //String middle = csFrom.convert(bin,midBin);//don't bother calculating this until we return
            String checkVal = bin.subtract(binum,bin.multiply(baseExp,midBin));
            if(bin.lessThan(checkVal,bin.zero()))
            {
                //less
                ubBin = bin.decrement(midBin);
            }
            else if(bin.lessThan(checkVal,baseExp))
            {
                //correct
                return csFrom.convert(bin,midBin);
            }
            else
            {
                //more
                lbBin = bin.increment(midBin);
            }
        }
        System.out.println("ERROR MCSAP(convert2BinSearch was probably given the wrong value for exp or csFrom");
        System.exit(2);
        return null;
    }

    /*
     * much faster than convert()

     O(n^3*m)
     (the log goes awway since it's O(m*n^2) in the loop)
     */
    void convert2(CountSystem csFrom, String num)
    {
        //start by finding the biggest digit
        //binary search for the correct value for this digit
        //the next digit is equal to (num - i^(exp) * (this digit)
        //this calculation is done in cs, so speed isn't a concern
        n.clear();
        String ourBaseInTheirBASE = csFrom.convert(cs,BASE);
        for(String exp = log(csFrom,num); !cs.lessThan(exp,csFrom.zero()) && !exp.equals(csFrom.zero()); exp = csFrom.decrement(exp))
        {
            String digit = convert2BinSearch(csFrom,num,exp,ourBaseInTheirBASE);
            num = csFrom.subtract(num,csFrom.multiply(csFrom.pow(ourBaseInTheirBASE,exp),digit));
            n.add(cs.convert(csFrom,digit));
        }
        n.add(cs.convert(csFrom,num));
    }

    /*
     * possibly add to CountSystem? Probably not, the problem is that all the BASEs are in _10
     *
     * XXXXX
     * Deprecated as of 2018-06-12. Use convert2 instead, it's much faster. This method will redirect there until things can be properly refactored.
     * Until then, the code for this will remain commented-out.
     */
    void convert(CountSystem csFrom, String num)
    {
        convert2(csFrom,num);
        //         n.clear();
        //
        //         String BASE_csf = csFrom.convert(cs,BASE);
        //         for(String exp = log(csFrom,num); !csFrom.lessThan(exp,csFrom.zero()) && !exp.equals(csFrom.zero()); exp = csFrom.decrement(exp))
        //         {
        //             String current = csFrom.pow(BASE_csf,exp);
        //             num = csFrom.subtract(num,current);
        //             String c = cs.zero();
        //             //binary search to find the right c/remainder?
        //             //keep a base 2 version of BASE?
        //             //too slow as it is
        //             while(num.charAt(0) != '-')
        //             {
        //                 num = csFrom.subtract(num,current);
        //                 c = cs.increment(c);
        //             }
        //             n.add(c);
        //             num = csFrom.add(num,current);
        //         }
        //         n.add(num);
        //speed boost: once we get down to where exp is zero, that's just the last digit
    }

    boolean equals(MetaCountSystemArbitraryPrecision b)
    {
        if(width() != b.width())
        {
            return false;
        }
        if(isNegative != b.isNegative)
        {
            return false;
        }

        for(int i = 0; i < width(); i++)
        {
            if(!at(i).equals(b.at(i)))
            return false;
        }
        return true;
    }

    /*
     * this number is less than b -> true
     */
    boolean lessThan(MetaCountSystemArbitraryPrecision b)
    {
        if(equals(b))return false;
        if(width() < b.width()) return true;
        if(width() > b.width()) return false;

        for(int checkIndex = 0; checkIndex < width(); checkIndex++)
        {
            if(cs.lessThan(at(checkIndex),b.at(checkIndex)))return true;
            if(cs.lessThan(b.at(checkIndex),at(checkIndex)))return false;
        }
        return false;
    }

    MetaCountSystemArbitraryPrecision logOut(CountSystem csTo)
    {
        Decimal d = new Decimal();
        MetaCountSystemArbitraryPrecision theirBaseInBASE = new MetaCountSystemArbitraryPrecision(BASE,cs);
        theirBaseInBASE.convert(d,csTo.BASE);
        MetaCountSystemArbitraryPrecision checkNum = new MetaCountSystemArbitraryPrecision(BASE,cs);
        checkNum.increment();
        MetaCountSystemArbitraryPrecision exp = new MetaCountSystemArbitraryPrecision(BASE,cs);//initialized to zero

        while(!(lessThan(checkNum)) && !equals(checkNum))//current >= checkNum
        {
            exp.increment();
            MetaCountSystemArbitraryPrecision temp = new MetaCountSystemArbitraryPrecision(BASE,cs,theirBaseInBASE.n,theirBaseInBASE.point);
            theirBaseInBASE.pow(exp);
            checkNum.set(theirBaseInBASE.n,theirBaseInBASE.point);
            theirBaseInBASE.set(temp.n,temp.point);
        }

        if(!equals(checkNum)) exp.decrement();
        return exp;
    }

    String convertOut(CountSystem csTo)
    {
        String r = "";
        Decimal d = new Decimal();
        MetaCountSystemArbitraryPrecision theirBaseInBASE = new MetaCountSystemArbitraryPrecision(BASE,cs);
        theirBaseInBASE.convert(d,csTo.BASE);
        MetaCountSystemArbitraryPrecision exp = logOut(csTo);
        //boolean goneOnceWhileAtZero = false;
        do// || !goneOnceWhileAtZero)
        {
            //if(exp.equals(zero()))goneOnceWhileAtZero = true;
            MetaCountSystemArbitraryPrecision current = theirBaseInBASE.powRet(exp);
            int c = 0;
            while(!lessThan(current))
            {
                subtract(current);
                c++;
            }
            r += csTo.alphaAt(c);
            exp.decrement();
        }while(!exp.isNegative);

        return r;
    }

    String convertOutWithPartials(CountSystem csTo)
    {
        Decimal d = new Decimal();
        MetaCountSystemArbitraryPrecision wholePartTmp = new MetaCountSystemArbitraryPrecision(BASE,cs,n,-2);
        String wholePart = wholePartTmp.convertOut(csTo);
        for(int i = 0; i <= point+1; i++)
        {
            n.remove(0);
        }
        point = -1;

        MetaCountSystemArbitraryPrecision theirBaseInBASE = new MetaCountSystemArbitraryPrecision(BASE,cs);
        theirBaseInBASE.convert(d,csTo.BASE);
        theirBaseInBASE.pow(widthInternal());
        int origWidth = width();
        multiply(theirBaseInBASE);
        int bound = width();
        for(int i = width() - origWidth; i < bound; i++)
        {
            n.remove(width()-origWidth);
        }
        String partial = convertOut(csTo);
        int dp = 0;
        for(dp = partial.length()-1; dp >= 0 && partial.charAt(dp) == cs.alphaAt(0); dp--){}
        return wholePart + "." + partial.substring(0,dp+1);
    }

    String convertOutWithPartials(CountSystem csTo, int precision)
    {
        Decimal d = new Decimal();
        MetaCountSystemArbitraryPrecision wholePartTmp = new MetaCountSystemArbitraryPrecision(BASE,cs,n,-2);
        String wholePart = wholePartTmp.convertOut(csTo);
        if(precision == 0)//truncation
        {
            return wholePart;
        }
        for(int i = 0; i <= point+1; i++)
        {
            n.remove(0);
        }
        point = -1;

        while(width() < precision)
        {
            n.add(cs.zero());
        }
        if(width() > precision)
        {
            precision = width();
        }
        MetaCountSystemArbitraryPrecision precisionInternal = new MetaCountSystemArbitraryPrecision(BASE,cs);
        precisionInternal.convert(d,Integer.toString(precision));

        MetaCountSystemArbitraryPrecision theirBaseInBASE = new MetaCountSystemArbitraryPrecision(BASE,cs);
        theirBaseInBASE.convert(d,csTo.BASE);
        theirBaseInBASE.pow(precisionInternal);
        multiply(theirBaseInBASE);
        int bound = width()-precision;
        for(int i = width()-1; i >= bound; i--)
        {
            n.remove(i);
        }
        String partial = convertOut(csTo);
        int dp = 0;
        for(dp = partial.length()-1; dp >= 0 && partial.charAt(dp) == cs.alphaAt(0); dp--){}
        return wholePart + "." + partial.substring(0,dp+1);
    }

    void add(MetaCountSystemArbitraryPrecision b)
    {
        if(isNegative && b.isNegative)
        {
            isNegative = false;
            b.isNegative = false;
            add(b);
            isNegative = true;
            b.isNegative = true;
            return;
        }
        if(!isNegative && b.isNegative)
        {
            subtract(b);
            return;
        }
        if(isNegative && !b.isNegative)
        {
            //b-a
            MetaCountSystemArbitraryPrecision bt = new MetaCountSystemArbitraryPrecision(BASE,cs,b.n,false);
            bt.subtract(this);
            n = new ArrayList<>(bt.n);
            isNegative = bt.isNegative;
            return;
        }
        MetaCountSystemArbitraryPrecision a = new MetaCountSystemArbitraryPrecision(BASE,cs,n);
        n.clear();
        int posA = a.width() - 1, posB = b.width() - 1;

        String indexSum = cs.zero();
        while(posA >= 0 && posB >= 0)
        {
            indexSum = cs.add(indexSum,cs.add(a.at(posA),b.at(posB)));
            if(cs.lessThan(indexSum,BASE))
            {
                n.add(0,indexSum);
                indexSum = cs.zero();
            }
            else
            {
                indexSum = cs.mod(indexSum,BASE);
                n.add(0,indexSum);
                indexSum = cs.one();
                if(posA == 0 && posB == 0)n.add(0,indexSum);
            }
            posA--;
            posB--;
        }

        if(!indexSum.equals(cs.zero()) && posA >= 0)
        {
            int pl = a.width();
            a.increment(posA);
            if(pl < a.width())posA++;
        }

        if(!indexSum.equals(cs.zero()) && posB >= 0)
        {
            int pl = b.width();
            b.increment(posB);
            if(pl < b.width())posB++;
        }

        while(posA >= 0)
        {
            n.add(0,a.at(posA));
            posA--;
        }

        while(posB >= 0)
        {
            n.add(0,b.at(posB));
            posB--;
        }

        while(width() > 1 && at(0).equals(cs.zero()))
        {
            n.remove(0);
        }
    }

    void subtract(MetaCountSystemArbitraryPrecision b)
    {
        if(isNegative && b.isNegative)
        {
            MetaCountSystemArbitraryPrecision bt = new MetaCountSystemArbitraryPrecision(BASE,cs,b.n,false);
            isNegative = false;
            bt.subtract(this);
            isNegative = bt.isNegative;
            n = new ArrayList<>(bt.n);
            return;
        }
        if(!isNegative && b.isNegative)
        {
            b.isNegative = false;
            add(b);
            b.isNegative = true;
            return;
        }
        if(isNegative && !b.isNegative)
        {
            isNegative = false;
            b.isNegative = false;
            add(b);
            isNegative = true;
            b.isNegative = true;
            return;
        }
        if(equals(b))
        {
            n = new ArrayList<>();
            n.add(cs.zero());
            return;
        }
        if(lessThan(b))
        {
            MetaCountSystemArbitraryPrecision bt = new MetaCountSystemArbitraryPrecision(BASE,cs,b.n,b.isNegative);
            bt.subtract(this);
            n = new ArrayList<>(bt.n);
            isNegative = true;
            return;
        }
        MetaCountSystemArbitraryPrecision a = new MetaCountSystemArbitraryPrecision(BASE,cs,n);
        n.clear();
        int posA = a.width() - 1, posB = b.width() - 1;

        String indexDiff = cs.zero();
        while(posA >= 0 && posB >= 0)
        {
            indexDiff = cs.add(indexDiff,cs.subtract(a.at(posA),b.at(posB)));
            if(!cs.lessThan(indexDiff,cs.zero()))
            {
                n.add(0,indexDiff);
                indexDiff = cs.zero();
            }
            else
            {
                indexDiff = cs.add(BASE,indexDiff);
                n.add(0,indexDiff);
                indexDiff = cs.neg + cs.one();
                if(posA == 0 & posB == 0)
                {
//                    isNegative = true;
                    System.out.println("ERROR: MCSAP went negative.\nHow did this happen? We're smarter than this.\n");
                    System.exit(1);
                }
            }
            posA--;
            posB--;
        }

        if(!indexDiff.equals(cs.zero()) && posA >= 0)
        {
            int lO = a.width();
            a.decrement(posA);
            if(a.isNegative)
            {
                a.n.add(0,cs.decrement(BASE));
            }
            if(lO > a.width())
            {
                posA--;
            }
        }

        if(!indexDiff.equals(cs.zero()) && posB >= 0)
        {
            int lO = b.width();
            b.decrement(posB);
            if(b.isNegative)
            {
                b.n.add(0,cs.decrement(BASE));
            }
            if(lO > b.width())
            {
                posB--;
            }
        }

        while(posA >= 0)
        {
            n.add(0,a.at(posA));
            posA--;
        }

        while(posB >= 0)
        {
            n.add(0,b.at(posB));
            posB--;
        }

        while(width() > 1 && at(0).equals(cs.zero()))
        {
            n.remove(0);
        }
    }

    /*
     * left shift properties:
     *
     * (a << b) * (c << d) = (ac) << (b + d)
     *
     * a * (b << c) = (ab) << c
     *
     * note that this only works correctly on integers, it doesn't account for arbit points
     *
     */
    void leftShift(int places)
    {
        for(int i = 0; i < places; i++)
        {
            n.add(cs.zero());
        }
    }

    MetaCountSystemArbitraryPrecision subWord(int startIndex, int endIndex)
    {
        ArrayList<String> retAL = new ArrayList<String>(endIndex-startIndex);
        for(int i = startIndex; i <= endIndex; i++)
        {
            retAL.add(n.get(i));
        }
        return new MetaCountSystemArbitraryPrecision(BASE,cs,retAL);

    }

    void multiply(MetaCountSystemArbitraryPrecision b)
    {
        multiplyKaratsuba(b);
    }

    void multiplyKaratsuba(MetaCountSystemArbitraryPrecision b)
    {
        int k = Integer.min(width(),b.width())/2;
        if(k<=1)
        {
            multiplyOld(b);
            return;
        }
//        MetaCountSystemArbitraryPrecision a = new MetaCountSystemArbitraryPrecision(BASE,cs,n);
        MetaCountSystemArbitraryPrecision x0 = subWord(width()-k,width()-1);
        MetaCountSystemArbitraryPrecision x1 = subWord(0,width()-k-1);
        while(x0.at(0).equals(cs.zero()) && x0.width() > 1)
        {
            x0.n.remove(0);
        }

        MetaCountSystemArbitraryPrecision y0 = b.subWord(b.width()-k,b.width()-1);
        MetaCountSystemArbitraryPrecision y1 = b.subWord(0,b.width()-k-1);
        while(y0.at(0).equals(cs.zero()) && y0.width() > 1)
        {
            y0.n.remove(0);
        }

        MetaCountSystemArbitraryPrecision z2 = new MetaCountSystemArbitraryPrecision(BASE,cs,x1.n);
        z2.multiplyKaratsuba(y1);
        MetaCountSystemArbitraryPrecision z0 = new MetaCountSystemArbitraryPrecision(BASE,cs,x0.n);
        z0.multiplyKaratsuba(y0);
        x1.add(x0);
        y1.add(y0);
        MetaCountSystemArbitraryPrecision z1 = new MetaCountSystemArbitraryPrecision(BASE,cs,x1.n);
        z1.multiplyKaratsuba(y1);
        z1.subtract(z2);
        z1.subtract(z0);

        n.clear();
        n = new ArrayList<String>(z0.n);
        z1.leftShift(k);
        z2.leftShift(k<<1);
        add(z1);
        add(z2);
    }

    /*
    karatsuba's a lot faster, so use it instead. This still can't be deleted since we need it as  subroutine for karatsuba
     */
    void multiplyOld(MetaCountSystemArbitraryPrecision b)
    {
        boolean finalIsNegative;
        boolean bOriginalIsNegative = b.isNegative;
        if(isNegative == b.isNegative)
        {
            finalIsNegative = false;
        }
        else
        {
            finalIsNegative = true;
        }
        isNegative = false;
        b.isNegative = false;
        if(b.equals(one()))
        {
            return;
        }
        MetaCountSystemArbitraryPrecision a = new MetaCountSystemArbitraryPrecision(BASE,cs,n);
        n.clear();
        if(a.equals(one()))
        {
            n = new ArrayList<String>(b.n);
            return;
        }
        n.add(cs.zero());
        if(a.equals(zero()) || b.equals(zero()))
        {
            return;
        }

        int posA = a.width() - 1, aShift = 0;//how much the current position is left-shifted if isolated

        while(posA >= 0)
        {
            int posB = b.width() - 1, bShift = 0;
            String adigCS = a.at(posA);
            if(!adigCS.equals(cs.zero()))
            {
                //MetaCountSystemArbitraryPrecision adig = new MetaCountSystemArbitraryPrecision(BASE,cs);
                //adig.convert(cs,adigCS);
                while(posB >= 0)
                {
                    String bdigCS = b.at(posB);
                    if(!bdigCS.equals(cs.zero()))
                    {
                        //multiply the arbits at posA and posB, store in temp
                        //MetaCountSystemArbitraryPrecision bdig = new MetaCountSystemArbitraryPrecision(BASE,cs);
                        //bdig.convert(cs,bdigCS);
                        MetaCountSystemArbitraryPrecision temp = new MetaCountSystemArbitraryPrecision(BASE,cs);
                        MetaCountSystemArbitraryPrecision count = new MetaCountSystemArbitraryPrecision(BASE,cs);

                        //multiply adig by bdig
                        String product = cs.multiply(adigCS,bdigCS);
                        temp.convert(cs,product);

                        temp.leftShift(bShift + aShift);
                        add(temp);
                    }
                    posB--;
                    bShift++;
                }
            }

            posA--;
            aShift++;
        }
        b.isNegative = bOriginalIsNegative;
        isNegative = finalIsNegative;
    }

    MetaCountSystemArbitraryPrecision powRet(MetaCountSystemArbitraryPrecision p)
    {
        MetaCountSystemArbitraryPrecision r = new MetaCountSystemArbitraryPrecision(BASE,cs,n);
        r.pow(p);
        return r;
    }

    /*
     * this ^ p
     */
    void pow(MetaCountSystemArbitraryPrecision po)
    {
        if(po.equals(zero()))
        {
            n.clear();
            n.add(cs.one());
            return;
        }
        MetaCountSystemArbitraryPrecision p = new MetaCountSystemArbitraryPrecision(BASE,cs,po.n);
        p.decrement();
        MetaCountSystemArbitraryPrecision a = new MetaCountSystemArbitraryPrecision(BASE,cs,n);
        MetaCountSystemArbitraryPrecision count = new MetaCountSystemArbitraryPrecision(BASE,cs);
        for(;count.lessThan(p); count.increment())
        {
            multiply(a);
        }
    }

    /*
     * The arb-point will be defined as a single empty string
     */
    void rightShift(int places)
    {
        if(point != -1)
        {
            n.remove(point--);
        }
        else
        {
            point = width()-1;
        }

        boolean addZero = false;
        for(int i = 0; i < places; i++)
        {
            if(--point < 0)
            {
                point = 0;
                n.add(0,cs.zero());
                addZero = true;
            }
        }

        n.add(point+1,"");
        if(addZero)
        {
            n.add(0,cs.zero());
            point++;
        }
        for(int i = width()-1; i >= 0 && (at(i).equals(cs.zero()) || at(i).equals("")); i--)
        {
            n.remove(i);
        }
    }
}
