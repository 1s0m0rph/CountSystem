
/**
 * Base_62 implementation of the countsystem
 * 
 * @author Daniel Taylor (Ambulator)
 * @version 13.11.17
 */
public class Base62 extends CountSystem
{
    static final String BASE = "62";
    static final int BASE_INT = 62;
    public Base62()
    {
        super(BASE);
    }
    
    public Base62(String start)
    {
        super(start,BASE);
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
        alpha[10] = 'a';
        alpha[11] = 'b';
        alpha[12] = 'c';
        alpha[13] = 'd';
        alpha[14] = 'e';
        alpha[15] = 'f';
        alpha[16] = 'g';
        alpha[17] = 'h';
        alpha[18] = 'i';
        alpha[19] = 'j';
        alpha[20] = 'k';
        alpha[21] = 'l';
        alpha[22] = 'm';
        alpha[23] = 'n';
        alpha[24] = 'o';
        alpha[25] = 'p';
        alpha[26] = 'q';
        alpha[27] = 'r';
        alpha[28] = 's';
        alpha[29] = 't';
        alpha[30] = 'u';
        alpha[31] = 'v';
        alpha[32] = 'w';
        alpha[33] = 'x';
        alpha[34] = 'y';
        alpha[35] = 'z';
        alpha[36] = 'A';
        alpha[37] = 'B';
        alpha[38] = 'C';
        alpha[39] = 'D';
        alpha[40] = 'E';
        alpha[41] = 'F';
        alpha[42] = 'G';
        alpha[43] = 'H';
        alpha[44] = 'I';
        alpha[45] = 'J';
        alpha[46] = 'K';
        alpha[47] = 'L';
        alpha[48] = 'M';
        alpha[49] = 'N';
        alpha[50] = 'O';
        alpha[51] = 'P';
        alpha[52] = 'Q';
        alpha[53] = 'R';
        alpha[54] = 'S';
        alpha[55] = 'T';
        alpha[56] = 'U';
        alpha[57] = 'V';
        alpha[58] = 'W';
        alpha[59] = 'X';
        alpha[60] = 'Y';
        alpha[61] = 'Z';
    }
}
