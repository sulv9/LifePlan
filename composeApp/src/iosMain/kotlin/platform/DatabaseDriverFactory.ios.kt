package platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.sulv.lifeplan.database.LifePlanDatabase

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DatabaseDriverFactory {
    actual fun createDriver(dbName: String): SqlDriver {
        return NativeSqliteDriver(schema = LifePlanDatabase.Schema, name = dbName)
    }
}