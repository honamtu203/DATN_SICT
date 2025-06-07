package com.qltc.finace.view.main.home

interface HomeListener {
    fun onToggleBalanceClick()
    fun onNotificationClick()
    fun onIncomeCardClick()
    fun onExpenseCardClick()
    fun onViewAllTransactionsClick()
    fun onTabSelected(position: Int)
} 