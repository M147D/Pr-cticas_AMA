# Práctica 03

**Nombre:** SistemaAsistencia_Num03

## Descripción

Este proyecto tiene como objetivo el entendimiento y uso de la funciones de orden superior y lambda en una aplicación de asistencia para representar su controles y
reglas que validen el control de asistencia, implementando la composición de funciones y funciones de extensión para estructurar la lógica de negocio.

## Conclusiones y Recomendaciones

### Conclusiones:

* El uso de funciones de orden superior y expresiones lambda en Kotlin permitió definir las reglas de validación (como usuario habilitado o horario válido) de manera
  declarativa, concisa y reutilizable, encapsulando la lógica de forma funcional.
* Se demostró que la composición de funciones (usando operaciones como and, or y not sobre las lambdas) es una técnica poderosa para construir políticas de validación
  complejas (la regla canRegister) a partir de reglas atómicas simples, mejorando la modularidad y la legibilidad del código.
* Las funciones de extensión fueron esenciales para agregar comportamiento de validación (isEnabled, isAdmin, allowedAt) directamente a las clases de datos (Usuario),
  lo que mantuvo la lógica de negocio cohesionada con los modelos de dominio sin necesidad de herencia o clases utilitarias estáticas.
* Al definir las reglas como lambdas, se logró una alta flexibilidad ya que la política de asistencia puede ser modificada o extendida dinámicamente sin alterar el
  flujo principal de registro de acceso.
### Recomendaciones:

*  Se recomienda seguir utilizando funciones de orden superior y lambdas para cualquier tipo de validación o regla
  de negocio que deba ser combinada, encadenada o pasada como parámetro, ya que esto resulta en un código más testable y mantenible.
*  Se recomienda seguir utilizando funciones de orden superior y lambdas para cualquier tipo de validación o
  regla de negocio que deba ser combinada, encadenada o pasada como parámetro, ya que esto resulta en un código más testable y mantenible.
*  Se recomienda estandarizar los nombres y la implementación de los combinadores lógicos (and, or, not) como funciones de extensión en el tipo de la regla, asegurando
  que su uso sea intuitivo y consistente a lo largo de todo el proyecto.
*  Para las firmas de las lambdas de reglas ((Usuario) -> Boolean), se sugiere el uso de typealias (e.g., typealias Rule = (Usuario) -> Boolean) para hacer la intención
  del código más clara y reducir la redundancia en las declaraciones de funciones.
