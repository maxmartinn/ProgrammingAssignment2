import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// Sealed interface which shows all possible message types exchanged  between client & server
public sealed interface Message {
	// Write a message to a DataOutputStream.
	public void send(DataOutputStream dout) throws IOException;

	// Reads a message from a DataInputStream.
	public static Message recv(DataInputStream din) throws IOException {
		byte msgType = din.readByte();

		switch (msgType) {
			case TextMessage.code:
				return TextMessage.recv(din);
			case ErrorMessage.code:
				return ErrorMessage.recv(din);
			case BinaryMessage.code:
				return BinaryMessage.recv(din);
			default:
				throw new IOException(String.format("unknown message type %d", msgType));
		}
	}

	// sends simple strings 
	public record TextMessage(String msg) implements Message {
		static final byte code = 0;
// Sends the message type
		public void send(DataOutputStream dout) throws IOException {
			dout.write(code);
			dout.writeUTF(msg);
		}
// Reads string from input stream and then mnakes a TextMessage
		public static Message recv(DataInputStream din) throws IOException {
			return new TextMessage(din.readUTF());
		}
	}
// Used for error responses 
	public record ErrorMessage(String msg) implements Message {
		static final byte code = 1;

		public void send(DataOutputStream dout) throws IOException {
			dout.write(code);
			dout.writeUTF(msg);
		}
// Reads error string then makes the message
		public static Message recv(DataInputStream din) throws IOException {
			return new ErrorMessage(din.readUTF());
		}
	}
// Used for sending file data as raw bytes
	public record BinaryMessage(byte[] data) implements Message {
		static final byte code = 2;

		public void send(DataOutputStream dout) throws IOException {
			dout.write(code);
			dout.writeInt(data.length);
			dout.write(data);
		}
// Reads binary data
		public static Message recv(DataInputStream din) throws IOException {
			int length = din.readInt();
			byte[] data = new byte[length];
			din.readFully(data);
			return new BinaryMessage(data);
		}
	}
}
