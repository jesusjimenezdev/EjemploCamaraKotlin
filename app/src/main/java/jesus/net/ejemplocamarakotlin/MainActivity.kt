package jesus.net.ejemplocamarakotlin

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.FileProvider
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    val SOLICITUD_TOMAR_FOTO = 1
    val SOLICITUD_SELECCIONAR_FOTO = 2
    val permisoCamara = android.Manifest.permission.CAMERA
    val permisoWriteStorage = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    val permisoReadStorage = android.Manifest.permission.READ_EXTERNAL_STORAGE
    var imgFoto: ImageView? = null
    var urlFotoActual = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imgFoto = findViewById(R.id.imgFoto)
        val btnCapturarFoto = findViewById<Button>(R.id.btnCapturar)
        val btnSeleccionar = findViewById<Button>(R.id.btnSeleccionar)

        btnCapturarFoto.setOnClickListener {
            tomarFoto()
        }

        btnSeleccionar.setOnClickListener {
            seleccionarFoto()
        }
    }

    fun tomarFoto() {
        pedirPermisos()
    }

    fun seleccionarFoto() {
        pedirPermisosSeleccionarFoto()
    }

    fun pedirPermisos() {
        val deboProveerContexto = ActivityCompat.shouldShowRequestPermissionRationale(this, permisoCamara)
        if (deboProveerContexto) {
            solicitudPermiso()
        } else {
            solicitudPermiso()
        }
    }

    fun pedirPermisosSeleccionarFoto() {
        val deboProveerContexto = ActivityCompat.shouldShowRequestPermissionRationale(this, permisoReadStorage)
        if (deboProveerContexto) {
            solicitudPermisoSeleccionarFoto()
        } else {
            solicitudPermisoSeleccionarFoto()
        }
    }

    fun solicitudPermiso() {
        requestPermissions(arrayOf(permisoCamara, permisoWriteStorage, permisoReadStorage), SOLICITUD_SELECCIONAR_FOTO)
    }


    fun solicitudPermisoSeleccionarFoto() {
        requestPermissions(arrayOf(permisoReadStorage), SOLICITUD_SELECCIONAR_FOTO)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            SOLICITUD_TOMAR_FOTO -> {
                if (grantResults.size > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    // tenemos permiso
                    intentTomarFoto()
                } else {
                    // no tenemos permiso
                    Toast.makeText(this, "No tenemos permiso para acceder a la camara y al almacenamiento", Toast.LENGTH_SHORT).show()
                }
            }

            SOLICITUD_SELECCIONAR_FOTO -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // tenemos permiso
                    intentSeleccionarFoto()
                } else {
                    // no tenemos permiso
                    Toast.makeText(this, "No tenemos permiso para acceder a tus fotos", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun intentTomarFoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            var archivoDeFoto: File? = null
            archivoDeFoto = crearArchivoImagen()
            if (archivoDeFoto != null) {
                var urlFoto = FileProvider.getUriForFile(this, "jesus.net.ejemplocamarakotlin", archivoDeFoto)
                intent.putExtra(MediaStore.EXTRA_OUTPUT, urlFoto)
                startActivityForResult(intent, SOLICITUD_TOMAR_FOTO)
            }
        }
    }

    fun intentSeleccionarFoto() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        startActivityForResult(Intent.createChooser(intent, "Selecciona una foto"), SOLICITUD_SELECCIONAR_FOTO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            SOLICITUD_TOMAR_FOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    //obtener imagen
                    //val extras = data?.extras
                    //val imageBitmap = extras!!.get("data") as Bitmap
                    /*val uri = Uri.parse(urlFotoActual)
                    val stream = contentResolver.openInputStream(uri)
                    val imageBitMap = BitmapFactory.decodeStream(stream)
                    imgFoto!!.setImageBitmap(imageBitMap)*/
                    mostrarBitMap(urlFotoActual)
                    addImagenGaleria()
                } else {
                    // cancelo la captura
                }
            }

            SOLICITUD_SELECCIONAR_FOTO -> {
                if (requestCode == Activity.RESULT_OK) {
                    mostrarBitMap(data?.data.toString())
                }
            }
        }
    }

    fun mostrarBitMap(url: String) {
        val uri = Uri.parse(url)
        val stream = contentResolver.openInputStream(uri)
        val imageBitMap = BitmapFactory.decodeStream(stream)
        imgFoto!!.setImageBitmap(imageBitMap)
    }

    fun crearArchivoImagen(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val nombreArchivoImagen = "JPEG_" + timeStamp + "_"
        //val directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val directorio = Environment.getExternalStorageDirectory()
        val directorioPictures = File(directorio.absolutePath + "/Pictures/")
        val imagen = File.createTempFile(nombreArchivoImagen, "jpg", directorioPictures)
        urlFotoActual = "file://" + imagen.absolutePath
        return imagen
    }

    fun addImagenGaleria() {
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val file = File(urlFotoActual)
        val uri = Uri.fromFile(file)
        intent.setData(uri)
        this.sendBroadcast(intent)
    }
}
