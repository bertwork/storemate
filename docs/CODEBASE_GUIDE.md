# StoreMate Codebase Guide

## 1. Application Overview

StoreMate is a single-user point-of-sale desktop app. It has two main screens:

- **Order:** browse products, build an order, receive payment, and create a receipt
- **Analytics:** inspect sales for a date range

The application is divided into:

- domain objects in `model`
- SQL repositories in `model.repository`
- controllers in `controller`
- Swing UI in `view`
- reusable helpers in `util`

The source level is Java 21 and the project uses the Swing Event Dispatch Thread for UI updates.

## 2. Startup and Composition

### `app.App`

`App` is the entry point.

- `main()` initializes global UI configuration.
- Startup failures are handled by `StartupErrorHandler`.
- `start()` starts `StartupLoader` on the Event Dispatch Thread.
- `openMainWindow()` is the composition root that creates the repositories, controllers, panels, and main frame.

### `app.StartupLoader`

`StartupLoader`:

- Requires calls on the EDT
- Creates and shows `SplashWindow`
- Uses `SwingWorker<List<Product>, String>` to load products off the EDT
- Keeps the splash visible for at least 1.5 seconds
- Calls the success callback or reports startup failure

### `util.AppInitializer`

Initializes Swing rendering and FlatLaf:

- Enables system antialiasing
- Loads Nunito regular and bold fonts
- Registers the custom FlatLaf properties under `resources/style`
- Calls `FlatLightLaf.setup()`

### `view.SplashWindow`

Displays the StoreMate logo and loading status. It includes a progress indicator-like visual and can update its status message.

## 3. Model Package

### `model.Product`

A Java record containing:

- `id`
- `name`
- `category`
- `price`
- `imagePath`

Records provide immutable data fields and generated `equals`, `hashCode`, and `toString()` methods. `Product` is also a key in the order `Map`, so equal records represent the same product.

### `model.Order`

The in-memory checkout.

Important behavior:

- `LinkedHashMap<Product, Integer>` preserves insertion order
- `add()` starts a new product at quantity 1
- `setQuantity()` removes items at quantity zero or below
- `subtotal()` sums unit price × quantity
- `total()` equals the subtotal
- `copy()` creates an independent snapshot for asynchronous saving
- `clear()` resets items
- `items()` returns an unmodifiable view

### `model.Receipt`

An immutable record describing a successfully saved order:

- order ID
- placement time
- item snapshot
- subtotal
- total
- received amount
- change

`Receipt.of(...)` copies the order items and calculates receipt values.

### `model.AnalyticsReport`

An immutable report containing:

- `Summary`
- sales points over time
- category totals
- top product totals

Nested records:

- `Summary.averageOrder()` divides revenue by order count and rounds to two decimal places
- `Point` is one chart bucket
- `CategorySales` is a category and revenue pair
- `ProductSales` is a product, units, and revenue

## 4. Repository Package

### `model.repository.Database`

The JDBC connection factory.

- Reads `/resources/db.properties`
- Falls back to Laragon-style local defaults
- Sets a 3-second connection timeout
- `connect()` creates a `Connection`
- `isReachable()` verifies database connectivity

Callers use try-with-resources to close connections and statements.

### `model.repository.ProductRepository`

Executes `SELECT` to join products and categories, ordered by category and product ID.

The result set is converted into `Product` records.

### `model.repository.OrderRepository`

Validates and saves an order using an explicit transaction:

1. Validate order, payment, products, and quantities
2. Open a connection
3. Set `autoCommit(false)`
4. Insert the order and generated ID
5. Insert every item as a JDBC batch
6. Commit
7. Roll back on SQL or runtime failure

The generated order ID is returned by `Statement.RETURN_GENERATED_KEYS`.

This transaction prevents an order header from being committed without its order items.

### `model.repository.AnalyticsRepository`

Loads four groups of report data with the same inclusive/exclusive date range:

- `created_at >= start`
- `created_at < end`
- `end` is the day after the selected end date

Queries calculate:

- order count and revenue
- quantity of items sold
- revenue by category
- top 10 products by units sold

Sales-chart granularity:

| Date span | Grouping |
|---|---|
| One day | Hourly |
| 2–92 days | Daily |
| More than 92 days | Monthly |

It fills missing chart buckets with zero so empty periods remain visible.

## 5. Controller Package

### `controller.OrderController`

The business controller for checkout.

Responsibilities:

- Implement `OrderPanel.Listener`
- Maintain the current `Order`
- Add, change, and remove items
- Refresh item and summary views
- Validate payment
- Create an order snapshot
- Save through `OrderRepository`
- Use `SwingWorker` to keep the UI responsive
- Clear the order only after save success
- Keep the order after save failure
- Prevent changes while saving

