package com.gematriga.nopepad.adapter

import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gematriga.nopepad.R
import com.gematriga.nopepad.databinding.ItemRvNotesBinding
import com.gematriga.nopepad.entities.Notes
import kotlinx.android.synthetic.main.item_rv_notes.view.*


class NotesAdapter() :
    RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {

    var listener:OnItemClickListener? = null
    var arrList = ArrayList<Notes>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val binding = ItemRvNotesBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return NotesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        holder.binding.cardTitle.setText(arrList[position].title)
        holder.binding.noteEditText.setText(arrList[position].noteText)
        holder.binding.noteDateTime.setText(arrList[position].dateTime)

        if(arrList[position].noteColor != null){
            holder.itemView.cardView.setBackgroundColor(Color.parseColor(arrList[position].noteColor))
        }else{
            holder.itemView.cardView.setBackgroundColor(Color.parseColor(R.color.defaultNote.toString()))
        }
        if (arrList[position].imgPath != null){
            holder.itemView.imageNote.setImageBitmap(BitmapFactory.decodeFile(arrList[position].imgPath))
            holder.itemView.imageNote.visibility = View.VISIBLE
        }else{
            holder.itemView.imageNote.visibility = View.GONE
        }
        if (arrList[position].webLink != ""){
            holder.itemView.usWebLink.text = arrList[position].webLink
            holder.itemView.usWebLink.visibility = View.VISIBLE
        }else{
            holder.itemView.usWebLink.visibility = View.GONE
        }

        holder.itemView.cardView.setOnClickListener {
            listener!!.onClicked(arrList[position].id!!)
        }

    }

    override fun getItemCount(): Int {
        return arrList.size
    }

    fun setData(arrNotesList: List<Notes>){

        arrList = arrNotesList as ArrayList<Notes>

    }

    fun setOnClickListener(listener1: OnItemClickListener){

        listener = listener1

    }

    class NotesViewHolder(val binding: ItemRvNotesBinding) : RecyclerView.ViewHolder(binding.root){

    }

    interface OnItemClickListener{

        fun onClicked(noteId: Int)

    }
}