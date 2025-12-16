package com.AMalagonj;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cliente {
    public static void main(String[] args) {
        final int PUERTO_SERVIDOR = 9876;
        String nombreCliente = "ClienteExplorador";

        String direccionBroadcast = "172.16.8.255"; 
        
        List<InetSocketAddress> amigosEncontrados = new ArrayList<>();

        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(2000);

            InetAddress broadcastAddress = InetAddress.getByName(direccionBroadcast);
            InetAddress miIp = InetAddress.getLocalHost();


            String mensaje = "@hola#" + nombreCliente + "@";
            byte[] bufferEnvio = mensaje.getBytes();
            DatagramPacket pregunta = new DatagramPacket(bufferEnvio, bufferEnvio.length, broadcastAddress, PUERTO_SERVIDOR);
            socket.send(pregunta);
            System.out.println("Broadcast enviado a " + direccionBroadcast + ". Esperando respuestas...");
            byte[] bufferRecepcion = new byte[1024];
            
            try {
                while (true) {
                    DatagramPacket peticion = new DatagramPacket(bufferRecepcion, bufferRecepcion.length);
                    socket.receive(peticion);


                    if (peticion.getAddress().equals(miIp)) {
                        continue;
                    }

                    String respuesta = new String(peticion.getData(), 0, peticion.getLength());
                    
                    Pattern pattern = Pattern.compile("@hola#(.+?)@");
                    Matcher matcher = pattern.matcher(respuesta);

                    if (matcher.matches()) {
                        String nombreServidor = matcher.group(1);
                        InetSocketAddress direccionAmigo = new InetSocketAddress(peticion.getAddress(), peticion.getPort());
                        
                        // Evitar duplicados
                        if (!amigosEncontrados.contains(direccionAmigo)) {
                            amigosEncontrados.add(direccionAmigo);
                            System.out.println("¡Amigo encontrado!: " + nombreServidor + " en " + peticion.getAddress());
                        }
                    } else {
                        System.out.println("Respuesta con formato no válido recibida de: " + peticion.getAddress());
                    }
                }
            } catch (SocketTimeoutException e) {
                System.out.println("Fin de la espera. Total amigos encontrados: " + amigosEncontrados.size());
            }

            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
