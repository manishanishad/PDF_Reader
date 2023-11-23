package com.pdfreader.pdfviewer.sign.languageActivity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.pdfreader.modalClas.LanguageList
import com.pdfreader.pdfviewer.sign.R
import kotlinx.android.synthetic.main.language_list.view.clLanguage
import kotlinx.android.synthetic.main.language_list.view.ivImage
import kotlinx.android.synthetic.main.language_list.view.rbLanguage
import kotlinx.android.synthetic.main.language_list.view.tvLanguage
import kotlinx.android.synthetic.main.language_list.view.tvSubLanguage

class LanguageAdapter(private val langList: List<LanguageList>, private val context: Context) :
    RecyclerView.Adapter<LanguageAdapter.MyViewHolder>() {

    private var setSelectedPosition = 0

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.ivImage
        val languageName: TextView = itemView.tvLanguage
        val subLanguageName: TextView = itemView.tvSubLanguage
        val radioButton: RadioButton = itemView.rbLanguage
        val clLanguage: ConstraintLayout = itemView.clLanguage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.language_list, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return langList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        /*holder.languageName.text = langList[position].languageName
         holder.subLanguageName.text = langList[position].subLanguage
         //holder.radioButton.isChecked = langList[position].isSelected

         holder.itemView.setOnClickListener {
             languageSelection(holder, position)
         }

         holder.radioButton.setOnClickListener {
             languageSelection(holder, position)
         }

         holder.itemView.rbLanguage.isChecked = position == setSelectedPosition*/

        holder.image.setImageDrawable(context.getDrawable(langList[position].image))
        holder.languageName.text = langList[position].languageName
        holder.subLanguageName.text = langList[position].subLanguage
        holder.radioButton.isChecked = langList[position].isSelected
        holder.clLanguage.tag = position
        holder.radioButton.tag = position

        holder.radioButton.setOnClickListener {
            selectionLanguage(it)
        }

        holder.clLanguage.setOnClickListener {
            selectionLanguage(it)
        }
    }

    private fun selectionLanguage(v: View) {
        for (language in langList) {
            language.isSelected = false
        }
        val pos: Int = v.tag as Int
        if (pos >= 0) {
            langList[pos].isSelected = (true)
            notifyDataSetChanged()
            setSelectedPosition = pos
        }
    }

    private fun languageSelection(holder: MyViewHolder, position: Int) {
        if (setSelectedPosition >= 0) {
            langList[position].removeOtherSelection(setSelectedPosition, langList)
            langList[position].isSelected = true
            notifyItemChanged(setSelectedPosition)
        }
        setSelectedPosition = holder.adapterPosition
        notifyItemChanged(setSelectedPosition)
    }

    fun setPosition(position: Int) {
        setSelectedPosition = position
    }
}