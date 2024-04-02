package data.repo

import com.sulv.lifeplan.database.LifePlanDatabase
import data.model.PlanEntity

class PlanRepository(database: LifePlanDatabase) {
    private val dbQuery = database.planQueries

    fun createPlan(
        title: String,
        description: String,
        startDateTime: String,
        endDateTime: String,
        priority: Long,
        progress: Long,
    ) {
        dbQuery.insertPlan(title, description, startDateTime, endDateTime, priority, progress)
    }

    fun getPlanByDate(date: String): List<PlanEntity> {
        return dbQuery.getPlanByDate(date) { id: Long, title: String, description: String, createDateTime: String, startDateTime: String, endDateTime: String, priority: Long, progress: Long ->
            PlanEntity(
                id,
                title,
                description,
                createDateTime,
                startDateTime,
                endDateTime,
                priority.toInt(),
                progress.toInt(), ""
            )
        }.executeAsList()
    }
}