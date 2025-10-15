# Práctica 02

## Información del Proyecto

**Nombre:** Estudio Comparativo de Lenguajes de Programación para Desarrollo Móvil


## Descripción

Este proyecto tiene como objetivo realizar un análisis exhaustivo de tres lenguajes de programación ampliamente utilizados en el desarrollo de software: **Kotlin**, **C#** y **JavaScript**. 

La práctica busca:
- Comprender la sintaxis básica de Kotlin
- Comparar las características fundamentales entre los tres lenguajes
- Identificar ventajas y desventajas de cada uno en el contexto del desarrollo móvil
- Analizar características especiales de Kotlin como null safety, data classes y sealed classes
- Explorar casos de uso reales de cada lenguaje en la industria

---
## Conclusiones y Recomendaciones

### Conclusiones:

* Kotlin se posiciona como el lenguaje más adecuado para desarrollo Android nativo debido a su adopción oficial por Google,
  características modernas como null safety y sintaxis concisa que reduce significativamente el código boilerplate comparado con Java.

* El sistema de tipado estático con inferencia presente en Kotlin y C# ofrece un mejor balance entre seguridad y productividad
  comparado con el tipado dinámico de JavaScript, previniendo errores comunes en tiempo de compilación en lugar de tiempo de ejecución.

* La característica de null safety de Kotlin es superior a las implementaciones de C# y JavaScript, ya que está integrada nativamente
  en el sistema de tipos y obliga al desarrollador a manejar explícitamente los valores nulos, reduciendo drásticamente los NullPointerException.

* Las data classes de Kotlin automatizan la generación de métodos como equals(), hashCode(), toString() y copy(), reduciendo código
  repetitivo que en C# requiere records (desde C# 9) y en JavaScript debe implementarse manualmente.

* JavaScript mantiene su dominio en desarrollo web y multiplataforma gracias a su ecosistema npm y frameworks como React Native,
  aunque sacrifica seguridad de tipos que tanto Kotlin como C# proporcionan nativamente.

* Las extension functions de Kotlin permiten mantener código más limpio y organizado al extender funcionalidades de clases sin herencia,
  característica que C# implementa de forma similar pero JavaScript solo puede simular mediante modificación de prototipos.

* C# mantiene su fortaleza en aplicaciones empresariales y desarrollo de videojuegos con Unity, donde su madurez, herramientas
  robustas y el ecosistema .NET lo hacen preferible sobre Kotlin y JavaScript para estos casos específicos.

* Los tres lenguajes soportan programación funcional con map, filter y reduce, aunque Kotlin y JavaScript lo implementan de
  forma más natural mientras C# utiliza LINQ que, aunque potente, requiere sintaxis diferente.

### Recomendaciones:

* Se recomienda utilizar Kotlin como lenguaje principal para desarrollo Android nativo, aprovechando su interoperabilidad con Java
  para migrar proyectos existentes de forma gradual sin necesidad de reescribir todo el código.

* Para proyectos que requieran desarrollo multiplataforma, se recomienda evaluar React Native (JavaScript) si el equipo tiene experiencia web
  y se prioriza velocidad de desarrollo, o Kotlin Multiplatform si se requiere máximo rendimiento y control.

* Al trabajar con Kotlin, se recomienda aprovechar las data classes para representar modelos de datos y DTOs, y las sealed classes para
  representar estados de la aplicación, ya que estas características reducen errores y mejoran la mantenibilidad del código.

* Se recomienda utilizar los operadores de null safety de Kotlin (?, ?:, !!) de forma consciente: preferir el safe call (?.) y Elvis operator
  (?:) sobre el not-null assertion (!!), ya que este último puede causar excepciones si no se usa correctamente.

* Para mantener código limpio y expresivo, se recomienda crear extension functions en lugar de clases de utilidad estáticas, especialmente
  para operaciones comunes de validación, formateo o transformación de datos.

* Se recomienda activar la característica de nullable reference types en proyectos de C# (desde C# 8.0) para obtener advertencias similares
  al null safety de Kotlin y prevenir errores de referencias nulas.

* Al elegir entre los tres lenguajes, se recomienda priorizar el contexto del proyecto: Kotlin para Android, C# para aplicaciones empresariales
  o gaming con Unity, y JavaScript/TypeScript para desarrollo web o cuando se requiera máxima versatilidad entre frontend y backend.

