import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;


// Client program where it connects the server, then it requests files, after that it
// receives them, then it goes and saves them locally, and finally measures round-trip time (RTT).
public class client {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java client [host] [port]");
            return;
        }
    // Parse port number safely
        String host = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number");
            return;
        }
 // List to store RTT values for stat
        ArrayList<Double> rtts = new ArrayList<>();

        try ( // Establish TCP conn
                Socket socket = new Socket(host, port);
                DataInputStream din = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream())); // Input stream to receive messages from server
                DataOutputStream dout = new DataOutputStream(
                        new BufferedOutputStream(socket.getOutputStream()));
                Scanner scanner = new Scanner(System.in)
        ) {  // Receive and display init welcome message from server
            Message welcome = Message.recv(din);
            if (welcome instanceof Message.TextMessage text) {
                System.out.println(text.msg());
            } else if (welcome instanceof Message.ErrorMessage error) {
                System.out.println(error.msg());
            } else {
                System.out.println("Unexpected welcome message from server");
                return;
            }
// main loop[
            while (true) {
                System.out.print("File name: ");
                String fileName = scanner.nextLine().trim();
// Ignore empty input
                if (fileName.isEmpty()) {
                    continue;
                }
 // Start RTT timer before sending request
                long startTime = System.currentTimeMillis(); 
                new Message.TextMessage(fileName).send(dout);// Send file name to server
                dout.flush();
                Message response = Message.recv(din);
                long endTime = System.currentTimeMillis();
                double rtt = (double) (endTime - startTime);

                if (response instanceof Message.TextMessage text) {
                    if (text.msg().equalsIgnoreCase("disconnected")) {
                        System.out.println("exit");
                        break;
                    } else {
                        System.out.println(text.msg());
                    }
                } else if (response instanceof Message.ErrorMessage error) {
                    System.out.println(error.msg());
                } else if (response instanceof Message.BinaryMessage binary) {
                    Path folder = Paths.get("downloads");  // Create downloads dir
                    Files.createDirectories(folder);

                    String outputName = Paths.get(fileName).getFileName().toString(); // Extract file name and save received data
                    Path outFile = folder.resolve(outputName);
                    Files.write(outFile, binary.data());

                    rtts.add(rtt);
                    System.out.println("Downloaded " + outputName + " (" + rtt + "ms)");
                } else {
                    System.out.println("Unknown response type received.");
                }
            }

            if (rtts.size() > 0) { // Compute and display RTT stat
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
 // Calculate variance
                double mean = total / rtts.size();
                double variance = 0;

                for (Double val : rtts) {
                    variance += Math.pow(val - mean, 2);
                }

                variance = variance / rtts.size();
                double stdDev = Math.sqrt(variance);
 // Print stat
                System.out.println("\nStats:");
                System.out.println("Count: " + rtts.size());
                System.out.println("Minimum: " + min + "ms");
                System.out.println("Mean: " + mean + "ms");
                System.out.println("Maximum: " + max + "ms");
                System.out.println("Standard Deviation: " + stdDev + "ms");
            }
        } catch (Exception e) { // Catch and print any unexpected errors
            e.printStackTrace();
        }
    }
}
