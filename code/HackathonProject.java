
import java.util.Vector;
import java.util.Scanner;

public class HackathonProject {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        int choice = 0;

        while (choice != 100) {
            System.out.println("Menu: ");
            System.out.println("Enter 1 to Read RFID Data");
            System.out.println("Enter 100 to quit");
            choice = scanner.nextInt();
            switch (choice) {
                case 1: //Read RFID Data 
                    Vector<String> rfidVector = readRFID();
                    System.out.println("RFID Labels: ");
                    for (int i = 0; i < rfidVector.size(); i++) {
                        System.out.println(rfidVector.get(i));
                    }

            }

        }

    }

    public static Vector readRFID() {
        Scanner scanner = new Scanner(System.in);
        Vector<String> rfidVector = new Vector<String>();

        while (true) {
            System.out.println("Reading RFID Data...(Type 'stop' to stop)");
            String rfidData = scanner.nextLine();

            if (rfidData.equals("stop")) {
                break;
            }
            System.out.println("RFID Data: " + rfidData);
            rfidVector.add(rfidData);
        }
        System.out.println("RFID Labels:");
        for (int i = 0; i < rfidVector.size(); i++) {
            System.out.println(rfidVector.get(i));

        }

        return rfidVector;
    }

}
