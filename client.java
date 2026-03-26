import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class client {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java client [host] [port]");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);

        ArrayList<Double> rtts = new ArrayList<>();

        try {
            Socket socket = new Socket(host, port);

            DataInputStream din = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));
            DataOutputStream dout = new DataOutputStream(
                    new BufferedOutputStream(socket.getOutputStream()));

            try (Scanner scanner = new Scanner(System.in)) {

                din.readByte();
                System.out.println(din.readUTF());

                while (true) {
                    System.out.print("File name: ");
                    String fileName = scanner.nextLine().trim();

                    if (fileName.isEmpty()) {
                        continue;
                    }

                    long startTime = System.currentTimeMillis();

                    dout.writeByte(0);
                    dout.writeUTF(fileName);
                    dout.flush();

                    byte msgType = din.readByte();

                    long endTime = System.currentTimeMillis();
                    double rtt = (double) (endTime - startTime);

                    if (msgType == 0 || msgType == 1) {
                        String response = din.readUTF();
                        System.out.println(response);

                        if (response.equalsIgnoreCase("disconnected")) {
                            System.out.println("exit");
                            break;
                        }

                        if (fileName.equalsIgnoreCase("bye")) {
                            System.out.println("exit");
                            break;
                        }

                    } else if (msgType == 2) {
                        int fileSize = din.readInt();
                        byte[] fileData = new byte[fileSize];
                        din.readFully(fileData);

                        File folder = new File("downloads");
                        if (!folder.exists()) {
                            folder.mkdir();
                        }

                        File outFile = new File(folder, fileName);
                        FileOutputStream fout = new FileOutputStream(outFile);
                        fout.write(fileData);
                        fout.close();

                        rtts.add(rtt);
                        System.out.println("Downloaded " + fileName + " (" + rtt + "ms)");
                    }
                }

                if (rtts.size() > 0) {
                    double total = 0;
                    double min = rtts.get(0);
                    double max = rtts.get(0);

                    for (Double val : rtts) {
                        total += val;
                        if (val < min) {
                            min = val;
                        }
                        if (val > max) {
                            max = val;
                        }
                    }

                    double mean = total / rtts.size();
                    double variance = 0;

                    for (Double val : rtts) {
                        variance += Math.pow(val - mean, 2);
                    }

                    variance = variance / rtts.size();
                    double stdDev = Math.sqrt(variance);

                    System.out.println("\nStats:");
                    System.out.println("Count: " + rtts.size());
                    System.out.println("Minimum: " + min + "ms");
                    System.out.println("Mean: " + mean + "ms");
                    System.out.println("Maximum: " + max + "ms");
                    System.out.println("Standard Deviation: " + stdDev + "ms");
                }

                socket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}