
public class Binary extends CountSystem
{
    static final String BASE = "2";
    static final int BASE_INT = 2;
    
    public Binary()
    {
        super(BASE);
    }
    
    void initAlphabet()
    {
        alpha = new char[BASE_INT];
        alpha[0] = '0';
        alpha[1] = '1';
    }
}