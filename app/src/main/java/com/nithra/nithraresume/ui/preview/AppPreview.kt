package com.nithra.nithraresume.ui.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark",  showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class AppPreview

@Preview(name = "Light", showBackground = true, showSystemUi = true)
@Preview(name = "Dark",  showBackground = true, showSystemUi = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class AppFullScreenPreview

@Preview(name = "Light", showBackground = true, widthDp = 360)
@Preview(name = "Dark",  showBackground = true, widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
annotation class AppDrawerPreview
