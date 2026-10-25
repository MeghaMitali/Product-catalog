# Product Catalog Skill

## Objective
Build the Android product catalog so it uses the backend APIs for product data and persists that data in Room for offline and filtered access.

## Required API flow
1. Call the product list endpoint `/products/all` and store all returned products in Room.
2. Use Room as the source of truth for product listing, filtering, and search.
3. On the home screen, call `/products/featured` to load the top 5 featured products.
4. Call `/categories` to load all the available categories and render them in a horizontally swipeable category bar.

## Room data requirements
- Create a Room entity for Product with fields such as:
  - id
  - title/name
  - description
  - price
  - category
  - imageUrl
  - featured
  - currency
  - ratingAverage
  - ratingCount
- Create a DAO to:
  - insert all products
  - fetch all products
  - fetch products by category
  - fetch featured products
  - fetch top featured products for a selected category
  - search products by name or category
- Use a repository layer that first reads from Room, and refreshes from API when needed.

## Home screen requirements
- Show a search bar at the top.
- Place the search icon on the right side of the search bar.
- On click of the search icon, navigate to the Search screen.
- Below the search bar, render a horizontally swipeable category tab bar.
- Each product card in the main list must follow a two-column layout:
  - column 1: product image
  - column 2: product title/description text
  - below the description: price aligned to the bottom/right of the card
- Each product card should display:
  - product image at the top of the card
  - product title and short description under the image
  - price below the description, aligned to the right/bottom inside the card
  - image should be loaded from `imageUrl` using a scalable image loader (e.g. Coil)
  - image should maintain a fixed aspect ratio and rounded corners
  - if the image is missing or fails to load, show a placeholder/fallback image
- When a category is selected:
  - filter Room products by the selected category
  - show featured products for that category first
  - show other products in that category underneath
  - use the `featured` flag to prioritize and group results
- The home screen should show top 5 featured items from `/products/featured` and use Room-filtered data for category-specific display.

## Search screen requirements
- Accept text input.
- Search product records from Room instead of calling the API every keystroke.
- Filter by product name and category.
- Navigate back to the home screen when the user closes the search screen.

## Recommended implementation notes
- Keep API calls limited to sync/fetch operations, not per-item UI rendering.
- Persist the full `/products/all` payload in Room so category and featured filtering is fast and reliable.
- When category is selected, query Room by `category` and sort featured items first.
- If the backend sends a `featured` boolean, use it directly; otherwise derive it from the backend metadata.

## Acceptance criteria
- Product data is fetched from `/products/all` and stored in Room.
- Home page loads featured items from `/products/featured`.
- Categories are loaded from `/categories` and displayed as swipeable tabs.
- Category selection filters the Room data and shows featured products first.
- Search icon navigates to the search screen.
- Products are filtered from Room by category and text query.
- Product cards in the home list use a two-column layout with image, description, and price stacked in the specified order.
- The image appears above the description and the price is positioned below the description within each card.
