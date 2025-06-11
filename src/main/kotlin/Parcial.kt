package ar.edu.algo2

import java.time.DayOfWeek
import java.time.LocalDate

object RepartijaDeRegalos {
    val personas = mutableListOf<Persona>()
    val regalos = mutableListOf<Regalo>()

    fun repartir() {
        personas.forEach { persona ->
            val regaloElegido = regalos.find {regalo -> persona.leGusta(regalo)} ?: Voucher() // Si no encuentra regalo acorde, va por Voucher
            persona.recibirRegalo(regaloElegido)

        }
    }
}

//Punto 1
class Persona{
    lateinit var domicilio: String
    lateinit var nombre: String
    lateinit var dni: String
    lateinit var email: String
    lateinit var criterioEleccionRegalo: CriterioEleccionRegalo
    val regalosRecibidosObservers = mutableListOf<RegaloRecibidoObserver>()
    var regalosRecibidos = mutableListOf<Regalo>()


    fun leGusta(regalo: Regalo) = criterioEleccionRegalo.leGusta(regalo)

    fun recibirRegalo(regalo: Regalo){
        regalosRecibidos.add(regalo)
        regalosRecibidosObservers.forEach {it.regaloRecibido(regalo, this)}
    }

}

// Strategy ==> desacoplar el algoritmo de elección de una clase
// La lógica de cómo se hace algo se separa de la clase que usa esa lógica
//
// En este caso desacoplar el algoritmo de elección de un regalo

interface CriterioEleccionRegalo {
    fun leGusta(regalo: Regalo): Boolean
}

object Conformista: CriterioEleccionRegalo{
    override fun leGusta(regalo: Regalo) = true
}

class Interesada(val plata: Int): CriterioEleccionRegalo{
    override fun leGusta(regalo: Regalo) = regalo.valor > plata
}

object Exigente: CriterioEleccionRegalo{
    override fun leGusta(regalo: Regalo) = regalo.valioso()
}

class Marquera (val marca: String): CriterioEleccionRegalo{
    override fun leGusta(regalo: Regalo) = regalo.marca == marca
}

// Composite ==> rama y hojas funcionan de manera polimórfica
// Implementan la misma interfaz o clase base
// Se pueden tratar de manera uniforme sin qué el código sepa si maneja una hoja o una rama
//
// En este caso Combineta es la rama y las anteriores las hojas
class Combineta(): CriterioEleccionRegalo{
    val criterioEleccionRegalos = mutableListOf<CriterioEleccionRegalo>() //Se puede de ambas formas, pero esta es más preferible

    override fun leGusta(regalo: Regalo) = criterioEleccionRegalos.any { it.leGusta(regalo) }
}


//Punto 2

// Template Method
// Definimos el esqueleto de un algoritmo donde una parte se repite
// pero permitimos que algunas otras, específicas, sean personalizables en clases hijas.
//
// Template valioso()
// Primitiva es valiosoEspecifico()
/**************************************************************************/
abstract class Regalo(val valor: Int, val marca: String, val codigo: String) {

    open fun valioso() = ((valor > 5000.0) && valiosoEspecifico())
    abstract fun valiosoEspecifico(): Boolean
}


class Ropa(precio:Int, marca: String, codigo: String): Regalo(precio, marca, codigo){
    val marcasValiosas = listOf<String>("Jordache", "Lee", "Charro","Motor Oil")
    override fun valiosoEspecifico() = marcasValiosas.contains(marca)
}

class Juguete(precio:Int, marca: String, codigo: String, val fechaLanzamiento:LocalDate): Regalo(precio, marca, codigo){
    val anioAComparar: Int = 2000
    override fun valiosoEspecifico() = fechaLanzamiento.year < anioAComparar
}

class Perfume(precio:Int, marca: String, codigo: String, val origenExtranjero: Boolean): Regalo(precio, marca, codigo){
    override fun valiosoEspecifico() = origenExtranjero
}

class Experiencia(precio:Int, marca: String, codigo: String,val fechaDeExperiencia: LocalDate): Regalo(precio, marca, codigo) {
    val diaAComparar: DayOfWeek = DayOfWeek.FRIDAY
    override fun valiosoEspecifico() = fechaDeExperiencia.dayOfWeek == diaAComparar
}

class Voucher: Regalo(2000, "Papapp","214125"){
    override fun valioso(): Boolean = false
    override fun valiosoEspecifico(): Boolean = false
}

// Punto 3
interface RegaloRecibidoObserver {
    fun regaloRecibido(regalo: Regalo, persona: Persona)

}

class RegaloRecibidoEnvioMail(): RegaloRecibidoObserver { //Setter Injection
    lateinit var mailSender: MailSender
    lateinit var appFrom: String

