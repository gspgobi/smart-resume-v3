package com.nithra.nithraresume.ui.section.child

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.canhub.cropper.CropImageActivity
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageView

class CropImageActivityWrapper : CropImageActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val decorView = window.decorView
        // Android 15+ ignores window.statusBarColor (edge-to-edge enforced); overlay a blue view instead.
        ViewCompat.getWindowInsetsController(decorView)?.isAppearanceLightStatusBars = false
        decorView.post {
            val statusBarHeight = ViewCompat.getRootWindowInsets(decorView)
                ?.getInsets(WindowInsetsCompat.Type.statusBars())?.top ?: 0
            if (statusBarHeight > 0) {
                (decorView as ViewGroup).addView(
                    View(this).apply { setBackgroundColor(0xFF1565C0.toInt()) },
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight)
                )
            }
        }
    }
}

@Suppress("DEPRECATION")
class SmartResumeCropContract : ActivityResultContract<CropImageContractOptions, CropImageView.CropResult>() {
    private val delegate = CropImageContract()
    override fun createIntent(context: Context, input: CropImageContractOptions): Intent =
        delegate.createIntent(context, input).setClass(context, CropImageActivityWrapper::class.java)
    override fun parseResult(resultCode: Int, intent: Intent?): CropImageView.CropResult =
        delegate.parseResult(resultCode, intent)
}
