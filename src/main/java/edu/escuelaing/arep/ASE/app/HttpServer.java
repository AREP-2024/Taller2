package edu.escuelaing.arep.ASE.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {

    private static final String GET_URL="https://www.omdbapi.com/";
    private static final String GET_KEY="926dbc03";
    public static void main(String[] args) throws IOException {
        
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        boolean running = true;
        while(running){
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {                
                System.err.println("Accept failed.");
                System.exit(1);
            }
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            clientSocket.getInputStream()));
            String inputLine;

            String esLaPrimera=null;
    
            while ((inputLine = in.readLine()) != null) {
                if (esLaPrimera==null) {
                    esLaPrimera = inputLine;                    
                }                                
                System.out.println("Received: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }
            if(esLaPrimera==null){
                continue;
            }           

            String recurso = esLaPrimera.split(" ")[1];
            if (recurso.endsWith(".png") ) {
                resolverImagen(clientSocket,recurso);
                
            }else{
                resolverTextoPlano(clientSocket,recurso);
            }
            
            in.close();
            clientSocket.close();
        }

        serverSocket.close();
    }

    private static void resolverImagen(Socket clientSocket, String recurso) throws IOException{
        FileInputStream fileInputStream = new FileInputStream("target/classes/public/" + recurso.split("/")[1]);

        long tamañoArchivo = fileInputStream.available();
        byte[] imagenBytes = new byte[(int) tamañoArchivo];
        fileInputStream.read(imagenBytes);
        fileInputStream.close();

        OutputStream out = clientSocket.getOutputStream();

        out.write("HTTP/1.1 200 OK\r\n".getBytes());
        out.write("Content-Length: ".getBytes());
        out.write(String.valueOf(imagenBytes.length).getBytes());
        out.write("\r\n".getBytes());
        out.write("Content-Type: image/png\r\n".getBytes());
        out.write("\r\n".getBytes());

        out.write(imagenBytes);
        out.flush();
        out.close();

    }
    

    private static void resolverTextoPlano(Socket clientSocket, String recurso) throws IOException{
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        HttpConnection httpConnection = new HttpConnection(GET_URL,GET_KEY);        
        String outputLine = "";
        if (recurso.startsWith("/Peliculas")) {
            outputLine = mostrarPelicula(httpConnection,recurso.split("/")[2]);                
        }else if (recurso.equals("/")){                
            outputLine = mostrarPagina("/pagina.html");
        }else if(recurso.endsWith(".css")){
            outputLine = mostrarEstilos(recurso);
        }else if(recurso.endsWith(".html") || recurso.endsWith(".js")){
            outputLine = mostrarPagina(recurso);            
        }else{
            outputLine = "HTTP/1.1 404 Not Found\r\n"
            + "Content-Type: text/html\r\n"
            + "\r\n"
            + "<!DOCTYPE html>\n"
            + "<html>\n"
            + "<head>\n"
            + "<meta charset=\"UTF-8\">\n"
            + "<title>Title of the document</title>\n"
            + "</head>\n"
            + "<body>\n"
            + "My Web Site\n"
            + "</body>\n"
            + "</html>\n";
        }
      
        out.println(outputLine);
        out.close();      

    }

    /*
        * Metodo que muetra la pagina principal
        * @return String con la pagina principal
     */
    public static String mostrarPagina(String path) throws IOException{

        StringBuilder pagina = new StringBuilder();

        String outputLine;

        pagina.append("HTTP/1.1 200 OK\r\n");
        pagina.append("Content-Type: text/html\r\n");
        pagina.append("\r\n");
        File file = new File("src/main/resources/public/"+path.split("/")[1]);
        BufferedReader reader = new BufferedReader(new FileReader(file));  
        while((outputLine = reader.readLine()) != null){
            pagina.append(outputLine);
            pagina.append("\n");
        }

        reader.close();
        return pagina.toString();    

    }

    public static String mostrarEstilos(String path) throws IOException{
        
        StringBuilder pagina = new StringBuilder();

        String outputLine;

        pagina.append("HTTP/1.1 200 OK\r\n");
        pagina.append("Content-Type: text/css\r\n");
        pagina.append("\r\n");
        File file = new File("src/main/resources/public/"+path.split("/")[1]);
        BufferedReader reader = new BufferedReader(new FileReader(file));  
        while((outputLine = reader.readLine()) != null){
            pagina.append(outputLine);
            pagina.append("\n");
        }

        reader.close();
        return pagina.toString();

    }


    
    /*
        * Metodo que muetra la informacion de una pelicula
        * @param httpConnection conexion con la api
        * @param nameMovie nombre de la pelicula
        * @return String con la informacion de la pelicula
     */
    public static String mostrarPelicula(HttpConnection httpConnection, String nameMovie) throws IOException{
        StringBuilder pagina = new StringBuilder();

        String outputLine = httpConnection.infoMovieWithCache(nameMovie);

        pagina.append("HTTP/1.1 200 OK\r\n");
        pagina.append("Content-Type: text/json\r\n");
        pagina.append("\r\n");
        pagina.append(outputLine);

        return pagina.toString();

    }
    
}
