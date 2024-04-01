package platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.sulv.lifeplan.database.LifePlanDatabase
import java.io.File

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class DatabaseDriverFactory {
    actual fun createDriver(dbName: String): SqlDriver {
        return JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY + dbName).apply {
            if (File(dbName).exists().not()) {
                LifePlanDatabase.Schema.create(this)
            }
        }
    }
}