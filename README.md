#  **Calculadora VLSM - Quilca Tatiana**

La **Calculadora VLSM** es una aplicación Android diseñada para calcular y visualizar subredes utilizando la técnica **VLSM**. VLSM permite dividir una red IP en subredes de diferentes tamaños, optimizando el uso del espacio de direcciones IP. Este enfoque es muy útil para **administradores de redes**, **estudiantes de redes** y **profesionales de TI** que necesitan realizar cálculos precisos para redes de tamaño variable.

---

###  **Características**

- **Ingreso del número de subredes**: El usuario puede ingresar el **número de subredes** que necesita, y la aplicación calculará cómo dividir la red original en subredes más pequeñas.
  
- **Ingreso del número de hosts por subred**: Además del número de subredes, el usuario puede ingresar cuántos **hosts** (dispositivos) se necesitan en cada subred. Con esta información, la calculadora determinará automáticamente el tamaño adecuado de la máscara de subred y los detalles relacionados con la asignación de direcciones IP.
  
- **Cálculos automáticos de subredes**:
  - **Dirección IP de red**: Calcula y muestra la primera dirección de cada subred.
  - **Primera y última dirección IP utilizable**: Muestra las direcciones IP que pueden ser asignadas a dispositivos dentro de cada subred.
  - **Dirección de Broadcast**: Calcula la dirección de Broadcast para cada subred.
  - **Número total de hosts**: Muestra la cantidad de dispositivos que se pueden conectar en cada subred, basada en el número de hosts especificado.

#### ¿Cómo funciona?

1. **Ingreso de parámetros**: El usuario ingresa la **dirección IP** de la red original, el **prefijo** o **máscara de subred** y la cantidad de **subredes** y **hosts** que necesita.
   
2. **Cálculo automático de subredes**: Con base en los valores proporcionados, la calculadora divide la red en subredes de longitud variable (VLSM), optimizando el uso del espacio de direcciones IP.

3. **Resultados claros y detallados**: Los resultados incluyen tablas con las subredes calculadas, mostrando información como la **IP de red**, las **IPs utilizables**, y la **dirección de broadcast** para cada subred.

#### ¿Por qué usar la Calculadora VLSM?

Esta herramienta es útil tanto para **profesionales de redes** como para **estudiantes** que necesiten practicar o realizar ejercicios de planificación y división de redes. Permite realizar estos cálculos de forma rápida y precisa, garantizando una mejor comprensión de cómo se asignan las direcciones IP en una red y cómo utilizar el espacio de direcciones de manera eficiente.

---

###  **Instalación y ejecución**

1. **Clona el repositorio**:
   
   git clone https://github.com/Tatiana-Quilca/CalculadoraVLSM_QuilcaTatiana.git
2. Ábrelo con Android Studio.
3. Ejecuta el proyecto en un emulador o dispositivo físico Android.


