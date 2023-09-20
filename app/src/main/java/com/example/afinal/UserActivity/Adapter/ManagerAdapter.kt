package com.example.afinal.UserActivity.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.afinal.R

class ManagerAdapter(private val context: Context, var managers: List<Manager>) :
    RecyclerView.Adapter<ManagerAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.manager_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val manager = managers[position]
        holder.bind(manager)
    }

    override fun getItemCount() = managers.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.manager_img)
        private val textName: TextView = itemView.findViewById(R.id.managerName)
        private val textNumber: TextView = itemView.findViewById(R.id.managerNum)
        private val textCity: TextView = itemView.findViewById(R.id.managerCity)

        fun bind(manager: Manager) {
           Glide.with(context).load(manager.image).into(imageView)
            textName.text = manager.name
            textNumber.text = manager.number
            textCity.text = manager.city
        }
    }
}
data class Manager(
    val name: String = "",
    val number: String = "",
    val city: String = "",
    val image: String = ""
)