### `controller.AnalyticsController`

Responsibilities:

- Implement `AnalyticsPanel.Listener`
- Validate date ranges
- Number each refresh request
- Ignore older request results
- Use `SwingWorker` for database queries
- Set loading state
- Send reports or errors to the view

### `controller.ConnectionStatusController`

- Uses a Swing `Timer` every 15 seconds
- Checks MySQL through `Database.isReachable()`
- Uses `SwingWorker` for the check
- Updates `FooterBar`

## 6. Main View Package

### `view.Screen`

A two-value enum:

- `ORDER` -> label `Order`
- `ANALYTICS` -> label `Analytics`

Its `toString()` supplies a friendly dropdown label.

### `view.MainFrame`

Creates the 1280 × 820 application window.

- Displays `HeaderBar`
- Uses `CardLayout` to switch between order and analytics panels
- Displays `FooterBar`
- Loads the application icon
- Moves search focus to the order screen on opening

### `view.SplashWindow`

The startup window shown while product data is loaded.

## 7. View Panels

### `view.panels.OrderPanel`

Combines `ProductsPanel` and `CheckoutPanel`.

It defines a listener interface with:

- `onAddProduct`
- `onQuantityChange`
- `onClear`
- `onPlaceOrder`

The panel does not save orders or access repositories. Controller callbacks perform those actions.

### `view.panels.ProductsPanel`

- Builds one `ProductCard` per database product
- Creates category tabs from database category values
- Filters by category and case-insensitive product-name search
- Displays a no-results state
- Rebuilds the grid when filters change
- Pressing Enter adds the product only when exactly one result matches

### `view.panels.CheckoutPanel`

The presentation and interaction coordinator for checkout.

It displays:

- checkout items
- subtotal
- total
- amount received
- change or shortfall
- quick-cash values
- clear and place-order controls

Payment behavior:

- `Exact` sets the received amount to the total
- Presets set ₱100, ₱200, ₱500, or ₱1000
- Accepted payment text supports digits, commas, peso signs, and up to two decimals
- Invalid text is treated as zero
- Place Order is enabled only for a non-empty order, sufficient payment, and no save operation

One item can be cleared immediately. Two or more items require a confirmation dialog.

### `view.panels.AnalyticsPanel`

- Defaults to the last 30 days
- Provides Today, Last 7 days, and Last 30 days presets
- Keeps start and end dates in chronological order
- Sends refresh requests when dates change
- Sends the report to summary, sales, category, and top-product components

## 8. View Components

### Products

- `CategoryTabs`: category tab container
- `TabButton`: toggle button used for category selection
- `SearchBar`: search input with Enter callback
- `ProductCard`: image, name, category, price, hover, and added state

### Checkout

- `CheckoutItemRow`: thumbnail, name, price, quantity control, remove button, line total
- `QuantityStepper`: minus/plus quantity buttons
- `TrashIcon`: custom Swing trash-can icon

### Analytics

- `AnalyticsSummaryPanel`: revenue, orders, average order, and items sold cards
- `StatCard`: reusable summary card
- `SalesChartPanel`: JFreeChart vertical bar chart
- `CategoryChartPanel`: JFreeChart pie chart
- `TopProductsPanel`: ranked, read-only product table
- `SectionCard`: rounded container for an analytics section

### Common

- `AppButton`: standard, tab, icon, and primary button styles
- `FooterBar`: version label and database status
- `HeaderBar`: logo, screen menu, live clock, and F1/F2 navigation
- `DotIcon`: custom status dot
- `ProductImage`: rounded image rendering and faded added state
- `RoundedPanel`: reusable rounded background/border component

## 9. Dialog

### `view.dialogs.ReceiptDialog`

An application-modal receipt shown after a successful transaction.

- Shows generated order number and placement time
- Lists purchased items and quantities
- Shows subtotal, total, received amount, and change
- Supports Enter and Escape for closing

## 10. Utility Package

### `util.Theme`

Central design constants:

- navy, accent, muted, border, success, danger
- application and footer backgrounds
- card and placeholder colors

It also provides font helper methods.

### `util.Money`

Formats `BigDecimal` amounts in US grouping and peso notation:

- Whole values use zero decimal places
- Fractional values use two decimal places

### `util.ImageLoader`

- Loads product-specific and category fallback images
- Supports PNG, JPG, and JPEG
- Shrinks images wider than 480 pixels
- Caches loaded images
- Returns `null` when no image exists

### `util.FontLoader`

Loads Nunito regular and bold TTF files into `GraphicsEnvironment`.

### `util.AppInfo`

