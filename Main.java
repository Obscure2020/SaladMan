import java.util.*;
import java.awt.Component;
import java.math.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.Font;

public class Main extends JFrame {
    private static DateTimeFormatter humanFormat = DateTimeFormatter.ofPattern("MMMM d, uuuu");
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
        if(header!=251 && header!=247) return null;
        if(header == 251){
            output.put("01 - RFID Type", "DSGTIN+"); // 251 is DSGTIN+
        } else {
            output.put("01 - RFID Type", "SGTIN+"); // 247 is SGTIN+
        }
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
        }
        int countUp = 3;
        String numerate = "";
        if(header == 251){
            b = trimChars(b[1], 4);
            int dateIndicator = Integer.parseInt(b[0], 2);
            b = trimChars(b[1], 7);
            int year = Integer.parseInt(b[0], 2);
            b = trimChars(b[1], 4);
            int month = Integer.parseInt(b[0], 2);
            b = trimChars(b[1], 5);
            int day = Integer.parseInt(b[0], 2);
            LocalDate date = LocalDate.of(2000+year, month, day);
            numerate = stringPad(Integer.toString(countUp++), '0', 2);
            if(dateIndicator>=0 && dateIndicator<=6){
                String[] dateTypes = {"Production date", "Packaging date", "Best before date", "Sell by date", "Expiration date", "First freeze date", "Harvest date"};
                output.put(numerate + " - " + dateTypes[dateIndicator], date.format(humanFormat));
            } else {
                output.put(numerate + " - Unspecified Date", date.format(humanFormat));
            }
        }
        b = trimChars(b[1], 56);
        String gtin = stringPad(Long.toString(Long.parseLong(b[0],2),16), '0', 14);
        numerate = stringPad(Integer.toString(countUp++), '0', 2);
        output.put(numerate + " - GTIN", gtin);
        b = variLengthDecode(b[1]);
        if(b == null) return null;
        numerate = stringPad(Integer.toString(countUp++), '0', 2);
        output.put(numerate + " - Serial Number", b[0]);
        if(plusData){
            int[] dateAIs = {11, 12, 13, 15, 17};
            String[] dateTypes = {"Production date", "Due date", "Packaging date", "Best before date", "Expiration date"};
            while(b[1].length()>0 && new BigInteger(b[1],2).compareTo(BigInteger.ZERO)>0){
                b = trimChars(b[1], 8);
                int ai = Integer.parseInt(Integer.toString(Integer.parseInt(b[0],2), 16));
                numerate = stringPad(Integer.toString(countUp++), '0', 2);
                if(ai == 10){
                    b = variLengthDecode(b[1]);
                    if(b == null) return output;
                    output.put(numerate + " - Batch/Lot", b[0]);
                    continue;
                }
                int dateCheck = Arrays.binarySearch(dateAIs, ai);
                if(dateCheck >= 0){
                    b = trimChars(b[1], 7);
                    int year = Integer.parseInt(b[0], 2);
                    b = trimChars(b[1], 4);
                    int month = Integer.parseInt(b[0], 2);
                    b = trimChars(b[1], 5);
                    int day = Integer.parseInt(b[0], 2);
                    LocalDate date = LocalDate.of(2000+year, month, day);
                    output.put(numerate + " - " + dateTypes[dateCheck], date.format(humanFormat));
                    continue;
                }
                break;
            }
        }
        return output;
    }
    public static void main(String[] args){
        // Scanner scan = new Scanner(System.in);
        // System.out.print("Hexadecimal Data: ");
        // String input = scan.nextLine();
        // scan.close();
        // HashMap<String,String> map = decodeRFID(new BigInteger(input, 16).toString(2));
        // String[] keys = map.keySet().stream().sorted().toList().toArray(new String[0]);
        // for(String key : keys){
        //     System.out.println(key + ": " + map.get(key));
        // }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Main();
            }
        });
    }

    private JLabel lettuceCount;
    private JLabel tomatoesCount;
    private JLabel containersCount;
    private Font standardFont;
    private Font boldFont;

    public Main(){
        setResizable(true);
        setTitle("SaladMan v0.1");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 720);
        setLocationRelativeTo(null);
        LookAndFeel defaultLF = UIManager.getLookAndFeel();
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            try{
                UIManager.setLookAndFeel(defaultLF);
            } catch (Exception f) {
                System.out.println("========= ERROR =========");
                System.out.println("Something has gone wrong with Java's Look-And-Feel system.");
                System.out.println("I tried to tell Java to use your system's inbuilt L&F, but Java spat out an exception.");
                System.out.println("So in response, I tried to set the L&F back to Java's default choice.");
                System.out.println("But somehow, some way, Java spat out an exception while trying to do THAT too!");
                System.out.println("I suspect that something's wrong with your JRE. My code is sound.");
                System.out.println("     - Joey Sodergren");
                System.exit(1);
            }
        }
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        add(panel);

        //Define Body Components
        JLabel lettuceInfo = new JLabel("Lettuce cases in inventory: ");
        standardFont = lettuceInfo.getFont().deriveFont(14.0f);
        boldFont = standardFont.deriveFont(Font.BOLD);
        lettuceInfo.setFont(boldFont);
        JPanel lettucePanel = new JPanel();
        lettucePanel.setLayout(new BoxLayout(lettucePanel, BoxLayout.X_AXIS));
        lettucePanel.setBorder(new EmptyBorder(0, 0, 4, 0));
        lettucePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        lettucePanel.add(lettuceInfo);
        lettuceCount = new JLabel("0");
        lettuceCount.setFont(standardFont);
        lettucePanel.add(lettuceCount);

        JLabel tomatoesInfo = new JLabel("Tomato cases in inventory: ");
        tomatoesInfo.setFont(boldFont);
        JPanel tomatoesPanel = new JPanel();
        tomatoesPanel.setLayout(new BoxLayout(tomatoesPanel, BoxLayout.X_AXIS));
        tomatoesPanel.setBorder(new EmptyBorder(0, 0, 4, 0));
        tomatoesPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tomatoesPanel.add(tomatoesInfo);
        tomatoesCount = new JLabel("0");
        tomatoesCount.setFont(standardFont);
        tomatoesPanel.add(tomatoesCount);

        JLabel containersInfo = new JLabel("Container cases in inventory: ");
        containersInfo.setFont(boldFont);
        JPanel containersPanel = new JPanel();
        containersPanel.setLayout(new BoxLayout(containersPanel, BoxLayout.X_AXIS));
        containersPanel.setBorder(new EmptyBorder(0, 0, 4, 0));
        containersPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        containersPanel.add(containersInfo);
        containersCount = new JLabel("0");
        containersCount.setFont(standardFont);
        containersPanel.add(containersCount);

        //Add everything to the panel
        panel.add(Box.createVerticalGlue());
        panel.add(lettucePanel);
        panel.add(tomatoesPanel);
        panel.add(containersPanel);
        panel.add(Box.createVerticalGlue());

        //Launch the Window
        setVisible(true);
    }
}