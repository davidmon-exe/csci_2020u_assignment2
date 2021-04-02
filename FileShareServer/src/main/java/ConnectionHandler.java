import java.io.*;
import java.net.*;
import java.util.*;

public class ConnectionHandler implements Runnable {

    private Socket socket = null;
    private BufferedReader requestInput = null;
    private DataOutputStream responseOutput = null;

    public ConnectionHandler(Socket socket) throws IOException {
        this.socket = socket;
        requestInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        responseOutput = new DataOutputStream(socket.getOutputStream());
        ObjectOutputStream objOS = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream objIS = new ObjectInputStream(socket.getInputStream());
    }

    public void run() {
        String line = null;
        try {
            line = requestInput.readLine();
            handleRequest(line);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                requestInput.close();
                responseOutput.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void handleRequest(String request) throws IOException {
        try {
            StringTokenizer tokenizer = new StringTokenizer(request);
            String command = tokenizer.nextToken();
            String uri = tokenizer.nextToken();

            if (command.equalsIgnoreCase("GET")) {
                File baseDir = new File("shareDir");
                sendFiles(baseDir, uri);
            } else {
                sendError(405, "Method Not Allowed", "You cannot use the '" + command + "' command on this server.");
            }

        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }

    }

    private void sendFiles(File baseDir, String uri) throws IOException {
        File[] files = new File(baseDir.getAbsolutePath()).listFiles();


        BufferedOutputStream bos = new BufferedOutputStream(socket.getOutputStream());
        DataOutputStream dos = new DataOutputStream(bos);

        assert files != null;
        dos.writeInt(files.length);

        for(File file : files)
        {
            long length = file.length();
            dos.writeLong(length);

            String name = file.getName();
            dos.writeUTF(name);

            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);

            int theByte = 0;
            while((theByte = bis.read()) != -1) bos.write(theByte);

            bis.close();
        }

        dos.close();
    }


    private void sendResponse(String responseCode, String contentType, byte[] content) throws IOException {
        responseOutput.writeBytes(responseCode);

        responseOutput.writeBytes("Content-Type: " + contentType + "\r\n");
        responseOutput.writeBytes("Date: " + (new Date()) + "\r\n");
        responseOutput.writeBytes("Server: Simple-Http-Server v1.0.0\r\n");
        responseOutput.writeBytes("Content-Length: " + content.length + "\r\n");
        responseOutput.writeBytes("Connection: Close\r\n\r\n");

        responseOutput.write(content);
        responseOutput.flush();
    }


    private void sendError(int errorCode,
                           String errorMessage,
                           String description) throws IOException {
        String responseCode = "HTTP/1.1 " + errorCode + " " + errorMessage + "\r\n";
        String content = "<!DOCTYPE html>" +
                "<html>" +
                "  <head>" +
                "    <title>" + errorCode + ": " + errorMessage + "</title>" +
                "  </head>" +
                "  <body>" +
                "    <h1>" + errorCode + ": " + errorMessage + "</h1>" +
                "    <p>" + description + "</p>" +
                "  </body>" +
                "</html>";
        sendResponse(responseCode, "text/html", content.getBytes());
    }
}