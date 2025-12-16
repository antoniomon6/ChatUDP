package com.AMalagonj;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Servidor {
    public static void main(String[] args) {
        final int PUERTO = 9876;
        byte[] buffer = new byte[1024];
        String nombreServidor = "ServidorAntonio";
        Scanner scanner = new Scanner(System.in);
        // Para recordar el nombre del cliente
        Map<InetAddress, String> clientesConectados = new HashMap<>();

        try {
            DatagramSocket socket = new DatagramSocket(PUERTO);
            System.out.println("Servidor UDP iniciado ...");

            while (true) {
                DatagramPacket peticion = new DatagramPacket(buffer, buffer.length);
                socket.receive(peticion);

                String mensajeRecibido = new String(peticion.getData(), 0, peticion.getLength());
                InetAddress direccionCliente = peticion.getAddress();
                int puertoCliente = peticion.getPort();

                Pattern pattern = Pattern.compile("@hola#(.+?)@");
                Matcher matcher = pattern.matcher(mensajeRecibido);

                if (matcher.matches()) {
                    //Descubrimiento
                    String nombreCliente = matcher.group(1);
                    clientesConectados.put(direccionCliente, nombreCliente); // Guardamos el nombre del cliente
                    
                    String respuesta = "@hola#" + nombreServidor + "@";
                    byte[] bufferRespuesta = respuesta.getBytes();
                    DatagramPacket respuestaPacket = new DatagramPacket(bufferRespuesta, bufferRespuesta.length, direccionCliente, puertoCliente);
                    socket.send(respuestaPacket);
                } else {
                    // Chat
                    String nombreCliente = clientesConectados.getOrDefault(direccionCliente, "Cliente");
                    
                    // Detectar si es el primer mensaje de chat
                    if (!mensajeRecibido.equals(".")) {
                         if (!clientesConectados.containsKey(direccionCliente + ":" + puertoCliente)) {
                            System.out.println("Cliente " + nombreCliente + " ha iniciado conversación desde " + direccionCliente + ":" + puertoCliente);
                            clientesConectados.put(direccionCliente, nombreCliente + ":" + puertoCliente);
                        }
                    }
                   
                    System.out.println(nombreCliente + ": " + mensajeRecibido);

                    if (mensajeRecibido.equals(".")) {
                        System.out.println(nombreServidor + " ha terminado la conversacion con " + nombreCliente + ".");
                        continue;
                    }
                    

                    System.out.print(nombreServidor + ": ");
                    String respuestaChat = scanner.nextLine();

                    byte[] bufferRespuestaChat = respuestaChat.getBytes();
                    DatagramPacket respuestaPacket = new DatagramPacket(bufferRespuestaChat, bufferRespuestaChat.length, direccionCliente, puertoCliente);
                    socket.send(respuestaPacket);

                     if (respuestaChat.equals(".")) {
                        System.out.println(nombreServidor + " ha terminado la conversacion con " + nombreCliente + ".");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
