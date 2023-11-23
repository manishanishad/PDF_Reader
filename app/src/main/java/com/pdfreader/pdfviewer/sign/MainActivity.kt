package com.pdfreader.pdfviewer.sign

import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.RatingBar.OnRatingBarChangeListener
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.example.pdfreader.modalClas.PdfList
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.pdfreader.pdfviewer.sign.common.AppUtils
import com.pdfreader.pdfviewer.sign.common.AppUtils.Companion.getFileSize
import com.pdfreader.pdfviewer.sign.common.AppUtils.Companion.hideKeyboard
import com.pdfreader.pdfviewer.sign.common.AppUtils.Companion.toSimpleString
import com.pdfreader.pdfviewer.sign.fragments.FavoriteFragment
import com.pdfreader.pdfviewer.sign.fragments.HomeFragment
import com.pdfreader.pdfviewer.sign.fragments.RecentFragment
import com.pdfreader.pdfviewer.sign.pdfAdapterClass.ShowPdfAdapter
import com.pdfreader.pdfviewer.sign.pdfViewer.PdfViewActivity
import com.pdfreader.pdfviewer.sign.pref.PreferencesManager
import com.pdfreader.pdfviewer.sign.settingsActivity.SettingActivity
import com.pdfreader.pdfviewer.sign.tabViewpager.TabViewPagerAdapter
import com.pdfreader.pdfviewer.sign.viewModel.ItemViewModel
import com.reader.office.constant.MainConstant
import com.reader.office.officereader.AppActivity
import kotlinx.android.synthetic.main.activity_main.container
import kotlinx.android.synthetic.main.activity_main.drawerLayout
import kotlinx.android.synthetic.main.activity_main.homeToolbar
import kotlinx.android.synthetic.main.activity_main.tabLayout
import kotlinx.android.synthetic.main.activity_main.tvVersion
import kotlinx.android.synthetic.main.confirmation_dialog.btnCancel
import kotlinx.android.synthetic.main.confirmation_dialog.btnDelete
import kotlinx.android.synthetic.main.confirmation_dialog.tvConfirmText
import kotlinx.android.synthetic.main.custom_ratebar.btnFeedback
import kotlinx.android.synthetic.main.custom_ratebar.ivCancelRate
import kotlinx.android.synthetic.main.custom_ratebar.ivRate
import kotlinx.android.synthetic.main.custom_ratebar.ratingBar
import kotlinx.android.synthetic.main.home_toolbar.etSearchDoc
import kotlinx.android.synthetic.main.home_toolbar.ivCancelSearch
import kotlinx.android.synthetic.main.home_toolbar.ivDrawerMenu
import kotlinx.android.synthetic.main.home_toolbar.ivSearch
import kotlinx.android.synthetic.main.home_toolbar.tvPdfReader
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.util.Calendar
import java.util.Date


