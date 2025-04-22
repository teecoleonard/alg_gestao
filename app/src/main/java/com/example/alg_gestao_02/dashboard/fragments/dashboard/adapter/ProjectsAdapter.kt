package com.example.alg_gestao_02.dashboard.fragments.dashboard.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.alg_gestao_02.R
import com.example.alg_gestao_02.dashboard.fragments.client.ProjectDetailActivity
import com.example.alg_gestao_02.dashboard.fragments.dashboard.model.Project
import com.example.alg_gestao_02.utils.LogUtils

class ProjectsAdapter(
    private var projects: List<Project> = emptyList(),
    private val onItemClick: (Project) -> Unit
) : RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder>() {

    fun updateData(newProjects: List<Project>) {
        this.projects = newProjects
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bind(projects[position])
    }

    override fun getItemCount(): Int = projects.size

    class ProjectViewHolder(
        itemView: View,
        private val onItemClick: (Project) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val ivProjectImage: ImageView = itemView.findViewById(R.id.ivProjectImage)
        private val tvProjectName: TextView = itemView.findViewById(R.id.tvProjectName)
        private val tvProjectLocation: TextView = itemView.findViewById(R.id.tvProjectLocation)
        private val tvProjectStatus: TextView = itemView.findViewById(R.id.tvProjectStatus)
        private val tvBudget: TextView = itemView.findViewById(R.id.tvBudget)
        private val tvExpenses: TextView = itemView.findViewById(R.id.tvExpenses)
        private val tvStartDate: TextView = itemView.findViewById(R.id.tvStartDate)
        private val tvEndDate: TextView = itemView.findViewById(R.id.tvEndDate)
        private val ivProjectMenu: ImageView = itemView.findViewById(R.id.ivProjectMenu)

        fun bind(project: Project) {
            tvProjectName.text = project.name
            tvProjectLocation.text = project.location
            tvBudget.text = project.budget
            tvExpenses.text = project.expenses
            tvStartDate.text = project.startDate
            tvEndDate.text = project.endDate
            
            // Configurar o status do projeto
            tvProjectStatus.text = getStatusText(project.status)
            tvProjectStatus.setBackgroundResource(getStatusBackground(project.status))
            
            // Imagem do projeto (em uma implementação real, usaria uma biblioteca como Glide)
            // Glide.with(itemView.context).load(project.imageUrl).into(ivProjectImage)
            
            // Configurar os cliques
            itemView.setOnClickListener {
                LogUtils.debug("ProjectsAdapter", "Projeto clicado: ${project.id}")
                
                // Navegar para a tela de detalhes do projeto
                val intent = Intent(itemView.context, ProjectDetailActivity::class.java)
                intent.putExtra("project_id", project.id)
                intent.putExtra("project_name", project.name)
                itemView.context.startActivity(intent)
                
                // Chamar o callback
                onItemClick(project)
            }
            
            ivProjectMenu.setOnClickListener {
                LogUtils.debug("ProjectsAdapter", "Menu do projeto clicado: ${project.id}")
                // Implementar exibição de menu de opções
            }
        }
        
        private fun getStatusText(status: String): String {
            return when (status) {
                "active" -> "Ativo"
                "in_progress" -> "Em Andamento"
                "completed" -> "Concluído"
                "cancelled" -> "Cancelado"
                else -> status.capitalize()
            }
        }
        
        private fun getStatusBackground(status: String): Int {
            return when (status) {
                "active" -> R.drawable.bg_status_active
                "in_progress" -> R.drawable.bg_status_in_progress
                "completed" -> R.drawable.bg_status_completed
                "cancelled" -> R.drawable.bg_status_cancelled
                else -> R.drawable.bg_status_active
            }
        }
        
        private fun String.capitalize(): String {
            return this.replaceFirstChar { 
                if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() 
            }
        }
    }
} 