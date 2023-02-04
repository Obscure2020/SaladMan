import java.math.BigInteger;
import java.util.*;

class Main {
    private static String[] trimChars(String input, int num){
        String[] arr = new String[2];
        arr[0] = input.substring(0,num);
        arr[1] = input.substring(num);
        return arr;
    }
    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        System.out.print("Hexadecimal Data: ");
        String input = scan.nextLine();
        scan.close();

        String binData = new BigInteger(input, 16).toString(2);
        String[] b = trimChars(binData, 8);
        System.out.println(b[0]);
        b = trimChars(b[1], 1);
        System.out.println(b[0]);
        b = trimChars(b[1],3);
        System.out.println(b[0]);
        System.out.println(b[1]);
    }
}