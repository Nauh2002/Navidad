import java.time.LocalDate
import java.time.DayOfWeek


// Punto 1
class Persona{
    lateinit var criterioEleccionRegalo : CriterioEleccionRegalo
    lateinit var direccion: String
    lateinit var nombre: String
    lateinit var dni: String
    val regalosRecibos = mutableListOf<Regalo>()
    val regalosRecibidosObserver = mutableListOf<RegaloRecibidoObserver>()

    fun recibirRegalo(regalo: Regalo){
        regalosRecibos.add(regalo)
        regalosRecibidosObserver.forEach{it.regaloRecibo(this, regalo)}
    }

    lateinit var mail: String

    fun leGusta(regalo: Regalo) = criterioEleccionRegalo.leGusta(regalo)
}


// Strategy: desacoplar algoritmo de eleccion de un regalo
interface CriterioEleccionRegalo {
    fun leGusta (regalo: Regalo) : Boolean
}

object Conformista : CriterioEleccionRegalo {
    override fun leGusta (regalo: Regalo) = true
}

class Interesada (var valorEstimado: Double) : CriterioEleccionRegalo {
    override fun leGusta (regalo: Regalo) = regalo.precio >= valorEstimado
}

object Exigente : CriterioEleccionRegalo {
    override fun leGusta (regalo: Regalo) = regalo.esValioso()
}

class Marquera (var marca: String) : CriterioEleccionRegalo {
    override fun leGusta (regalo: Regalo) = regalo.marca == marca
}

// Composite: rama y las anteriores son las hojas y funcionan de manera polimorfica
class Combineta : CriterioEleccionRegalo{
    val criterioEleccionRegalo = mutableListOf<CriterioEleccionRegalo>()
    override fun leGusta (regalo: Regalo) = criterioEleccionRegalo.any{ it.leGusta(regalo) }
}


// Punto 2
abstract class Regalo(val precio: Int,val marca: String) {
    lateinit var codigo: String
    //Template Method
    open fun esValioso() = precio >= 5000 && condicionEspecifica()

    //Primitiva
    abstract fun condicionEspecifica(): Boolean
}

class Ropa(precio: Int, marca: String) : Regalo(precio, marca) {
    var marcasValiosas = listOf("Jordache", "Lee", "Charro", "Moto Oil")
    override fun condicionEspecifica() = marcasValiosas.contains(marca)
}

class Juguete (precio: Int, marca: String, var fechaDeLazamiento: LocalDate) : Regalo(precio, marca) {
    override fun condicionEspecifica() =  fechaDeLazamiento.year < 2000
}

class Perfume (precio: Int, marca: String, var origenExtrajero: Boolean): Regalo(precio, marca) {
    override fun condicionEspecifica() = origenExtrajero
}

class Experiencia (precio: Int, marca: String, var dia: DayOfWeek) : Regalo(precio, marca) {
    override fun condicionEspecifica() = dia == DayOfWeek.FRIDAY
}


// Punto 3

//Observer: Esperando a que suceda un evento
object RepartijaDeRegalos {
    val personas = mutableListOf<Persona>()
    val regalos = mutableListOf<Regalo>()

    fun repartir() {
        personas.forEach { persona ->
            val regaloElegido = regalos.find { regalo -> persona.leGusta(regalo) } ?: Voucher()
            persona.recibirRegalo(regaloElegido)
        }
    }

}

class Voucher : Regalo(2000, "Pappap") {
    override fun esValioso() = false
    override fun condicionEspecifica() = false
}


interface ServicioMail {
    fun enviarMail(mail: Mail)
}

data class Mail(val from: String, val to: String, val subject: String, val context: String)

interface RegaloRecibidoObserver {
    fun regaloRecibo(persona: Persona, regalo: Regalo){
    }
}

class NotificarPersonas : RegaloRecibidoObserver{
    lateinit var servicioMail : ServicioMail
    lateinit var from: String
    override fun regaloRecibo(persona: Persona, regalo: Regalo) {
        servicioMail.enviarMail(Mail(
            from = from,
            to = persona.mail,
            subject = "recibiste un regalo",
            context = "recibiste un regalo ${regalo.marca} de valor ${regalo.precio}"
        ))
    }
}

interface EmpresaFleteElRenoLoco{
    fun notificarEnvio(persona: Persona, regalo: Regalo)
}

class NoticarImpresaFleteElRenoLoco : EmpresaFleteElRenoLoco{
    override fun notificarEnvio(persona: Persona, regalo: Regalo) {

    }
}

interface FleteEnviar{
    fun notificarFlete(data: DatosPersona)
}

data class DatosPersona(val direccion: String, val nombre: String, val dni: String, val codigoRegalo: String)

class RegaloRecibidoInfromarFlete(var fleteEnviar: FleteEnviar) : EmpresaFleteElRenoLoco{
    override fun notificarEnvio(persona: Persona, regalo: Regalo) {
        fleteEnviar.notificarFlete( DatosPersona(
            direccion = persona.direccion,
            nombre = persona.nombre,
            dni = persona.dni,
            codigoRegalo = regalo.codigo
        ))
    }
}

class RegaloRecicibidoPasarAinterado : RegaloRecibidoObserver {
    override fun regaloRecibo(persona: Persona, regalo: Regalo) {
        if (regalo.precio > 10000){
            persona.criterioEleccionRegalo = Interesada(5000.0)
        }
    }
}