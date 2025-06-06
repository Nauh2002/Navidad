import java.time.LocalDate
import java.time.DayOfWeek


// Punto 1
class Persona{
    lateinit var criterioEleccionRegalo : CriterioEleccionRegalo

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






