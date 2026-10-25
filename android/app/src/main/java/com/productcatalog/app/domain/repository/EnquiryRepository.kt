package com.productcatalog.app.domain.repository

import com.productcatalog.app.domain.model.EnquiryRequest

interface EnquiryRepository {
    suspend fun submitEnquiry(request: EnquiryRequest): Result<Unit>
}
