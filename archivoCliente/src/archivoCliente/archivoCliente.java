package archivoCliente;

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class archivoCliente {

    private Socket socket;
    private DataInputStream bufferDeEntrada = null;
    private DataOutputStream bufferDeSalida = null;
    Scanner teclado = new Scanner(System.in);
    final String COMANDO_TERMINACION = "fin";

    BufferedInputStream bis;
    BufferedOutputStream bos;

    int in;
    byte[] byteArray;
    //Fichero a transferir
    final String filename = "C:\\Users\\Salvador\\Desktop\\Pruebas\\test.txt";
    

    public void AbrirConexion(String ip, int puerto) {
        try {
            socket = new Socket(ip, puerto);
            mostrarTexto("Conectado a :" + socket.getInetAddress().getHostName());
        } catch (Exception e) {
            mostrarTexto("Error al iniciar la conexión: " + e.getMessage());
            System.exit(0);
        }
    }

    public static void mostrarTexto(String s) {
        System.out.println(s);
    }

    public void abrirFlujos() {
        try {
            bufferDeEntrada = new DataInputStream(socket.getInputStream());
            bufferDeSalida = new DataOutputStream(socket.getOutputStream());
            bufferDeSalida.flush();
        } catch (IOException e) {
            mostrarTexto("Error en la conexión y transferencia de  paquetes");
        }
    }

    public void enviar(String s) {
        try {
            bufferDeSalida.writeUTF(s);
            bufferDeSalida.flush();
        } catch (IOException e) {
            mostrarTexto("Error al enviar");
        }
    }

    public void cerrarConexion() {
        try {
            bufferDeEntrada.close();
            bufferDeSalida.close();
            socket.close();
            mostrarTexto("Conexión terminada");
        } catch (IOException e) {
            mostrarTexto("Error al cerrar conexion");
        } finally {
            System.exit(0);
        }
    }

    public void ejecutarConexion(String ip, int puerto) {
        Thread hilo = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AbrirConexion(ip, puerto);
                    abrirFlujos();
                    recibirDatos();
                } finally {
                    cerrarConexion();
                }
            }
        });
        hilo.start();
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
        }
    }

    public void escribirDatos() {
        String entrada = "";
        while (true) {
            System.out.print("      Tú:  ");
            entrada = teclado.nextLine();
            if (entrada.length() > 0) {
                enviar(entrada);
            }
        }
    }

    public void enviarArchivo() {
        try {
            final File localFile = new File(filename);
            bis = new BufferedInputStream(new FileInputStream(localFile));
            bos = new BufferedOutputStream(socket.getOutputStream());
            //Enviamos el nombre del fichero
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF(localFile.getName());
            //Enviamos el fichero
            byteArray = new byte[8192];
            while ((in = bis.read(byteArray)) != -1) {
                bos.write(byteArray, 0, in);
            }

            bis.close();
            bos.close();

        } catch (IOException e) {
            System.err.println("Error al enviar el archivo: " + e);
        }
    }

    public static void main(String[] args) {
        archivoCliente cliente = new archivoCliente();
        Scanner escaner = new Scanner(System.in);
        //Especificación de IP
        mostrarTexto("IP:  ");
        String ip = escaner.nextLine();
        if (ip.length() <= 0) {
            ip = "localhost";
        }
        //Especificación del puerto
        mostrarTexto("Puerto:  ");
        String puerto = escaner.nextLine();
        if (puerto.length() <= 0) {
            puerto = "5050";
        }


        cliente.ejecutarConexion(ip, Integer.parseInt(puerto));
        cliente.escribirDatos();


        cliente.enviarArchivo();

    }
}
