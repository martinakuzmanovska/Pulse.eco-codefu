import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codefu.pulse_eco.domain.models.ShopItem
import com.codefu.pulse_eco.R

class ShopItemAdapter(
    private val items: List<ShopItem>
) : RecyclerView.Adapter<ShopItemAdapter.ShopItemViewHolder>() {

    class ShopItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.shop_item_image)
        val name: TextView = itemView.findViewById(R.id.shop_item_name)
        val description: TextView = itemView.findViewById(R.id.shop_item_description)
        val points: TextView = itemView.findViewById(R.id.shop_item_points)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.shop_item, parent, false)
        return ShopItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShopItemViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.description.text = item.description
        holder.points.text = "Points: ${item.pointsRequired}"

        Glide.with(holder.itemView.context)
            .load(item.url)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.image)
    }

    override fun getItemCount(): Int = items.size
}
