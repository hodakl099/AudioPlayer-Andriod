package com.plcoding.audioplayer.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.plcoding.audioplayer.R
import kotlinx.android.synthetic.main.swipe_item.view.*

class SwipeAudioAdapter : BaseAudioAdapter(R.layout.list_item) {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: BaseAudioAdapter.SongViewHolder, position: Int) {
        val song = audios[position]
        holder.itemView.apply {
            val text = "${song.title} - ${song.subtitle}"
            tvPrimary.text = text

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }

}