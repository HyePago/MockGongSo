package com.emirim.hyejin.mokgongso.smallMandalart.page

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.emirim.hyejin.mokgongso.R
import com.emirim.hyejin.mokgongso.smallMandalart.CreateMandalart
import kotlinx.android.synthetic.main.activity_small_page_1.*
import kotlinx.android.synthetic.main.activity_small_page_1.view.*


class Page1 : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val constraintLayout = inflater.inflate(R.layout.activity_small_page_1, container, false) as ConstraintLayout

        // getWindow().setSoftInputMode(WindowManager.LayoutParams.Soft_INPUT_ADJUST_RESIZE);
        constraintLayout.setBackgroundResource(R.color.backgroundColor)

        if(Mandalart.title != null) {
            constraintLayout.mandalartTitle.setText(Mandalart.title)
            constraintLayout.mandalartTitle.isFocusableInTouchMode = false
            constraintLayout.mandalartTitle.isClickable = false

            CreateMandalart.mViewPager.currentItem = 1
        } else {
            constraintLayout.rightArrow.visibility = View.GONE
        }

        constraintLayout.mandalartTitle.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) { }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s.toString().trim().isNotEmpty()) {
                    rightArrow.visibility = View.VISIBLE
                } else {
                    rightArrow.visibility = View.GONE
                }
            }
        })

        constraintLayout.rightArrow.setOnClickListener {
            Mandalart.title = constraintLayout.mandalartTitle.text.toString()
            CreateMandalart.mViewPager.currentItem = 1
        }

        return constraintLayout
    }
}