Stores application name and version:

- `StoreMate`
- `1.0`

### `util.StartupErrorHandler`

Shows startup failures. Database/SQL failures include a Laragon/MySQL hint.

## 11. OOP Concepts in StoreMate

### Encapsulation

Classes keep state private and expose methods. Examples:

- `Order` controls checkout item state
- `CheckoutPanel` controls UI fields and exposes simple update methods
- `AnalyticsController` owns the latest request identifier

### Abstraction

Views communicate through small interfaces:

- `OrderPanel.Listener`
- `AnalyticsPanel.Listener`

The UI does not need SQL implementation details.

### Composition

`App.openMainWindow()` composes panels, controllers, and repositories.

Panels compose smaller components:

- `OrderPanel` contains products and checkout panels
- `MainFrame` contains header, content, and footer
- `CheckoutPanel` contains item rows and payment controls

### Polymorphism

Swing components inherit shared classes:

- `AppButton` extends `JButton`
- `RoundedPanel` extends `JPanel`
- `ProductCard` extends `RoundedPanel`
- `ProductCard` and checkout rows extend Swing components

`DotIcon` implements `Icon`.

### Records

Immutable value objects include:

- `Product`
- `Receipt`
- `AnalyticsReport`
- nested `AnalyticsReport` records

### Events and callbacks

The project uses:

- `Runnable`
- `Consumer`
- `Supplier`
- `BiConsumer`
- `IntConsumer`
- listener interfaces
- Swing action listeners

These callbacks connect UI interactions to application behavior without tight coupling.

### Separation of concerns

- `model.Order` knows no Swing
- `AnalyticsReport` knows no Swing
- presentation panels do not execute SQL
- repositories know no Swing
- controllers coordinate background work and UI updates

## 12. Threading and Concurrency

All Swing component creation and updates are performed on the Swing EDT.

Database work uses `SwingWorker`:

- `doInBackground()` performs JDBC or startup loading
- `done()` runs on the EDT
- `get()` retrieves the result or exception
- `process()` receives startup progress messages

The connection status check uses a Swing `Timer` plus `SwingWorker`.

This separation keeps slow or unavailable database operations from freezing the UI.

## 13. Database Transactions

Order saving is atomic:

```text
begin transaction
  insert orders row
  obtain generated order ID
  insert each orders_item row
commit
```

If item insertion fails, the order header is rolled back. If rollback itself fails, the rollback exception is attached as suppressed data to the original exception.

## 14. Error Handling

- Startup SQL failures show a database-running hint
- Order save failures preserve the order and display an error dialog
- Analytics failures display a detailed error dialog
- Interrupted workers restore the thread interrupt flag
- Connection checks fall back to the offline state
- Invalid payment text is parsed as zero

## 15. Resources

The source resources are in `src/resources/`:

- `db.properties`
- Nunito font TTF files
- category and logo PNG images
- FlatLaf property overrides

Image fallback behavior:

1. Product-specific image path
2. Category image
3. Blue placeholder rendering

## 16. Source File Inventory

### `app`

- `App.java`
- `StartupLoader.java`

### `controller`

- `AnalyticsController.java`
- `ConnectionStatusController.java`
- `OrderController.java`

### `model`

- `AnalyticsReport.java`
- `Order.java`
- `Product.java`
- `Receipt.java`

### `model/repository`

- `AnalyticsRepository.java`
- `Database.java`
- `OrderRepository.java`
- `ProductRepository.java`

### `util`

- `AppInfo.java`
- `AppInitializer.java`
- `FontLoader.java`
- `ImageLoader.java`
- `Money.java`
- `StartupErrorHandler.java`
- `Theme.java`

### `view`

- `MainFrame.java`
- `Screen.java`
- `SplashWindow.java`
- `view.components.RoundedPanel.java`

### `view.components.analytics`

- `AnalyticsSummaryPanel.java`
- `CategoryChartPanel.java`
- `SalesChartPanel.java`
- `SectionCard.java`
- `StatCard.java`
- `TopProductsPanel.java`

### `view.components.checkout`

- `CheckoutItemRow.java`
- `QuantityStepper.java`
- `TrashIcon.java`

### `view.components.common`

- `AppButton.java`
- `DotIcon.java`
- `FooterBar.java`
- `HeaderBar.java`
- `ProductImage.java`

### `view.components.products`

- `CategoryTabs.java`
- `ProductCard.java`
- `SearchBar.java`
- `TabButton.java`

### `view.dialogs`

- `ReceiptDialog.java`

### `view.panels`

- `AnalyticsPanel.java`
- `CheckoutPanel.java`
- `OrderPanel.java`
- `ProductsPanel.java`
