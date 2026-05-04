package com.example.appfunctionsdemo

import com.example.appfunctionsdemo.core.ledger.ExpenseLedger

object ExpenseGraph {
    var ledger: ExpenseLedger = ExpenseLedger()
        internal set
}
