package com.example.overlord.eventapp.main.event


import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import java.io.Serializable
import com.example.overlord.eventapp.R
import com.example.overlord.eventapp.model.Event
import kotlinx.android.synthetic.main.fragment_event.*
import kotlinx.android.synthetic.main.fragment_event_item.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import android.content.Intent
import android.net.Uri
import com.example.overlord.eventapp.base.BaseFragment
import android.provider.CalendarContract.Events
import android.provider.CalendarContract




class EventFragment : BaseFragment() {

    //Declare your data here
    class FragmentInputs(val firstName: String = "Diksha", val surname: String = "Agarwal") : Serializable

    interface FragmentInteractor : Serializable {
        //Implement your methods here
        fun onButtonPressed(message: String)
    }

    private var inputs: FragmentInputs? = null
    private var interactor: FragmentInteractor? = null

    companion object {
        @JvmStatic
        fun newInstance(inputs: FragmentInputs?, interactor: FragmentInteractor) =
            EventFragment().apply {
                this.interactor = interactor
                arguments = Bundle().apply { putSerializable("inputs", inputs) }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inputs = arguments?.getSerializable("inputs") as FragmentInputs?
        //Initialize Heavier things here because onCreateView and onViewCreated are called much more number of times
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        eventRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)

        val events = createEventList()

        eventRecyclerView.adapter = EventAdapter(events)
    }

    private fun createEventList() : ArrayList<Event> {
        val events = ArrayList<Event>()
        val format = SimpleDateFormat("yyyy-mm-dd'T'HH:mm aaa", Locale.UK)

        events.add(Event("Pool Party",
            format.parse("2019-02-08T10:00 AM"),
            "10:00 AM onwards",
            "Lunch at 1:00 PM",
            "Palm Heights Gymkhana, Bhubaneswar",
            R.drawable.poolparty,
            "Come in blue and white and enjoy our party with great delight!!")
        )

        events.add(Event("Haldi",
            format.parse("2019-02-08T04:00 PM"),
            "4:00 PM onwards",
            "Dinner at 7:30 PM",
            "Palm Heights Gymkhana, Bhubaneswar",
            R.drawable.haldi,
            "Together, lets make yellow an auspicious color for the bride.")
        )

        events.add(Event("Mehendi",
            format.parse("2019-02-09T08:00 AM"),
            "8:00 AM onwards",
            "Lunch at 12:30 PM",
            "Palm Heights Gymkhana, Bhubaneswar",
            R.drawable.mehendi,
            "Darker the color, darker is his love. Lets shower our blessings on this dove.")
        )

        events.add(Event("Wedding",
            format.parse("2019-02-10T07:00 AM"),
            "7:00 AM onwards",
            "Lunch at 12:30 PM",
            "Mayfair Convention, Bhubaneswar",
            R.drawable.wedding,
            "Shaaadiii Shaadiiiiiiiii")
        )


        events.add(Event("Reception",
            format.parse("2019-02-14T07:30 PM"),
            "7:30 PM onwards",
            "Dinner at 7:30 PM",
            "Sri Venkateshwara Swamy Kalyana Mandapam, SriNagar Colony, Hyderabad",
            R.drawable.reception,
            "Let's bless the newly married couple for a long and a happy life together forever.")
        )

        return events
    }

    inner class EventAdapter(val eventList: ArrayList<Event>) : RecyclerView.Adapter<EventAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventAdapter.ViewHolder {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.fragment_event_item, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: EventAdapter.ViewHolder, position: Int) {
            holder.bindItems(eventList[position])
        }

        override fun getItemCount(): Int {
            return eventList.size
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bindItems(event : Event) {
                itemView.event_header.text = event.name
                Glide.with(itemView).load(event.image).into(itemView.eventImageView)
                itemView.event_time.text = event.eventTime
                itemView.event_food.text = event.foodTime
                itemView.event_address.text = event.location
                itemView.event_message.text = event.message

                val calendar = Calendar.getInstance()
                calendar.time = event.date
                itemView.event_year.text = calendar.get(Calendar.YEAR).toString()
                itemView.event_month.text = "Feb"
                itemView.event_date.text = calendar.get(Calendar.DAY_OF_MONTH).toString()

                arrayOf(
                    itemView.address_icon,
                    itemView.event_address
                ).forEach {
                    it.setOnClickListener {
                        val uri = "http://maps.google.co.in/maps?q=${event.location}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                        startActivity(intent)
                    }
                }

                itemView.set_date.setOnClickListener { setEventInCalender(event) }
            }
        }
    }

    fun setEventInCalender(event : Event) {
        val calendar = Calendar.getInstance()
        calendar.time = event.date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val startTime: Long = Calendar.getInstance().run {
            set(year, month, day, hour-1, minute)
            timeInMillis
        }
        val endTime: Long = Calendar.getInstance().run {
            set(year, month, day, hour+3, minute)
            timeInMillis
        }

        val intent = Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.CONTENT_URI)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
                    .putExtra(Events.TITLE, event.name)
                    .putExtra(Events.DESCRIPTION, event.message)
                    .putExtra(Events.EVENT_LOCATION, event.location)
                    .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)

        startActivity(intent)
    }
}
