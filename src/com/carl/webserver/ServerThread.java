package com.carl.webserver;

import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread{
    private Socket clientSocket;
    private BufferedReader inStream;
    private PrintStream outStream;

    ServerThread(Socket connection) throws IOException {
        clientSocket = connection;
        inStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        outStream = new PrintStream(clientSocket.getOutputStream(), true);
    }

    public void run() {
        try {
//            System.out.println("Server Established Success");
            String line;
            String firstLine = null;
            StringBuilder lastLine = new StringBuilder();
            int size = 0;
            int lineCount=0;
            while((line=inStream.readLine()) != null) {
                lineCount++;
                if(lineCount == 1) firstLine = line;
                if(lineCount == 1 && line.substring(0, 3).equals("GET")) {
                    File file = new File(line.substring(5, line.length() - 9));
                    if(file.exists()) {
                        String type = line.substring(line.length()-12, line.length()-9);
                        outStream.println("HTTP/1.1 200 OK");
                        outStream.println("Accept-Ranges:bytes");
                        outStream.println("Content-Length:" + file.length());
                        if(type.equals("jpg")) {
                            outStream.println("Content-Type:image/jpeg");
                        } else if (type.equals("css")) {
                            outStream.println("Content-Type:text/css");
                        } else {
                            outStream.println("Content-Type:text/html");
                        }
                        outStream.println();
                        FileInputStream fis = new FileInputStream(file);
                        byte[] content = new byte[fis.available()];
                        fis.read(content);
                        outStream.write(content);
                        fis.close();
                    } else {
                        outStream.println("HTTP/1.1 404 Not Found");
                        outStream.println("Accept-Ranges:bytes");
                        outStream.println("Content-Length:" + file.length());
                        outStream.println("Content-Type:text/html");
                    }
                    break;
                } else if(firstLine.substring(0, 4).equals("POST")) {
                    if(line.length() > 14 && line.substring(0, 14).equals("Content-Length")) {
                        size = Integer.parseInt(line.substring(16));
                    }
                    if(line.equals("")) {
                        for(int i = 0; i < size; i++) {
                            lastLine.append((char) inStream.read());
                        }
                        String output = execPHP(lastLine.toString(), size);
                        outStream.println("HTTP/1.1 200 OK");
                        outStream.println("Accept-Ranges:bytes");
                        outStream.println("Content-Length:" + output.getBytes().length);
                        outStream.println("Content-Type:text/html");
                        outStream.println();
                        outStream.write(output.getBytes());
                        outStream.flush();
                        break;
                    }
                }
            }
            inStream.close();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(clientSocket!=null) {
                try{
                    clientSocket.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
    private String execPHP(String param, int size) {
        StringBuilder output = new StringBuilder();
        BufferedReader br = null;
        String cmd = "php-cgi";
        try {
            String line;
            String[] envp = new String[]{
                "REDIRECT_STATUS=true",
                "SCRIPT_FILENAME=C:\\Users\\pilgr\\IdeaProjects\\WebServer\\web\\login.php",
                "REQUEST_METHOD=POST",
                "GATEWAY_INTERFACE=CGI/1.1",
                "CONTENT_LENGTH=" + size,
                "CONTENT_TYPE=application/x-www-form-urlencoded"
            };
            Process p = Runtime.getRuntime().exec(cmd, envp);
            OutputStream pOutPutStream = p.getOutputStream();
            PrintWriter outPutWriter = new PrintWriter(pOutPutStream);
            outPutWriter.write(param, 0, param.length());
            outPutWriter.flush();
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while((line = br.readLine()) != null) {
                if(line.equals("")) {
                    output.append(br.readLine());
                }
            }
        } catch(Exception err) {
            err.printStackTrace();
        } finally {
            if(br != null){
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return output.toString();
    }
}