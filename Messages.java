import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Messages {
	// Writes a text message.
	public static void sendTextMessage(String msg, DataOutputStream dout) throws IOException {
		dout.writeByte(0);
		dout.writeUTF(msg);
	}

	// Writes an error message.
	public static void sendErrorMessage(String msg, DataOutputStream dout) throws IOException {
		dout.writeByte(1);
		dout.writeUTF(msg);
	}

	// Writes a data message.
	public static void sendDataMessage(byte[] data, DataOutputStream dout) throws IOException {
		dout.writeByte(2);
		dout.writeInt(data.length);
		dout.write(data);
	}

	// Reads a text message.
	public static String readTextMessage(DataInputStream din) throws IOException {
		// Check the message is the correct type.
		byte msgType = din.readByte();
		if (msgType != 0) {
			throw new IOException(String.format("got unexpected message type %d, expected 0", msgType));
		}

		return din.readUTF();
	}

	// Reads an error message.
	public static String readErrorMessage(DataInputStream din) throws IOException {
		// Check the message is the correct type.
		byte msgType = din.readByte();
		if (msgType != 1) {
			throw new IOException(String.format("got unexpected message type %d, expected 1", msgType));
		}

		return din.readUTF();

	}

	// Reads a data message.
	public static byte[] readDataMessage(DataInputStream din) throws IOException {
		// Check the message is the correct type.
		byte msgType = din.readByte();
		if (msgType != 2) {
			throw new IOException(String.format("got unexpected message type %d, expected 2", msgType));
		}

		// Read the length of the message and allocate a buffer to store it.
		int length = din.readInt();
		byte[] data = new byte[length];

		// Read and return the message.
		din.read(data);
		return data;
	}
}
