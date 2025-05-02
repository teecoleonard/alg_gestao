package com.example.alg_gestao_02.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.alg_gestao_02.data.db.dao.ProjectContractDao
import com.example.alg_gestao_02.data.db.dao.ProjectDao
import com.example.alg_gestao_02.data.db.dao.ProjectInvoiceDao
import com.example.alg_gestao_02.data.db.entity.ProjectContractEntity
import com.example.alg_gestao_02.data.db.entity.ProjectEntity
import com.example.alg_gestao_02.data.db.entity.ProjectInvoiceEntity

/**
 * Classe principal do banco de dados Room para a aplicação.
 * Define as entidades e versão do banco.
 */
@Database(
    entities = [
        ProjectEntity::class,
        ProjectContractEntity::class,
        ProjectInvoiceEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * DAO para operações com projetos.
     */
    abstract fun projectDao(): ProjectDao
    
    /**
     * DAO para operações com contratos de projetos.
     */
    abstract fun projectContractDao(): ProjectContractDao
    
    /**
     * DAO para operações com faturas de projetos.
     */
    abstract fun projectInvoiceDao(): ProjectInvoiceDao
    
    companion object {
        // Nome do banco de dados
        private const val DATABASE_NAME = "alg_gestao_database"
        
        // Instância singleton volátil para garantir que seja atualizada por todas as threads
        @Volatile
        private var instance: AppDatabase? = null
        
        /**
         * Obtém a instância do banco de dados usando o padrão Singleton.
         * Se a instância não existir, cria uma nova.
         *
         * @param context O contexto da aplicação.
         * @return A instância do banco de dados.
         */
        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }
        
        /**
         * Constrói e configura o banco de dados.
         */
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration() // Em produção, substitua por uma estratégia de migração adequada
                .build()
        }
    }
} 