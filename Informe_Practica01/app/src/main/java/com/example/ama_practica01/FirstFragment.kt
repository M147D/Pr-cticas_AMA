package com.example.ama_practica01

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.ama_practica01.databinding.FragmentFirstBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCalcularEdad.setOnClickListener {
            calcularEdad()
        }
    }

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}