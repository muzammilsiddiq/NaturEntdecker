package com.example.naturentdecker.features.tour.list

sealed class TourTab {
    data object All : TourTab()
    data object Top5 : TourTab()
}