# Portfolio-Tracker-App
A modern Android application built with Kotlin that allows users to track real-time cryptocurrency prices and monitor their own portfolio's profit and loss. The app fetches live price data from the Binance API and other data from the CoinGeckoAPI. It uses a combination of Firebase Firestore and Room Database to manage user data and store market data locally.

  Features
-Real-time tracking of top 100 cryptocurrencies by market cap
-Profit/loss tracking for each coin based on user's purchase price and quantity
-Live price data fetched from Binance API
-Local caching of market data using Room Database
-User portfolio stored securely in Firebase Firestore
-Built with MVVM architecture
-Asynchronous operations with Coroutines
-Smooth and dynamic UI with RecyclerView

  Tech Stack
-Kotlin
-MVVM Architecture
-Retrofit
-Room Database
-Firebase Firestore
-LiveData & ViewModel
-Coroutines for asynchronous tasks
-RecyclerView for displaying coin list
