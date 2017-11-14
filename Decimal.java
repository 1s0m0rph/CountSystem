
/**
 * Decimal (_10) implementation for the countsystem.
 * Note that it is REQUIRED to implement this in order to implement countsystem
 * 
 * @author Daniel Taylor (Ambulator)
 * @version 13.11.17
 */
public class Decimal extends CountSystem
{
    static final String BASE = "10";
    static final int BASE_INT = 10;
    public Decimal(String start)
    {
        super(start,BASE);
    }
    
    public Decimal()
    {
        super(BASE);
    }
    
    void initAlphabet()
    {
        alpha = new char[BASE_INT];
        alpha[0] = '0';
        alpha[1] = '1';
        alpha[2] = '2';
        alpha[3] = '3';
        alpha[4] = '4';
        alpha[5] = '5';
        alpha[6] = '6';
        alpha[7] = '7';
        alpha[8] = '8';
        alpha[9] = '9';
    }
}
