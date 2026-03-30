package com.example.naturentdecker.data.usecases

import com.example.naturentdecker.data.repository.TourRepository
import com.example.naturentdecker.data.model.Contact
import com.example.naturentdecker.utils.Result
import javax.inject.Inject

class GetContactUseCase @Inject constructor(
    private val repository: TourRepository
) {
    suspend operator fun invoke(): Result<Contact> = repository.getContact()
}
