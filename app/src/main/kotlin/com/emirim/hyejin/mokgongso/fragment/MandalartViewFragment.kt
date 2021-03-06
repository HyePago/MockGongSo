package com.emirim.hyejin.mokgongso.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment;
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.emirim.hyejin.mokgongso.MainActivity
import com.emirim.hyejin.mokgongso.MandalartActivity
import com.emirim.hyejin.mokgongso.R
import com.emirim.hyejin.mokgongso.User
import com.emirim.hyejin.mokgongso.mandalart.Mandalart
import com.emirim.hyejin.mokgongso.mandalart.ModificationActivity
import kotlinx.android.synthetic.main.fragment_mandalartview.*
import kotlinx.android.synthetic.main.fragment_mandalartview.view.*

class MandalartViewFragment : Fragment() {
    companion object {
        fun newInstance(): MandalartViewFragment {
            return MandalartViewFragment()
        }

        lateinit var secondSelector: Array<TextView>
        lateinit var constraintLayout: View
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        constraintLayout = inflater?.inflate(R.layout.fragment_mandalartview, container, false)

        if(User.first) {
            User.first = false

            var intent = Intent(activity, ModificationActivity::class.java)
            startActivity(intent)
        }

        // title
        constraintLayout.title.text = Mandalart.title
        constraintLayout.subTitle.text = "지금까지 ${Mandalart.achievement} % 달성 중"

        secondSelector = arrayOf(constraintLayout.secondTitle1, constraintLayout.secondTitle2, constraintLayout.secondTitle3, constraintLayout.secondTitle4, constraintLayout.secondTitle5, constraintLayout.secondTitle6, constraintLayout.secondTitle7, constraintLayout.secondTitle8)

        if(Mandalart.viewer == 0) {
            secondInit()
        } else {
            thirdInit()
        }

        for(i in 0..7) {
            secondSelector[i].setOnClickListener {
                if(Mandalart.viewer == 0) {
                    secondClick(i)
                } else if(Mandalart.viewer == 1) {
                    thirdClick(i)
                }
            }
        }

        constraintLayout.firstTitle.setOnClickListener {
            if(Mandalart.viewer == 1) {
                Mandalart.thirdSelect = -1
                Mandalart.secondSelect = -1
                Mandalart.viewer = 0
                secondInit()
            } else {
                fragmentManager
                        ?.beginTransaction()
                        ?.replace(R.id.frameLayout, MandalartallFragment.newInstance())
                        ?.commit()

                MandalartActivity.rightButtonImageView.setImageResource(0)
            }
        }

        return constraintLayout
    }

    fun secondInit() {
        for (i in 0..7) {
            secondSelector[i].setBackgroundResource(R.drawable.mandalart_box_2)
        }

        for(i in 0..7) {
            if(Mandalart.subMandalartTitle[i] != null && !(Mandalart.subMandalartTitle[i].equals(""))) {
                secondSelector[i].setBackgroundResource(R.drawable.mandalart_box_1)
            }
        }
    }

    fun thirdInit() {
        for (j in 0..7) {
            secondSelector[j].setBackgroundResource(R.drawable.mandalart_box_2)
        }

        for(j in 0..7) {
            if(Mandalart.thirdContent[Mandalart.secondSelect][j] != null && !(Mandalart.thirdContent[Mandalart.secondSelect][j].equals(""))) {
                secondSelector[j].setBackgroundResource(R.drawable.mandalart_box_1)
            }
         }
    }

    fun secondClick(i: Int) {
        if(Mandalart.secondSelect == i) {
            Mandalart.viewer = 1

            thirdInit()
        } else {
            if (Mandalart.subMandalartTitle[i] != null && !(Mandalart.subMandalartTitle[i].equals(""))) {
                if (Mandalart.secondSelect != -1) {
                    secondSelector[Mandalart.secondSelect].setBackgroundResource(R.drawable.mandalart_box_1)
                }

                secondSelector[i].setBackgroundResource(R.drawable.mandalart_box_3)
                Mandalart.secondSelect = i
                constraintLayout.title.text = Mandalart.subMandalartTitle[i]
                constraintLayout.subTitle.text = Mandalart.title
            }
        }
    }

    fun thirdClick(i: Int) {
        if(Mandalart.thirdSelect == i) {
            // 이동
            Mandalart.viewer = 2
            fragmentManager!!
                    .beginTransaction()
                    .replace(R.id.frameLayout, MandalartViewThirdFragment.newInstance())
                    .commit()
        } else if(Mandalart.thirdSelect != -1) {
            if(Mandalart.thirdContent[Mandalart.secondSelect][i] != null && !(Mandalart.thirdContent[Mandalart.secondSelect][i].equals(""))) {
                secondSelector[Mandalart.thirdSelect].setBackgroundResource(R.drawable.mandalart_box_1)
            }
        }

        if(Mandalart.thirdContent[Mandalart.secondSelect][i] != null && !(Mandalart.thirdContent[Mandalart.secondSelect][i].equals(""))) {
            secondSelector[i].setBackgroundResource(R.drawable.mandalart_box_3)
            Mandalart.thirdSelect = i
            constraintLayout.title.text = Mandalart.thirdContent[Mandalart.secondSelect][i]
            constraintLayout.subTitle.text = Mandalart.subMandalartTitle[Mandalart.secondSelect]
        }
    }
}