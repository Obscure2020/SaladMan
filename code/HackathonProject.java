import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;
import java.util.Scanner;

public class HackathonProject {
   public static Vector<String> scanRFID() {
      Scanner scanner = new Scanner(System.in);
      Vector<String> rfidVector = new Vector<String>();

      while (true) {
         System.out.println("RFID Scanner Input: ");
         String rfidData = scanner.nextLine();

         if (rfidData.equals("stop")) {
            break;
         }
         System.out.println("RFID Data: " + rfidData);
         rfidVector.add(rfidData);
      }
      return rfidVector;
   }

   public static void main(String[] args) {
      Vector<String> rfidVector = scanRFID();
      HashMap<String, Integer> trace = new HashMap<String, Integer>();
      for (int i = 0; i < rfidVector.size(); i++) {
         String rfidLabel = rfidVector.get(i);
         if (trace.containsKey(rfidLabel)) {
            int count = trace.get(rfidLabel);
            trace.put(rfidLabel, count + 1);
         } else {
            trace.put(rfidLabel, 1);
         }
      }
      try (BufferedWriter bw = new BufferedWriter(new FileWriter("rfid.csv"))) {
         for (String rfidLabel : trace.keySet()) {
            bw.write(rfidLabel + "," + trace.get(rfidLabel));
            bw.newLine();
         }
         System.out.println("RFID data written to rfid.csv");
      } catch (IOException e) {
         System.out.println("Error writing to file: " + e.getMessage());
      }
   }
}
