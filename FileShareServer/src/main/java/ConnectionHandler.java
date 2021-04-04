import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Handle each new command sent from a client
 */
public class ConnectionHandler implements Runnable {

    private ObjectInputStream input = null;
    private ObjectOutputStream output;
    private final Socket socket;

    /**
     * Initializer for each new connection
     *
     * @param socket        Socket that server runs on and that is being listened to
     * @throws IOException
     */
    public ConnectionHandler(Socket socket) throws IOException {
        this.socket=socket;

        output = new ObjectOutputStream(socket.getOutputStream());
        input = new ObjectInputStream(socket.getInputStream());

    }

    /**
     * Get the command and send it to the request handler.
     * Once it has finished close the socket of the the connected client
     */
    public void run() {
        String command;
        try {
            System.out.println("Client connected");
            command = input.readUTF();
            System.out.println("Command: " + command);
            handleRequest(command);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Client disconnected");
                input.close();
                output.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** Command is brought in and will either send a fileList to client if "Fetch",
     * receive a file is "Upload", or will send a file if "download"
     *
     * @param request   command sent from the client
     * @throws IOException
     */
    public void handleRequest(String request) throws IOException {
        try {
            StringTokenizer tokenizer = new StringTokenizer(request);
            String command = tokenizer.nextToken();
            File baseDir = new File("shareDir");
            if (command.equalsIgnoreCase("FETCH")) {
                listFiles(baseDir);
            }
            else if (command.equalsIgnoreCase("UPLOAD")){
                receiveFiles();
            }
            else if (command.equalsIgnoreCase("DOWNLOAD")){
                sendFiles();
            }
        } catch (NoSuchElementException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * dis will listen for an incoming fileName. File is then searched and found in the shared directory of the server,
     * then the size is sent to client for the remaking of the file. The file is then sent through a
     * file input stream to be converted to a byte array, which is then sent to the client.
     * @throws IOException
     */
    private void sendFiles() throws IOException {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        String wantedFile = dis.readUTF();
        System.out.println(wantedFile);
        File file = new File("shareDir/" + wantedFile);
        System.out.println(file.getAbsolutePath());
        long fileSize = file.length();
        System.out.println(fileSize);
        dos.writeLong(fileSize);
        FileInputStream fis = new FileInputStream(file);
        byte [] fileBytes  = new byte[(int) fileSize];
        while (fis.read(fileBytes) > 0){
            dos.write(fileBytes);
        }
        fis.close();
        dos.close();
        dis.close();
    }

    /** Obtain the name of the file that is to be sent, which is used to create a new file output stream.
     * The size of the file is then received, and a byte array of the same size as the file is created.
     * The incoming bytes are then received and are writen to the file output stream
     *
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void receiveFiles() throws IOException, ClassNotFoundException {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        FileOutputStream fos = new FileOutputStream("shareDir/" + dis.readUTF());;
        int fileSize = (int) dis.readLong();
        byte[] fileBytes = new byte[fileSize];
        int read = 0;
        int totalRead = 0;
        int remaining = fileSize;
        while((read = dis.read(fileBytes, 0, Math.min(fileBytes.length, remaining))) > 0) {
            totalRead += read;
            remaining -= read;
            System.out.println("read " + totalRead + " bytes.");
            fos.write(fileBytes, 0, read);
        }
        dis.close();
        fos.close();
    }

    /**
     * Files are read into an ArrayList so they can be displayed in a TreeView
     *
     * @param baseDir  Shared directory of the server
     * @throws IOException
     */
    private void listFiles(File baseDir) throws IOException {
        ArrayList<File> fileList = new ArrayList<>(Arrays.asList(Objects.requireNonNull(baseDir.listFiles())));
        output.writeObject(fileList);
        output.flush();
    }


}