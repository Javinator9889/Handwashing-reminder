/*
 * Copyright © 2020 - present | Handwashing reminder by Javinator9889
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see https://www.gnu.org/licenses/.
 *
 * Created by Javinator9889 on 30/03/20 - Handwashing reminder.
 */
package com.javinator9889.handwashingreminder.collections

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.javinator9889.handwashingreminder.R
import javinator9889.localemanager.fragment.BaseFragment
import org.sufficientlysecure.htmltextview.HtmlFormatter
import org.sufficientlysecure.htmltextview.HtmlFormatterBuilder

class PrivacyTermsCollectionAdapter(fm: FragmentActivity) :
    FragmentStateAdapter(fm) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        val fragment = PolicyTextViewFragment()
        fragment.arguments = Bundle().apply {
            putInt(ARG_POSITION, position)
        }
        return fragment
    }

}

internal const val ARG_POSITION = "item:position"

class PolicyTextViewFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.simple_text_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.takeIf { it.containsKey(ARG_POSITION) }?.apply {
            val textView = view.findViewById<TextView>(R.id.text)
            val htmlBuilder = HtmlFormatterBuilder().apply {
                isRemoveTrailingWhiteSpace = true
            }
            when (requireArguments().getInt(ARG_POSITION)) {
                0 -> textView.text = HtmlFormatter.formatHtml(
                    htmlBuilder.setHtml(getString(R.string.privacy_policy_text))
                )
                1 -> textView.text = HtmlFormatter.formatHtml(
                    htmlBuilder.setHtml(getString(R.string.tos))
                )
            }
            textView.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}