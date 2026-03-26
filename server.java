import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class server {
	public static void main(String[] args) {
		// Check that a port was given
		if (args.length != 1) {
			System.out.println("usage: java server <port>");
			return;
		}

		int port;

		// Try to parse the port number and return an error message if we fail.
		try {
			port = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.out.println("invalid port number");
			return;
		}

		// Start listening on the port.
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			try (
				Socket clientSocket = serverSocket.accept();
				DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
				DataInputStream din = new DataInputStream(clientSocket.getInputStream())
			) {
				// Send the welcome message to the client.
				new Message.TextMessage("Hello!").send(dout);

				// Loop until we get the exit message.
				boolean exit = false;
				while (!exit) {
					// Receive the next message from the client.
					Message msg = Message.recv(din);

					switch (msg) {
						case Message.TextMessage tm -> {
							// If we get the exit message, we exit.
							if ("bye".equals(tm.msg())) {
								exit = true;
								break;
							}

							// Array to store the file data in.
							byte[] data;

							// Try to read the file and send it to the client.
							try {
								Path path = Paths.get(tm.msg());
								data = Files.readAllBytes(path);
							}

							// Handle file not found exception.
							catch (NoSuchFileException e) {
								new Message.ErrorMessage("File not found").send(dout);
								break;
							}

							// Handle any other exception if it occurs.
							catch (IOException e) {
								// If we do not know the error type exactly, send back a general
								// error message.
								new Message.ErrorMessage(String.format(
										"unknown error (%s) while trying to read file %s",
										e.toString(), tm.msg())).send(dout);
								break;
							}

							// Send a binary message with the file contents in it.
							new Message.BinaryMessage(data).send(dout);

						}
						// If it's an error message, print it to the console.
						case Message.ErrorMessage em -> {
							System.out.println(String.format("client error: %s", em.msg()));
						}
						// If its a binary message, send back an error as the server doesn't accept
						// binary messages.
						case Message.BinaryMessage bm -> {
							new Message.ErrorMessage("server doesn't support binary messages")
									.send(dout);
						}
					}
				}

				// Send the disconnected message to the client then close the socket.
				new Message.TextMessage("disconnected").send(dout);
			}
		}

		// If a socket error occurred, print it.
		catch (IOException e) {
			System.out.println(String.format("failed to communicate with client (%s)", e.toString()));
			return;
		}
	}
}
