
/**
 * Main countsystem class implementation
 * 
 * @author Daniel Taylor (Ambulator)
 * @version 13.11.17
 */
public abstract class CountSystem
{
    String BASE;
    int BASE_INT;
    String current;
    char[] alpha;
    
    /*/
    class constructor with just given base
    
    @param _base the base to set to, value for base is stored in a string (use decimal implementation for math)
    /*/
    public CountSystem(String _base)
    {
        BASE = _base;
        BASE_INT = Integer.parseInt(BASE);
        initAlphabet();
        current = Character.toString(alpha[0]);
    }
    
    
    /*/
    constructor with given start value
    
    @param start the start value, useful when using this for just counting in any given base
    /*/
    public CountSystem(String start, String _base)
    {
        BASE = _base;
        BASE_INT = Integer.parseInt(BASE);
        initAlphabet();
        current = convert(start);
    }
    
    /*/
    in the inheriting classes, this sets up the alphabet with any desired characters
    /*/
    abstract void initAlphabet();
    
    /*/
    replaces the index of the given string with the given character at the given index
    
    @param original the string to change
    @param index the index to change
    @param newChar the character to change to
    @return the new string (with the character and index index changed)
    /*/
    String replaceStringIndex(String original, int index, char newChar)
    {
        String r = "";
        for(int i = 0; i < original.length(); i++)
        {
            if(i == index) r += newChar;
            else r += original.charAt(i);
        }
        return r;
    }
    
    /*/
    converts the current number to base 10, used when using the class for counting
    /*/
    String convertTo_10()
    {
        return convertTo_10(current);
    }
    
    /*/
    returns the log base BASE of a given base 10 number. Used in conversion
    
    @param num the (_10) number to take the log_BASE() of
    @return the log in base 10
    /*/
    String log(String num)
    {
        if(BASE.equals("10"))
        {
            return Integer.toString(num.length());
        }
        
        Decimal d = new Decimal();
        String r = "0";
        while(d.lessThan(d.pow(BASE,r),num))
        {
            r = d.increment(r);
        }
        return d.decrement(r);
    }
    
