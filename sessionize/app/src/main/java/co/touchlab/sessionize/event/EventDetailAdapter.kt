package co.touchlab.sessionize.event

import android.app.Activity
import android.net.Uri
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.touchlab.droidcon.db.UserAccount
import co.touchlab.sessionize.NavigationHost
import co.touchlab.sessionize.R
import co.touchlab.sessionize.speaker.SpeakerFragment
import com.squareup.picasso.Picasso
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import org.json.JSONObject
import java.util.*

class EventDetailAdapter(private val activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data = ArrayList<Detail>()

    suspend fun translate(txtEN: String): String {
        val uriTranslate = Uri.parse("https://api.microsofttranslator.com/v2/ajax.svc/TranslateArray")
            .buildUpon()
            .query("appId=%22TA9jw1by3nOI8Yvw9t0A-3RFVQkcg3HwLBEPWLB_BAQk*%22&texts=%5B%22$txtEN%22%5D&from=%22en%22&to=%22zh-chs%22&oncomplete=onComplete_4&onerror=onError_4")
            .build()
        val result = HttpClient().get<String>(uriTranslate.toString())
        return result.let { txtResponse ->
            """onComplete_4\(\[(.+)\]\)"""
                .toRegex()
                .matchEntire(txtResponse)
                ?.groupValues
                ?.firstOrNull()
                ?.let { JSONObject(it) }
                ?.let { it.optString("TranslatedText") }
                ?: ""
        }
    }

    fun addHeader(title: String) {
        data.add(HeaderDetail(EntryType.TYPE_HEADER, title))
    }

    fun addBody(description: String) {
        data.add(TextDetail(EntryType.TYPE_BODY, description, 0))
        suspend {
            val translation = translate(description)
            data.add(TextDetail(EntryType.TYPE_BODY, translation, 0))
        }
    }

    fun addInfo(description: String) {
        data.add(TextDetail(EntryType.TYPE_INFO, description, 0))
    }

    fun addSpeaker(speaker: UserAccount) {
        data.add(SpeakerDetail(EntryType.TYPE_SPEAKER,
                speaker.profilePicture,
                speaker.fullName,
                speaker.tagLine,
                speaker.bio,
                speaker.id))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (EntryType.values()[viewType]) {
            EntryType.TYPE_HEADER -> {
                val view = LayoutInflater.from(activity).inflate(R.layout.item_event_header, parent, false)
                HeaderVH(view)
            }
            EntryType.TYPE_BODY -> {
                val view = LayoutInflater.from(activity).inflate(R.layout.item_event_text, parent, false)
                TextVH(view)
            }
            EntryType.TYPE_INFO -> {
                val view = LayoutInflater.from(activity).inflate(R.layout.item_event_info, parent, false)
                InfoVH(view)
            }
            EntryType.TYPE_SPEAKER -> {
                val view = LayoutInflater.from(activity).inflate(R.layout.item_speaker_summary, parent, false)
                SpeakerVH(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun getItemViewType(position: Int): Int {
        return data[position].getItemType()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (EntryType.values()[holder.itemViewType]) {
            EntryType.TYPE_HEADER -> {
                val view = (holder as HeaderVH).itemView
                view.findViewById<TextView>(R.id.title).text = (data[position] as HeaderDetail).title
            }

            EntryType.TYPE_INFO -> {
                val view = (holder as InfoVH).itemView
                view.findViewById<TextView>(R.id.info).text = Html.fromHtml((data[position] as TextDetail).text.trim())
            }

            EntryType.TYPE_BODY -> {
                val view = (holder as TextVH).itemView
                view.findViewById<TextView>(R.id.body).text = Html.fromHtml((data[position] as TextDetail).text.trim())
            }

            EntryType.TYPE_SPEAKER -> {
                val view = (holder as SpeakerVH).itemView
                val user = data[position] as SpeakerDetail

                if (!user.avatar.isNullOrBlank()) {
                    Picasso.get()
                            .load(user.avatar)
                            .noFade()
                            .placeholder(R.drawable.circle_profile_placeholder)
                            .into(view.findViewById<ImageView>(R.id.profile_image))
                }

                val companyName = if (user.company.isNullOrEmpty()) "" else user.company
                view.findViewById<TextView>(R.id.name).text = activity.getString(R.string.event_speaker_name).format(user.name, companyName)

                view.setOnClickListener {
                    (activity as NavigationHost).navigateTo(SpeakerFragment.newInstance(user.userId), true)
                }
                if(user.bio == null)
                    view.findViewById<TextView>(R.id.bio).text =""
                else
                    view.findViewById<TextView>(R.id.bio).text = Html.fromHtml(user.bio.trim())
            }
        }
    }

    enum class EntryType{
        TYPE_HEADER,
        TYPE_BODY,
        TYPE_INFO,
        TYPE_SPEAKER
    }

    open inner class Detail(val type: EntryType) {
        fun getItemType(): Int {
            return type.ordinal
        }
    }

    inner class HeaderDetail(type: EntryType, val title: String) : Detail(type)

    inner class TextDetail(type: EntryType, val text: String, val icon: Int) : Detail(type)

    inner class SpeakerDetail(type: EntryType, val avatar: String?, val name: String, val company: String?, val bio: String?, val userId: String) : Detail(type)

    inner class HeaderVH(val item: View) : RecyclerView.ViewHolder(item)

    inner class InfoVH(val item: View) : RecyclerView.ViewHolder(item)

    inner class TextVH(val item: View) : RecyclerView.ViewHolder(item)

    inner class SpeakerVH(val item: View) : RecyclerView.ViewHolder(item)
}
