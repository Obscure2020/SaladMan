import java.util.*;
import java.math.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

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
    private static String[] variLengthDecode(String input){
        String[] b = trimChars(input, 3);
        int encoder = Integer.parseInt(b[0], 2);
        b = trimChars(b[1], 5);
        int len = Integer.parseInt(b[0], 2);
        if(encoder == 0){ //Decimal
            int binaryLen = ninesBitCount(len);
            b = trimChars(b[1], binaryLen);
            return new String[] {new BigInteger(b[0], 2).toString(), b[1]};
        }
        if(encoder < 3){ //Hexadecimal
            int binaryLen = len * 4;
            b = trimChars(b[1], binaryLen);
            return new String[] {new BigInteger(b[0], 2).toString(16).toUpperCase(), b[1]};
        }
        if(encoder == 3){ //Base 64
            int binaryLen = len * 6;
            b = trimChars(b[1], binaryLen);
            StringBuilder sb = new StringBuilder(len);
            String table = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_";
            for(int i=0; i<len; i++){
                String slice = b[0].substring(i*6,(i+1)*6);
                int six = Integer.parseInt(slice, 2);
                sb.append(table.charAt(six));
            }
            return new String[] {sb.toString(), b[1]};
        }
        if(encoder == 4){ //7-bit ASCII
            int binaryLen = len * 7;
            b = trimChars(b[1], binaryLen);
            StringBuilder sb = new StringBuilder(len);
            for(int i=0; i<len; i++){
                String slice = b[0].substring(i*7,(i+1)*7);
                int seven = Integer.parseInt(slice, 2);
                sb.append((char) seven);
            }
            return new String[] {sb.toString(), b[1]};
        }
        if(encoder == 5){ //Variable-length URN Code 40
            while(len%3 != 0) len++;
            int groupCount = len/3;
            int binaryLen = groupCount * 16;
            b = trimChars(b[1], binaryLen);
            StringBuilder sb = new StringBuilder(len);
            String table = " ABCDEFGHIJKLMNOPQRSTUVWXYZ-.:0123456789";
            for(int i=0; i<groupCount; i++){
                String slice = b[0].substring(i*16,(i+1)*16);
                int chunk = Integer.parseInt(slice, 2);
                int first = chunk / 1600;
                chunk -= first * 1600;
                int second = chunk / 40;
                chunk -= second * 40;
                chunk--;
                if(first > 0) sb.append(table.charAt(first));
                if(second > 0) sb.append(table.charAt(second));
                if(chunk > 0) sb.append(table.charAt(chunk));
            }
            return new String[] {sb.toString(), b[1]};
        }
        System.out.print("Unsupported Vari-length Encoder: " + encoder);
        return null;
    }
    private static HashMap<String, String> decodeRFID(String binData){
        HashMap<String, String> output = new HashMap<>();
        String[] b = trimChars(binData, 8);
        int header = Integer.parseInt(b[0], 2);
        if(header == 251){
            output.put("01 - RFID Type", "DSGTIN+");
            b = trimChars(b[1], 1);
            boolean plusData = b[0].equals("1");
            b = trimChars(b[1], 3);
            int filterValue = Integer.parseInt(b[0], 2);
            switch(filterValue){
                case 1:
                    output.put("02 - Filter Value", "Point of Sale Trade Item");
                    break;
                case 2:
                    output.put("02 - Filter Value", "Full Case for Transport");
                    break;
                default:
                    output.put("02 - Filter Value", "Unknown");
                    break;
            }
            b = trimChars(b[1], 4);
            int dateIndicator = Integer.parseInt(b[0], 2);
            switch(dateIndicator){
                case 0:
                    output.put("03 - Date Type", "Production date");
                    break;
                case 1:
                    output.put("03 - Date Type", "Packaging date");
                    break;
                case 2:
                    output.put("03 - Date Type", "Best before date");
                    break;
                case 3:
                    output.put("03 - Date Type", "Sell by date");
                    break;
                case 4:
                    output.put("03 - Date Type", "Expiration date");
                    break;
                case 5:
                    output.put("03 - Date Type", "First freeze date");
                    break;
                case 6:
                    output.put("03 - Date Type", "Harvest date");
                    break;
                default:
                    output.put("03 - Date Type", "Unknown");
                    break;
            }
            b = trimChars(b[1], 7);
            int year = Integer.parseInt(b[0], 2);
            b = trimChars(b[1], 4);
            int month = Integer.parseInt(b[0], 2);
            b = trimChars(b[1], 5);
            int day = Integer.parseInt(b[0], 2);
            LocalDate date = LocalDate.of(2000+year, month, day);
            output.put("04 - Date", date.format(DateTimeFormatter.ofPattern("MMMM d, uuuu")));
            b = trimChars(b[1], 56);
            String gtin = stringPad(Long.toString(Long.parseLong(b[0],2),16), '0', 14);
            output.put("05 - GTIN", gtin);
            b = variLengthDecode(b[1]);
            if(b == null) return null;
            output.put("06 - Serial Number", b[0]);
            if(plusData){
                int countUp = 7;
                while(b[1].length()>0 && new BigInteger(b[1],2).compareTo(BigInteger.ZERO)>0){
                    b = trimChars(b[1], 8);
                    int ai = Integer.parseInt(Integer.toString(Integer.parseInt(b[0],2), 16));
                    if(ai == 10){
                        b = variLengthDecode(b[1]);
                        if(b == null) return output;
                        String numerate = stringPad(Integer.toString(countUp++), '0', 2);
                        output.put(numerate + " - Batch/Lot", b[0]);
                        continue;
                    }
                    return output;
                }
            }
            return output;
        }
        if(header == 247){
            System.out.println("Not yet implemented.");
        }
        return null;
    }
    public static void main(String[] args){
        Scanner scan = new Scanner(System.in);
        System.out.print("Hexadecimal Data: ");
        String input = scan.nextLine();
        scan.close();
        HashMap<String,String> map = decodeRFID(new BigInteger(input, 16).toString(2));
        String[] keys = map.keySet().stream().sorted().toList().toArray(new String[0]);
        for(String key : keys){
            System.out.println(key + ": " + map.get(key));
        }
    }
}