    override fun regaloRecibido(regalo: Regalo, persona: Persona) {
        mailSender.sendMail(
            Mail(appFrom,
                persona.email,
                "Recibiste un regalo!",
                "Recibiste ${regalo.marca} ${regalo.codigo}" //Acá va lo que quieras del regalo
            )
        )
    }
}

interface MailSender {
    fun sendMail(mail: Mail)
}

data class Mail(val from: String,val to: String, val subject: String, val content: String)


class RegaloRecibidoInformarFlete(val fleteSender: FleteSender): RegaloRecibidoObserver { //Constructor Injection
    override fun regaloRecibido(regalo: Regalo, persona: Persona) {
        fleteSender.notificarFlete(
            InterfazFleteRenoLoco(persona.domicilio,
                persona.nombre,
                persona.dni,
                regalo.codigo
            )
        )
    }
}

interface FleteSender { // Como no te dice con qué manda simulamos otro tipo de sender
    fun notificarFlete(data: InterfazFleteRenoLoco)
}

data class InterfazFleteRenoLoco(val domicilio:String, val nombre: String, val dni: String, val codigoRegalo: String)


class RegaloRecibidoPasarAInteresada(): RegaloRecibidoObserver {

    override fun regaloRecibido(regalo: Regalo, persona: Persona) {
        if (regalo.valor > 10000){
            persona.criterioEleccionRegalo = Interesada(5000)
        }
    }
}


/*Observaciones
*
* ---------------------------------------------------
* Tipos de ideas posibles para resolver los CriterioEleccionRegalo:
*
* 1-Strategy (Lo implementado)
* Buscamos dinamismo, generando cambios durante la ejecución
* Desacopla comportamientos a una clase (que es la beneficiada)
* Es más simple agregar otro elemento a la estructura
*
* 2-Crear subclases
* Separa comportamientos en clases hijas (Solo una herencia por clase)
* Útil cuando las variantes son pocas, estables y bien definidas (Regalos)
* Poca flexibilidad, implica nueva clase o subclases
*
* 3-Tener variables y condicionales
* También otorga dinamismo: tener una variable con un string y condicionales me da la misma posibilidad
* que el strategy
* El problema es que acopla la lógica a una clase concreta con if y when
* Útil cuando se necesitan soluciones rápidas y/o con pocos casos
*
* ---------------------------------------------------
* En cuanto al criterio de Strategy (dependiendo de si quiero asignar algún valor)
* -Atenti que usa object y clases dependiendo del tipo de State que aplica-
*
* 1- Stateless
* Reutilizable como object
*
* 2- State full
* Se utilizan clases ya que cada asignación necesitara de un object independiente
*
*-----------------------------------------------------
* Ventajas en cuanto al armar un Strategy con Interface sobre Abstract Class
* No pierdo la posibilidad de subclasificar la clase (no gasto la bala de la clase)
*
* -----------------------------------------------------
* Cuando va Strategy?
* Cuando necesito separar de mi objeto ese algoritmo que encapsulo en objetos polimórficos
*
* Cuando va Template Method?
* Necesito tener objetos polimórficos en una jerarquía que no va a cambiar
* por lo que no me interesa modificar la identidad del objeto a futuro
*
* -----------------------------------------------------
*
* Abstract Class
*
* 1-Uso de super
* Problema si al hacer una nueva clase y no se carga, puede generar problemas en la lógica
*
* 2-Metodo abstracto
* Me obliga a cargar el metodo necesario al crear nuevas clases
* Se usa porque así lo dice el Dodain, sino, pal lobby
*
* -----------------------------------------------------
* Inmutabilidad
*
* 1-val
* Al generar objeto con atributos con "val" se generan objetos del tipo inmutables
* Es una decisión de diseño que implica que no queremos que ese atributo sea modificado
* Se puede leer, pero no se puede volver a asignar
* En una colección, podra ser mutable su contenido, pero no la referencia (el tipo de objetos que guardo).
*
* 2-var
* El atributo puede ser reemplazado con otra instancia o valor en cualquier momento
* Se puede leer y escribir
* Menos seguro cuando se busca inmutabilidad
*
* -----------------------------------------------------
* Observer
*
* Uso de Long Parameter Method
* Al usar varios campos de otros objetos como la data class, se puede estructurar de una forma más ordenada el contenido que tendría un mail por ej
* Como no están agrupados los atributos que tienen la data class, ayuda a un mejor uso
*
* Data class
* Se tiene por default metodos como equals, copy... permiten testearlos más fácil
* No necesitan getters ni setters
* Permiten representar los objetos que vamos a vincular con las APIs
* Inmutables por defecto
*
* Value Object
* Como el data class de mail, que lo usamos para representar el concepto de un mail
*/