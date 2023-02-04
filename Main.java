import java.math.BigInteger;
import java.util.*;

class Main {
    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        System.out.print("Hexadecimal Data: ");
        String input = scan.nextLine();
        scan.close();

        BigInteger data = new BigInteger(input, 16);
        System.out.println("Base 10: " + data.toString(10));
    }
}