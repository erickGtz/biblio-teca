package com.fcc.biblioteca.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.fcc.biblioteca.model.Libro

class MyDBHandler(
    context: Context,
    name: String? = DATABASE_NAME,
    factory: SQLiteDatabase.CursorFactory? = null,
    version: Int = DATABASE_VERSION
) : SQLiteOpenHelper(context, name, factory, version) {

    companion object {
        private const val DATABASE_VERSION = 8
        private const val DATABASE_NAME = "biblioteca_personal.db"
        const val MAX_LOANS = 3

        // TABLE LIBROS
        const val TABLE_LIBROS = "libros"
        const val COLUMN_LIBRO_ID = "id_libro"
        const val COLUMN_LIBRO_TITULO = "titulo"
        const val COLUMN_LIBRO_CATEGORIA = "categoria"
        const val COLUMN_LIBRO_AUTOR = "autor"
        const val COLUMN_LIBRO_ISBN = "isbn"
        const val COLUMN_LIBRO_ESTADO = "estado"
        const val COLUMN_LIBRO_STOCK = "stock"
        const val COLUMN_LIBRO_SINOPSIS = "sinopsis"
        const val COLUMN_LIBRO_IMAGEN = "imagen"

        // TABLE USUARIOS
        const val TABLE_USUARIOS = "usuarios"
        const val COLUMN_USUARIO_ID = "id_usuario"
        const val COLUMN_USUARIO_NOMBRE = "nombre"
        const val COLUMN_USUARIO_APELLIDO1 = "apellido1"
        const val COLUMN_USUARIO_APELLIDO2 = "apellido2"
        const val COLUMN_USUARIO_ROL = "rol"

        // TABLE PRESTAMOS
        const val TABLE_PRESTAMOS = "prestamos"
        const val COLUMN_PRESTAMO_ID = "id_prestamo"
        const val COLUMN_PRESTAMO_ID_USUARIO = "id_usuario"
        const val COLUMN_PRESTAMO_ID_LIBRO = "id_libro"
        const val COLUMN_PRESTAMO_FECHA_INICIO = "fecha_inicio"
        const val COLUMN_PRESTAMO_FECHA_FIN = "fecha_fin"

        // TABLE USUARIOS_CREDENCIALES
        const val TABLE_CREDENCIALES = "usuarios_credenciales"
        const val COLUMN_CRED_ID = "id_credencial"
        const val COLUMN_CRED_ID_USUARIO = "id_usuario"
        const val COLUMN_CRED_CORREO = "correo"
        const val COLUMN_CRED_TELEFONO = "telefono"
        const val COLUMN_CRED_CONTRASENA = "contrasena"

        private val coverList = listOf("cover_cien_anos", "cover_quijote", "cover_principito", "cover_sapiens", "cover_davinci", "cover_1984")
        private var lastCoverIndex = -1
        
        fun getRandomCover(): String {
            var newIndex = coverList.indices.random()
            if (lastCoverIndex != -1) {
                while (newIndex == lastCoverIndex) {
                    newIndex = coverList.indices.random()
                }
            }
            lastCoverIndex = newIndex
            return coverList[newIndex]
        }
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createLibros = ("CREATE TABLE " + TABLE_LIBROS + "("
                + COLUMN_LIBRO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_LIBRO_TITULO + " TEXT NOT NULL,"
                + COLUMN_LIBRO_CATEGORIA + " TEXT,"
                + COLUMN_LIBRO_AUTOR + " TEXT,"
                + COLUMN_LIBRO_ISBN + " TEXT UNIQUE,"
                + COLUMN_LIBRO_ESTADO + " TEXT DEFAULT 'disponible',"
                + COLUMN_LIBRO_STOCK + " INTEGER DEFAULT 1,"
                + COLUMN_LIBRO_SINOPSIS + " TEXT,"
                + COLUMN_LIBRO_IMAGEN + " TEXT)")

        val createUsuarios = ("CREATE TABLE " + TABLE_USUARIOS + "("
                + COLUMN_USUARIO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_USUARIO_NOMBRE + " TEXT NOT NULL,"
                + COLUMN_USUARIO_APELLIDO1 + " TEXT NOT NULL,"
                + COLUMN_USUARIO_APELLIDO2 + " TEXT,"
                + COLUMN_USUARIO_ROL + " TEXT DEFAULT 'usuario')")

        val createCredenciales = ("CREATE TABLE " + TABLE_CREDENCIALES + "("
                + COLUMN_CRED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CRED_ID_USUARIO + " INTEGER NOT NULL,"
                + COLUMN_CRED_CORREO + " TEXT NOT NULL UNIQUE,"
                + COLUMN_CRED_TELEFONO + " TEXT NOT NULL UNIQUE,"
                + COLUMN_CRED_CONTRASENA + " TEXT NOT NULL,"
                + "FOREIGN KEY(" + COLUMN_CRED_ID_USUARIO + ") REFERENCES " + TABLE_USUARIOS + "(" + COLUMN_USUARIO_ID + ") ON DELETE CASCADE)")

        val createPrestamos = ("CREATE TABLE " + TABLE_PRESTAMOS + "("
                + COLUMN_PRESTAMO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_PRESTAMO_ID_USUARIO + " INTEGER NOT NULL,"
                + COLUMN_PRESTAMO_ID_LIBRO + " INTEGER NOT NULL,"
                + COLUMN_PRESTAMO_FECHA_INICIO + " TEXT NOT NULL,"
                + COLUMN_PRESTAMO_FECHA_FIN + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_PRESTAMO_ID_USUARIO + ") REFERENCES " + TABLE_USUARIOS + "(" + COLUMN_USUARIO_ID + "),"
                + "FOREIGN KEY(" + COLUMN_PRESTAMO_ID_LIBRO + ") REFERENCES " + TABLE_LIBROS + "(" + COLUMN_LIBRO_ID + "))")

        db.execSQL(createLibros)
        db.execSQL(createUsuarios)
        db.execSQL(createCredenciales)
        db.execSQL(createPrestamos)

        // Crear Admin default
        db.execSQL("INSERT INTO $TABLE_USUARIOS ($COLUMN_USUARIO_NOMBRE, $COLUMN_USUARIO_APELLIDO1, $COLUMN_USUARIO_APELLIDO2, $COLUMN_USUARIO_ROL) VALUES ('Admin', 'Root', '', 'admin')")
        db.execSQL("INSERT INTO $TABLE_CREDENCIALES ($COLUMN_CRED_ID_USUARIO, $COLUMN_CRED_CORREO, $COLUMN_CRED_TELEFONO, $COLUMN_CRED_CONTRASENA) VALUES (1, 'admin@admin.com', '1234567890', 'admin123')")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRESTAMOS)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CREDENCIALES)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIBROS)
        onCreate(db)
    }

    fun registerUsuario(nombre: String, ap1: String, ap2: String, correo: String, telefono: String, pass: String): Boolean {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            val userValues = ContentValues().apply {
                put(COLUMN_USUARIO_NOMBRE, nombre)
                put(COLUMN_USUARIO_APELLIDO1, ap1)
                put(COLUMN_USUARIO_APELLIDO2, ap2)
                put(COLUMN_USUARIO_ROL, "usuario")
            }
            val userId = db.insert(TABLE_USUARIOS, null, userValues)
            if (userId != -1L) {
                val credValues = ContentValues().apply {
                    put(COLUMN_CRED_ID_USUARIO, userId)
                    put(COLUMN_CRED_CORREO, correo)
                    put(COLUMN_CRED_TELEFONO, telefono)
                    put(COLUMN_CRED_CONTRASENA, pass)
                }
                val credId = db.insert(TABLE_CREDENCIALES, null, credValues)
                if (credId != -1L) {
                    db.setTransactionSuccessful()
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
        return false
    }

    fun loginUsuario(credencial: String, contrasena: String): com.fcc.biblioteca.model.Usuario? {
        val db = this.readableDatabase
        val query = """
            SELECT u.$COLUMN_USUARIO_ID, u.$COLUMN_USUARIO_NOMBRE, u.$COLUMN_USUARIO_APELLIDO1, u.$COLUMN_USUARIO_APELLIDO2, u.$COLUMN_USUARIO_ROL, c.$COLUMN_CRED_CORREO, c.$COLUMN_CRED_TELEFONO
            FROM $TABLE_CREDENCIALES c 
            INNER JOIN $TABLE_USUARIOS u ON c.$COLUMN_CRED_ID_USUARIO = u.$COLUMN_USUARIO_ID 
            WHERE (c.$COLUMN_CRED_CORREO = ? OR c.$COLUMN_CRED_TELEFONO = ?) AND c.$COLUMN_CRED_CONTRASENA = ?
        """
        val cursor = db.rawQuery(query, arrayOf(credencial, credencial, contrasena))
        var usuario: com.fcc.biblioteca.model.Usuario? = null
        if (cursor.moveToFirst()) {
            usuario = com.fcc.biblioteca.model.Usuario(
                id_usuario = cursor.getInt(0),
                nombre = cursor.getString(1),
                apellido1 = cursor.getString(2),
                apellido2 = cursor.getString(3),
                rol = cursor.getString(4),
                correo = cursor.getString(5),
                telefono = cursor.getString(6)
            )
        }
        cursor.close()
        return usuario
    }

    fun getLibros(): List<Libro> {
        val list = mutableListOf<Libro>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_LIBROS", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(
                    Libro(
                        id_libro = cursor.getInt(0),
                        titulo = cursor.getString(1),
                        categoria = cursor.getString(2),
                        autor = cursor.getString(3),
                        isbn = cursor.getString(4),
                        estado = cursor.getString(5),
                        stock = cursor.getInt(6),
                        sinopsis = cursor.getString(7),
                        imagen = cursor.getString(8)
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
    
    fun getUniqueCategories(): List<String> {
        val list = mutableListOf<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT DISTINCT $COLUMN_LIBRO_CATEGORIA FROM $TABLE_LIBROS WHERE $COLUMN_LIBRO_CATEGORIA IS NOT NULL AND $COLUMN_LIBRO_CATEGORIA != ''", null)
        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
    
    fun deleteLibro(id: Int) {
        val db = this.writableDatabase
        db.delete(TABLE_LIBROS, "$COLUMN_LIBRO_ID=?", arrayOf(id.toString()))
    }
    
    fun updateLibroStock(id: Int, change: Int) {
        val db = this.writableDatabase
        db.execSQL("UPDATE $TABLE_LIBROS SET $COLUMN_LIBRO_STOCK = MAX(0, $COLUMN_LIBRO_STOCK + ?) WHERE $COLUMN_LIBRO_ID = ?", arrayOf(change, id))
    }
    
    fun addLibro(titulo: String, autor: String, isbn: String, categoria: String, stock: Int, sinopsis: String? = null): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LIBRO_TITULO, titulo)
            put(COLUMN_LIBRO_AUTOR, autor)
            put(COLUMN_LIBRO_ISBN, isbn)
            put(COLUMN_LIBRO_CATEGORIA, categoria)
            put(COLUMN_LIBRO_STOCK, stock)
            put(COLUMN_LIBRO_SINOPSIS, sinopsis)
            put(COLUMN_LIBRO_IMAGEN, getRandomCover())
        }
        return try {
            val res = db.insert(TABLE_LIBROS, null, values)
            res != -1L
        } catch (e: Exception) {
            false
        }
    }

    fun updateLibro(id: Int, titulo: String, autor: String, isbn: String, categoria: String, stock: Int, sinopsis: String?): Boolean {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LIBRO_TITULO, titulo)
            put(COLUMN_LIBRO_AUTOR, autor)
            put(COLUMN_LIBRO_ISBN, isbn)
            put(COLUMN_LIBRO_CATEGORIA, categoria)
            put(COLUMN_LIBRO_STOCK, stock)
            put(COLUMN_LIBRO_SINOPSIS, sinopsis)
        }
        return try {
            val res = db.update(TABLE_LIBROS, values, "$COLUMN_LIBRO_ID=?", arrayOf(id.toString()))
            res > 0
        } catch (e: Exception) {
            false
        }
    }

    fun insertMockLibrosIfEmpty() {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_LIBROS", null)
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()

        if (count == 0) {
            val wdb = this.writableDatabase
            // Ficción (4 libros)
            wdb.execSQL("INSERT INTO $TABLE_LIBROS (titulo, categoria, autor, isbn, estado, stock, sinopsis, imagen) VALUES ('Cien años de soledad', 'Ficción', 'Gabriel García Márquez', '111', 'disponible', 12, 'La epopeya de la familia Buendía en el mítico pueblo de Macondo.', 'cover_cien_anos')")
            wdb.execSQL("INSERT INTO $TABLE_LIBROS (titulo, categoria, autor, isbn, estado, stock, sinopsis, imagen) VALUES ('1984', 'Ficción', 'George Orwell', '666', 'disponible', 7, 'Una distopía sobre el control total del pensamiento.', 'cover_1984')")
            wdb.execSQL("INSERT INTO $TABLE_LIBROS (titulo, categoria, autor, isbn, estado, stock, sinopsis, imagen) VALUES ('Rayuela', 'Ficción', 'Julio Cortázar', '667', 'disponible', 4, 'Una novela que se puede leer en varios órdenes.', 'cover_quijote')")
            wdb.execSQL("INSERT INTO $TABLE_LIBROS (titulo, categoria, autor, isbn, estado, stock, sinopsis, imagen) VALUES ('Ensayo sobre la ceguera', 'Ficción', 'José Saramago', '668', 'disponible', 3, 'Una epidemia de ceguera blanca se extiende por una ciudad.', 'cover_cien_anos')")

            // Clásicos (4 libros)
            wdb.execSQL("INSERT INTO $TABLE_LIBROS (titulo, categoria, autor, isbn, estado, stock, sinopsis, imagen) VALUES ('Don Quijote de la Mancha', 'Clásicos', 'Miguel de Cervantes', '222', 'disponible', 8, 'Las aventuras del ingenioso hidalgo.', 'cover_quijote')")
            wdb.execSQL("INSERT INTO $TABLE_LIBROS (titulo, categoria, autor, isbn, estado, stock, sinopsis, imagen) VALUES ('La Odisea', 'Clásicos', 'Homero', '223', 'disponible', 5, 'El viaje de regreso de Ulises a Ítaca.', 'cover_sapiens')")
            wdb.execSQL("INSERT INTO $TABLE_LIBROS (titulo, categoria, autor, isbn, estado, stock, sinopsis, imagen) VALUES ('Hamlet', 'Clásicos', 'William Shakespeare', '224', 'disponible', 1, 'Tragedia sobre la duda y la venganza.', 'cover_davinci')")
            wdb.execSQL("INSERT INTO $TABLE_LIBROS (titulo, categoria, autor, isbn, estado, stock, sinopsis, imagen) VALUES ('Orgullo y Prejuicio', 'Clásicos', 'Jane Austen', '225', 'disponible', 3, 'Relaciones personales en la Inglaterra rural.', 'cover_principito')")

            // Infantil (3 libros)
            wdb.execSQL("INSERT INTO $TABLE_LIBROS (titulo, categoria, autor, isbn, estado, stock, sinopsis, imagen) VALUES ('El principito', 'Infantil', 'Antoine de Saint-Exupéry', '333', 'disponible', 15, 'Un pequeño príncipe viaja por el universo.', 'cover_principito')")
            wdb.execSQL("INSERT INTO $TABLE_LIBROS (titulo, categoria, autor, isbn, estado, stock, sinopsis, imagen) VALUES ('Peter Pan', 'Infantil', 'J.M. Barrie', '334', 'disponible', 4, 'El niño que no quería crecer.', 'cover_principito')")
            wdb.execSQL("INSERT INTO $TABLE_LIBROS (titulo, categoria, autor, isbn, estado, stock, sinopsis, imagen) VALUES ('Alicia en el país de las maravillas', 'Infantil', 'Lewis Carroll', '335', 'disponible', 5, 'Un viaje a un mundo de fantasía absurda.', 'cover_principito')")

            // Historia (3 libros)
            wdb.execSQL("INSERT INTO $TABLE_LIBROS (titulo, categoria, autor, isbn, estado, stock, sinopsis, imagen) VALUES ('Sapiens', 'Historia', 'Yuval Noah Harari', '444', 'disponible', 6, 'Una breve historia de la humanidad.', 'cover_sapiens')")
            wdb.execSQL("INSERT INTO $TABLE_LIBROS (titulo, categoria, autor, isbn, estado, stock, sinopsis, imagen) VALUES ('El mundo de ayer', 'Historia', 'Stefan Zweig', '445', 'disponible', 4, 'Memorias de un europeo en tiempos de guerra.', 'cover_sapiens')")
            wdb.execSQL("INSERT INTO $TABLE_LIBROS (titulo, categoria, autor, isbn, estado, stock, sinopsis, imagen) VALUES ('Guns, Germs, and Steel', 'Historia', 'Jared Diamond', '446', 'disponible', 3, 'Los destinos de las sociedades humanas.', 'cover_sapiens')")

            // Misterio / Ciencia (3 libros)
            wdb.execSQL("INSERT INTO $TABLE_LIBROS (titulo, categoria, autor, isbn, estado, stock, sinopsis, imagen) VALUES ('El código Da Vinci', 'Misterio', 'Dan Brown', '555', 'disponible', 10, 'Misterio histórico en el arte.', 'cover_davinci')")
            wdb.execSQL("INSERT INTO $TABLE_LIBROS (titulo, categoria, autor, isbn, estado, stock, sinopsis, imagen) VALUES ('Cosmos', 'Ciencia', 'Carl Sagan', '777', 'disponible', 5, 'Exploración del universo y la ciencia.', 'cover_davinci')")
            wdb.execSQL("INSERT INTO $TABLE_LIBROS (titulo, categoria, autor, isbn, estado, stock, sinopsis, imagen) VALUES ('Brief History of Time', 'Ciencia', 'Stephen Hawking', '778', 'disponible', 4, 'Desde el Big Bang hasta los agujeros negros.', 'cover_davinci')")
        }
    }

    fun prestarLibro(idUsuario: Int, idLibro: Int): Int {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            // 1. Verificar si ya alcanzó el límite máximo
            val userLoanCount = getContadorPrestamos(idUsuario)
            if (userLoanCount >= MAX_LOANS) {
                return 2 // Límite alcanzado
            }

            // 2. Verificar si ya tiene ESTE libro
            val countCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_PRESTAMOS WHERE $COLUMN_PRESTAMO_ID_USUARIO = ? AND $COLUMN_PRESTAMO_ID_LIBRO = ?", arrayOf(idUsuario.toString(), idLibro.toString()))
            var alreadyBorrowed = false
            if (countCursor.moveToFirst()) {
                alreadyBorrowed = countCursor.getInt(0) > 0
            }
            countCursor.close()
            
            if (alreadyBorrowed) {
                return 0 // Ya lo tiene
            }

            // 3. Verificar stock
            val cursor = db.rawQuery("SELECT $COLUMN_LIBRO_STOCK FROM $TABLE_LIBROS WHERE $COLUMN_LIBRO_ID = ?", arrayOf(idLibro.toString()))
            var stock = 0
            if (cursor.moveToFirst()) {
                stock = cursor.getInt(0)
            }
            cursor.close()
            
            if (stock > 0) {
                val calendar = java.util.Calendar.getInstance()
                val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                val fechaInicio = dateFormat.format(calendar.time)
                calendar.add(java.util.Calendar.DAY_OF_YEAR, 14) // +14 days
                val fechaFin = dateFormat.format(calendar.time)

                val prestamoValues = android.content.ContentValues().apply {
                    put(COLUMN_PRESTAMO_ID_USUARIO, idUsuario)
                    put(COLUMN_PRESTAMO_ID_LIBRO, idLibro)
                    put(COLUMN_PRESTAMO_FECHA_INICIO, fechaInicio)
                    put(COLUMN_PRESTAMO_FECHA_FIN, fechaFin)
                }
                val res = db.insert(TABLE_PRESTAMOS, null, prestamoValues)
                if (res != -1L) {
                    // Actualizar stock de forma robusta
                    db.execSQL("UPDATE $TABLE_LIBROS SET $COLUMN_LIBRO_STOCK = $COLUMN_LIBRO_STOCK - 1 WHERE $COLUMN_LIBRO_ID = ?", arrayOf(idLibro.toString()))
                    db.setTransactionSuccessful()
                    return 1 // Éxito
                }
            } else {
                return -2 // Sin stock (opcional diferenciar de error genérico)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
        return -1 // Error genérico
    }

    fun getPrestamosPorUsuario(idUsuario: Int): List<com.fcc.biblioteca.model.Prestamo> {
        val list = mutableListOf<com.fcc.biblioteca.model.Prestamo>()
        val db = this.readableDatabase
        val query = """
            SELECT p.$COLUMN_PRESTAMO_ID, p.$COLUMN_PRESTAMO_FECHA_INICIO, p.$COLUMN_PRESTAMO_FECHA_FIN, 
                   l.$COLUMN_LIBRO_ID, l.$COLUMN_LIBRO_TITULO, l.$COLUMN_LIBRO_CATEGORIA, l.$COLUMN_LIBRO_AUTOR, l.$COLUMN_LIBRO_ISBN, l.$COLUMN_LIBRO_ESTADO, l.$COLUMN_LIBRO_STOCK, l.$COLUMN_LIBRO_SINOPSIS, l.$COLUMN_LIBRO_IMAGEN
            FROM $TABLE_PRESTAMOS p
            INNER JOIN $TABLE_LIBROS l ON p.$COLUMN_PRESTAMO_ID_LIBRO = l.$COLUMN_LIBRO_ID
            WHERE p.$COLUMN_PRESTAMO_ID_USUARIO = ?
        """
        val cursor = db.rawQuery(query, arrayOf(idUsuario.toString()))
        if (cursor.moveToFirst()) {
            do {
                val libro = com.fcc.biblioteca.model.Libro(
                    id_libro = cursor.getInt(3),
                    titulo = cursor.getString(4),
                    categoria = cursor.getString(5),
                    autor = cursor.getString(6),
                    isbn = cursor.getString(7),
                    estado = cursor.getString(8),
                    stock = cursor.getInt(9),
                    sinopsis = cursor.getString(10),
                    imagen = cursor.getString(11)
                )
                list.add(
                    com.fcc.biblioteca.model.Prestamo(
                        id_prestamo = cursor.getInt(0),
                        libro = libro,
                        fechaInicio = cursor.getString(1),
                        fechaFin = cursor.getString(2) ?: ""
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }

    fun getContadorPrestamos(idUsuario: Int): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_PRESTAMOS WHERE $COLUMN_PRESTAMO_ID_USUARIO = ?", arrayOf(idUsuario.toString()))
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }

    fun getTotalContadorPrestamos(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_PRESTAMOS", null)
        var count = 0
        if (cursor.moveToFirst()) count = cursor.getInt(0)
        cursor.close()
        return count
    }

    fun procesarDevolucionesExpiradas() {
        val db = this.writableDatabase
        val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        db.beginTransaction()
        try {
            val cursor = db.rawQuery("SELECT $COLUMN_PRESTAMO_ID, $COLUMN_PRESTAMO_ID_LIBRO FROM $TABLE_PRESTAMOS WHERE $COLUMN_PRESTAMO_FECHA_FIN < ?", arrayOf(today))
            if (cursor.moveToFirst()) {
                do {
                    val pId = cursor.getInt(0)
                    val lId = cursor.getInt(1)
                    db.execSQL("UPDATE $TABLE_LIBROS SET $COLUMN_LIBRO_STOCK = $COLUMN_LIBRO_STOCK + 1 WHERE $COLUMN_LIBRO_ID = ?", arrayOf(lId))
                    db.delete(TABLE_PRESTAMOS, "$COLUMN_PRESTAMO_ID = ?", arrayOf(pId.toString()))
                } while (cursor.moveToNext())
            }
            cursor.close()
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    fun devolverLibro(idPrestamo: Int, idLibro: Int) {
        val db = this.writableDatabase
        db.beginTransaction()
        try {
            db.execSQL("UPDATE $TABLE_LIBROS SET $COLUMN_LIBRO_STOCK = $COLUMN_LIBRO_STOCK + 1 WHERE $COLUMN_LIBRO_ID = ?", arrayOf(idLibro.toString()))
            db.delete(TABLE_PRESTAMOS, "$COLUMN_PRESTAMO_ID = ?", arrayOf(idPrestamo.toString()))
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    fun getPrestamosPorExpirar(idUsuario: Int): List<com.fcc.biblioteca.model.Prestamo> {
        val list = mutableListOf<com.fcc.biblioteca.model.Prestamo>()
        val db = this.readableDatabase
        
        // Obtenemos fecha de hoy + 3 días para el rango de expiración
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val cal = java.util.Calendar.getInstance()
        val today = sdf.format(cal.time)
        cal.add(java.util.Calendar.DAY_OF_YEAR, 3)
        val inThreeDays = sdf.format(cal.time)

        val query = """
            SELECT p.$COLUMN_PRESTAMO_ID, p.$COLUMN_PRESTAMO_FECHA_INICIO, p.$COLUMN_PRESTAMO_FECHA_FIN, 
                   l.$COLUMN_LIBRO_ID, l.$COLUMN_LIBRO_TITULO, l.$COLUMN_LIBRO_CATEGORIA, l.$COLUMN_LIBRO_AUTOR, l.$COLUMN_LIBRO_IMAGEN
            FROM $TABLE_PRESTAMOS p
            INNER JOIN $TABLE_LIBROS l ON p.$COLUMN_PRESTAMO_ID_LIBRO = l.$COLUMN_LIBRO_ID
            WHERE p.$COLUMN_PRESTAMO_ID_USUARIO = ? AND p.$COLUMN_PRESTAMO_FECHA_FIN BETWEEN ? AND ?
        """
        
        val cursor = db.rawQuery(query, arrayOf(idUsuario.toString(), today, inThreeDays))
        if (cursor.moveToFirst()) {
            do {
                val libro = com.fcc.biblioteca.model.Libro(
                    id_libro = cursor.getInt(3),
                    titulo = cursor.getString(4),
                    categoria = cursor.getString(5),
                    autor = cursor.getString(6),
                    imagen = cursor.getString(7) // Usando parámetro nombrado para asegurar que no se asigne a isbn por error posicional
                )
                list.add(
                    com.fcc.biblioteca.model.Prestamo(
                        id_prestamo = cursor.getInt(0),
                        libro = libro,
                        fechaInicio = cursor.getString(1),
                        fechaFin = cursor.getString(2) ?: ""
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return list
    }
}
