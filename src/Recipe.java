import java.util.*;

public class Recipe {
    public record Ingredient(String name, double quantity, String unit) {
        public Ingredient { if (quantity <= 0) throw new IllegalArgumentException("Ingredient quantity must be positive"); }
        @Override public String toString() { return quantity + " " + unit + " " + name; }
    }
    private final String name;
    private final List<Ingredient> ingredients;
    public Recipe(String name, List<Ingredient> ingredients) { this.name = name; this.ingredients = List.copyOf(ingredients); }
    public String getName() { return name; }
    public List<Ingredient> getIngs() { return ingredients; }
    public List<Ingredient> getRecipe(String requested) { return name.equalsIgnoreCase(requested) ? ingredients : List.of(); }
}
