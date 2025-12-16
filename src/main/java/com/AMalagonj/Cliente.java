package com.AMalagonj;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cliente {
    public static void main(String[] args) {
        final int PUERTO_SERVIDOR = 9876;
        String direccionBroadcast = "172.16.8.255";
        Scanner scanner = new Scanner(System.in);

        System.out.print("Ingrese su nombre: ");
        String nombreCliente = scanner.nextLine();

        try {
            DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);
            socket.setSoTimeout(4000);

            InetAddress broadcastAddress = InetAddress.getByName(direccionBroadcast);
            InetAddress miIp = InetAddress.getLocalHost();

            //FASE 2: DESCUBRIMIENTO
            String mensajeSaludo = "@hola#" + nombreCliente + "@";
            byte[] bufferEnvio = mensajeSaludo.getBytes();
            DatagramPacket paqueteSaludo = new DatagramPacket(bufferEnvio, bufferEnvio.length, broadcastAddress, PUERTO_SERVIDOR);
            socket.send(paqueteSaludo);

            byte[] bufferRecepcion = new byte[1024];
            InetSocketAddress servidorEncontrado = null;
            String nombreServidor = "Servidor";

            try {
                // Esperamos la primera respuesta válida
                while (true) {
                    DatagramPacket peticion = new DatagramPacket(bufferRecepcion, bufferRecepcion.length);
                    socket.receive(peticion);

                    if (peticion.getAddress().equals(miIp)) continue;

                    String respuesta = new String(peticion.getData(), 0, peticion.getLength());
                    Pattern pattern = Pattern.compile("@hola#(.+?)@");
                    Matcher matcher = pattern.matcher(respuesta);

                    if (matcher.matches()) {
                        nombreServidor = matcher.group(1);
                        servidorEncontrado = new InetSocketAddress(peticion.getAddress(), peticion.getPort());
                        System.out.println(nombreServidor + ": " + respuesta);
                        break; // Nos quedamos con el primero que responda para iniciar el chat
                    }
                }
            } catch (SocketTimeoutException e) {
                System.out.println("No se encontró ningún servidor.");
                return;
            }

            //FASE 3: CHAT P2P BÁSICO
            if (servidorEncontrado != null) {
                socket.connect(servidorEncontrado);
                socket.setSoTimeout(0);

                while (true) {

                    System.out.print(nombreCliente + ": ");
                    String mensajeChat = scanner.nextLine();
                    
                    byte[] bufferChatEnvio = mensajeChat.getBytes();
                    DatagramPacket paqueteEnvio = new DatagramPacket(bufferChatEnvio, bufferChatEnvio.length);
                    socket.send(paqueteEnvio);

                    if (mensajeChat.equals(".")) {
                        System.out.println("Has terminado la conversación.");
                        break;
                    }


                    DatagramPacket paqueteRecepcion = new DatagramPacket(bufferRecepcion, bufferRecepcion.length);
                    socket.receive(paqueteRecepcion);

                    String mensajeRecibido = new String(paqueteRecepcion.getData(), 0, paqueteRecepcion.getLength());
                    System.out.println(nombreServidor + ": " + mensajeRecibido);

                    if (mensajeRecibido.equals(".")) {
                        System.out.println(nombreServidor + " ha terminado la conversación.");
                        break;
                    }
                }
            }
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
