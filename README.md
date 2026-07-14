# Mise — Document-Driven Restaurant Control

A no-dependency Java 17 web app for a single-location restaurant. Upload an existing POS sales report, review the extracted rows, then turn it into estimated ingredient usage, food cost, profit, waste, and reorder recommendations.

## Run

```bash
mkdir -p bin
javac -d bin src/*.java
java -cp bin RestaurantManager
```

Alternatively, run `sh run.sh`. The wildcard is intentional: it ensures supporting modules such as `SupplierStore.java` are included in every build.

Then open **http://localhost:8080** in a browser. To use another port, pass it as an argument, for example `java -cp bin RestaurantManager 9090`.

The app supports `.csv` and ordinary `.xlsx` workbooks. Try [examples/sample-sales.csv](examples/sample-sales.csv). It automatically detects common column names and lets the user correct the mapping before confirming.

Purchases use the same review-first workflow. Try [examples/sample-purchase.csv](examples/sample-purchase.csv). Expected columns include delivery date, ingredient, received quantity, unit cost or total, supplier, and invoice number. Confirmation adds stock, updates the latest unit cost, writes purchase transactions, and archives the source document.

Confirmed originals are retained in `data/imports/`. File hashes prevent duplicate imports. `inventory-transactions.csv`, `imported-sales.csv`, and `waste.csv` provide the audit trail; `inventory.csv` holds the current estimate.

The **Suppliers** area keeps vendor contacts, terms, delivery performance, ingredient price history, and invoice balances. Confirmed purchase documents automatically add price records and one payable per supplier invoice. Payments can be recorded as partial or full, with overdue status calculated from the due date.

## Typical shift

1. Export today's sales from the restaurant's POS as CSV or Excel.
2. Open **Sales import**, upload it, check the column mapping and flagged rows, then confirm.
3. Record spoilage or other losses under **Waste**.
4. Receive deliveries under **Purchases**.
5. Use **Today** and **Reports** for food cost, profit, stock alerts, and menu economics.

This MVP intentionally avoids replacing the POS. Its focus is restaurant control from existing documents. The starter menu, recipes, aliases, ingredient costs, and reorder levels live in `RestaurantManager.seed()`.
