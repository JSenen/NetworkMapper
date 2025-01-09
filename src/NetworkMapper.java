import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NetworkMapper {

    // Clase para almacenar información de cada dispositivo
    static class Device {
        String ipAddress;
        String macAddress;
        String type;
        List<Integer> openPorts = new ArrayList<>();

        public Device(String ipAddress, String macAddress, String type) {
            this.ipAddress = ipAddress;
            this.macAddress = macAddress;
            this.type = type;
        }

        @Override
        public String toString() {
            return String.format("%-15s %-20s %-10s %s", ipAddress, macAddress, type, openPorts.toString());
        }
    }

    public static void main(String[] args) {
        String subnet = getSubnet();
        if (subnet == null) {
            System.out.println("No se pudo determinar la subred.");
            return;
        }

        System.out.println("Escaneando la red: " + subnet + "0/24");

        List<Device> devices = new ArrayList<>();
        for (int i = 1; i < 255; i++) {
            String host = subnet + i;
            try {
                InetAddress address = InetAddress.getByName(host);
                if (address.isReachable(300)) { // Tiempo de espera 300ms
                    System.out.println("\nDispositivo encontrado: " + address.getHostAddress() + " (" + address.getHostName() + ")");
                    Device device = findMacAddress(host);
                    if (device != null) {
                        scanOpenPorts(device, host);
                        devices.add(device);
                    }
                }
            } catch (Exception e) {
                // Ignorar excepciones
            }
        }

        // Mostrar resultados en formato de tabla
        System.out.println("\nResultados:");
        System.out.printf("%-15s %-20s %-10s %s\n", "IP Address", "MAC Address", "Type", "Open Ports");
        System.out.println("--------------------------------------------------------------------------------");
        for (Device device : devices) {
            System.out.println(device);
        }
    }

    private static String getSubnet() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            String ip = localHost.getHostAddress();
            return ip.substring(0, ip.lastIndexOf('.') + 1);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Obtener MAC Address en Windows
    public static Device findMacAddress(String host) {
        try {
            Process process = Runtime.getRuntime().exec("arp -a");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "CP850"));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith(host)) {
                    String[] parts = line.trim().split("\\s{2,}");
                    if (parts.length >= 3) {
                        String ipAddress = parts[0];
                        String macAddress = parts[1];
                        String type = parts[2];
                        return new Device(ipAddress, macAddress, type);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("No se encontró la dirección MAC para: " + host);
        return new Device(host, "N/A", "N/A");
    }

    // Escaneo de puertos abiertos
    private static void scanOpenPorts(Device device, String host) {
        for (int port = 1; port <= 100; port++) { // Limitar el rango para acelerar
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 200); // Tiempo de espera 200ms
                device.openPorts.add(port);
            } catch (Exception ignored) {
                // El puerto está cerrado o no responde
            }
        }
    }
}

 /* MAC EN LINUX/MAC
    private static void findMacAddress(String host) {
    try {
        Process process = Runtime.getRuntime().exec("arp -a " + host);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(host)) {
                // Ejemplo de formato: "? (192.168.1.100) at 00:14:22:01:23:45 [ether]"
                int atIndex = line.indexOf(" at ");
                int spaceIndex = line.indexOf(" ", atIndex + 4);
                if (atIndex != -1 && spaceIndex != -1) {
                    String macAddress = line.substring(atIndex + 4, spaceIndex);
                    System.out.println("MAC Address: " + macAddress);
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

     */
