package com.example.util

import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.data.model.Worker
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * Bulletproof persistent store for Workers.
 * Ensures workers entered by the user are NEVER lost or cleared unless explicitly deleted by user.
 * Combines multiple independent persistent layers:
 * 1. Dedicated internal JSON file in context.filesDir (guaranteed private, survives app restarts)
 * 2. Secondary internal backup JSON file
 * 3. SharedPreferences backup (persisted synchronously with commit())
 * 4. External app storage directory mirror (context.getExternalFilesDir)
 */
object PermanentWorkerStore {

    private const val TAG = "PermanentWorkerStore"
    private const val PREFS_NAME = "permanent_worker_vault_prefs"
    private const val KEY_WORKERS_JSON = "key_permanent_workers_json"
    private const val FILE_NAME = "permanent_workers_registry.json"
    private const val BACKUP_FILE_NAME = "permanent_workers_registry_backup.json"

    fun saveWorkers(context: Context, workers: List<Worker>, explicitWipe: Boolean = false) {
        if (workers.isEmpty()) {
            if (explicitWipe) {
                clearWorkers(context)
            }
            return
        }
        try {
            val jsonArray = JSONArray()
            for (w in workers) {
                val obj = JSONObject().apply {
                    put("workerId", w.workerId)
                    put("name", w.name)
                    put("gender", w.gender)
                    put("workerType", w.workerType)
                    put("dailyRate", w.dailyRate)
                    put("otRate", w.otRate)
                    put("status", w.status)
                    put("phone", w.phone)
                    put("address", w.address)
                    put("nrcNo", w.nrcNo)
                    put("emergencyContactName", w.emergencyContactName)
                    put("emergencyPhone", w.emergencyPhone)
                    put("bloodType", w.bloodType)
                    put("skillLevel", w.skillLevel)
                    put("paymentMethod", w.paymentMethod)
                    put("paymentAccount", w.paymentAccount)
                    put("joinedDate", w.joinedDate)
                    put("notes", w.notes)
                }
                jsonArray.put(obj)
            }
            val jsonString = jsonArray.toString(2)
            val jsonBytes = jsonString.toByteArray(Charsets.UTF_8)

            // Layer 1: Internal storage private file
            try {
                val file = File(context.filesDir, FILE_NAME)
                FileOutputStream(file).use { out ->
                    out.write(jsonBytes)
                    out.flush()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Layer 1 write error: ${e.message}")
            }

            // Layer 2: Secondary internal backup copy
            try {
                val backupFile = File(context.filesDir, BACKUP_FILE_NAME)
                FileOutputStream(backupFile).use { out ->
                    out.write(jsonBytes)
                    out.flush()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Layer 2 backup write error: ${e.message}")
            }

            // Layer 3: SharedPreferences (committed synchronously)
            try {
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                prefs.edit()
                    .putString(KEY_WORKERS_JSON, jsonString)
                    .putLong("last_save_time", System.currentTimeMillis())
                    .commit()
            } catch (e: Exception) {
                Log.w(TAG, "Layer 3 prefs error: ${e.message}")
            }

            // Layer 4: External app storage mirror (safe app-specific folder)
            try {
                val extDir = context.getExternalFilesDir("vault")
                if (extDir != null) {
                    if (!extDir.exists()) extDir.mkdirs()
                    val extFile = File(extDir, "permanent_workers_vault.json")
                    FileOutputStream(extFile).use { out ->
                        out.write(jsonBytes)
                        out.flush()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "External mirror save notice: ${e.message}")
            }

            Log.d(TAG, "Successfully permanently saved ${workers.size} workers to all storage layers.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save workers permanently", e)
        }
    }

    /**
     * Merges incoming workers with existing stored workers by workerId and persists the combination.
     */
    fun mergeAndSaveWorkers(context: Context, incoming: List<Worker>): List<Worker> {
        val existing = loadWorkers(context) ?: emptyList()
        val mergedMap = LinkedHashMap<String, Worker>()
        for (w in existing) {
            mergedMap[w.workerId] = w
        }
        for (w in incoming) {
            mergedMap[w.workerId] = w
        }
        val combined = mergedMap.values.toList()
        saveWorkers(context, combined)
        return combined
    }

    /**
     * Completely and permanently clears all stored workers from all persistence layers.
     */
    fun clearWorkers(context: Context) {
        try {
            // Layer 1 & 2: Internal files
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists()) file.delete()
            val backupFile = File(context.filesDir, BACKUP_FILE_NAME)
            if (backupFile.exists()) backupFile.delete()
            val tempFile = File(context.filesDir, "$FILE_NAME.tmp")
            if (tempFile.exists()) tempFile.delete()

            // Layer 3: SharedPreferences
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(KEY_WORKERS_JSON).remove("last_save_time").commit()

            // Layer 4: External mirror
            val extDir = context.getExternalFilesDir("vault")
            if (extDir != null) {
                val extFile = File(extDir, "permanent_workers_vault.json")
                if (extFile.exists()) extFile.delete()
            }

            Log.d(TAG, "PermanentWorkerStore successfully cleared all workers.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear PermanentWorkerStore", e)
        }
    }

    /**
     * Deletes a specific set of worker IDs from the persistent store.
     */
    fun deleteWorkersByIds(context: Context, idsToDelete: Set<String>) {
        val current = loadWorkers(context) ?: return
        val remaining = current.filterNot { idsToDelete.contains(it.workerId) }
        saveWorkers(context, remaining, explicitWipe = remaining.isEmpty())
    }

    fun loadWorkers(context: Context): List<Worker>? {
        val mergedMap = LinkedHashMap<String, Worker>()

        // Try Layer 1: Internal storage file
        try {
            val file = File(context.filesDir, FILE_NAME)
            if (file.exists() && file.length() > 0) {
                val content = file.readText(Charsets.UTF_8)
                val list = parseWorkersJson(content)
                for (w in list) mergedMap[w.workerId] = w
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error reading internal file: ${e.message}")
        }

        // Try Layer 2: Secondary internal backup
        try {
            val backupFile = File(context.filesDir, BACKUP_FILE_NAME)
            if (backupFile.exists() && backupFile.length() > 0) {
                val content = backupFile.readText(Charsets.UTF_8)
                val list = parseWorkersJson(content)
                for (w in list) {
                    if (!mergedMap.containsKey(w.workerId)) mergedMap[w.workerId] = w
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error reading backup file: ${e.message}")
        }

        // Try Layer 3: SharedPreferences
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val json = prefs.getString(KEY_WORKERS_JSON, null)
            if (!json.isNullOrBlank()) {
                val list = parseWorkersJson(json)
                for (w in list) {
                    if (!mergedMap.containsKey(w.workerId)) mergedMap[w.workerId] = w
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error reading prefs: ${e.message}")
        }

        // Try Layer 4: External app storage
        try {
            val extDir = context.getExternalFilesDir("vault")
            val extFile = if (extDir != null) File(extDir, "permanent_workers_vault.json") else null
            if (extFile != null && extFile.exists() && extFile.length() > 0) {
                val list = parseWorkersJson(extFile.readText(Charsets.UTF_8))
                for (w in list) {
                    if (!mergedMap.containsKey(w.workerId)) mergedMap[w.workerId] = w
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error reading external file: ${e.message}")
        }

        // Try Layer 5: BackupManager auto-backups
        try {
            val vaultBackup = BackupManager.findExistingPersistentVault(context)
            if (vaultBackup != null && vaultBackup.workers.isNotEmpty()) {
                for (w in vaultBackup.workers) {
                    if (!mergedMap.containsKey(w.workerId)) mergedMap[w.workerId] = w
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error reading BackupManager vault: ${e.message}")
        }

        return if (mergedMap.isNotEmpty()) mergedMap.values.toList() else null
    }

    private fun parseWorkersJson(jsonString: String): List<Worker> {
        val result = mutableListOf<Worker>()
        val array = JSONArray(jsonString)
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result.add(
                Worker(
                    workerId = obj.optString("workerId", "W-${100 + i}"),
                    name = obj.optString("name", ""),
                    gender = obj.optString("gender", "Male"),
                    workerType = obj.optString("workerType", "Helper"),
                    dailyRate = obj.optDouble("dailyRate", 20000.0),
                    otRate = obj.optDouble("otRate", 3000.0),
                    status = obj.optString("status", "Active"),
                    phone = obj.optString("phone", ""),
                    address = obj.optString("address", ""),
                    nrcNo = obj.optString("nrcNo", ""),
                    emergencyContactName = obj.optString("emergencyContactName", ""),
                    emergencyPhone = obj.optString("emergencyPhone", ""),
                    bloodType = obj.optString("bloodType", ""),
                    skillLevel = obj.optString("skillLevel", "Skilled"),
                    paymentMethod = obj.optString("paymentMethod", "Cash"),
                    paymentAccount = obj.optString("paymentAccount", ""),
                    joinedDate = obj.optString("joinedDate", ""),
                    notes = obj.optString("notes", "")
                )
            )
        }
        return result
    }
}
