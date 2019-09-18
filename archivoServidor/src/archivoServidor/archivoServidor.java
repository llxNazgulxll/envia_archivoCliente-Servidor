package archivoServidor;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class archivoServidor {

    private Socket socket;
    private ServerSocket serverSocket;
    private DataInputStream bufferDeEntrada = null;
    private DataOutputStream bufferDeSalida = null;
    Scanner escaner = new Scanner(System.in);
    final String COMANDO_TERMINACION = "fin";

    //Envío de archivos
    byte[] receivedData;
    int in;
    String file;

    DataInputStream dis;
    DataOutputStream output;
    BufferedInputStream bis;
    BufferedOutputStream bos;

    public void AbrirConexion(int puerto) {
        try {
            serverSocket = new ServerSocket(puerto);
            mostrarTexto("Esperando conexión en el puerto " + String.valueOf(puerto) + "...");
            socket = serverSocket.accept();
            mostrarTexto("Conexión establecida con: " + socket.getInetAddress().getHostName() + "\n\n\n");
        } catch (Exception e) {
            mostrarTexto("Error al intentar conexión: " + e.getMessage());
            System.exit(0);
        }
    }

    public void flujos() {
        try {
            bufferDeEntrada = new DataInputStream(socket.getInputStream());
            bufferDeSalida = new DataOutputStream(socket.getOutputStream());
            bufferDeSalida.flush();
        } catch (IOException e) {
            mostrarTexto("Error en la conexión e intercambio de paquetes");
        }
    }

    public void recibirDatos() {
        String st = "";
        try {
            do {
                st = (String) bufferDeEntrada.readUTF();
                mostrarTexto("\n Servidor:  " + st);
                System.out.print("\n      Tú:  ");
            } while (!st.equals(COMANDO_TERMINACION));
        } catch (IOException e) {
            cerrarConexion();
        }
    }

    public void enviar(String s) {
        try {
            bufferDeSalida.writeUTF(s);
            bufferDeSalida.flush();
        } catch (IOException e) {
            mostrarTexto("Error en enviar: " + e.getMessage());
        }
    }

    public static void mostrarTexto(String s) {
        System.out.print(s);
    }

    public void escribirDatos() {
        while (true) {
            System.out.print("      Tú:  ");
            enviar(escaner.nextLine());
        }
    }

    public void cerrarConexion() {
        try {
            bufferDeEntrada.close();
            bufferDeSalida.close();
            socket.close();
        } catch (IOException e) {
            mostrarTexto("Excepción en cerrarConexion(): " + e.getMessage());
        } finally {
            mostrarTexto("Conversación finalizada....");
            System.exit(0);

        }
    }

    public void ejecutarConexion(int puerto) {
        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        AbrirConexion(puerto);
                        flujos();
                        recibirDatos();
                    } finally {
                        cerrarConexion();
                    }
                }
            }
        });
        hilo.start();
    }

    public void enviarArchivo() {
        try {
            while (true) {
                //Buffer de 1024 bytes
                receivedData = new byte[1024];
                bis = new BufferedInputStream(socket.getInputStream());
                dis = new DataInputStream(socket.getInputStream());
                //Recibimos el nombre del fichero
                file = dis.readUTF();
                file = file.substring(file.indexOf("C:\\Users\\Salvador\\Desktop\\RecibeArchivo\\") + 1, file.length());
                //Para guardar fichero recibido
                bos = new BufferedOutputStream(new FileOutputStream(file));
                while ((in = bis.read(receivedData)) != -1) {
                    bos.write(receivedData, 0, in);
                }
                bos.close();
                dis.close();
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public static void main(String[] args) {
        archivoServidor s = new archivoServidor();
        Scanner sc = new Scanner(System.in);

        mostrarTexto("Puerto:  ");
        String puerto = sc.nextLine();
        if (puerto.length() <= 0) {
            puerto = "4000";
        }


        s.ejecutarConexion(Integer.parseInt(puerto));
        s.escribirDatos();

        s.enviarArchivo();

    }
}
