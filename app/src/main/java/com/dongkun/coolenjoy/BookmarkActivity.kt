package com.dongkun.coolenjoy

import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemClickListener
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.ArrayList
import java.util.HashMap

class BookmarkActivity : AppCompatActivity() {
    var listRowData: ArrayList<HashMap<String, String>>? = null

    var TAG_TITLE = "title"
    var TAG_LINK = "link"

    var listView: ListView? = null
    var adapter: ListAdapter? = null

    var linearLayout: LinearLayout? = null
    var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)

//        val toolbar: Toolbar = findViewById(R.id.toolbar)
//        setSupportActionBar(toolbar)
//        supportActionBar!!.setTitle("BOOKMARKS")
//        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
//        toolbar.setTitleTextColor(resources.getColor(android.R.color.white))
//        toolbar.setNavigationOnClickListener(View.OnClickListener { onBackPressed() })

        listView = findViewById(R.id.listView)
        linearLayout = findViewById(R.id.emptyList)

        mSwipeRefreshLayout = findViewById(R.id.swipeToRefresh)
        mSwipeRefreshLayout?.setColorSchemeResources(R.color.black)
        mSwipeRefreshLayout?.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                LoadBookmarks().execute()
            }
        })

        LoadBookmarks().execute()
        listView?.onItemClickListener = OnItemClickListener { parent, view, position, id ->
            val o = listView?.adapter?.getItem(position)
            if (o is Map<*, *>) {
                val map = o
                val `in` = Intent(this@BookmarkActivity, MainActivity::class.java)
                `in`.putExtra("url", map[TAG_LINK].toString())
                startActivity(`in`)
            }
        }


    }

    inner private class LoadBookmarks : AsyncTask<String, String, String>() {

        override fun doInBackground(vararg args: String?): String? {
            // updating UI from Background Thread
            runOnUiThread(Runnable {
                val sharedPreferences: SharedPreferences =
                    getSharedPreferences(
                        MainActivity().PREFERENCES,
                        MODE_PRIVATE
                    )
                val jsonLink = sharedPreferences.getString(MainActivity().WEB_LINKS, null)
                val jsonTitle = sharedPreferences.getString(MainActivity().WEB_TITLE, null)
                listRowData = ArrayList<HashMap<String, String>>()
                if (jsonLink != null && jsonTitle != null) {
                    val gson = Gson()
                    val linkArray = gson.fromJson<ArrayList<String>>(
                        jsonLink,
                        object : TypeToken<ArrayList<String?>?>() {}.type
                    )
                    val titleArray = gson.fromJson<ArrayList<String>>(
                        jsonTitle,
                        object : TypeToken<ArrayList<String?>?>() {}.type
                    )
                    for (i in linkArray.indices) {
                        val map = HashMap<String, String>()
                        if (titleArray[i].isEmpty())
                            map[TAG_TITLE] = "Bookmark " + (i + 1)
                        else map[TAG_TITLE] = titleArray[i]
                        map[TAG_LINK] = linkArray[i]
                        listRowData?.add(map)
                    }
                    adapter = SimpleAdapter(
                        this@BookmarkActivity,
                        listRowData,
                        R.layout.bookmark_list_row,
                        arrayOf(TAG_TITLE, TAG_LINK),
                        intArrayOf(R.id.title, R.id.link)
                    )
                    listView?.adapter = adapter
                }
                linearLayout?.visibility = View.VISIBLE
                listView?.emptyView = linearLayout
            })
            return null
        }

        override fun onPostExecute(args: String?) {
            mSwipeRefreshLayout?.isRefreshing = false
        }

    }

}