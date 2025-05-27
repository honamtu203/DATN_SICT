package com.qltc.finace.data.repository

import com.qltc.finace.data.repository.local.category.CategoryRepository
import com.qltc.finace.data.repository.local.category.CategoryRepositoryImp
import com.qltc.finace.data.repository.local.expense.ExpenseRepository
import com.qltc.finace.data.repository.local.expense.ExpenseRepositoryImp
import com.qltc.finace.data.repository.local.income.InComeRepository
import com.qltc.finace.data.repository.local.income.InComeRepositoryImp
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@dagger.Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun provideExpenseRepository(expenseRepository: ExpenseRepositoryImp) : ExpenseRepository
    @Binds
    abstract fun provideCategoryRepository(categoryRepositoryImp: CategoryRepositoryImp) : CategoryRepository
    @Binds
    abstract fun provideIncomeRepository(incomeRepositoryImp: InComeRepositoryImp) : InComeRepository
}