package com.example.expensetracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.model.Expense

class RecentExpenseAdapter(
    private var expenses: List<Expense>
) : RecyclerView.Adapter<RecentExpenseAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoryIcon: TextView = view.findViewById(R.id.tvCategoryIcon)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvNote: TextView = view.findViewById(R.id.tvNote)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_expense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenses[position]
        holder.tvCategoryIcon.text = getCategoryEmoji(expense.category)
        holder.tvCategory.text = expense.category
        holder.tvNote.text = expense.note.ifEmpty { expense.date }
        holder.tvAmount.text = "PKR ${String.format("%.0f", expense.amount)}"
    }

    override fun getItemCount(): Int = expenses.size

    fun updateData(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }

    private fun getCategoryEmoji(category: String): String = when (category.lowercase()) {
        "food" -> "🍔"
        "transport" -> "🚗"
        "shopping" -> "🛍️"
        "health" -> "💊"
        "education" -> "📚"
        "entertainment" -> "🎬"
        "utilities" -> "💡"
        "rent" -> "🏠"
        "clothing" -> "👕"
        else -> "💰"
    }
}
