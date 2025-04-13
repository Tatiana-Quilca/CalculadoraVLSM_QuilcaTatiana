package ec.edu.utn.com.example.calculadoravlsm_quilcatatiana;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private EditText ipInput, prefijoInput, subredesInput;
    private Button calcularButton, mostrarButton, nuevaIpButton;
    private TableLayout resultTable, tablaIps;
    private List<Subred> subredes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        ipInput = findViewById(R.id.ipInput);
        prefijoInput = findViewById(R.id.prefijoInput);
        subredesInput = findViewById(R.id.subredesInput);
        calcularButton = findViewById(R.id.calcularButton);
        mostrarButton = findViewById(R.id.mostrarButton);
        nuevaIpButton = findViewById(R.id.nuevaIpButton);
        resultTable = findViewById(R.id.resultTable);
        tablaIps = findViewById(R.id.tablaIps);

        // Configurar listeners
        calcularButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crearTablaSubredes();
            }
        });

        mostrarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calcularYMostrarSubredes();
            }
        });

        nuevaIpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                limpiarCampos();
            }
        });
    }

    private void crearTablaSubredes() {
        if (!validarDatos()) {
            return;
        }

        int numSubredes = Integer.parseInt(subredesInput.getText().toString());
        subredes.clear();

        // Limpiar tabla anterior
        int childCount = resultTable.getChildCount();
        if (childCount > 1) {
            resultTable.removeViews(1, childCount - 1);
        }

        // Crear filas para ingresar datos de cada subred
        for (int i = 0; i < numSubredes; i++) {
            TableRow row = new TableRow(this);

            // Celda para el nombre de subred
            EditText nombreSubredInput = new EditText(this);
            nombreSubredInput.setHint("Subred " + (i + 1));
            row.addView(nombreSubredInput);

            // Celda para el número de hosts
            EditText hostsInput = new EditText(this);
            hostsInput.setHint("# Hosts");
            hostsInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            row.addView(hostsInput);

            resultTable.addView(row);

            Subred subred = new Subred();
            subred.setNombre("Subred " + (i + 1));
            subred.setNumHosts(0);  // Valor por defecto
            subredes.add(subred);
        }
    }

    private boolean validarDatos() {
        String ip = ipInput.getText().toString();
        String mascaraStr = prefijoInput.getText().toString();
        String subredesStr = subredesInput.getText().toString();

        if (ip.isEmpty() || mascaraStr.isEmpty() || subredesStr.isEmpty()) {
            mostrarMensaje("Todos los campos son requeridos");
            return false;
        }

        if (!validarIP(ip)) {
            mostrarMensaje("IP inválida");
            return false;
        }

        try {
            int mascara = Integer.parseInt(mascaraStr);
            int numSubredes = Integer.parseInt(subredesStr);

            if (mascara > 32 || mascara < 0) {
                mostrarMensaje("Máscara inválida (0-32)");
                return false;
            }

            int maxSubredes = (int) Math.pow(2, 32 - mascara);
            if (numSubredes > maxSubredes) {
                mostrarMensaje("Número de subredes inválido para la máscara");
                return false;
            }

            return true;
        } catch (NumberFormatException e) {
            mostrarMensaje("Ingrese valores numéricos válidos");
            return false;
        }
    }

    private boolean validarIP(String ip) {
        String[] octetos = ip.split("\\.");
        if (octetos.length != 4) {
            return false;
        }

        for (String octeto : octetos) {
            try {
                int valor = Integer.parseInt(octeto);
                if (valor < 0 || valor > 255) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private void calcularYMostrarSubredes() {
        // Recolectar datos de la tabla de entrada
        recolectarDatosSubredes();

        // Ordenar subredes por número de hosts (de mayor a menor)
        Collections.sort(subredes, new Comparator<Subred>() {
            @Override
            public int compare(Subred s1, Subred s2) {
                return s2.getNumHosts() - s1.getNumHosts();
            }
        });

        // Calcular las subredes
        IP baseIP = new IP(ipInput.getText().toString(), Integer.parseInt(prefijoInput.getText().toString()));
        List<ResultadoSubred> resultados = calcularSubredes(baseIP, subredes);

        // Mostrar resultados en la tabla
        mostrarResultados(resultados);
    }

    private void recolectarDatosSubredes() {
        subredes.clear();

        // Empezar desde 1 para omitir la fila de encabezado
        for (int i = 1; i < resultTable.getChildCount(); i++) {
            TableRow row = (TableRow) resultTable.getChildAt(i);

            EditText nombreEdit = (EditText) row.getChildAt(0);
            EditText hostsEdit = (EditText) row.getChildAt(1);

            String nombre = nombreEdit.getText().toString();
            int numHosts;
            try {
                numHosts = Integer.parseInt(hostsEdit.getText().toString());
            } catch (NumberFormatException e) {
                numHosts = 0;
            }

            Subred subred = new Subred();
            subred.setNombre(nombre.isEmpty() ? "Subred " + i : nombre);
            subred.setNumHosts(numHosts);
            subredes.add(subred);
        }
    }

    private List<ResultadoSubred> calcularSubredes(IP ipBase, List<Subred> subredes) {
        List<ResultadoSubred> resultados = new ArrayList<>();
        IP ipActual = ipBase;

        for (Subred subred : subredes) {
            // Hosts necesarios (hosts requeridos + dirección de red + broadcast)
            int hostsNecesarios = subred.getNumHosts() + 2;

            // Bits necesarios para los hosts
            int bitsNecesarios = (int) Math.ceil(Math.log(hostsNecesarios) / Math.log(2));

            // Nueva máscara para esta subred
            int nuevaMascara = 32 - bitsNecesarios;

            // Crear resultado
            ResultadoSubred resultado = new ResultadoSubred();
            resultado.setNombre(subred.getNombre());
            resultado.setNumHosts(subred.getNumHosts());

            // Dirección de red
            String ipRed = ipActual.getIP();
            resultado.setIpRed(ipRed + "/" + nuevaMascara);

            // Calcular última IP utilizable
            IP ultimaIP = ipActual.obtenerUltimaIP(nuevaMascara);
            resultado.setUltimaIP(ultimaIP.getIP());

            // Calcular broadcast
            IP broadcast = ipActual.obtenerBroadcast(nuevaMascara);
            resultado.setBroadcast(broadcast.getIP());

            resultados.add(resultado);

            // Actualizar la IP para la siguiente subred
            ipActual = new IP(broadcast.nextIP(), ipBase.getPrefijo());
        }

        return resultados;
    }

    private void mostrarResultados(List<ResultadoSubred> resultados) {
        // Limpiar tabla anterior
        int childCount = tablaIps.getChildCount();
        if (childCount > 1) {
            tablaIps.removeViews(1, childCount - 1);
        }

        // Mostrar resultados
        for (ResultadoSubred resultado : resultados) {
            TableRow row = new TableRow(this);

            // Columna: Nombre de subred
            TextView nombreTV = new TextView(this);
            nombreTV.setText(resultado.getNombre());
            nombreTV.setPadding(8, 8, 8, 8);
            row.addView(nombreTV);

            // Columna: Número de hosts
            TextView hostsTV = new TextView(this);
            hostsTV.setText(String.valueOf(resultado.getNumHosts()));
            hostsTV.setPadding(8, 8, 8, 8);
            row.addView(hostsTV);

            // Columna: IP de red
            TextView ipRedTV = new TextView(this);
            ipRedTV.setText(resultado.getIpRed());
            ipRedTV.setPadding(8, 8, 8, 8);
            row.addView(ipRedTV);

            // Columna: Última IP
            TextView ultimaIPTV = new TextView(this);
            ultimaIPTV.setText(resultado.getUltimaIP());
            ultimaIPTV.setPadding(8, 8, 8, 8);
            row.addView(ultimaIPTV);

            // Columna: Broadcast
            TextView broadcastTV = new TextView(this);
            broadcastTV.setText(resultado.getBroadcast());
            broadcastTV.setPadding(8, 8, 8, 8);
            row.addView(broadcastTV);

            tablaIps.addView(row);
        }
    }

    private void limpiarCampos() {
        ipInput.setText("");
        prefijoInput.setText("");
        subredesInput.setText("");

        // Limpiar tabla de subredes
        int childCount = resultTable.getChildCount();
        if (childCount > 1) {
            resultTable.removeViews(1, childCount - 1);
        }

        // Limpiar tabla de resultados
        childCount = tablaIps.getChildCount();
        if (childCount > 1) {
            tablaIps.removeViews(1, childCount - 1);
        }

        subredes.clear();
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    // Clase interna para manejar la IP
    private class IP {
        private String ip;
        private int prefijo;

        public IP(String ip, int prefijo) {
            this.ip = ip;
            this.prefijo = prefijo;
        }

        public String getIP() {
            return ip;
        }

        public int getPrefijo() {
            return prefijo;
        }

        public String obtenerIPBinario() {
            StringBuilder ipBinaria = new StringBuilder();
            String[] octetos = ip.split("\\.");

            for (String octeto : octetos) {
                String binarioOcteto = String.format("%8s", Integer.toBinaryString(Integer.parseInt(octeto)))
                        .replace(' ', '0');
                ipBinaria.append(binarioOcteto);
            }

            return ipBinaria.toString();
        }

        public IP obtenerUltimaIP(int nuevaMascara) {
            String ipBinaria = obtenerIPBinario();
            String redPart = ipBinaria.substring(0, nuevaMascara);
            String hostPart = "1".repeat(31 - nuevaMascara) + "0"; // Última IP utilizable

            String ultimaIPBin = redPart + hostPart;
            return new IP(binarioADecimal(ultimaIPBin), nuevaMascara);
        }

        public IP obtenerBroadcast(int nuevaMascara) {
            String ipBinaria = obtenerIPBinario();
            String redPart = ipBinaria.substring(0, nuevaMascara);
            String hostPart = "1".repeat(32 - nuevaMascara); // Todos los bits de host a 1

            String broadcastBin = redPart + hostPart;
            return new IP(binarioADecimal(broadcastBin), nuevaMascara);
        }

        public String binarioADecimal(String ipBinario) {
            StringBuilder ipDecimal = new StringBuilder();

            for (int i = 0; i < 32; i += 8) {
                String octeto = ipBinario.substring(i, i + 8);
                int decimal = Integer.parseInt(octeto, 2);
                ipDecimal.append(decimal);

                if (i < 24) {
                    ipDecimal.append(".");
                }
            }

            return ipDecimal.toString();
        }

        public String nextIP() {
            String[] octetos = ip.split("\\.");
            int[] octetosInt = new int[4];

            for (int i = 0; i < 4; i++) {
                octetosInt[i] = Integer.parseInt(octetos[i]);
            }

            // Incrementar el último octeto
            octetosInt[3]++;

            // Propagar el carry si es necesario
            for (int i = 3; i > 0; i--) {
                if (octetosInt[i] > 255) {
                    octetosInt[i] = 0;
                    octetosInt[i-1]++;
                }
            }

            return octetosInt[0] + "." + octetosInt[1] + "." +
                    octetosInt[2] + "." + octetosInt[3];
        }
    }

    // Clase para almacenar información de cada subred
    private class Subred {
        private String nombre;
        private int numHosts;

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public int getNumHosts() {
            return numHosts;
        }

        public void setNumHosts(int numHosts) {
            this.numHosts = numHosts;
        }
    }

    // Clase para almacenar los resultados de cada subred
    private class ResultadoSubred {
        private String nombre;
        private int numHosts;
        private String ipRed;
        private String ultimaIP;
        private String broadcast;

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public int getNumHosts() {
            return numHosts;
        }

        public void setNumHosts(int numHosts) {
            this.numHosts = numHosts;
        }

        public String getIpRed() {
            return ipRed;
        }

        public void setIpRed(String ipRed) {
            this.ipRed = ipRed;
        }

        public String getUltimaIP() {
            return ultimaIP;
        }

        public void setUltimaIP(String ultimaIP) {
            this.ultimaIP = ultimaIP;
        }

        public String getBroadcast() {
            return broadcast;
        }

        public void setBroadcast(String broadcast) {
            this.broadcast = broadcast;
        }
    }
}