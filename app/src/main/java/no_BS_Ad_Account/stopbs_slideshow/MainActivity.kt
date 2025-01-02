package no_BS_Ad_Account.stopbs_slideshow

import android.content.ContentUris
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //PERMISSION
        val permissions= if (Build.VERSION.SDK_INT >=33) {
            arrayOf(
                android.Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        ActivityCompat.requestPermissions(
            this,
            permissions,
            0
        )

        //FUNCTION get img paths
        fun getListIMGPath(): MutableList<String> {

            val result= mutableListOf<String>()
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DISPLAY_NAME)

            applicationContext.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
            )?.use { cursor ->

                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                while (cursor.moveToNext()) {

                    val id = cursor.getLong(idColumn)
                    val contentUri: Uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id)
                    val imgpath=cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA))
                    result += imgpath
                }
            }
            return result
        }
        val ListIMGPath: MutableList<String> = getListIMGPath()

        //ELEMENT
        val textview: TextView=findViewById(R.id.textview)
        textview.apply {
            text="Init"
            setTextColor(getColor(R.color.white))
        }
        val imageview: ImageView = findViewById(R.id.imageview)
        val edittextfolder: EditText = findViewById(R.id.edittextfolder)
        var currentfolder: String=edittextfolder.text.toString()

        var ListIMGPathfiltered= mutableListOf<String>()
        fun filterlist(): MutableList<String>  {
            //filter folder
            val result= mutableListOf<String>()
            if (edittextfolder.length()>0) {
                for (iterfolder in edittextfolder.text.toString().uppercase().trim().split(","))
                    for (iterpath in ListIMGPath.filter{it.contains( "/" + iterfolder + "/")}) {
                        result += iterpath
                    }
            } else {
                for (iterpath in ListIMGPath) {
                    result +=iterpath
                }
            }
            return result
        }
        ListIMGPathfiltered=filterlist()

        //Filter LIST for folder
        fun nextdisplay(){
            val edittextfolderupdated: EditText = findViewById(R.id.edittextfolder)
            //Log.d("1", currentfolder.toString())
            //Log.d("2", edittextfolderupdated.text.toString())
            if (currentfolder.uppercase().trim()!=edittextfolderupdated.text.toString().uppercase().trim()) {
                ListIMGPathfiltered=filterlist()
                currentfolder=edittextfolderupdated.text.toString().uppercase().trim()
            }
            //get random
            val useimgpath: String =ListIMGPathfiltered.random()
            ListIMGPathfiltered.remove(useimgpath)
            if (ListIMGPathfiltered.size<5) {
                ListIMGPathfiltered=filterlist()
            }
            //Log.d("checksize", ListIMGPathfiltered.size.toString())
            //display
            if (useimgpath.length>0){
                val myBitmap = BitmapFactory.decodeFile(useimgpath)
                textview.text=useimgpath
                imageview.setImageBitmap(myBitmap);
            }

        }

        val edittextinterval: EditText = findViewById(R.id.edittextinterval)
        val nbsecstr:String = edittextinterval.text.toString()
        var nbsec : Long
        if (nbsecstr.length > 0) {
            nbsec=nbsecstr.toLong()*1000
        } else {
            nbsec=5000
        }

        //RUN recursive loop on separate thread
        var stoprun: String= "STOP"
        suspend fun loopdisplay(){
            delay(nbsec)
            if (stoprun=="START") {

                runOnUiThread(Runnable {
                    nextdisplay()
                })
                loopdisplay()
            }
        }

        //BUTTON
        val nextbutton: Button =findViewById(R.id.buttonnext)
        nextbutton.apply {
            setOnClickListener{
                nextdisplay()
            }
        }
        val startbutton: Button =findViewById(R.id.buttonstart)
        startbutton.apply {
            setOnClickListener{
                stoprun="START"
                ListIMGPathfiltered=filterlist()
                nextdisplay()
                GlobalScope.launch {
                    loopdisplay()
                    Thread.sleep(1L)
                }
            }
        }
        val stopbutton: Button =findViewById(R.id.buttonstop)
        stopbutton.apply {
            setOnClickListener{
                stoprun="STOP"
                textview.text=stoprun
            }
        }

    }

}