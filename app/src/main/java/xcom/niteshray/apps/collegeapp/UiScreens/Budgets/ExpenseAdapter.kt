package xcom.niteshray.apps.collegeapp.UiScreens.Budgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import xcom.niteshray.apps.collegeapp.R
import xcom.niteshray.apps.collegeapp.model.Expense
import xcom.niteshray.apps.collegeapp.model.Sponsor

class ExpenseAdapter(private val context: Context, private val expense: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sponsorname = itemView.findViewById<TextView>(R.id.sponsorname)
        val sponsoramount = itemView.findViewById<TextView>(R.id.sponsor_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sponsors, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val current = expense[position]
        holder.sponsorname.text = current.item
        holder.sponsoramount.text = "-$"+current.cost.toString()
}
    override fun getItemCount(): Int = expense.size
}