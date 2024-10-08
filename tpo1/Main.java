package tpo1;

import tpo1.models.Pedido;
import tpo1.models.Cliente;
import tpo1.services.TiendaOnline;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.InputMismatchException;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) {
        TiendaOnline tienda = new TiendaOnline();
        List<Pedido> pedidosPendientes = new ArrayList<>();
        List<Pedido> pedidosProcesados = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);

        boolean continuar = true;

        int cantPedidos = 0;

        while (continuar) {
            System.out.println("=== Menú de Tienda Online ===");
            System.out.println("1. Crear un nuevo pedido");
            System.out.println("2. Procesar todos los pedidos pendientes");
            System.out.println("3. Listar los pedidos procesados.");
            System.out.println("4. Mostrar pedidos pendientes por procesar");
            System.out.println("5. Salir");
            int opcion = 0;
            while (true) {
                try {
                    System.out.println("Seleccione una opción: ");
                    opcion = scanner.nextInt(); // lee un entero ingresado por el usuario
                    scanner.nextLine();
                    break;
                } catch(InputMismatchException e) {
                    System.out.println("Entrada no válida. Por favor, ingrese un número entero.");
                    scanner.nextLine(); // Para limpiar el buffer
                }
            }
            switch (opcion) {
                case 1:
                    cantPedidos++;
                    String id = String.valueOf(cantPedidos);
                    System.out.println("Ingrese el nombre del cliente: ");
                    String nombreCliente = scanner.nextLine();

                    // Crear un nuevo cliente
                    Cliente cliente = new Cliente(nombreCliente);

                    // Crear un nuevo pedido
                    Pedido pedido = new Pedido(id, cliente);

                    // Crear y registrar un observador único para este pedido
                    pedido.registrarObservador(cliente);

                    // Agregar el pedido a la lista
                    pedidosPendientes.add(pedido);

                    System.out.println("\nPedido creado exitosamente. \n");
                    break;

                case 2:
                    System.out.println("\nVerificando si hay pedidos pendientes por procesar...");
                    if (!pedidosPendientes.isEmpty()) {
                        System.out.println("\nProcesando pedidos pendientes... \n");
                        List<Future<Pedido>> futures = new ArrayList<>();
    
                        // Procesar todos los pedidos en paralelo
                        for (Pedido p : pedidosPendientes) {
                            Future<Pedido> future = tienda.procesarPedido(p);
                            futures.add(future);
                        }

                        // Esperar a que todos los pedidos sean procesados
                        for (Future<Pedido> future : futures) {
                            try {
                                Pedido p = future.get(); // Bloquea hasta que el pedido haya sido procesado
                                pedidosProcesados.add(p); // Mueve el pedido a la lista de procesados
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        }
                        
                        // Limpiar la lista de pedidos pendientes
                        pedidosPendientes.clear();
                        System.out.println("\nTodos los pedidos han sido procesados. \n");
                    } else {
                        System.out.println("No hay pedidos pendientes por procesar.\n");
                    }

                    break;

                case 3:
                    System.out.println("\nLista de pedidos procesados y su resultado: \n");
                    if (!pedidosProcesados.isEmpty()) {
                        for (Pedido p : pedidosProcesados) {
                            System.out.println("Pedido ID: " + p.getId() + ", cliente: " + p.getCliente().getNombre()
                                    + ", estado: " + p.getEstado());
                        }
                        System.out.println("");
                    } else {
                        System.out.println("\n No hay pedidos procesados aún. \n");
                    }
                    break;

                case 4: 
                    if(!pedidosPendientes.isEmpty()){
                        System.out.println("\nLista de pedidos pendientes:\n");
                        for(Pedido ped : pedidosPendientes){
                            System.out.println("Pedido pendiente con ID: " + ped.getId() + " del Cliente: " + ped.getCliente().getNombre());
                        }
                        System.out.println("");
                    }else{
                        System.out.println("\nNo hay pedidos pendientes por procesar.\n");
                    }
                    break;

                case 5:
                    continuar = false;
                    System.out.println("Saliendo del sistema.");
                    break;

                default:
                    System.out.println("Opción no válida, intente nuevamente. \n");
                    break;
            }
        }

        // Cerrar el ExecutorService antes de salir
        tienda.shutdown();
        scanner.close();
    }
}
