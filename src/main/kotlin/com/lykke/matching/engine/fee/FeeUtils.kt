package com.lykke.matching.engine.fee

import com.lykke.matching.engine.daos.v2.FeeInstruction
import com.lykke.matching.engine.daos.FeeType
import com.lykke.matching.engine.daos.v2.LimitOrderFeeInstruction
import com.lykke.matching.engine.daos.fee.v2.Fee
import com.lykke.matching.engine.daos.fee.v2.NewFeeInstruction
import com.lykke.matching.engine.daos.fee.v2.NewLimitOrderFeeInstruction
import java.math.BigDecimal
import java.util.LinkedList

fun listOfFee(fee: FeeInstruction?, fees: List<NewFeeInstruction>?): List<NewFeeInstruction> {
    val result = LinkedList<NewFeeInstruction>()
    fee?.let { result.add(it.toNewFormat()) }
    fees?.let { result.addAll(it) }
    return result
}

fun listOfLimitOrderFee(fee: LimitOrderFeeInstruction?, fees: List<NewLimitOrderFeeInstruction>?): List<NewLimitOrderFeeInstruction> {
    val result = LinkedList<NewLimitOrderFeeInstruction>()
    fee?.let { result.add(it.toNewFormat()) }
    fees?.let { result.addAll(it) }
    return result
}

fun singleFeeTransfer(feeInstruction: FeeInstruction?, fees: List<Fee>) = if (feeInstruction != null && fees.isNotEmpty()) fees.first().transfer else null

fun checkFee(feeInstruction: FeeInstruction?, feeInstructions: List<NewFeeInstruction>?): Boolean {
    if (feeInstruction != null && feeInstructions?.isNotEmpty() == true) return false
    listOfFee(feeInstruction, feeInstructions).forEach {
        if (!checkFee(it)) {
            return false
        }
    }
    return true
}

private fun checkFee(feeInstruction: FeeInstruction): Boolean {
    if (feeInstruction.type == FeeType.NO_FEE) {
        return true
    }

    if (feeInstruction.sizeType == null ||
            feeInstruction.size != null && feeInstruction.size < BigDecimal.ZERO ||
            feeInstruction.targetClientId == null ||
            feeInstruction.type == FeeType.EXTERNAL_FEE && feeInstruction.sourceClientId == null) {
        return false
    }

    var mandatorySize = true
    if (feeInstruction is LimitOrderFeeInstruction) {
        if (feeInstruction.makerSize == null && feeInstruction.size == null ||
                feeInstruction.makerSize != null && feeInstruction.makerSize < BigDecimal.ZERO) {
            return false
        }
        mandatorySize = false
    }

    if (feeInstruction is NewLimitOrderFeeInstruction) {
        if (feeInstruction.makerSize == null && feeInstruction.size == null ||
                feeInstruction.makerSize != null && feeInstruction.makerSize < BigDecimal.ZERO) {
            return false
        }
        feeInstruction.makerFeeModificator?.let { if (it <= BigDecimal.ZERO) return false }
        mandatorySize = false
    }

    if (mandatorySize && feeInstruction.size == null) {
        return false
    }

    return true
}