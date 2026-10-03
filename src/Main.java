import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.Queue; // [เพิ่มใหม่] นำเข้าไลบรารี Queue
import java.util.Scanner;

public class Main {

    // =====================================================================================
    // [ส่วนที่ 1: Intro & Concept] - โครงสร้างข้อมูลพื้นฐาน (Array และ Queue)
    // =====================================================================================

    static String[] movies = {"Avenger: Endgame", "Inception", "Interstellar"};
    static double[] prices = {250.0, 200.0, 180.0};
    static String[] showtimes = {"13:00", "15:30", "19:00"};

    static final int ROWS = 5;
    static final int COLS = 10;

    // 3D Array เก็บผังที่นั่ง: [หนัง][แถว][คอลัมน์]
    static char[][][] seats = new char[movies.length][ROWS][COLS];
    static char[] rowLabels = {'A', 'B', 'C', 'D', 'E'};

    // [ไฮไลท์ใหม่] Queue สำหรับทำระบบ "คิวรอชำระเงิน (Pending Queue)"
    // Queue ทำงานแบบ FIFO (First-In, First-Out) ใครกดจองก่อน จะถูกนำไปประมวลผลออกตั๋วก่อน
    // การใช้ Queue ใน Java มักจะสร้างผ่านคลาส LinkedList
    static Queue<String> bookingQueue = new LinkedList<>();


    // =====================================================================================
    // [ส่วนที่ 2: Logic & Algorithm] - ภาพรวมระบบ และเมนูการทำงานของคิว
    // =====================================================================================

    public static void main(String[] args) {
        initializeSeats();
        Scanner scanner = new Scanner(System.in);
        boolean isRunning = true;

        System.out.println("=======================================");
        System.out.println("   Welcome to Cinema Booking System");
        System.out.println("=======================================");

        while (isRunning) {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. View Movies (ดูรายชื่อหนัง)");
            System.out.println("2. View Seats (ดูผังที่นั่ง)");
            System.out.println("3. Add to Queue (เลือกที่นั่งเข้าคิวรอชำระเงิน)"); // นำข้อมูลเข้า Queue (Enqueue)
            System.out.println("4. Process Payment (ชำระเงินและออกตั๋วตามคิว)"); // ดึงข้อมูลจาก Queue (Dequeue)
            System.out.println("5. Exit (ออกจากระบบ)");

            // แสดงจำนวนคิวที่ค้างอยู่ในระบบปัจจุบัน
            System.out.println(">> Current Pending Tickets in Queue: " + bookingQueue.size());

            int choice = getValidInput(scanner, "Please select an option (1-5): ", 1, 5);

            switch (choice) {
                case 1: displayMovies(); break;
                case 2: viewSeatsMenu(scanner); break;
                case 3: addToQueue(scanner); break;
                case 4: processQueue(); break;
                case 5:
                    System.out.println("Thank you for using our system. Goodbye!");
                    isRunning = false;
                    break;
            }
        }
        scanner.close();
    }

    public static void initializeSeats() {
        for (int m = 0; m < movies.length; m++) {
            for (int i = 0; i < ROWS; i++) {
                for (int j = 0; j < COLS; j++) {
                    seats[m][i][j] = 'O'; // 'O' = ว่าง
                }
            }
        }
    }

    public static void displayMovies() {
        System.out.println("\n--- Now Showing ---");
        for (int i = 0; i < movies.length; i++) {
            System.out.printf("%d. %s | Showtime: %s | %.2f THB\n",
                    (i + 1), movies[i], showtimes[i], prices[i]);
        }
    }

    public static void viewSeatsMenu(Scanner scanner) {
        displayMovies();
        int movieChoice = getValidInput(scanner, "Select a movie to view seats (1-" + movies.length + "): ", 1, movies.length);
        displaySeats(movieChoice - 1);
    }

    public static void displaySeats(int movieIndex) {
        System.out.println("\n--- Screen for: " + movies[movieIndex] + " (" + showtimes[movieIndex] + ") ---");
        System.out.print("     ");

        for (int j = 1; j <= COLS; j++) {
            System.out.printf("%-4d", j);
        }
        System.out.println();

        for (int i = 0; i < ROWS; i++) {
            System.out.print(rowLabels[i] + "   ");
            for (int j = 0; j < COLS; j++) {
                if (seats[movieIndex][i][j] == 'O') {
                    System.out.print("[ ] "); // ว่าง
                } else if (seats[movieIndex][i][j] == 'P') {
                    System.out.print("[P] "); // [อัปเดต] P = Pending (อยู่ในคิวรอชำระเงิน)
                } else {
                    System.out.print("[X] "); // จองแล้ว
                }
            }
            System.out.println();
        }
        System.out.println("Legend: '[ ]' = Available | '[P]' = Pending in Queue | '[X]' = Booked");
    }


    // =====================================================================================
    // [ส่วนที่ 3: Live Coding & Demo] - ระบบเข้าคิว (Enqueue / Offer)
    // =====================================================================================

