package screen.detail

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel

class PlanDetailScreen : Screen {
    @Composable
    override fun Content() {
        val detailScreenModel = getScreenModel<PlanDetailScreenModel>()
    }
}