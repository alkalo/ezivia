package com.ezivia.launcher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ezivia.launcher.databinding.ItemHomeQuickActionBinding

class HomeQuickActionsAdapter(
    private val onActionClick: (HomeQuickAction) -> Unit,
) : RecyclerView.Adapter<HomeQuickActionsAdapter.QuickActionViewHolder>() {

    private val actions = mutableListOf<HomeQuickAction>()

    fun submitList(newActions: List<HomeQuickAction>) {
        actions.clear()
        actions.addAll(newActions)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuickActionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemHomeQuickActionBinding.inflate(inflater, parent, false)
        return QuickActionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuickActionViewHolder, position: Int) {
        holder.bind(actions[position])
    }

    override fun getItemCount(): Int = actions.size

    inner class QuickActionViewHolder(
        private val binding: ItemHomeQuickActionBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(action: HomeQuickAction) {
            binding.quickActionIcon.setImageResource(action.iconRes)
            binding.quickActionTitle.setText(action.titleRes)
            binding.quickActionDescription.setText(action.descriptionRes)
            binding.root.setOnClickListener { onActionClick(action) }
        }
    }
}
