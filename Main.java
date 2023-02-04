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
        System.out.println(b[0]); //DSGTIN+ Header
        b = trimChars(b[1], 1);
        System.out.println(b[0]); //AIDC Indicator
        b = trimChars(b[1], 3);
        System.out.println(Integer.parseInt(b[0], 2)); //Filter Value???
        b = trimChars(b[1], 4);
        System.out.println(b[0]); //Date Indicator???
        b = trimChars(b[1], 7);
        System.out.println(Integer.parseInt(b[0], 2)); //Year
        b = trimChars(b[1], 4);
        System.out.println(Integer.parseInt(b[0], 2)); //Month
        b = trimChars(b[1], 5);
        System.out.println(Integer.parseInt(b[0], 2)); //Day
        b = trimChars(b[1], 56);
        System.out.println(Long.toString(Long.parseLong(b[0],2), 16)); //GTIN
        b = trimChars(b[1], 3);
        System.out.println(b[0]); //Encoding Indicator???
        b = trimChars(b[1], 5);
        System.out.println(Integer.parseInt(b[0], 2)); //Length Indicator
        System.out.println(b[1]);
    }
}