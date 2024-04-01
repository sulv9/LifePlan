package platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.sulv.lifeplan.database.LifePlanDatabase
import org.koin.mp.KoinPlatform

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DatabaseDriverFactory {
    actual fun createDriver(dbName: String): SqlDriver {
        return AndroidSqliteDriver(
            schema = LifePlanDatabase.Schema,
            context = KoinPlatform.getKoin().get(),
            name = dbName
        )
    }
}