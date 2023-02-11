import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.InputMismatchException;

public class Server {
    private final String pluginsDirectory;
    private ServerSocket serverSocket;
    public static final int bufsize = 4 * 1024;

    public Server(String pluginsDirectory, int port) throws IOException {
        this.pluginsDirectory = pluginsDirectory;
        serverSocket = new ServerSocket(port); 
    }

    public void start() throws IOException {
        while(true) {
            // System.out.println("Waiting...");
            new ClientHandler(serverSocket.accept()).start();
        }
    }

    private class ClientHandler extends Thread {
        private Socket clientSocket;
        private DataInputStream socketInput;
        private DataOutputStream socketOutput;

        public ClientHandler(Socket socket) throws IOException {
            clientSocket = socket;
            socketInput = new DataInputStream(socket.getInputStream());
            socketOutput = new DataOutputStream(socket.getOutputStream());
        }

        private void clear() {
            try {
                clientSocket.close();
                socketInput.close();
                socketOutput.close();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }

        private void sendFile(String name) throws IOException {
            File file = new File(name);
            // send the length of file
            socketOutput.writeLong(file.length());
            FileInputStream stream = new FileInputStream(file);

            byte[] buf = new byte[Server.bufsize];
            int bytes = 0;
            while((bytes = stream.read(buf)) != -1) {
                socketOutput.write(buf, 0, bytes);
                socketOutput.flush();
            }
            stream.close();
        }

        private void sendPlugin(String name) throws IOException {
            String fullName = pluginsDirectory + '/' + name;
            File pluginDir = new File(fullName);
            String[] files = pluginDir.list();
            // send the number of files in the plugin directory
            socketOutput.writeInt(files.length);
            for(String filename : files) {
                // send the name of the file
                socketOutput.writeUTF(filename);
                // send the file itself
                sendFile(fullName + '/' + filename);
            }
        }

        private void sendList() throws IOException {
            File dir = new File(pluginsDirectory);
            String[] names = dir.list();
            socketOutput.writeInt(names.length);
            for(String name : names)
                socketOutput.writeUTF(name);
        }

        @Override
        public void run() {
            try {
                while(true) {
                    String query = socketInput.readUTF();
                    if(query.equals("get")) {
                        String pluginName = socketInput.readUTF();
                        sendPlugin(pluginName);
                    }
                    else if(query.equals("list")) {
                        sendList();
                    }
                    else if(query.equals("bye")) {
                        break;
                    } 
                    else {
                        break; // sth went wrong
                    }
                }
                clear();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
            finally {
                clear();
            }
        }
    }

    public static class Parser {
        private String pluginsDirectory = null;
        private int port = -1;
        private static final String errMsg = "error in server.confing file";

        public Parser() {
            try {
                File file = new File("server.config");
                Scanner scanner = new Scanner(file);
                while(scanner.hasNext()) {
                    String s = scanner.next();
                    if(s.equalsIgnoreCase("PORT")) {
                        this.port = scanner.nextInt();
                    }
                    else if(s.equalsIgnoreCase("DIRECTORY")) {
                        this.pluginsDirectory = scanner.next();
                    }
                    else
                        throw new Exception();
                }
                if(port == -1 || pluginsDirectory == null) {
                    System.err.println(errMsg);
                    System.err.println("insufficient data");
                    System.exit(-1);
                }
                File pd = new File(pluginsDirectory);
                if(!pd.exists()) {
                    System.err.println(errMsg);
                    System.err.println("supplied plugin directory does not exist");
                    System.exit(-1);
                }
                scanner.close();
            }
            catch(FileNotFoundException e) {
                System.err.println("server.confing file not found");
                System.exit(-1);
            }
            catch(InputMismatchException e) {
                System.err.println(errMsg);
                System.err.println("PORT is not int");
                System.exit(-1);
            }
            catch(Exception e) {
                System.err.println(errMsg);
                System.err.println("incorrect format");
                System.exit(-1);
            }
        }

        public String getPluginsDirectory() {
            return pluginsDirectory;
        }

        public int getPort() {
            return port;
        }
    }

    public static void main(String[] argv) {
        try {
            Parser parser = new Parser();
            Server server = new Server(parser.getPluginsDirectory(), parser.getPort());
            server.start();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}
