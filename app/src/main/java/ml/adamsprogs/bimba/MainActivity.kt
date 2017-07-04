package ml.adamsprogs.bimba

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.widget.Toast
import com.arlib.floatingsearchview.FloatingSearchView
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val context = this as Context

        val stops = listOf(Suggestion("Kołłątaja\n610 -> Dębiec"), Suggestion("Dębiecka\n610 -> Górczyn")) //todo get from db
        val searchView = findViewById(R.id.search_view) as FloatingSearchView

        searchView.setOnQueryChangeListener({ _, newQuery ->
            searchView.swapSuggestions(stops.filter { deAccent(it.body.split("\n")[0]).contains(newQuery, true) })
        })

        searchView.setOnSearchListener(object : FloatingSearchView.OnSearchListener {
            override fun onSuggestionClicked(searchSuggestion: SearchSuggestion) {
                Toast.makeText(context, "clicked "+ searchSuggestion.body, Toast.LENGTH_SHORT).show()
                //todo to next screen
            }
            override fun onSearchAction(query: String) {
            }
        })

        searchView.setOnBindSuggestionCallback { _, _, textView, item, _ ->
            val suggestion = item as Suggestion
            val text = suggestion.body.split("\n")
            val t = "<small><font color=\"#a0a0a0\">" + text[1] + "</font></small>"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textView.text = Html.fromHtml(text[0]+"<br/>"+t, Html.FROM_HTML_MODE_LEGACY)
            } else {
                @Suppress("DEPRECATION")
                textView.text = Html.fromHtml(text[0]+"<br/>"+t)
            }
        }

        //todo searchView.attachNavigationDrawerToMenuButton(mDrawerLayout)
    }

    fun deAccent(str: String): String {
        var result = str.replace('ę', 'e')
        result = result.replace('ó', 'o')
        result = result.replace('ą', 'a')
        result = result.replace('ś', 's')
        result = result.replace('ł', 'l')
        result = result.replace('ż', 'ż')
        result = result.replace('ź', 'ź')
        result = result.replace('ć', 'ć')
        result = result.replace('ń', 'n')
        return result
    }

    class Suggestion(text: String) : SearchSuggestion {
        private val body: String = text

        constructor(parcel: Parcel) : this(parcel.readString())

        override fun describeContents(): Int {
            TODO("not implemented")
        }

        override fun writeToParcel(dest: Parcel?, flags: Int) {
            TODO("not implemented")
        }

        override fun getBody(): String {
            return body
        }

        companion object CREATOR : Parcelable.Creator<Suggestion> {
            override fun createFromParcel(parcel: Parcel): Suggestion {
                return Suggestion(parcel)
            }

            override fun newArray(size: Int): Array<Suggestion?> {
                return arrayOfNulls(size)
            }
        }
    }
}