import java.io.*;
import java.nio.file.*;
import java.util.*;

/** A single ingredient stock record plus a small CSV-backed inventory store. */
public class Inventory {
    private final String name;
    private double quantity;
    private final String unit;
    private double reorderLevel;
    private double unitCost;
    private String supplier;
    private String storage;

    public Inventory(String name, double quantity, String unit, double reorderLevel) {
        this(name, quantity, unit, reorderLevel, 0, "", "");
    }
    public Inventory(String name, double quantity, String unit, double reorderLevel, double unitCost, String supplier, String storage) {
        if (name == null || name.isBlank() || unit == null || unit.isBlank())
            throw new IllegalArgumentException("Name and unit are required");
        if (quantity < 0 || reorderLevel < 0) throw new IllegalArgumentException("Amounts cannot be negative");
        this.name = name.trim(); this.quantity = quantity; this.unit = unit.trim(); this.reorderLevel = reorderLevel;
        this.unitCost=unitCost; this.supplier=supplier==null?"":supplier; this.storage=storage==null?"":storage;
    }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public double getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(double level) { if (level < 0) throw new IllegalArgumentException("Minimum cannot be negative"); reorderLevel = level; }
    public double getUnitCost() { return unitCost; }
    public String getSupplier() { return supplier; }
    public String getStorage() { return storage; }
    public boolean isLow() { return quantity <= reorderLevel; }
    public void add(double amount) { if (amount <= 0) throw new IllegalArgumentException("Amount must be positive"); quantity += amount; }
    public void use(double amount) { if (amount <= 0 || amount > quantity) throw new IllegalArgumentException("Insufficient " + name); quantity -= amount; }
    public void adjust(double amount) { if (quantity + amount < 0) throw new IllegalArgumentException("Insufficient " + name); quantity += amount; }
    public void setUnitCost(double cost) { if(cost<0)throw new IllegalArgumentException("Cost cannot be negative");unitCost=cost; }
    public void setinvQuantity(int q) { if (q < 0) throw new IllegalArgumentException("Amount cannot be negative"); quantity = q; }
    public String getinvName() { return name; }
    public int getinvQuantity() { return (int) quantity; }
    public String getinvUnits() { return unit; }
    @Override public String toString() { return String.format("%-20s %8.2f %-10s%s", name, quantity, unit, isLow() ? "  LOW" : ""); }

    public static Map<String, Inventory> load(Path file) throws IOException {
        Map<String, Inventory> items = new LinkedHashMap<>();
        if (!Files.exists(file)) return items;
        for (String line : Files.readAllLines(file)) {
            if (line.isBlank() || line.startsWith("name,")) continue;
            String[] p = line.split(",", -1);
            if (p.length >= 4) items.put(key(p[0]), new Inventory(p[0], Double.parseDouble(p[1]), p[2], Double.parseDouble(p[3]), p.length>4?Double.parseDouble(p[4]):0, p.length>5?p[5]:"", p.length>6?p[6]:""));
        }
        return items;
    }
    public static void save(Path file, Collection<Inventory> items) throws IOException {
        Files.createDirectories(file.getParent());
        List<String> lines = new ArrayList<>(List.of("name,quantity,unit,reorderLevel,unitCost,supplier,storage"));
        for (Inventory i : items) lines.add(String.join(",", i.name, Double.toString(i.quantity), i.unit, Double.toString(i.reorderLevel), Double.toString(i.unitCost), i.supplier, i.storage));
        Files.write(file, lines);
    }
    public static String key(String value) { return value.trim().toLowerCase(Locale.ROOT).replace('_', ' '); }
}
