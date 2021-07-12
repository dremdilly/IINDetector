package com.daurenbek.iindetector

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.isDigitsOnly
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import kotlinx.android.synthetic.main.activity_main.*
import java.time.YearMonth
import java.util.stream.IntStream.range

private const val CAMERA_REQUEST_CODE = 101

class MainActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupPermissions()
        codeScanner()
    }

    private fun codeScanner() {
        codeScanner = CodeScanner(this, scanner_view)

        codeScanner.apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS

            autoFocusMode = AutoFocusMode.SAFE
            scanMode = ScanMode.CONTINUOUS
            isAutoFocusEnabled = true
            isFlashEnabled = false

            decodeCallback = DecodeCallback {
                runOnUiThread {
                    if (isValid(it.text))
                        tv_textView.text = it.text
                    else
                        tv_textView.text = "Invalid IIN"
                }
            }

            errorCallback = ErrorCallback {
                runOnUiThread {
                    Log.e("Main", "Camera initialization error: ${it.message}")
                }
            }
        }

        scanner_view.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE)
    }

    private fun isValid(iin: String): Boolean {
        if (12 == iin.length && iin.isDigitsOnly()) {
            var year: String = ""
            for (i in iin.indices) {
                if(i == 0 || i == 1) {
                    if(iin[i].isDigit())
                        continue;
                    else
                        return false;
                } else if(i == 2) {
                   if (iin[i] == '0' || (iin[i] == '1' && (iin[3] == '1' || iin[3] == '2' || iin[3] == '0'))) {
                       continue;
                   } else
                       return false;
                } else if(i == 4) {
                    year += if(iin[0] == '0' || iin[0] == '1' || iin[0] == '2') {
                        "20" + iin[0] + iin[1]
                    } else {
                        "19" + iin[0] + iin[1]
                    }
                    val month: String = "" + iin[2] + iin[3]
                    val day: String = "" + iin[i] + iin[5]
                    val date: YearMonth = YearMonth.of(year.toInt(), month.toInt())
                    if(date.isValidDay(day.toInt()))
                        continue
                    else
                        return false
                } else if(i == 6) {
                    if(iin[i] == '1' || iin[i] == '2' || iin[i] == '3' || iin[i] == '4' || iin[i] == '5' || iin[i] == '6')
                        continue
                    else
                        return false
                } else if(i == 7 || i == 8 || i == 9 || i == 10) {
                     if(iin[i].isDigit())
                         continue
                     else
                         return false
                } else if(i == 11) {
                    var sum: Int = (iin[0].toInt()*1 + iin[1].toInt()*2  + iin[2].toInt()*3 + iin[3].toInt()*4 + iin[4].toInt()*5 + iin[5].toInt()*6 + iin[6].toInt()*7 +
                            iin[7].toInt()*8 + iin[8].toInt()*9 + iin[9].toInt()*10 + iin[10].toInt()*11) % 11
                    return if(sum == 10) {
                        sum = (iin[0].toInt()*3 + iin[1].toInt()*4  + iin[2].toInt()*5 + iin[3].toInt()*6 + iin[4].toInt()*7 + iin[5].toInt()*8 + iin[6].toInt()*9 +
                                iin[7].toInt()*10 + iin[8].toInt()*11 + iin[9].toInt()*1 + iin[10].toInt()*2) % 11
                        sum != 10
                    } else {
                        true
                    }
                }
            }
        }

        return false;
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "You need the camera permission to be able to use this app!", Toast.LENGTH_SHORT).show()
                } else {
                    //successful
                }
            }
        }
    }
}