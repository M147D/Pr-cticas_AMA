package com.example.ama_practica02

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.ama_practica02.databinding.FragmentFirstBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ============================================
// PRÁCTICA 02: Conceptos de Kotlin
// ============================================

// 1. VARIABLES CON DIFERENTES TIPOS
// Declaración de variables con al menos 5 tipos diferentes
val enteroInmutable: Int = 42
var numeroDecimal: Double = 3.14159
var textoMutable: String = "Hola Kotlin"
var esVerdadero: Boolean = true
var listaNumeros: List<Int> = listOf(1, 2, 3, 4, 5)

// 2. FUNCIÓN CONVENCIONAL
// Función que calcula el cuadrado de un número
fun calcularCuadrado(numero: Int): Int {
    return numero * numero
}

// 3. CLASE TRADICIONAL CON PROPIEDAD Y METODO
// Clase que representa un contador simple
class Contador {
    var valor: Int = 0  // Propiedad

    // Metodo que incrementa el contador
    fun incrementar() {
        valor++
    }

    fun obtenerValor(): Int {
        return valor
    }
}

// 4. DATA CLASS USUARIO
// Data class para representar un usuario
data class Usuario(
    val nombre: String,
    val correo: String,
    val edad: Int
)

// EXTENSION FUNCTIONS para la data class Usuario
// Función de extensión que devuelve el nombre con formato
fun Usuario.nombreFormateado(): String {
    return "Sr/Sra. ${this.nombre.uppercase()}"
}

// Función de extensión que determina si el usuario es mayor de edad
fun Usuario.esMayorDeEdad(): Boolean {
    return this.edad >= 18
}

// 5. SEALED CLASS (para ejemplo comparativo)
// Sealed class que representa el estado de una operación
sealed class EstadoOperacion {
    data class Exito(val mensaje: String) : EstadoOperacion()
    data class Error(val error: String) : EstadoOperacion()
    object Cargando : EstadoOperacion()
}
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    // Instancia de la clase Contador
    private val contador = Contador()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sección 1: Calculador de edad
        binding.buttonCalcularEdad.setOnClickListener {
            calcularEdad()
        }

        // Sección 2: Función calcularCuadrado
        binding.buttonCalcularCuadrado.setOnClickListener {
            probarFuncionCuadrado()
        }

        // Sección 3: Clase Contador
        binding.buttonIncrementar.setOnClickListener {
            incrementarContador()
        }

        binding.buttonResetear.setOnClickListener {
            resetearContador()
        }

        // Sección 4: Data class Usuario con extension functions
        binding.buttonCrearUsuario.setOnClickListener {
            crearYMostrarUsuario()
        }

        // Sección 5: Mostrar variables
        binding.buttonMostrarVariables.setOnClickListener {
            mostrarVariables()
        }
    }

    // ==================== SECCIÓN 1: CALCULADOR DE EDAD ====================
    private fun calcularEdad() {
        val fechaNacimientoStr = binding.editTextFechaNacimiento.text.toString()

        if (fechaNacimientoStr.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor ingrese la fecha de nacimiento", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.isLenient = false
            val fechaNacimiento = sdf.parse(fechaNacimientoStr)

            if (fechaNacimiento == null) {
                Toast.makeText(requireContext(), "Fecha inválida. Use el formato dd/MM/yyyy", Toast.LENGTH_SHORT).show()
                return
            }

            val calNacimiento = Calendar.getInstance()
            calNacimiento.time = fechaNacimiento
            val calHoy = Calendar.getInstance()
            var edad = calHoy.get(Calendar.YEAR) - calNacimiento.get(Calendar.YEAR)

            if (calHoy.get(Calendar.DAY_OF_YEAR) < calNacimiento.get(Calendar.DAY_OF_YEAR)) {
                edad--
            }

            if (edad < 0) {
                Toast.makeText(requireContext(), "La fecha de nacimiento no puede ser futura", Toast.LENGTH_SHORT).show()
                return
            }

            binding.textViewResultado.text = "Tu edad es: $edad años"

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Fecha inválida. Use el formato dd/MM/yyyy", Toast.LENGTH_SHORT).show()
        }
    }

    // ==================== SECCIÓN 2: FUNCIÓN CALCULAR CUADRADO ====================
    private fun probarFuncionCuadrado() {
        val numeroStr = binding.editTextNumero.text.toString()

        if (numeroStr.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor ingrese un número", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val numero = numeroStr.toInt()
            val resultado = calcularCuadrado(numero)

            binding.textViewResultadoCuadrado.text = """
                ENTRADA: $numero
                FUNCIÓN: calcularCuadrado($numero)
                SALIDA: $resultado

                Explicación: $numero × $numero = $resultado
            """.trimIndent()
        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "Por favor ingrese un número válido", Toast.LENGTH_SHORT).show()
        }
    }

    // ==================== SECCIÓN 3: CLASE CONTADOR ====================
    private fun incrementarContador() {
        contador.incrementar()
        actualizarVistaContador()
    }

    private fun resetearContador() {
        contador.valor = 0
        actualizarVistaContador()
    }

    private fun actualizarVistaContador() {
        binding.textViewContadorValor.text = "Valor del contador: ${contador.obtenerValor()}"
    }

    // ==================== SECCIÓN 4: DATA CLASS USUARIO + EXTENSIONS ====================
    private fun crearYMostrarUsuario() {
        val nombre = binding.editTextNombre.text.toString()
        val correo = binding.editTextCorreo.text.toString()
        val edadStr = binding.editTextEdad.text.toString()

        if (nombre.isEmpty() || correo.isEmpty() || edadStr.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val edad = edadStr.toInt()

            // Crear instancia de data class Usuario
            val usuario = Usuario(nombre, correo, edad)

            // Usar las extension functions
            val nombreFormato = usuario.nombreFormateado()
            val mayoriaEdad = usuario.esMayorDeEdad()
            val estadoEdad = if (mayoriaEdad) "SÍ es mayor de edad" else "NO es mayor de edad"

            binding.textViewResultadoUsuario.text = """
                📋 DATA CLASS CREADA:
                $usuario

                🔧 EXTENSION FUNCTIONS:

                1️⃣ nombreFormateado():
                   → "$nombreFormato"

                2️⃣ esMayorDeEdad():
                   → $mayoriaEdad
                   → $estadoEdad (edad >= 18)
            """.trimIndent()

        } catch (e: NumberFormatException) {
            Toast.makeText(requireContext(), "La edad debe ser un número válido", Toast.LENGTH_SHORT).show()
        }
    }

    // ==================== SECCIÓN 5: MOSTRAR VARIABLES ====================
    private fun mostrarVariables() {
        binding.textViewVariables.text = """
            VARIABLES DECLARADAS (5 tipos):

            1. Int (Entero):
               val enteroInmutable: Int = $enteroInmutable

            2. Double (Decimal):
               var numeroDecimal: Double = $numeroDecimal

            3. String (Texto):
               var textoMutable: String = "$textoMutable"

            4. Boolean (Booleano):
               var esVerdadero: Boolean = $esVerdadero

            5. List<Int> (Lista):
               var listaNumeros: List<Int> = $listaNumeros
        """.trimIndent()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}