* Se recomienda adoptar TypeScript en lugar de JavaScript puro para proyectos medianos y grandes, ya que añade tipado estático que facilita
  el mantenimiento, reduce bugs y mejora la experiencia de desarrollo con mejor autocompletado.

* Para desarrolladores que vienen de Java, se recomienda estudiar las características funcionales de Kotlin como lambdas, funciones de orden
  superior y operaciones de colecciones, ya que permiten escribir código más conciso y expresivo.

* Se recomienda usar val por defecto en Kotlin, const en C# y const en JavaScript para declarar variables, cambiando a var/let solo cuando
  la mutabilidad sea estrictamente necesaria, promoviendo así código más seguro y predecible.
---

## 1. Cuadro Comparativo

### Tipado de Variables

| Aspecto | Kotlin | C# | JavaScript |
|---------|--------|-----|------------|
| **Tipo de sistema** | Estático, con inferencia | Estático, con inferencia | Dinámico |
| **Declaración mutable** | `var nombre: String = "Juan"` | `var nombre = "Juan";` | `let nombre = "Juan";` |
| **Declaración inmutable** | `val nombre: String = "Juan"` | `const string nombre = "Juan";` | `const nombre = "Juan";` |
| **Inferencia de tipos** | `val edad = 25` (infiere Int) | `var edad = 25;` (infiere int) | `let edad = 25;` (tipo dinámico) |
| **Nullable por defecto** | No (debe especificarse con `?`) | No (desde C# 8.0 con nullable reference types) | Sí (todo puede ser undefined/null) |

### Manejo de Funciones

| Aspecto | Kotlin | C# | JavaScript |
|---------|--------|-----|------------|
| **Función básica** | `fun suma(a: Int, b: Int): Int { return a + b }` | `int Suma(int a, int b) { return a + b; }` | `function suma(a, b) { return a + b; }` |
| **Función de expresión** | `fun suma(a: Int, b: Int) = a + b` | `int Suma(int a, int b) => a + b;` | `const suma = (a, b) => a + b;` |
| **Parámetros por defecto** | `fun saludar(nombre: String = "Mundo")` | `void Saludar(string nombre = "Mundo")` | `function saludar(nombre = "Mundo")` |
| **Parámetros nombrados** | Sí: `saludar(nombre = "Ana")` | Sí: `Saludar(nombre: "Ana")` | No (requiere objeto) |
| **Funciones de orden superior** | Nativas y comunes | Soportadas (delegados, Func, Action) | Nativas y muy comunes |
| **Funciones de extensión** | Sí (característica nativa) | Sí (métodos de extensión) | No (solo prototipos) |

### Orientación a Objetos

| Aspecto | Kotlin | C# | JavaScript |
|---------|--------|-----|------------|
| **Declaración de clase** | `class Persona(val nombre: String)` | `public class Persona { public string Nombre { get; set; } }` | `class Persona { constructor(nombre) { this.nombre = nombre; } }` |
| **Herencia** | `class Estudiante : Persona()` (clases cerradas por defecto) | `class Estudiante : Persona` (clases abiertas por defecto) | `class Estudiante extends Persona` |
| **Interfaces** | `interface Volador { fun volar() }` | `interface IVolador { void Volar(); }` | No hay interfaces verdaderas (solo clases abstractas) |
| **Propiedades** | Nativas con getters/setters automáticos | Propiedades con get/set | Campos directos o getters/setters manuales |
| **Modificadores de acceso** | public, private, protected, internal | public, private, protected, internal | # para private (ES2022) |
| **Clases abstractas** | `abstract class Animal` | `abstract class Animal` | Simuladas con throw en métodos |
| **Data classes** | `data class Usuario(val id: Int)` | `record Usuario(int Id);` (C# 9+) | No nativas (requiere implementación manual) |

### Manejo de Colecciones

| Aspecto | Kotlin | C# | JavaScript |
|---------|--------|-----|------------|
| **Lista inmutable** | `val lista = listOf(1, 2, 3)` | `var lista = new List<int> { 1, 2, 3 }.AsReadOnly();` | `const lista = [1, 2, 3];` (mutable internamente) |
| **Lista mutable** | `val lista = mutableListOf(1, 2, 3)` | `var lista = new List<int> { 1, 2, 3 };` | `let lista = [1, 2, 3];` |
| **Map/Diccionario** | `val mapa = mapOf("a" to 1, "b" to 2)` | `var dict = new Dictionary<string, int> { {"a", 1}, {"b", 2} };` | `const mapa = { a: 1, b: 2 };` o `new Map([['a', 1]])` |
| **Set** | `val conjunto = setOf(1, 2, 3)` | `var set = new HashSet<int> { 1, 2, 3 };` | `const conjunto = new Set([1, 2, 3]);` |
| **Operaciones funcionales** | map, filter, reduce, etc. (nativas) | LINQ (Select, Where, Aggregate) | map, filter, reduce (nativas) |
| **Seguridad de tipos** | Fuertemente tipado con genéricos | Fuertemente tipado con genéricos | Sin tipos (arrays genéricos) |

---

## 2. Ejemplos de Código Equivalentes

### Ejemplo 1: Declaración de Variables

**Kotlin:**
```kotlin
val nombre: String = "Ana"
var edad: Int = 25
val pi = 3.14159  // Infiere Double
```

**C#:**
```csharp
const string nombre = "Ana";
int edad = 25;
var pi = 3.14159;  // Infiere double
```

**JavaScript:**
```javascript
const nombre = "Ana";
let edad = 25;
const pi = 3.14159;  // number
```

### Ejemplo 2: Funciones

**Kotlin:**
```kotlin
fun calcularArea(base: Double, altura: Double): Double {
    return base * altura
}

// Versión de expresión
fun calcularArea(base: Double, altura: Double) = base * altura
```

**C#:**
```csharp
double CalcularArea(double baseVal, double altura) {
    return baseVal * altura;
}

// Versión de expresión
double CalcularArea(double baseVal, double altura) => baseVal * altura;
```

**JavaScript:**
```javascript
function calcularArea(base, altura) {
    return base * altura;
}

// Arrow function
const calcularArea = (base, altura) => base * altura;
```

### Ejemplo 3: Clases y Herencia

**Kotlin:**
```kotlin
open class Persona(val nombre: String, var edad: Int) {
    open fun presentarse() {
        println("Hola, soy $nombre y tengo $edad años")
    }
}

class Estudiante(nombre: String, edad: Int, val carrera: String) 
    : Persona(nombre, edad) {
    override fun presentarse() {
        println("Soy $nombre, estudio $carrera")
    }
}
```

**C#:**
```csharp
public class Persona {
    public string Nombre { get; set; }
    public int Edad { get; set; }
    
    public Persona(string nombre, int edad) {
        Nombre = nombre;
        Edad = edad;
    }
    
    public virtual void Presentarse() {
        Console.WriteLine($"Hola, soy {Nombre} y tengo {Edad} años");
    }
}

public class Estudiante : Persona {
    public string Carrera { get; set; }
    
    public Estudiante(string nombre, int edad, string carrera) 
        : base(nombre, edad) {
        Carrera = carrera;
    }
    
    public override void Presentarse() {
        Console.WriteLine($"Soy {Nombre}, estudio {Carrera}");
    }
}
```

**JavaScript:**
```javascript
class Persona {
    constructor(nombre, edad) {
        this.nombre = nombre;
        this.edad = edad;
    }
    
    presentarse() {
        console.log(`Hola, soy ${this.nombre} y tengo ${this.edad} años`);
    }
}

class Estudiante extends Persona {
    constructor(nombre, edad, carrera) {
        super(nombre, edad);
        this.carrera = carrera;
    }
    
    presentarse() {
        console.log(`Soy ${this.nombre}, estudio ${this.carrera}`);
    }
}
```

### Ejemplo 4: Colecciones y Operaciones

**Kotlin:**
```kotlin
val numeros = listOf(1, 2, 3, 4, 5)
val pares = numeros.filter { it % 2 == 0 }
val duplicados = numeros.map { it * 2 }
val suma = numeros.reduce { acc, num -> acc + num }
```

**C#:**
```csharp
var numeros = new List<int> { 1, 2, 3, 4, 5 };
var pares = numeros.Where(n => n % 2 == 0).ToList();
var duplicados = numeros.Select(n => n * 2).ToList();
var suma = numeros.Aggregate((acc, num) => acc + num);
```

**JavaScript:**
```javascript
const numeros = [1, 2, 3, 4, 5];
const pares = numeros.filter(n => n % 2 === 0);
const duplicados = numeros.map(n => n * 2);
const suma = numeros.reduce((acc, num) => acc + num);
```

---

## 3. Características Específicas de Kotlin

### 3.1 Null Safety (?, !!)

**Concepto:** Kotlin distingue entre referencias que pueden contener null y las que no, previniendo el famoso NullPointerException.

**Ejemplo:**
```kotlin
// Variable no nullable - No puede ser null
var nombre: String = "Ana"
// nombre = null  // ERROR de compilación

// Variable nullable - Puede ser null
var apellido: String? = "García"
apellido = null  // OK

// Operador safe call (?)
// Ejecuta solo si no es null, retorna null si es null
val longitud = apellido?.length  // Int? (puede ser null)

// Operador Elvis (?:)
// Proporciona un valor por defecto si es null
val longitudSegura = apellido?.length ?: 0

// Operador not-null assertion (!!)
// Afirma que el valor NO es null (lanza excepción si lo es)
val longitudForzada = apellido!!.length  // Peligroso si apellido es null

// Let con safe call
apellido?.let { 
    println("El apellido tiene ${it.length} caracteres")
}

// Ejemplo práctico
data class Usuario(val nombre: String, val email: String?)

fun enviarEmail(usuario: Usuario) {
    // Safe call con let
    usuario.email?.let { email ->
        println("Enviando email a: $email")
    } ?: println("Usuario sin email")
    
    // Con Elvis operator
    val emailDestino = usuario.email ?: "sin-email@example.com"
}

val usuario1 = Usuario("Pedro", "pedro@example.com")
val usuario2 = Usuario("María", null)

enviarEmail(usuario1)  // Imprime: Enviando email a: pedro@example.com
enviarEmail(usuario2)  // Imprime: Usuario sin email
```

### 3.2 Data Classes

**Concepto:** Clases diseñadas principalmente para almacenar datos. El compilador genera automáticamente `equals()`, `hashCode()`, `toString()`, `copy()`, y funciones de desestructuración.

**Ejemplo:**
```kotlin
// Data class básica
data class Producto(
    val id: Int,
    val nombre: String,
    val precio: Double
)

fun ejemploDataClass() {
    val producto1 = Producto(1, "Laptop", 999.99)
    
    // toString() automático
    println(producto1)  // Producto(id=1, nombre=Laptop, precio=999.99)
    
    // equals() automático (compara por contenido, no por referencia)
    val producto2 = Producto(1, "Laptop", 999.99)
    println(producto1 == producto2)  // true
    
    // copy() para crear copias modificadas
    val productoRebajado = producto1.copy(precio = 799.99)
    println(productoRebajado)  // Producto(id=1, nombre=Laptop, precio=799.99)
    
    // Desestructuración
    val (id, nombre, precio) = producto1
    println("ID: $id, Nombre: $nombre, Precio: $precio")
}

// Ejemplo más complejo
data class Direccion(
    val calle: String,
    val ciudad: String,
    val codigoPostal: String
)

data class Cliente(
    val id: Int,
    val nombre: String,
    val email: String,
    val direccion: Direccion
)

fun ejemploComplejo() {
    val cliente = Cliente(
        id = 101,
        nombre = "Laura Martínez",
        email = "laura@example.com",
        direccion = Direccion("Av. Principal 123", "Madrid", "28001")
    )
    
    // Modificar dirección manteniendo el resto igual
    val clienteMudado = cliente.copy(
        direccion = cliente.direccion.copy(ciudad = "Barcelona")
    )
    
    println(clienteMudado)
}
```

### 3.3 Sealed Classes

**Concepto:** Clases restringidas que representan jerarquías limitadas. Perfectas para representar estados o resultados con un conjunto finito de posibilidades.

**Ejemplo:**
```kotlin
// Sealed class para representar el resultado de una operación
sealed class Resultado {
    data class Exito(val datos: String) : Resultado()
    data class Error(val mensaje: String, val codigo: Int) : Resultado()
    object Cargando : Resultado()
}

fun procesarResultado(resultado: Resultado) {
    // When exhaustivo - el compilador verifica que cubras todos los casos
    when (resultado) {
        is Resultado.Exito -> {
            println("Operación exitosa: ${resultado.datos}")
        }
        is Resultado.Error -> {
            println("Error ${resultado.codigo}: ${resultado.mensaje}")
        }
        is Resultado.Cargando -> {
            println("Cargando...")
        }
        // No necesita else, porque sealed class garantiza casos finitos
    }
}

// Ejemplo de uso
fun obtenerDatosDelServidor(): Resultado {
    return try {
        // Simular llamada exitosa
        Resultado.Exito("Datos obtenidos correctamente")
    } catch (e: Exception) {
        Resultado.Error("No se pudo conectar al servidor", 500)
    }
}

// Ejemplo más elaborado: Estados de una pantalla
sealed class EstadoPantalla {
    object Inicial : EstadoPantalla()
    object Cargando : EstadoPantalla()
    data class Contenido(val items: List<String>) : EstadoPantalla()
    data class Error(val mensaje: String) : EstadoPantalla()
}

class PantallaViewModel {
    var estado: EstadoPantalla = EstadoPantalla.Inicial
        set(value) {
            field = value
            renderizar()
        }
    
    private fun renderizar() {
        when (estado) {
            is EstadoPantalla.Inicial -> 
                println("Pantalla inicial")
            is EstadoPantalla.Cargando -> 
                println("Mostrando indicador de carga...")
            is EstadoPantalla.Contenido -> {
                val contenido = estado as EstadoPantalla.Contenido
                println("Mostrando ${contenido.items.size} items")
            }
            is EstadoPantalla.Error -> {
                val error = estado as EstadoPantalla.Error
                println("Error: ${error.mensaje}")
            }
        }
    }
    
    fun cargarDatos() {
        estado = EstadoPantalla.Cargando
        // Simular carga
        Thread.sleep(1000)
        estado = EstadoPantalla.Contenido(listOf("Item 1", "Item 2", "Item 3"))
    }
}
```

---

## 4. Extension Functions

Permiten añadir nuevas funciones a clases existentes sin modificar su código fuente ni usar herencia. Ayudan a mantener el código limpio y expresivo. Ya que:

- Extiende funcionalidad de clases que no controlas (librerías, clases del sistema)
- Mantiene el código organizado y legible
- Evita clases de utilidad estáticas

### Ejemplos:

```kotlin
// Extension function básica
fun String.esPalindromo(): Boolean {
    val limpio = this.replace(" ", "").lowercase()
    return limpio == limpio.reversed()
}

fun ejemploBasico() {
    println("anita lava la tina".esPalindromo())  // true
    println("hola".esPalindromo())  // false
}

// Extension function con parámetros
fun String.truncar(maxLength: Int, sufijo: String = "..."): String {
    return if (this.length > maxLength) {
        this.substring(0, maxLength - sufijo.length) + sufijo
    } else {
        this
    }
}

fun ejemploConParametros() {
    val texto = "Este es un texto muy largo que necesita ser truncado"
    println(texto.truncar(20))  // "Este es un texto..."
}

// Extension function para Int
fun Int.esPar(): Boolean = this % 2 == 0
fun Int.esImpar(): Boolean = this % 2 != 0

fun Int.veces(accion: (Int) -> Unit) {
    for (i in 1..this) {
        accion(i)
    }
}

fun ejemploNumeros() {
    println(4.esPar())  // true
    println(7.esImpar())  // true
    
    3.veces { numero ->
        println("Iteración $numero")
    }
}

// Extension function para colecciones
fun <T> List<T>.segundoElementoONull(): T? {
    return if (this.size >= 2) this[1] else null
}

fun List<Int>.sumarPares(): Int {
    return this.filter { it % 2 == 0 }.sum()
}

fun ejemploColecciones() {
    val numeros = listOf(1, 2, 3, 4, 5)
    println(numeros.segundoElementoONull())  // 2
    println(numeros.sumarPares())  // 6 (2 + 4)
}

// Extension functions para tipos nullable
fun String?.esNullOVacia(): Boolean {
    return this == null || this.isEmpty()
}

fun ejemploNullable() {
    val texto1: String? = null
    val texto2: String? = ""
    val texto3: String? = "Hola"
    
    println(texto1.esNullOVacia())  // true
    println(texto2.esNullOVacia())  // true
    println(texto3.esNullOVacia())  // false
}

// Extension properties
val String.primeraMayuscula: String
    get() = this.replaceFirstChar { it.uppercase() }

val List<Int>.promedio: Double
    get() = if (this.isEmpty()) 0.0 else this.sum().toDouble() / this.size

fun ejemploPropiedades() {
    println("hola".primeraMayuscula)  // "Hola"
    println(listOf(1, 2, 3, 4, 5).promedio)  // 3.0
}

// Caso práctico: Validaciones
fun String.esEmailValido(): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$".toRegex()
    return this.matches(emailRegex)
}

fun String.esTelefonoValido(): Boolean {
    val telefonoRegex = "^[0-9]{9,10}$".toRegex()
    return this.replace(" ", "").matches(telefonoRegex)
}

data class FormularioRegistro(val email: String, val telefono: String)

fun validarFormulario(formulario: FormularioRegistro): List<String> {
    val errores = mutableListOf<String>()
    
    if (!formulario.email.esEmailValido()) {
        errores.add("Email inválido")
    }
    
    if (!formulario.telefono.esTelefonoValido()) {
        errores.add("Teléfono inválido")
    }
    
    return errores
}
```

### Ventajas de Extension Functions:

1. **Código más legible**: `texto.truncar(20)` vs `StringUtils.truncar(texto, 20)`
2. **Organización**: Agrupas funcionalidad relacionada
3. **Autocompletado**: El IDE sugiere las extensiones
4. **No contaminas clases**: Las funciones están separadas pero se usan naturalmente
5. **Testeable**: Fácil de probar unitariamente

---

## 5. Casos de Uso Comunes por Lenguaje

### C# - Casos de Uso

#### 1. **Aplicaciones Empresariales**
- **ERP (Enterprise Resource Planning)**: Sistemas de gestión empresarial integral
- **CRM (Customer Relationship Management)**: Gestión de relaciones con clientes
- **Sistemas bancarios y financieros**: Trading platforms, sistemas de contabilidad
- **Portales corporativos**: Intranets empresariales con ASP.NET
- **Aplicaciones de recursos humanos**: Nóminas, gestión de personal
- **Herramientas de automatización**: Workflows empresariales, procesos de negocio

**Ejemplos reales:**
- Microsoft Dynamics (ERP/CRM)
- Sistemas internos de bancos como JPMorgan Chase
- Plataformas de trading de bolsa

#### 2. **Videojuegos**
- **Motor Unity**: Uno de los motores de videojuegos más populares usa C#
- **Juegos móviles**: Candy Crush, Pokémon GO, Among Us
- **Juegos indie**: Hollow Knight, Cuphead, Ori and the Blind Forest
- **Juegos AAA**: Escape from Tarkov, Cities: Skylines, Subnautica
- **Herramientas de desarrollo**: Editores personalizados, sistemas de modding

**Por qué C# en gaming:**
- Excelente integración con Unity
- Balance entre rendimiento y facilidad de uso
- Sistema de tipos robusto para lógica compleja
- Debugging eficiente

#### 3. **Aplicaciones de Escritorio**
- **Aplicaciones Windows**: WPF, WinForms, Windows Forms
- **Software de productividad**: Microsoft Office (partes escritas en C#)
- **Herramientas de desarrollo**: Visual Studio (parcialmente)
- **Software científico**: Aplicaciones de análisis de datos
- **Aplicaciones CAD/CAM**: Software de diseño asistido
- **Clientes de gestión**: Aplicaciones de inventario, POS (Point of Sale)

**Ejemplos reales:**
- Paint.NET (editor de imágenes)
- Bitwarden (gestor de contraseñas)
- ShareX (captura de pantalla)

#### 4. **Servicios Web y APIs**
- **ASP.NET Core**: APIs RESTful de alto rendimiento
- **Microservicios**: Arquitecturas escalables
- **Servicios en la nube**: Azure Functions, Azure App Services
- **WebSockets**: Aplicaciones en tiempo real

### JavaScript - Casos de Uso

#### 1. **Aplicaciones Web (Frontend)**
- **Single Page Applications (SPA)**: React, Angular, Vue.js
- **Sitios web interactivos**: Formularios dinámicos, validación en tiempo real
- **Dashboards y paneles de control**: Visualización de datos
- **E-commerce**: Carritos de compra, catálogos de productos
- **Plataformas de redes sociales**: Interfaces interactivas
- **Aplicaciones web progresivas (PWA)**: Aplicaciones que funcionan offline

**Ejemplos reales:**
- Facebook (React)
- Netflix (React)
- Gmail (JavaScript vanilla y frameworks propios)
- Trello (gestión de proyectos)
- Spotify Web Player

#### 2. **Aplicaciones Móviles (React Native, Ionic)**
- **Apps multiplataforma**: Una base de código para iOS y Android
- **Aplicaciones empresariales móviles**: Aplicaciones internas de empresas
- **Apps de contenido**: Noticias, blogs, medios
- **E-commerce móvil**: Tiendas online

**Ejemplos reales:**
- Instagram (React Native)
- Discord (React Native)
- Uber Eats (React Native)
- Bloomberg (React Native)

#### 3. **Desarrollo Backend con Node.js**
- **APIs RESTful**: Servicios web escalables
- **Aplicaciones en tiempo real**: Chat, notificaciones, colaboración
- **Streaming de datos**: Video, audio, datos en vivo
- **Microservicios**: Arquitecturas distribuidas
- **Herramientas de automatización**: Scripts, bots, webhooks
- **Procesamiento de archivos**: Conversión, compresión, análisis

**Ejemplos reales:**
- LinkedIn (Node.js en backend)
- PayPal (Node.js)
- Uber (Node.js para servicios)
- Netflix (Node.js en partes del backend)

#### 4. **Herramientas de Desarrollo**
- **Editores de código**: Visual Studio Code (Electron + JavaScript)
- **Bundlers y build tools**: Webpack, Vite, Rollup
- **Testing frameworks**: Jest, Mocha, Cypress
- **Automatización**: Gulp, Grunt, npm scripts

#### 5. **IoT (Internet of Things)**
- **Johnny-Five**: Framework para robótica y IoT
- **Node-RED**: Programación visual para IoT
- **Control de dispositivos**: Raspberry Pi, Arduino con Node.js

#### 6. **Aplicaciones de Escritorio (Electron)**
- **Editores de código**: Visual Studio Code, Atom
- **Aplicaciones de comunicación**: Slack, Discord, Microsoft Teams
- **Herramientas de productividad**: Notion, Figma (parcialmente)
- **Clientes de bases de datos**: MongoDB Compass

---

## 6. Diferencias Clave Documentadas

### 6.1 Tipado de Variables

| Diferencia | Kotlin | C# | JavaScript |
|------------|--------|-----|------------|
| **Seguridad de tipos** | Fuerte con null safety explícito | Fuerte con nullable types opcionales | Débil, tipos dinámicos |
| **Inmutabilidad** | `val` nativo y ampliamente usado | `const` para referencias constantes | `const` pero solo para referencias |
| **Inferencia** | Muy potente, infiere la mayoría de tipos | Buena con `var`, pero menos agresiva | No aplica (tipado dinámico) |

**Impacto práctico:**
- Kotlin reduce bugs de null en tiempo de compilación
- C# requiere más disciplina del desarrollador para null safety
- JavaScript requiere validaciones en tiempo de ejecución

### 6.2 Manejo de Funciones

| Diferencia | Kotlin | C# | JavaScript |
|------------|--------|-----|------------|
| **Sintaxis concisa** | Muy concisa con expresiones lambda | Moderada con expresiones lambda | Muy concisa con arrow functions |
| **Funciones de primera clase** | Nativas | Mediante delegados | Nativas |
| **Parámetros nombrados** | Sí, nativos | Sí, nativos | No nativos (workaround con objetos) |

**Impacto práctico:**
- Kotlin permite código más expresivo y legible
- JavaScript tiene la sintaxis más flexible pero menos estructurada
- C# ofrece balance entre formalidad y expresividad

### 6.3 Orientación a Objetos

| Diferencia | Kotlin | C# | JavaScript |
|------------|--------|-----|------------|
| **Herencia por defecto** | Clases cerradas (final) | Clases abiertas | Clases abiertas |
| **Propiedades** | Backing fields automáticos | Propiedades con sintaxis especial | Campos simples |
| **Interfaces** | Pueden tener implementaciones | Solo desde C# 8.0 | No existen formalmente |

**Impacto práctico:**
- Kotlin favorece composición sobre herencia (mejor diseño)
- C# es más flexible pero puede llevar a malas prácticas
- JavaScript requiere más disciplina arquitectónica

### 6.4 Manejo de Colecciones

| Diferencia | Kotlin | C# | JavaScript |
|------------|--------|-----|------------|
| **Mutabilidad explícita** | Colecciones mutables e inmutables separadas | Una colección con métodos de solo lectura | Sin distinción real |
| **Operaciones funcionales** | Nativas y optimizadas | LINQ (muy potente pero sintaxis diferente) | Nativas |
| **Tipos genéricos** | Covarianza/Contravarianza nativas | Covarianza/Contravarianza explícitas | No aplica |

**Impacto práctico:**
- Kotlin hace explícita la intención de mutabilidad
- C# LINQ es extremadamente potente para consultas complejas
- JavaScript es más simple pero menos seguro

### 6.5 Características Únicas

**Kotlin:**
- Null safety en el sistema de tipos
- Data classes con funciones generadas automáticamente
- Sealed classes para estados finitos
- Extension functions sin herencia
- Corrutinas nativas para asincronía

**C#:**
- LINQ para consultas declarativas
- Async/await maduro y eficiente
- Records para datos inmutables (C# 9+)
- Pattern matching potente
- Reflexión robusta

**JavaScript:**
- Prototipado dinámico
- Closures y scope flexible
- Event loop para asincronía
- Ecosystem npm masivo
- Ejecución en navegadores nativamente

---

## 7. Conclusiones

### 7.1 Conclusiones Generales

1. **Kotlin emerge como líder para Android**: Su adopción oficial por Google, combinada con características modernas como null safety y sintaxis concisa, lo posicionan como la mejor opción para desarrollo Android nativo.

2. **C# mantiene fortaleza en el ecosistema empresarial**: Su integración con .NET, herramientas maduras y capacidad para aplicaciones empresariales y videojuegos lo hacen invaluable en ciertos nichos.

3. **JavaScript domina el desarrollo web**: Su versatilidad para frontend, backend (Node.js) y móvil (React Native) lo hace el lenguaje más universal, aunque con compromisos en seguridad de tipos.

4. **El tipado estático previene errores**: Tanto Kotlin como C# demuestran que el tipado estático con inferencia ofrece el mejor balance entre seguridad y productividad.

5. **Null safety es crucial**: La característica de null safety de Kotlin debería ser estándar en lenguajes modernos, reduciendo significativamente los crashes en producción.

### 7.2 Comparación para Desarrollo Móvil

**Para Android Nativo:**
- **Kotlin** - Soporte oficial, rendimiento óptimo, características modernas
- **Java** - Legado y compatibilidad

**Para Desarrollo Multiplataforma:**
- **Depende del contexto**
  - JavaScript/React Native: Prototipado rápido, equipos web
  - C#/.NET MAUI: Aplicaciones empresariales, equipos .NET
  - Kotlin Multiplatform: Máximo control, equipos Android

### 7.3 Por Tipo de Proyecto

| Tipo de Proyecto | Lenguaje Recomendado | Justificación |
|------------------|----------------------|---------------|
| App Android nativa | Kotlin | Soporte oficial, mejor rendimiento |
| App iOS nativa | Swift | Nativo de Apple |
| App multiplataforma | React Native (JS) | Comunidad, recursos, velocidad |
| Videojuegos | C# (Unity) | Ecosistema maduro, herramientas |
| Aplicación empresarial | C# o Kotlin | Tipado fuerte, mantenibilidad |
| Prototipo rápido | JavaScript | Flexibilidad, desarrollo ágil |
| Aplicación backend | Node.js (JS) o C# | Rendimiento vs. ecosistema |

   - Expresiones en lugar de statements
   - Composición de funciones
