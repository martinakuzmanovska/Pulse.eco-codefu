package com.codefu.pulse_eco.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.codefu.pulse_eco.R
import com.codefu.pulse_eco.domain.models.EventCardModel
import com.bumptech.glide.Glide

class EventCardAdapter(
    context: Context,
    private var eventCardModelList: MutableList<EventCardModel>
) : ArrayAdapter<EventCardModel>(context, 0, eventCardModelList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val listItemView = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.event_card_item, parent, false)

        val model = getItem(position)
        val cardTitleText = listItemView.findViewById<TextView>(R.id.cardTitleText)
        val cardTitleDate = listItemView.findViewById<TextView>(R.id.cardDate)
        val cardImg = listItemView.findViewById<ImageView>(R.id.card_image)


        cardTitleText.text = model?.getCardTitleText() ?: ""
        cardTitleDate.text = model?.getCardDate() ?: ""

        Glide.with(context)
                    .load(model?.getImgid())
                    .into(cardImg)

        return listItemView
    }



    fun updateData(newEventList: List<EventCardModel>) {
        eventCardModelList.clear()
        eventCardModelList.addAll(newEventList)
        notifyDataSetChanged()
    }
}
