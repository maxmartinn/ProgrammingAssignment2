import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

public class client {

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java client [host] [port]");
            return;
        }

        String host = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number");
            return;
        }

        ArrayList<Double> rtts = new ArrayList<>();

        try (
                Socket socket = new Socket(host, port);
                DataInputStream din = new DataInputStream(
                        new BufferedInputStream(socket.getInputStream()));
                DataOutputStream dout = new DataOutputStream(
                        new BufferedOutputStream(socket.getOutputStream()));
                Scanner scanner = new Scanner(System.in)
        ) {
            Message welcome = Message.recv(din);
            if (welcome instanceof Message.TextMessage text) {
                System.out.println(text.msg());
            } else if (welcome instanceof Message.ErrorMessage error) {
                System.out.println(error.msg());
            } else {
                System.out.println("Unexpected welcome message from server");
                return;
            }

            while (true) {
                System.out.print("File name: ");
                String fileName = scanner.nextLine().trim();

                if (fileName.isEmpty()) {
                    continue;
                }

                long startTime = System.currentTimeMillis();
                new Message.TextMessage(fileName).send(dout);
                dout.flush();
                Message response = Message.recv(din);
                long endTime = System.currentTimeMillis();
                double rtt = (double) (endTime - startTime);

                if (response instanceof Message.TextMessage text) {
                    System.out.println(text.msg());

                    if (text.msg().equalsIgnoreCase("disconnected")) {
                        System.out.println("exit");
                        break;
                    }
                } else if (response instanceof Message.ErrorMessage error) {
                    System.out.println(error.msg());
                } else if (response instanceof Message.BinaryMessage binary) {
                    Path folder = Paths.get("downloads");
                    Files.createDirectories(folder);

                    String outputName = Paths.get(fileName).getFileName().toString();
                    Path outFile = folder.resolve(outputName);
                    Files.write(outFile, binary.data());

                    rtts.add(rtt);
                    System.out.println("Downloaded " + outputName + " (" + rtt + "ms)");
                } else {
                    System.out.println("Unknown response type received.");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}