package di

import com.sulv.lifeplan.database.LifePlanDatabase
import data.repo.PlanRepository
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import platform.DatabaseDriverFactory
import platform.platformModule
import screen.detail.PlanDetailScreenModel
import screen.main.MainScreenModel
import screen.new.NewPlanScreenModel

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(dataModule, screenModelsModule, platformModule())
    }
}

val dataModule = module {
    single {
        val json = Json { ignoreUnknownKeys = true }
        HttpClient {
            install(ContentNegotiation) {
                json(json, contentType = ContentType.Any)
            }
        }
    }

    single {
        LifePlanDatabase(
            driver = get<DatabaseDriverFactory>().createDriver("lifePlan.db")
        )
    }

    single { PlanRepository(get()) }
}

val screenModelsModule = module {
    factoryOf(::MainScreenModel)
    factoryOf(::NewPlanScreenModel)
    factoryOf(::PlanDetailScreenModel)
}