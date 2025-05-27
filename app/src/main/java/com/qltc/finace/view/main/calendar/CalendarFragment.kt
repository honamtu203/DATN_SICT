package com.qltc.finace.view.main.calendar

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qltc.finace.R
import com.qltc.finace.base.BaseFragment
import com.qltc.finace.base.Constant
import com.qltc.finace.databinding.DayOfWeekHeaderBinding
import com.qltc.finace.databinding.FragmentCalendarBinding
import com.qltc.finace.extension.navigateWithAnim
import com.qltc.finace.extension.setTimeSelected
import com.qltc.finace.view.adapter.AdapterExpenseIncomeReport
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.nextMonth
import com.kizitonwose.calendar.core.previousMonth
import com.kizitonwose.calendar.view.MonthHeaderFooterBinder
import com.kizitonwose.calendar.view.ViewContainer
import dagger.hilt.android.AndroidEntryPoint
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

@AndroidEntryPoint
class CalendarFragment : BaseFragment<FragmentCalendarBinding, CalendarViewModel>(),
    CalendarListener, AdapterExpenseIncomeReport.OnClickListener, MondayView.OnClickDayListener {

    override val viewModel: CalendarViewModel by viewModels()
    override val layoutID: Int = R.layout.fragment_calendar


    private val adapter by lazy { AdapterExpenseIncomeReport(this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getDataByDate()
        viewBinding.apply {
            listener = this@CalendarFragment
            listIncomeAndExpense.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            listIncomeAndExpense.adapter = this@CalendarFragment.adapter
            viewModel = this@CalendarFragment.viewModel
        }
        viewModel.listSyntheticByDate.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        val daysOfWeek = daysOfWeek()
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(200)
        val endMonth = currentMonth.plusMonths(200)
        configureBinders(daysOfWeek)
        viewBinding.calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        viewBinding.calendarView.scrollToMonth(currentMonth)

        viewBinding.calendarView.monthScrollListener = { month ->
            viewBinding.monthYearText.text = month.yearMonth.displayText()
            viewModel.selectedDate?.let {
                viewModel.selectedDate = null
                viewBinding.calendarView.notifyDateChanged(it)
            }
            viewModel.filterListSyntheticByMonth(month.yearMonth)
        }
        viewModel.isGetDataByMonth.observe(viewLifecycleOwner) {
            viewBinding.calendarView.notifyCalendarChanged()
        }

        viewBinding.filter.setTimeSelected(
            time = null,
            yearMonth = YearMonth.now(),
            isSelectedDay = false
        )
    }


    private fun configureBinders(daysOfWeek: List<DayOfWeek>) {

        viewBinding.calendarView.dayBinder =
            MondayView(viewModel = this@CalendarFragment.viewModel, this)

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val listDayOfWeek = DayOfWeekHeaderBinding.bind(view).listDayOfWeek.root
        }

        viewBinding.calendarView.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, data: CalendarMonth) {
                    // Setup each header day text if we have not done that already.
                    if (container.listDayOfWeek.tag == null) {
                        container.listDayOfWeek.tag = data.yearMonth
                        container.listDayOfWeek.children.map { it as TextView }
                            .forEachIndexed { index, tv ->
                                tv.text = daysOfWeek[index].displayText(uppercase = true)
                            }
                    }
                }
            }
    }

    override fun onClickDay(selectedDate: LocalDate, oldDate: LocalDate?) {
        viewBinding.calendarView.notifyDateChanged(selectedDate)
        oldDate?.let {
            viewBinding.calendarView.notifyDateChanged(it)
        }
        viewModel.filterListSyntheticByDate(selectedDate)
        viewBinding.filter.setTimeSelected(
            time = viewModel.selectedDate,
            yearMonth = null,
            isSelectedDay = true
        )
    }

    override fun exFiveNextMonthImage() {
        viewBinding.calendarView.findFirstVisibleMonth()?.let {
            viewBinding.calendarView.smoothScrollToMonth(it.yearMonth.nextMonth)
            viewModel.filterListSyntheticByMonth(it.yearMonth.nextMonth)
            viewBinding.filter.setTimeSelected(
                time = null,
                yearMonth = it.yearMonth.nextMonth,
                isSelectedDay = false
            )
        }

    }

    override fun exFivePreviousMonthImage() {
        viewBinding.calendarView.findFirstVisibleMonth()?.let {
            viewBinding.calendarView.smoothScrollToMonth(it.yearMonth.previousMonth)
            viewModel.filterListSyntheticByMonth(it.yearMonth.previousMonth)
            viewBinding.filter.setTimeSelected(
                time = null,
                yearMonth = it.yearMonth.previousMonth,
                isSelectedDay = false
            )
        }
    }

    override fun onClickItemEI(item: FinancialRecord) {
        findNavController().navigateWithAnim(R.id.frg_edit_i_e, bundleOf(
            Constant.KEY_ITEM_IE to item,
            Constant.KEY_LIST_CATEGORY to viewModel.filterListCategory(item.typeExpenseOrIncome)
        ))
    }
}
