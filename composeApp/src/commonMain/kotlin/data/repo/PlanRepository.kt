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
        remindDateTime: String,
    ) {
        dbQuery.insertPlan(title, description, startDateTime, endDateTime, priority, progress, remindDateTime)
    }

    fun getPlan(id: Long): PlanEntity {
        return dbQuery.getPlan(id) { idMap: Long, title: String, description: String, createDateTime: String, startDateTime: String, endDateTime: String, priority: Long, progress: Long, remindDateTime: String ->
            PlanEntity(
                idMap,
                title,
                description,
                createDateTime,
                startDateTime,
                endDateTime,
                priority.toInt(),
                progress.toInt(),
                remindDateTime
            )
        }.executeAsOne()
    }

    fun getPlanByDate(date: String): List<PlanEntity> {
        return dbQuery.getPlanByDate(date) { id: Long, title: String, description: String, createDateTime: String, startDateTime: String, endDateTime: String, priority: Long, progress: Long, remindDateTime: String ->
            PlanEntity(
                id,
                title,
                description,
                createDateTime,
                startDateTime,
                endDateTime,
                priority.toInt(),
                progress.toInt(),
                remindDateTime
            )
        }.executeAsList()
    }

    fun deletePlan(id: Long) {
        dbQuery.deletePlan(id)
    }

    fun updateTitle(id: Long, title: String) {
        dbQuery.updateTitle(title, id)
    }

    fun updateDescription(id: Long, description: String) {
        dbQuery.updateDescription(description, id)
    }

    fun updateStartDateTime(id: Long, startDateTime: String) {
        dbQuery.updateStartDateTime(startDateTime, id)
    }

    fun updateEndDateTime(id: Long, endDateTime: String) {
        dbQuery.updateEndDateTime(endDateTime, id)
    }

    fun updatePriority(id: Long, priority: Long) {
        dbQuery.updatePriority(priority, id)
    }

    fun updateProgress(id: Long, progress: Long) {
        dbQuery.updateProgress(progress, id)
    }

    fun updateRemindDateTime(id: Long, remindDateTime: String) {
        dbQuery.updateRemindDateTime(remindDateTime, id)
    }
}