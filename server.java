import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
			// Accept an incoming connection.
			Socket clientSocket = serverSocket.accept();

			// Create an input and output data stream for working with the socket.
			DataOutputStream dout = new DataOutputStream(clientSocket.getOutputStream());
			DataInputStream din = new DataInputStream(clientSocket.getInputStream());

			// Send the welcome message to the client.
			sendTextMessage("Hello!", dout);

			// Loop until we get the exit message
			while (true) {
				String msg = readTextMessage(din);

				// If we get the exit message, we exit.
				if (msg == "bye") {
					break;
				}

				sendErrorMessage("File not found", dout);
			}

			// Send the disconnected message to the client then close the socket.
			sendTextMessage("disconnected", dout);
			clientSocket.close();
		} catch (IOException e) {
			System.out.println(String.format("failed to communicate with client (%s)", e.toString()));
			return;
		}
	}

	// Writes a text message.
	static void sendTextMessage(String msg, DataOutputStream dout) throws IOException {
		dout.writeByte(0);
		dout.writeUTF(msg);
	}

	// Writes an error message.
	static void sendErrorMessage(String msg, DataOutputStream dout) throws IOException {
		dout.writeByte(1);
		dout.writeUTF(msg);
	}

	// Writes a data message.
	static void sendDataMessage(byte[] data, DataOutputStream dout) throws IOException {
		dout.writeByte(2);
		dout.writeInt(data.length);
		dout.write(data);
	}

	// Reads a text message.
	static String readTextMessage(DataInputStream din) throws IOException {
		// Check the message is the correct type.
		if (din.readByte() != 0) {
			throw new IOException("got unexpected message type");
		}

		return din.readUTF();
	}

	// Reads an error message.
	static String readErrorMessage(DataInputStream din) throws IOException {
		// Check the message is the correct type.
		if (din.readByte() != 1) {
			throw new IOException("got unexpected message type");
		}

		return din.readUTF();

	}

	// Reads a data message.
	static byte[] readDataMessage(DataInputStream din) throws IOException {
		// Check the message is the correct type.
		if (din.readByte() != 2) {
			throw new IOException("got unexpected message type");
		}

		// Read the length of the message and allocate a buffer to store it.
		int length = din.readInt();
		byte[] data = new byte[length];

		// Read and return the message.
		din.read(data);
		return data;
	}
}
