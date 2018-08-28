package org.samberry.recentorder

data class MutableOrderStatistics(
    private var sum: OrderAmount,
    private var avg: OrderAmount,
    private var max: OrderAmount,
    private var min: OrderAmount,
    private var count: Long
) {
    @Synchronized
    fun addOrder(
        orderAmount: OrderAmount
    ) {
        val newCount = this.count + 1
        val newSum = this.sum.plus(orderAmount)

        this.sum = newSum
        this.avg = newSum.divide(newCount)
        this.max = this.max.orMax(orderAmount)
        this.min = this.min.orMin(orderAmount)
        this.count = newCount
    }

    @Synchronized
    fun toImmutable(): OrderStatistics {
        return OrderStatistics(
            sum = sum,
            avg = avg,
            max = max,
            min = min,
            count = count
        )
    }
}