class MainActivity : AppCompatActivity(), View.OnClickListener,
    NavigationView.OnNavigationItemSelectedListener {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var tabViewPagerAdapter: TabViewPagerAdapter
    private var fileList = ArrayList<File>()
    private var pdfList = ArrayList<PdfList>()
    private lateinit var showPdfAdapter: ShowPdfAdapter
    private lateinit var viewModel: ItemViewModel
    private lateinit var viewPager: ViewPager
    private lateinit var recentFragment: RecentFragment
    private lateinit var favoriteFragment: FavoriteFragment
    private var rateDismissType = 0
    private var permissionTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //AppUtils.changeLanguage(this)
        viewPager = findViewById(R.id.viewPager)
        checkPermission()
        initView()
        Log.d(TAG, "CheckkkkDataTab1")
        tabSelection()
        Log.d(TAG, "CheckkkkDataTab2")
        clickListener()
        run {
            viewModel = ViewModelProvider(this@MainActivity)[ItemViewModel::class.java]
        }
        /*// Check Crash
        val crashButton = Button(this)
        crashButton.text = "Test Crash"
        crashButton.setOnClickListener {
            throw RuntimeException("Test Crash") // Force a crash
        }
        addContentView(crashButton, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT))*/
    }

    private fun checkPermission() {
        if (AppUtils.isPermissionGranted(this)) {
            //Toast.makeText(this, "Granted", Toast.LENGTH_LONG).show()
            val dir = File(Environment.getExternalStorageDirectory().absolutePath)
            AppUtils.filePdf.clear()
            AppUtils.filePdflist.clear()
            Log.d(TAG, "CheckkkkData1")
            fileList = AppUtils.getFile(dir)
            Log.d(TAG, "CheckkkkData2")
            pdfList = AppUtils.filePdflist
            Log.d(TAG, "CheckkkkData3")
            tabSelection()
            Log.d(TAG, "CheckkkkData4")
        } else {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.R) {
                val intent =
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivityForResult(intent, 245)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val intent =
                    Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivityForResult(intent, 2453)
            }
        }
    }

    private fun tabSelection() {
        tabViewPagerAdapter = TabViewPagerAdapter(supportFragmentManager, pdfList)
        tabViewPagerAdapter.addFragment(createViewPagerFragment(0), getString(R.string.all))
        tabViewPagerAdapter.addFragment(createViewPagerFragment(1), getString(R.string.pdf))
        tabViewPagerAdapter.addFragment(createViewPagerFragment(2), getString(R.string.word))
        tabViewPagerAdapter.addFragment(createViewPagerFragment(3), getString(R.string.excel))
        tabViewPagerAdapter.addFragment(createViewPagerFragment(4), getString(R.string.ppt))
        tabViewPagerAdapter.addFragment(createViewPagerFragment(5), getString(R.string.txt))
        viewPager.adapter = tabViewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
    }

    private fun createViewPagerFragment(pos: Int): Fragment {
        val homeFragment = HomeFragment()
        val bundle = Bundle()

        when (pos) {
            0 -> {
                bundle.putString("TYPE", "ALL")
                //bundle.putSerializable("PdfList", pdfList)
                homeFragment.arguments = bundle
                etSearchDoc.text.clear()
            }

            1 -> {
                bundle.putString("TYPE", "PDF")
                //bundle.putSerializable("PdfList", pdfList)
                homeFragment.arguments = bundle
                etSearchDoc.text.clear()
            }

            2 -> {
                bundle.putString("TYPE", "WORD")
                //bundle.putSerializable("PdfList", pdfList)
                homeFragment.arguments = bundle
                etSearchDoc.text.clear()
            }

            3 -> {
                bundle.putString("TYPE", "EXCEL")
                //bundle.putSerializable("PdfList", pdfList)
                homeFragment.arguments = bundle
                etSearchDoc.text.clear()
            }

            4 -> {
                bundle.putString("TYPE", "PPT")
                //bundle.putSerializable("PdfList", pdfList)
                homeFragment.arguments = bundle
                etSearchDoc.text.clear()
            }

            5 -> {
                bundle.putString("TYPE", "TXT")
                //bundle.putSerializable("PdfList", pdfList)
                homeFragment.arguments = bundle
                etSearchDoc.text.clear()
            }
        }
        return homeFragment
    }

    private fun initView() {
        val bundle = Bundle()
        //loadFragment(homeFragment)
        bottomNav = findViewById(R.id.bottomNav)
        ivSearch.imageTintList = getColorStateList(R.color.black)
        tvVersion.text = getString(R.string._v_).plus(" ").plus(BuildConfig.VERSION_NAME)
        AppUtils.changeStatusBarColor(this)
        window.statusBarColor = getColor(R.color.bgCommon)

        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)
        navView.bringToFront()

        showPdfAdapter = ShowPdfAdapter(arrayListOf(), pdfList, this)

        etSearchDoc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val mBottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNav)
                Log.d(TAG, "IDDDD::: ${mBottomNavigationView.selectedItemId}")
                when (mBottomNavigationView.selectedItemId) {
                    R.id.homeFragment -> {
                        if (s.toString().isNotEmpty()) {
                            viewModel.onSearchHome(s.toString())
                        } else {
                            viewModel.onClearSearchH("")
                        }
                    }

                    R.id.recentFragment -> {
                        if (s.toString().isNotEmpty()) {
                            viewModel.onSearchRecent(s.toString())
                        } else {
                            viewModel.onClearSearchR("")
                        }
                    }

                    R.id.favoriteFragment -> {
                        if (s.toString().isNotEmpty()) {
                            viewModel.onSearchFav(s.toString())
                        } else {
                            viewModel.onClearSearchF("")
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        /*val dir = File(Environment.getExternalStorageDirectory().absolutePath)
        AppUtils.filePdf.clear()
        AppUtils.filePdflist.clear()
        fileList = AppUtils.getFile(dir)
        pdfList = AppUtils.filePdflist*/
        //bundle.putSerializable("PdfList", pdfList)
        //homeFragment.arguments = bundle

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homeFragment -> {
                    tvPdfReader.text = getString(R.string.pdf_reader)
                    etSearchDoc.hideKeyboard()
                    etSearchDoc.text.clear()
                    tabLayout.visibility = View.VISIBLE
                    tvPdfReader.visibility = View.VISIBLE
                    ivSearch.visibility = View.VISIBLE
                    ivDrawerMenu.visibility = View.VISIBLE
                    etSearchDoc.visibility = View.GONE
                    ivCancelSearch.visibility = View.GONE
                    viewPager.visibility = View.VISIBLE
                    container.visibility = View.GONE
                    ivDrawerMenu.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_menu
                        )
                    )
                    /*viewPager.visibility = View.GONE
                    container.visibility = View.VISIBLE
                    homeFragment = HomeFragment()
                    homeFragment.arguments = bundle
                    loadFragment(homeFragment)*/
                    true
                }

                R.id.recentFragment -> {
                    tvPdfReader.text = getString(R.string.recent)
                    etSearchDoc.hideKeyboard()
                    etSearchDoc.text.clear()
                    tvPdfReader.visibility = View.VISIBLE
                    ivSearch.visibility = View.VISIBLE
                    ivDrawerMenu.visibility = View.VISIBLE
                    tabLayout.visibility = View.GONE
                    etSearchDoc.visibility = View.GONE
                    ivCancelSearch.visibility = View.GONE
                    ivDrawerMenu.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_back
                        )
                    )
                    viewPager.visibility = View.GONE
                    container.visibility = View.VISIBLE
                    recentFragment = RecentFragment()
                    //bundle.putSerializable("PdfList", pdfList)
                    recentFragment.arguments = bundle
                    loadFragment(recentFragment)
                    true
                }

                R.id.favoriteFragment -> {
                    tvPdfReader.text = getString(R.string.favorite)
                    etSearchDoc.hideKeyboard()
                    etSearchDoc.text.clear()
                    tvPdfReader.visibility = View.VISIBLE
                    ivSearch.visibility = View.VISIBLE
                    ivDrawerMenu.visibility = View.VISIBLE
                    tabLayout.visibility = View.GONE
                    etSearchDoc.visibility = View.GONE
                    ivCancelSearch.visibility = View.GONE
                    ivDrawerMenu.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_back
                        )
                    )
                    viewPager.visibility = View.GONE
                    container.visibility = View.VISIBLE
                    favoriteFragment = FavoriteFragment()
                    //bundle.putSerializable("PdfList", pdfList)
                    favoriteFragment.arguments = bundle
                    loadFragment(favoriteFragment)
                    true
                }

                else -> {
                    false
                }
            }
        }

        val gson = Gson()
        val recentList: Array<PdfList>? = gson.fromJson(
            PreferencesManager.getString(this, PreferencesManager.PREF_RECENT),
            Array<PdfList>::class.java
        )

        Log.d(TAG, "CheckkkkData5")
        if (!recentList.isNullOrEmpty()) {
            for (i in pdfList) {
                for (j in recentList) {
                    if (i.absPath?.equals(j.absPath) == true) {
                        i.pdfTime = j.pdfTime
                    }
                }
            }
        }
        Log.d(TAG, "CheckkkkData6")

        val favorList = gson.fromJson(
            PreferencesManager.getString(this, PreferencesManager.PREF_FAVORITE),
            Array<String>::class.java
        )

        Log.d(TAG, "CheckkkkData7")
        if (favorList != null && favorList.isNotEmpty()) {
            for (i in pdfList) {
                for (j in favorList) {
                    if (i.absPath?.equals(j) == true) {
                        i.isFav = true
                    }
                }
            }
        }
        Log.d(TAG, "CheckkkkData8")
    }

    override fun onBackPressed() {
        val mBottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNav)
        if (mBottomNavigationView.selectedItemId == R.id.homeFragment) {
            val getRating: String = PreferencesManager.getString(this, PreferencesManager.PREF_RATE)
            if (getRating.isEmpty()) {
                rateDismissType = 2
                openRatingDialog()
            } else {
                //super.onBackPressed()
                openExitDialog()
            }
        } else {
            mBottomNavigationView.selectedItemId = R.id.homeFragment
        }
    }

    private fun openExitDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.confirmation_dialog)
        dialog.tvConfirmText.text = getString(R.string.exit_app)
        dialog.btnDelete.text = getString(R.string.yes)
        dialog.btnCancel.text = getString(R.string.no)
        dialog.btnDelete.setOnClickListener {
            finish()
        }
        dialog.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        val window: Window? = dialog.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        window?.setGravity(Gravity.CENTER)
        dialog.show()
    }

    private fun clickListener() {
        ivDrawerMenu.setOnClickListener(this)
        ivCancelSearch.setOnClickListener(this)
        ivSearch.setOnClickListener(this)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    when (tab.position.toString()) {
                        "0" -> {
                            etSearchDoc.text.clear()
                            ivSearch.visibility = View.VISIBLE
                            ivDrawerMenu.visibility = View.VISIBLE
                            tvPdfReader.visibility = View.VISIBLE
                            ivDrawerMenu.imageTintList = getColorStateList(R.color.black)
                            ivCancelSearch.imageTintList = getColorStateList(R.color.black)
                            ivSearch.imageTintList = getColorStateList(R.color.black)
                            tvPdfReader.setTextColor(getColor(R.color.black))
                            etSearchDoc.setHintTextColor(getColor(R.color.gray))
                            etSearchDoc.setTextColor(getColor(R.color.black))
                            homeToolbar.setBackgroundColor(getColor(R.color.bgCommon))
                            tabLayout.setBackgroundColor(getColor(R.color.bgCommon))
                            tabLayout.setSelectedTabIndicatorColor(getColor(R.color.red))
                            tabLayout.tabTextColors =
                                ContextCompat.getColorStateList(
                                    this@MainActivity,
                                    R.color.black
                                )
                            AppUtils.changeStatusBarColor(this@MainActivity)
                            window.statusBarColor = getColor(R.color.bgCommon)
                            tabViewPagerAdapter.setPosition(tab.position)
                        }

                        "1" -> {
                            etSearchDoc.text.clear()
                            ivSearch.visibility = View.GONE
                            etSearchDoc.visibility = View.GONE
                            ivCancelSearch.visibility = View.GONE
                            ivDrawerMenu.visibility = View.VISIBLE
                            tvPdfReader.visibility = View.VISIBLE
                            ivDrawerMenu.imageTintList = getColorStateList(R.color.white)
                            ivCancelSearch.imageTintList = getColorStateList(R.color.white)
                            ivSearch.imageTintList = getColorStateList(R.color.white)
                            tvPdfReader.setTextColor(getColor(R.color.white))
                            etSearchDoc.setHintTextColor(getColor(R.color.white))
                            etSearchDoc.setTextColor(getColor(R.color.white))
                            homeToolbar.setBackgroundColor(getColor(R.color.red))
                            tabLayout.setBackgroundColor(getColor(R.color.red))
                            tabLayout.setSelectedTabIndicatorColor(getColor(R.color.white))
                            tabLayout.tabTextColors =
                                ContextCompat.getColorStateList(
                                    this@MainActivity,
                                    R.color.white
                                )
                            AppUtils.changeStatusBarColor(this@MainActivity)
                            window.statusBarColor = getColor(R.color.red)
                            tabViewPagerAdapter.setPosition(tab.position)
                        }

                        "2" -> {
                            etSearchDoc.text.clear()
                            ivSearch.visibility = View.GONE
                            etSearchDoc.visibility = View.GONE
                            ivCancelSearch.visibility = View.GONE
                            ivDrawerMenu.visibility = View.VISIBLE
                            tvPdfReader.visibility = View.VISIBLE
                            ivDrawerMenu.imageTintList = getColorStateList(R.color.white)
                            ivCancelSearch.imageTintList = getColorStateList(R.color.white)
                            ivSearch.imageTintList = getColorStateList(R.color.white)
                            tvPdfReader.setTextColor(getColor(R.color.white))
                            etSearchDoc.setHintTextColor(getColor(R.color.white))
                            etSearchDoc.setTextColor(getColor(R.color.white))
                            homeToolbar.setBackgroundColor(getColor(R.color.sky_blue))
                            tabLayout.setBackgroundColor(getColor(R.color.sky_blue))
                            tabLayout.setSelectedTabIndicatorColor(getColor(R.color.white))
                            tabLayout.tabTextColors =
                                ContextCompat.getColorStateList(
                                    this@MainActivity,
                                    R.color.white
                                )
                            AppUtils.changeStatusBarColor(this@MainActivity)
                            window.statusBarColor = getColor(R.color.sky_blue)
                            tabViewPagerAdapter.setPosition(tab.position)
                        }

                        "3" -> {
                            etSearchDoc.text.clear()
                            ivSearch.visibility = View.GONE
                            etSearchDoc.visibility = View.GONE
                            ivCancelSearch.visibility = View.GONE
                            ivDrawerMenu.visibility = View.VISIBLE
                            tvPdfReader.visibility = View.VISIBLE
                            ivDrawerMenu.imageTintList = getColorStateList(R.color.white)
                            ivCancelSearch.imageTintList = getColorStateList(R.color.white)
                            ivSearch.imageTintList = getColorStateList(R.color.white)
                            tvPdfReader.setTextColor(getColor(R.color.white))
                            etSearchDoc.setHintTextColor(getColor(R.color.white))
                            etSearchDoc.setTextColor(getColor(R.color.white))
                            homeToolbar.setBackgroundColor(getColor(R.color.green))
                            tabLayout.setBackgroundColor(getColor(R.color.green))
                            tabLayout.setSelectedTabIndicatorColor(getColor(R.color.white))
                            tabLayout.tabTextColors =
                                ContextCompat.getColorStateList(
                                    this@MainActivity,
                                    R.color.white
                                )
                            AppUtils.changeStatusBarColor(this@MainActivity)
                            window.statusBarColor = getColor(R.color.green)
                            tabViewPagerAdapter.setPosition(tab.position)
                        }

                        "4" -> {
                            etSearchDoc.text.clear()
                            ivSearch.visibility = View.GONE
                            etSearchDoc.visibility = View.GONE
                            ivCancelSearch.visibility = View.GONE
                            ivDrawerMenu.visibility = View.VISIBLE
                            tvPdfReader.visibility = View.VISIBLE
                            ivDrawerMenu.imageTintList = getColorStateList(R.color.white)
                            ivCancelSearch.imageTintList = getColorStateList(R.color.white)
                            ivSearch.imageTintList = getColorStateList(R.color.white)
                            tvPdfReader.setTextColor(getColor(R.color.white))
                            etSearchDoc.setHintTextColor(getColor(R.color.white))
                            etSearchDoc.setTextColor(getColor(R.color.white))
                            homeToolbar.setBackgroundColor(getColor(R.color.orange))
                            tabLayout.setBackgroundColor(getColor(R.color.orange))
                            tabLayout.setSelectedTabIndicatorColor(getColor(R.color.white))
                            tabLayout.tabTextColors =
                                ContextCompat.getColorStateList(
                                    this@MainActivity,
                                    R.color.white
                                )
                            AppUtils.changeStatusBarColor(this@MainActivity)
                            window.statusBarColor = getColor(R.color.orange)
                            tabViewPagerAdapter.setPosition(tab.position)
                        }

                        "5" -> {
                            etSearchDoc.text.clear()
                            ivSearch.visibility = View.GONE
                            etSearchDoc.visibility = View.GONE
                            ivCancelSearch.visibility = View.GONE
                            ivDrawerMenu.visibility = View.VISIBLE
                            tvPdfReader.visibility = View.VISIBLE
                            ivDrawerMenu.imageTintList = getColorStateList(R.color.white)
                            ivCancelSearch.imageTintList = getColorStateList(R.color.white)
                            ivSearch.imageTintList = getColorStateList(R.color.white)
                            tvPdfReader.setTextColor(getColor(R.color.white))
                            etSearchDoc.setHintTextColor(getColor(R.color.white))
                            etSearchDoc.setTextColor(getColor(R.color.white))
                            homeToolbar.setBackgroundColor(getColor(R.color.gray))
                            tabLayout.setBackgroundColor(getColor(R.color.gray))
                            tabLayout.setSelectedTabIndicatorColor(getColor(R.color.white))
                            tabLayout.tabTextColors =
                                ContextCompat.getColorStateList(
                                    this@MainActivity,
                                    R.color.white
                                )
                            AppUtils.changeStatusBarColor(this@MainActivity)
                            window.statusBarColor = getColor(R.color.gray)
                            tabViewPagerAdapter.setPosition(tab.position)
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name))
            var shareMessage =
                "\nHi! I Just checked this app in play store, You must try it out:\n\n"
            shareMessage =
                """${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}""".trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(
                Intent.createChooser(
                    shareIntent,
                    resources.getString(R.string.pdf_reader)
                )
            )
        } catch (e: Exception) {
        }
    }

    private fun openRatingDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.custom_ratebar)

        val getRating: String = PreferencesManager.getString(this, PreferencesManager.PREF_RATE)
        if (getRating.isNotEmpty()) {
            dialog.ratingBar.rating = java.lang.Float.valueOf(getRating)
        } else {
            dialog.ratingBar.rating = java.lang.Float.valueOf("5.0")
        }

        dialog.ratingBar.onRatingBarChangeListener =
            OnRatingBarChangeListener { ratingBar, _, _ ->
                when (ratingBar.rating.toString()) {
                    "0.5", "1.0" -> {
                        dialog.ivRate.setImageResource(R.drawable.ic_rate_1)
                        dialog.btnFeedback.text = getString(R.string.feedback)
                    }

                    "1.5", "2.0" -> {
                        dialog.ivRate.setImageResource(R.drawable.ic_rate_2)
                        dialog.btnFeedback.text = getString(R.string.feedback)
                    }

                    "2.5", "3.0" -> {
                        dialog.ivRate.setImageResource(R.drawable.ic_rate_3)
                        dialog.btnFeedback.text = getString(R.string.feedback)
                    }

                    "3.5", "4.0" -> {
                        dialog.ivRate.setImageResource(R.drawable.ic_rate_4)
                        dialog.btnFeedback.text = getString(R.string.feedback)
                    }

                    "4.5", "5.0" -> {
                        dialog.ivRate.setImageResource(R.drawable.ic_rate_5)
                        dialog.btnFeedback.text = getString(R.string.rate_now)
                    }
                }
            }

        dialog.btnFeedback.setOnClickListener {
            if (dialog.ratingBar != null) {
                val msg = dialog.ratingBar.rating.toString()
                if (msg >= "0.5") {
                    if (msg == "5.0") {
                        AppUtils.openPlayStore(this)
                        dialog.dismiss()
                    } else {
                        AppUtils.moveToEmail(this)
                        dialog.dismiss()
                    }
                    PreferencesManager.setString(this, PreferencesManager.PREF_RATE, msg)
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.please_give_ratings),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        dialog.ivCancelRate.setOnClickListener {
            if (rateDismissType == 1) {
                dialog.dismiss()
            } else {
                finish()
            }
        }
        val window: Window? = dialog.window
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        window?.setGravity(Gravity.CENTER)
        dialog.show()
    }

    private fun selectFilesFromStorage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/jpeg"
        val mimetypes = arrayOf("application/*")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes)
        startActivityForResult(intent, 43)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "Codeeee--->>> $requestCode")
        if (requestCode == 43) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    val uri = data.data
                    val getPath = AppUtils.getRealPathFromURI(uri!!, this)
                    val file = File(getPath)
                    val getDate = Date(file.lastModified()).toSimpleString()
                    val getSize = getFileSize(file.length())
                    val fileName = file.name
                    val fileExt = file.name.endsWith(".pdf")
                    if (fileExt) {
                        val intent = Intent(applicationContext, PdfViewActivity::class.java)
                        intent.putExtra("fileType", 1)
                        intent.putExtra("fileName", fileName)
                        intent.putExtra("fileDate", getDate)
                        //intent.putExtra("fileExt", pdfList[position].pdfExt)
                        intent.putExtra("fileSize", getSize)
                        intent.putExtra("fileFav", false)
                        ///intent.putExtra("itemPosition", position)
                        intent.putExtra(MainConstant.INTENT_FILED_FILE_PATH, getPath)
                        startActivity(intent)
                    } else {
                        val intent = Intent(applicationContext, AppActivity::class.java)
                        intent.putExtra("fileType", 1)
                        intent.putExtra("fileName", fileName)
                        intent.putExtra("fileDate", getDate)
                        //intent.putExtra("fileExt", pdfList[position].pdfExt)
                        intent.putExtra("fileSize", getSize)
                        intent.putExtra("fileFav", false)
                        ///intent.putExtra("itemPosition", position)
                        intent.putExtra(MainConstant.INTENT_FILED_FILE_PATH, getPath)
                        startActivity(intent)
                    }
                }
            } else {
                finish()
            }
        }
        if (requestCode == 2453) {
            if (AppUtils.isPermissionGranted(this)) {
                val dir = File(Environment.getExternalStorageDirectory().absolutePath)
                AppUtils.filePdf.clear()
                AppUtils.filePdflist.clear()
                fileList = AppUtils.getFile(dir)
                pdfList = AppUtils.filePdflist
                tabSelection()
            }
        }
        if (requestCode == 245) {
            if (AppUtils.isPermissionGranted(this)) {
                /*val dir = File(Environment.getExternalStorageDirectory().absolutePath)
                AppUtils.filePdf.clear()
                AppUtils.filePdflist.clear()
                fileList = AppUtils.getFile(dir)
                pdfList = AppUtils.filePdflist
                tabSelection()*/
            }
        }
        if (requestCode == 147) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (data.getIntExtra("TYPE", 0) == 1) {
                        pdfList.removeAt(data.getIntExtra("deletedItem", 0))
                    } else if (data.getIntExtra("TYPE", 0) == 2) {
                        pdfList[data.getIntExtra("renameDocPos", 0)].pdfName =
                            data.getStringExtra("renameDoc")

                        val oldPath = pdfList[data.getIntExtra("renameDocPos", 0)].absPath
                        val path: String? = File(oldPath.toString()).parentFile?.absolutePath
                        val newPath = File(path.plus("/").plus(data.getStringExtra("renameDoc")))
                        pdfList[data.getIntExtra("renameDocPos", 0)].absPath = newPath.toString()
                    } else {
                        Log.d(TAG, "CheckkkkDataOn1")
                        val dir = File(Environment.getExternalStorageDirectory().absolutePath)
                        Log.d(TAG, "CheckkkkDataOn2")
                        AppUtils.filePdf.clear()
                        Log.d(TAG, "CheckkkkDataOn3")
                        AppUtils.filePdflist.clear()
                        Log.d(TAG, "CheckkkkDataOn4")
                        fileList = AppUtils.getFile(dir)
                        Log.d(TAG, "CheckkkkDataOn5")
                        pdfList = AppUtils.filePdflist
                        Log.d(TAG, "CheckkkkDataOn6")
                        tabSelection()
                        Log.d(TAG, "CheckkkkDataOn7")
                        EventBus.getDefault().post(Intent("SORTED_TYPE"))
                    }
                }
            }
        }
        if (requestCode == 1212) {
            EventBus.getDefault().post(Intent("PERMISSION_RESULT"))
            val dir = File(Environment.getExternalStorageDirectory().absolutePath)
            AppUtils.filePdf.clear()
            AppUtils.filePdflist.clear()
            fileList = AppUtils.getFile(dir)
            pdfList = AppUtils.filePdflist
            tabSelection()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val currentTime = Calendar.getInstance().timeInMillis
        /*for (fragment in supportFragmentManager.fragments) {
             Log.d(TAG, "Codeeee::: $requestCode")
             fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
         }*/
        Log.d(TAG, "Codeeee::: $requestCode")
        if (requestCode == 1) {
            EventBus.getDefault().post(Intent("PERMISSION_RESULT"))
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                val dir = File(Environment.getExternalStorageDirectory().absolutePath)
                AppUtils.filePdf.clear()
                AppUtils.filePdflist.clear()
                fileList = AppUtils.getFile(dir)
                pdfList = AppUtils.filePdflist
                tabSelection()
            } else if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                fileList.clear()
                pdfList.clear()
                /*val diff = currentTime - permissionTime
                if (diff < 500) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", this.packageName, null)
                    intent.data = uri
                    startActivity(intent)
                    finish()
                    return
                }*/
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                closeDrawer()
            }

            R.id.selectFile -> {
                selectFilesFromStorage()
                closeDrawer()
            }

            R.id.settings -> {
                closeDrawer()
                startActivity(Intent(this, SettingActivity::class.java))
            }

            R.id.rateUs -> {
                closeDrawer()
                rateDismissType = 1
                val getRating: String =
                    PreferencesManager.getString(this, PreferencesManager.PREF_RATE)
                if (getRating.isEmpty()) {
                    openRatingDialog()
                } else {
                    AppUtils.openPlayStore(this)
                }
            }

            R.id.shareApp -> {
                closeDrawer()
                shareApp()
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.ivSearch -> {
                ivDrawerMenu.visibility = View.GONE
                ivCancelSearch.visibility = View.VISIBLE
                tvPdfReader.visibility = View.GONE
                ivSearch.visibility = View.INVISIBLE
                etSearchDoc.visibility = View.VISIBLE
                etSearchDoc.isFocusableInTouchMode = true
                etSearchDoc.textCursorDrawable = getDrawable(R.drawable.cursor_color)
                etSearchDoc.clearFocus()
                etSearchDoc.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
                //etSearchDoc.openKeyboard()
            }

            R.id.ivCancelSearch -> {
                ivDrawerMenu.visibility = View.VISIBLE
                ivCancelSearch.visibility = View.GONE
                etSearchDoc.text.clear()
                etSearchDoc.hideKeyboard()
                tvPdfReader.visibility = View.VISIBLE
                ivSearch.visibility = View.VISIBLE
                etSearchDoc.visibility = View.GONE
            }

            R.id.ivDrawerMenu -> {
                val mBottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNav)
                if (mBottomNavigationView.selectedItemId == R.id.homeFragment) {
                    drawerLayout.openDrawer(GravityCompat.START)
                } else {
                    mBottomNavigationView.selectedItemId = R.id.homeFragment
                }
            }
        }
    }
}