    /*/
    convert a given number into base BASE
    
    @param num the number (_10) to convert
    @return the converted number in base BASE
    /*/
    String convert(String num)
    {
        if(num.length() < 2)return Character.toString(alpha[Integer.parseInt(num)]);
        if(num.charAt(0) == '-')return "-" + convert(num.substring(1));
        String r = "";
        if(BASE.equals("10"))
            return num;
        Decimal d = new Decimal();
        
        for(String exp = log(num); !d.lessThan(exp,"0"); exp = d.decrement(exp))
        {
            String current = d.pow(BASE,exp);
            num = d.subtract(num,current);
            int c = 0;
            while(num.charAt(0) != '-')
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
    convert a given number from base BASE to base 10
    
    @param conv the number (_BASE) to convert
    @return the converted number in base 10
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
    
    String increment()
    {
        return increment(current.length()-1);
    }
    
    String increment(int digitPos)
    {
        return increment(current,digitPos);
    }
    
    String increment(String num)
    {
        return increment(num,num.length()-1);
    }
    
    /*/
    adds one to a given number in base BASE recursively
    
    @param num the number to increment
    @param digitPos the digit to start from (used to walk backwards through the number, but you can call from any given digit to increment from it)
    @return num + 1
    /*/
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
    
    String decrement()
    {
        return decrement(current.length()-1);
    }
    
    String decrement(int digitPos)
    {
        return decrement(current,digitPos);
    }
    
    String decrement(String num)
    {
        return decrement(num,num.length()-1);
    }
    
    /*/
    takes one from a given number in base BASE recursively
    
    @param num the number to decrement
    @param digitPos the digit to start from (used to walk backwards through the number, but you can call from any given digit to increment from it)
    @return num - 1
    /*/
    String decrement(String num, int digitPos)
    {
        if(num.equals(Character.toString(alpha[0])))return "-" + alpha[1];
        if(num.charAt(0) == '-')return "-" + increment(num.substring(1));
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
    
    /*/
    get the index of a given character in the number base's alphabet
    
    @param c the character whose index is to be found
    @return the character's index
    /*/
    int alphaIndex(char c)
    {
        int r;
        for(r = 0; r < BASE_INT; r++)
        {
            if(alpha[r] == c) return r;
        }
        return -1;
    }
    
    /*/
    adds the value of a to b. Can handle negatives properly
    
    @param a the first number
    @param b the second number
    @return a + b (_BASE)
    /*/
    String add(String a, String b)
    {
        if(a.charAt(0) == '-' && b.charAt(0) != '-')
        {
            return subtract(b,a.substring(1));
        }
        if(a.charAt(0) != '-' && b.charAt(0) == '-')
        {
            return subtract(a,b.substring(1));
        }
        if(a.charAt(0) == '-' && b.charAt(0) == '-')
        {
            return "-" + add(a.substring(1),b.substring(1));
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
    
    /*/
    check if a < b
    
    @param a
    @param b
    @return true if a < b, false if a >= b
    /*/
    boolean lessThan(String a, String b)
    {
        if(a.equals(b))return false;
        if(a.equals(Character.toString(alpha[0])))return b.charAt(0) != '-';
        if(b.equals(Character.toString(alpha[0])))return a.charAt(0) == '-';
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
    
    /*/
    take a - b. Can handle negatives. No conversion to _10
    
    @param a
    @param b
    @return a - b (_BASE)
    /*/
    String subtract(String a, String b)
    {
        if(a.charAt(0) == '-' && b.charAt(0) != '-')
        {
            return "-" + add(a.substring(1),b);
        }
        if(b.charAt(0) == '-')
        {
            return add(a,b.substring(1));
        }
        if(lessThan(a,b))
        {
            return "-" + subtract(b,a);
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
                if(posA == 0 && posB == 0)r = "-" + r;
            }
            posA--;
            posB--;
        }
        
        if(indexDiff != 0 && posA >= 0)
        {
            int lO = a.length();
            a = decrement(a.substring(0,posA+1),posA) + a.substring(posA+1);
            if(a.charAt(0) == '-')
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
            if(b.charAt(0) == '-')
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
    
    /*/
    helper method to get a string with a given number of zeroes
    not that "zeroes" refers to the number base's character for zero, which isn't necessarily 0
    
    @param n the number of zeroes
    @return a string with n zero characters
    /*/
    String nZeroes(int n)
    {
        String r = "";
        for(int i = 0; i < n; i++)
        {
            r += alpha[0];
        }
        return r;
    }
    
    /*/
    take a*b
    
    @param a
    @param b
    @return a * b (_BASE)
    /*/
    String multiply(String a, String b)
    {
        if(lessThan(a,b))return multiply(b,a);
        String r = Character.toString(alpha[0]);
        if(a.length() == 1 && b.length() == 1)
        {
            for(String i = Character.toString(alpha[0]); lessThan(i,b); i = increment(i))
            {
                r = add(r,a);
            }
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
    
    /*/
    take a^b (take a*a, b times)
    
    @param a
    @param b
    @return a^b (_BASE)
    /*/
    String pow(String a, String b)
    {
        if(b.equals(Character.toString(alpha[0])))return "1";
        String r = a;
        for(String i = Character.toString(alpha[1]); lessThan(i,b); i = increment(i))
        {
            r = multiply(r,a);
        }
        return r;
    }
    
    /*/
    take a % b. (same as the remainder for divide(String,String))
    
    @param a
    @param b
    @return a % b (_BASE)
    /*/
    String mod(String a, String b)
    {
        if(!(a.equals(b) || lessThan(b,a)) || a.substring(0,1).equals("-"))return null;
        
        while(a.equals(b) || lessThan(b,a))
        {
            a = subtract(a,b);
        }
        
        return a;
    }
    
    /*/
    take a / b. Does not return decimal representation, as CountSystem was designed for ints
    note that this just takes a-b a number of times, which is quite inefficient
    a way to improve this would be to use the LDA, I have not due to lack of time and lack of interest
    
    @param a
    @param b
    @return a / b (_BASE)
    /*/
    String divide(String a, String b)
    {
        String div = Character.toString(alpha[0]);
        while(!lessThan(a,b) && !a.substring(0,1).equals("-"))
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
}
