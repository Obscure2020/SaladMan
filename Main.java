import java.math.BigInteger;
import java.util.*;

class Main {
    private static String[] trimChars(String input, int num){
        String[] arr = new String[2];
        arr[0] = input.substring(0,num);
        arr[1] = input.substring(num);
        return arr;
    }
    private static int ninesBitCount(int num){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<num; i++) sb.append('9');
        BigInteger b = new BigInteger(sb.toString());
        return b.toString(2).length();
    }
    private static String stringPad(String str, char c, int length){
        StringBuilder sb = new StringBuilder(str);
        while(sb.length() < length) sb.insert(0, c);
        return sb.toString();
    }
    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        System.out.print("Hexadecimal Data: ");
        String input = scan.nextLine();
        scan.close();

        String binData = new BigInteger(input, 16).toString(2);
        String[] b = trimChars(binData, 8);
        System.out.println("DSGTIN+ Header - " + b[0]);
        b = trimChars(b[1], 1);
        boolean plusData = b[0].equals("1");
        System.out.println("+AIDC Indicator - " + plusData);
        b = trimChars(b[1], 3);
        System.out.println("Filter Value??? - " + Integer.parseInt(b[0], 2));
        b = trimChars(b[1], 4);
        System.out.println("Date Indicator??? - " + b[0]);
        b = trimChars(b[1], 7);
        System.out.println("Year - " + Integer.parseInt(b[0], 2));
        b = trimChars(b[1], 4);
        System.out.println("Month - " + Integer.parseInt(b[0], 2));
        b = trimChars(b[1], 5);
        System.out.println("Day - " + Integer.parseInt(b[0], 2));
        b = trimChars(b[1], 56);
        System.out.println("GTIN - " + stringPad(Long.toString(Long.parseLong(b[0],2),16), '0', 14) );
        b = trimChars(b[1], 3);
        System.out.println("Encoding Indicator??? - " + b[0]);
        b = trimChars(b[1], 5);
        int serialLen = Integer.parseInt(b[0], 2); //Length Indicator
        System.out.println("Length Indicator - " + serialLen);
        int serialBinaryLen = ninesBitCount(serialLen);
        b = trimChars(b[1], serialBinaryLen);
        System.out.println("Serial Number - " + new BigInteger(b[0],2)); //Serial Number
        if(plusData){
            System.out.println("==== Additional Data ====");
            b = trimChars(b[1], 8);
            System.out.println("AI - " + Integer.toString(Integer.parseInt(b[0],2), 16));
            b = trimChars(b[1], 3);
            System.out.println("Encoding Indicator??? - " + b[0]);
            b = trimChars(b[1], 5);
            int len = Integer.parseInt(b[0], 2);
            System.out.println("Length Indicator - " + len);
            int binaryLen = ninesBitCount(len);
            b = trimChars(b[1], binaryLen);
            System.out.println("Batch/Lot - " + new BigInteger(b[0],2));
            System.out.println(b[1]);
        }
    }
}