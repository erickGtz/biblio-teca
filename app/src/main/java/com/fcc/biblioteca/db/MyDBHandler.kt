package com.fcc.biblioteca.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDBHandler(
    context: Context,
    name: String? = DATABASE_NAME,
    factory: SQLiteDatabase.CursorFactory? = null,
    version: Int = DATABASE_VERSION
) : SQLiteOpenHelper(context, name, factory, version) {

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "biblioteca_personal.db"

        // TABLE LIBROS
        const val TABLE_LIBROS = "libros"
        const val COLUMN_LIBRO_ID = "id_libro"
        const val COLUMN_LIBRO_TITULO = "titulo"
        const val COLUMN_LIBRO_CATEGORIA = "categoria"
        const val COLUMN_LIBRO_AUTOR = "autor"
        const val COLUMN_LIBRO_ISBN = "isbn"
        const val COLUMN_LIBRO_ESTADO = "estado"

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
        const val COLUMN_CRED_USUARIO = "usuario"
        const val COLUMN_CRED_CONTRASENA = "contrasena"
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
                + COLUMN_LIBRO_ESTADO + " TEXT DEFAULT 'disponible')")

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
                + COLUMN_CRED_USUARIO + " TEXT NOT NULL UNIQUE,"
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
        db.execSQL("INSERT INTO $TABLE_CREDENCIALES ($COLUMN_CRED_ID_USUARIO, $COLUMN_CRED_CORREO, $COLUMN_CRED_USUARIO, $COLUMN_CRED_CONTRASENA) VALUES (1, 'admin@admin.com', 'admin', 'admin123')")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRESTAMOS)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CREDENCIALES)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIOS)
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LIBROS)
        onCreate(db)
    }

    fun registerUsuario(nombre: String, ap1: String, ap2: String, correo: String, usuario: String, pass: String): Boolean {
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
                    put(COLUMN_CRED_USUARIO, usuario)
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

    fun loginUsuario(credencial: String, contrasena: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_CREDENCIALES WHERE ($COLUMN_CRED_CORREO = ? OR $COLUMN_CRED_USUARIO = ?) AND $COLUMN_CRED_CONTRASENA = ?"
        val cursor = db.rawQuery(query, arrayOf(credencial, credencial, contrasena))
        val count = cursor.count
        cursor.close()
        return count > 0
    }
}
