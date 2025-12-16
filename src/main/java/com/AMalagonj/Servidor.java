package com.AMalagonj;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Servidor {
    public static void main(String[] args) {
        final int PUERTO = 9876;
        byte[] buffer = new byte[1024]; // Buffer fijo para recepción
        String nombreServidor = "ServidorPrincipal";

        try {
            DatagramSocket socket = new DatagramSocket(PUERTO);
            System.out.println("Servidor UDP escuchando en el puerto " + PUERTO);

            while (true) {
                // Reiniciamos el DatagramPacket de recepción con el buffer original completo
                DatagramPacket peticion = new DatagramPacket(buffer, buffer.length);
                socket.receive(peticion);

                String mensajeRecibido = new String(peticion.getData(), 0, peticion.getLength());

                System.out.println("Mensaje recibido: " + mensajeRecibido);
                Pattern pattern = Pattern.compile("@hola#(.+?)@");
                Matcher matcher = pattern.matcher(mensajeRecibido);

                if (matcher.matches()) {
                    String nombreCliente = matcher.group(1);
                    System.out.println("Cliente conectado: " + nombreCliente);

                    InetAddress direccionCliente = peticion.getAddress();
                    int puertoCliente = peticion.getPort();

                    // Preparamos la respuesta en un array de bytes NUEVO, sin tocar el buffer de recepción
                    String respuesta = "@hola#" + nombreServidor + "@";
                    byte[] bufferRespuesta = respuesta.getBytes();
                    
                    DatagramPacket respuestaPacket = new DatagramPacket(bufferRespuesta, bufferRespuesta.length, direccionCliente, puertoCliente);
                    socket.send(respuestaPacket);
                } else {
                    System.out.println("Trama incorrecta recibida: " + mensajeRecibido);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
