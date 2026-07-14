import java.time.LocalDateTime;
import java.util.*;

public class Order {
    public enum Status { OPEN, PAID, CANCELLED }
    public record Line(String itemName, int quantity, double unitPrice) { public double total() { return quantity * unitPrice; } }
    private final int id, table;
    private final LocalDateTime openedAt = LocalDateTime.now();
    private final List<Line> lines = new ArrayList<>();
    private Status status = Status.OPEN;
    public Order(int id, int table) { if (table < 1) throw new IllegalArgumentException("Table must be positive"); this.id=id; this.table=table; }
    public int id() { return id; } public int table() { return table; } public LocalDateTime openedAt() { return openedAt; }
    public List<Line> lines() { return List.copyOf(lines); } public Status status() { return status; }
    public void add(String name, int qty, double price) { if (status != Status.OPEN || qty < 1) throw new IllegalStateException("Order is not open"); lines.add(new Line(name, qty, price)); }
    public double subtotal() { return lines.stream().mapToDouble(Line::total).sum(); }
    public void pay() { if (lines.isEmpty()) throw new IllegalStateException("Cannot pay an empty order"); status=Status.PAID; }
    public void cancel() { status=Status.CANCELLED; }
}
