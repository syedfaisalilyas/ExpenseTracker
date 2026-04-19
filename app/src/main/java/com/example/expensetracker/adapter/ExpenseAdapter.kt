package com.example.expensetracker.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker.R
import com.example.expensetracker.model.Expense

class ExpenseAdapter(
    private var expenses: MutableList<Expense>,
    private val onEdit: (Expense) -> Unit,
    private val onDelete: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategoryIcon: TextView = view.findViewById(R.id.tvCategoryIcon)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvNote: TextView = view.findViewById(R.id.tvNote)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val btnEdit: TextView = view.findViewById(R.id.btnEdit)
        val btnDelete: TextView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenses[position]
        holder.tvCategoryIcon.text = getCategoryEmoji(expense.category)
        holder.tvCategory.text = expense.category
        holder.tvNote.text = expense.note.ifEmpty { "No note" }
        holder.tvDate.text = expense.date
        holder.tvAmount.text = "PKR ${String.format("%.0f", expense.amount)}"
        holder.btnEdit.setOnClickListener { onEdit(expense) }
        holder.btnDelete.setOnClickListener { onDelete(expense) }
    }

    override fun getItemCount(): Int = expenses.size

    fun updateData(newExpenses: List<Expense>) {
        expenses.clear()
        expenses.addAll(newExpenses)
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
