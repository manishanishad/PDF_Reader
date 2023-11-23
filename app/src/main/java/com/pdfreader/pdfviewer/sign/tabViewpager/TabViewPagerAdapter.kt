package com.pdfreader.pdfviewer.sign.tabViewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.pdfreader.modalClas.PdfList

class TabViewPagerAdapter(
    fragmentManager: FragmentManager,
    private val pdfList: ArrayList<PdfList>
) :
    FragmentStatePagerAdapter(fragmentManager) {

    private val fragmentList = ArrayList<Fragment>()
    private val fragmentTitle = ArrayList<String>()
    private var tabPosition = 0

    override fun getCount() = fragmentList.size

    override fun getItem(position: Int): Fragment {
        /*val bundle = Bundle()
        bundle.putSerializable("PdfList", pdfList)
        val homeFragment = HomeFragment()
        homeFragment.arguments = bundle

        return when (position) {
            0 -> homeFragment
            1 -> homeFragment
            2 -> homeFragment
            3 -> homeFragment
            4 -> homeFragment
            5 -> homeFragment
            else -> throw IllegalArgumentException("Invalid tab position")
        }*/

        return fragmentList[position]
    }

    override fun getPageTitle(position: Int) = fragmentTitle[position]

    fun addFragment(fragment: Fragment, title: String) {
        fragmentList.add(fragment)
        fragmentTitle.add(title)
    }

    fun setPosition(position: Int) {
        tabPosition = position
    }
}