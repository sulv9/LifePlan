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
    ) {
        dbQuery.insertPlan(title, description, startDateTime, endDateTime, priority)
    }

    fun getPlanByDay(datetime: String): List<PlanEntity> {
        return dbQuery.getPlanByDay(datetime) { id: Long, title: String, description: String, createDateTime: String, startDateTime: String, endDateTime: String, priority: Long ->
            PlanEntity(
                id,
                title,
                description,
                createDateTime,
                startDateTime,
                endDateTime,
                priority.toInt()
            )
        }.executeAsList()
    }
}