package com.AMalagonj;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cliente {
    public static void main(String[] args) {
        final int PUERTO_SERVIDOR = 9876;
        String nombreCliente = "Antonio";

        try {
            InetAddress direccionServidor = InetAddress.getByName("localhost");
            DatagramSocket socket = new DatagramSocket();
            socket.setSoTimeout(5000); // 5 segundos de timeout

            // Buffer para enviar
            String mensaje = "@hola#" + nombreCliente + "@";
            byte[] bufferEnvio = mensaje.getBytes();
            DatagramPacket pregunta = new DatagramPacket(bufferEnvio, bufferEnvio.length, direccionServidor, PUERTO_SERVIDOR);
            socket.send(pregunta);

            // Buffer para recibir
            byte[] bufferRecepcion = new byte[1024];
            DatagramPacket peticion = new DatagramPacket(bufferRecepcion, bufferRecepcion.length);
            socket.receive(peticion);

            String respuesta = new String(peticion.getData(), 0, peticion.getLength());

            Pattern pattern = Pattern.compile("@hola#(.+?)@");
            Matcher matcher = pattern.matcher(respuesta);

            if (matcher.matches()) {
                String nombreServidor = matcher.group(1);
                System.out.println("Conectado al servidor: " + nombreServidor);
            } else {
                System.out.println("Respuesta del servidor con formato no válido: " + respuesta);
            }

            socket.close();

        } catch (SocketTimeoutException e) {
            System.out.println("Tiempo de espera agotado. El servidor no respondió.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
