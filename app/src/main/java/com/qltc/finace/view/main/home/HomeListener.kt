package com.qltc.finace.view.main.home

interface HomeListener {
    fun onNotificationClick()
    fun onToggleBalanceClick()
    fun onTabSelected(position: Int)
    fun onViewAllTransactionsClick()
    fun onIncomeCardClick()
    fun onExpenseCardClick()
} 