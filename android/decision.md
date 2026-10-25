# Decision Log

## Purpose
This file captures the key architecture decisions for the Android Product Catalog app so future changes stay aligned with the data flow and user experience.

## 1. Products are stored in Room as the local source of truth
The app persists product data in the Room database (`ProductDatabase` and `ProductDao`) after sync from the backend. This is intentional because it allows:

- instant local reads for the home screen and category screens
- search and category filtering without repeating network calls
- offline use when the backend is temporarily unavailable
- faster repeated access after the first sync

The repository methods use the Room cache as the primary source for product reads, while `syncProducts()` refreshes the cache from the server when needed.

## 2. Category and search flows rely on Room query methods
Category and search are not implemented as ad hoc in-memory filters. Instead, the app uses dedicated Room DAO queries:

- `ProductDao.getByCategory(category)` for category-based product retrieval
- `ProductDao.search(query)` for text search over product names and categories
- `ProductDao.getCategories()` for category listing

This keeps filtering logic consistent with the persisted product set and avoids mismatches between the UI and cached data.

## 3. Product and category loads are performed in parallel
To reduce home-screen initialization time, the app launches product sync, featured product lookup, and category retrieval concurrently with Kotlin coroutines. This avoids unnecessary sequential waits when the app first loads the home page.

The loading strategy is intentionally parallel in `HomeViewModel.loadProducts()` so the user sees the home view as soon as the combined data set is ready, instead of waiting for each request to finish one-by-one.

## 4. Featured products remain persistent and are derived from the product cache
Featured products are tracked using the `featured` flag on each stored product entity. This keeps the chosen products stable across app restarts and ensures the home carousel reflects the persisted user selection rather than transient memory state.

## 5. Fallback behavior keeps the app resilient
If the backend sync fails, the app falls back to the Room cache instead of crashing or showing an empty state immediately. This ensures the app remains usable while still exposing an error message for the failed refresh.

## 6. Code alignment check
The current code reflects these decisions:

- Room database and DAO are present in `android/app/src/main/java/com/productcatalog/app/data/local/`
- `ProductRepositoryImpl` reads cached products and categories from Room, then refreshes from the backend as needed
- `ProductDao` exposes `getByCategory(...)` and `search(...)`
- `HomeViewModel.loadProducts()` now executes the product and category fetches in parallel using `async { ... }` coroutines

This ensures the decision log stays consistent with the actual implementation and helps guide future feature work without drifting away from the architecture.