    public static void addToQueue(Scanner scanner) {
        System.out.println("\n--- Add Ticket(s) to Queue ---");
        displayMovies();
        int movieChoice = getValidInput(scanner, "Select a movie (1-" + movies.length + "): ", 1, movies.length);
        int movieIndex = movieChoice - 1;

        displaySeats(movieIndex);
        scanner.nextLine();

        System.out.print("\nSelect your seat(s) to queue (e.g., A1 A2) or type '0' to cancel: ");
        String rawInput = scanner.nextLine().trim().toUpperCase();

        if (rawInput.equals("0")) {
            System.out.println("Operation cancelled.");
            return;
        }

        String[] requestedSeats = rawInput.split("[,\\s]+");
        int queuedCount = 0;

        for (int s = 0; s < requestedSeats.length; s++) {
            String seatInput = requestedSeats[s];

            if (seatInput.length() >= 2) {
                char rowChar = seatInput.charAt(0);
                int rIndex = -1;

                for (int i = 0; i < rowLabels.length; i++) {
                    if (rowLabels[i] == rowChar) {
                        rIndex = i;
                        break;
                    }
                }

                if (rIndex != -1) {
                    try {
                        int colNum = Integer.parseInt(seatInput.substring(1));
                        int cIndex = colNum - 1;

                        if (colNum >= 1 && colNum <= COLS) {
                            if (seats[movieIndex][rIndex][cIndex] == 'O') {

                                // 1. เปลี่ยนสถานะที่นั่งเป็น 'P' (Pending) เพื่อล็อคไว้ก่อน
                                seats[movieIndex][rIndex][cIndex] = 'P';

                                // =======================================================
                                // 2. [หัวใจของ Queue: ENQUEUE] โยนข้อมูลต่อท้ายคิว
                                // ใช้คำสั่ง .offer() หรือ .add() เพื่อนำข้อมูลไปต่อท้ายแถว
                                // =======================================================
                                String queueRecord = movieIndex + "," + rIndex + "," + cIndex + "," + seatInput;
                                bookingQueue.offer(queueRecord);
                                queuedCount++;

                            } else if (seats[movieIndex][rIndex][cIndex] == 'P') {
                                System.out.println("[Warning] Seat " + seatInput + " is already in the pending queue.");
                            } else {
                                System.out.println("[Warning] Seat " + seatInput + " is already booked.");
                            }
                        } else {
                            System.out.println("[Error] Seat " + seatInput + " is invalid.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("[Error] Seat format " + seatInput + " is invalid.");
                    }
                }
            }
        }

        if (queuedCount > 0) {
            System.out.println("\n[Success] Added " + queuedCount + " ticket(s) to the pending queue.");
            System.out.println("Don't forget to go to Menu 4 to process your payment!");
        }
    }


    // =====================================================================================
    // [ส่วนที่ 3 (ต่อ): Live Coding & Demo] - ระบบประมวลผลคิว (Dequeue / Poll)
    // =====================================================================================

    public static void processQueue() {
        System.out.println("\n--- Process Payment & Checkout ---");

        // เช็คว่ามีคิวรออยู่หรือไม่
        if (bookingQueue.isEmpty()) {
            System.out.println("[Info] The queue is empty. No tickets to process.");
            return;
        }

        double totalAmount = 0;
        int processedTickets = 0;

        System.out.println("Processing tickets in FIFO order (เข้าก่อน ออกก่อน)...");
        System.out.println("---------------------------------------");

        // =======================================================
        // [หัวใจของ Queue: DEQUEUE] ดึงข้อมูลจากหัวคิวออกมาทำงาน
        // ใช้ลูป while เพื่อประมวลผลไปเรื่อยๆ จนกว่าคิวจะว่างเปล่า
        // =======================================================
        while (!bookingQueue.isEmpty()) {
            // คำสั่ง .poll() จะดึงข้อมูลคิวตัวแรกสุด (หัวคิว) ออกมา และลบออกจากคิวไปในตัว
            String ticketData = bookingQueue.poll();

            String[] data = ticketData.split(",");
            int mIndex = Integer.parseInt(data[0]);
            int rIndex = Integer.parseInt(data[1]);
            int cIndex = Integer.parseInt(data[2]);
            String seatName = data[3];

            // ยืนยันการจอง: เปลี่ยนสถานะจาก 'P' (Pending) เป็น 'X' (Booked) ถาวร
            seats[mIndex][rIndex][cIndex] = 'X';

            double ticketPrice = prices[mIndex];
            totalAmount += ticketPrice;
            processedTickets++;

            System.out.printf("Processed: %s - Seat %s (%.2f THB)\n", movies[mIndex], seatName, ticketPrice);
        }

        // ออกใบเสร็จรวบยอด
        System.out.println("---------------------------------------");
        System.out.println("All pending tickets have been successfully processed!");
        System.out.printf("Total Tickets Issued: %d\n", processedTickets);
        System.out.printf("Total Amount Paid: %.2f THB\n", totalAmount);
        System.out.println("=======================================\n");
    }


    // =====================================================================================
    // [ส่วนที่ 4: Discussion & Conclusion] - Error Handling
    // =====================================================================================

    public static int getValidInput(Scanner scanner, String prompt, int min, int max) {
        int input = -1;
        boolean isValid = false;

        while (!isValid) {
            System.out.print(prompt);
            try {
                input = scanner.nextInt();
                if (input >= min && input <= max) {
                    isValid = true;
                } else {
                    System.out.println("[Error] Please enter a number between " + min + " and " + max + ".");
                }
            } catch (InputMismatchException e) {
                System.out.println("[Error] Invalid input. Please enter numbers only.");
                scanner.next();
            }
        }
        return input;
    }
}