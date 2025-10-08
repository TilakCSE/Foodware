package com.example.recipietracker;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PantryOptimizerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry_optimizer);

        EditText etPantry = findViewById(R.id.etPantry);
        Button btnAdd = findViewById(R.id.btnAddPantry);
        TextView txtPantry = findViewById(R.id.txtPantry);
        Button btnOptimize = findViewById(R.id.btnOptimize);
        TextView txtResults = findViewById(R.id.txtResults);

        DBHelper db = new DBHelper(this);

        btnAdd.setOnClickListener(v -> {
            String line = etPantry.getText().toString().trim();
            if (TextUtils.isEmpty(line)) {
                Toast.makeText(this, "Please enter an ingredient", Toast.LENGTH_SHORT).show();
                return;
            }
            PantryItem item = parsePantryLine(line);
            if (item != null) {
                db.addPantryItem(item);
                etPantry.setText("");
                renderPantry(db.getPantry(), txtPantry);
                Toast.makeText(this, "Added: " + item.getName(), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Invalid format. Try: '2 eggs' or '200g paneer'", Toast.LENGTH_SHORT).show();
            }
        });

        renderPantry(db.getPantry(), txtPantry);

        btnOptimize.setOnClickListener(v -> {
            List<Recipe> all = db.getAllRecipes();
            List<PantryItem> pantry = db.getPantry();
            
            if (all.isEmpty()) {
                txtResults.setText("No recipes found. Please add some recipes first.");
                return;
            }
            
            if (pantry.isEmpty()) {
                txtResults.setText("No pantry items found. Please add some ingredients first.");
                return;
            }
            
            OptimizationResult result = findBestPlan(all, pantry);
            txtResults.setText(result.toDisplayString());
        });
    }

    private void renderPantry(List<PantryItem> items, TextView out) {
        if (items.isEmpty()) {
            out.setText("No items in pantry yet");
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        for (PantryItem p : items) {
            sb.append("• ").append(capitalizeFirst(p.getName())).append(" ");
            if (p.getQuantity() > 0) {
                sb.append(p.getQuantity());
                if (p.getUnit() != null && !p.getUnit().isEmpty()) {
                    sb.append(" ").append(p.getUnit());
                }
            }
            sb.append("\n");
        }
        out.setText(sb.toString());
    }
    
    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private PantryItem parsePantryLine(String line) {
        // Enhanced parsing: e.g., "2 eggs", "200g paneer", "1 cup rice", "tomato"
        try {
            line = line.trim().toLowerCase();
            PantryItem p = new PantryItem();
            
            // Check if it starts with a number
            if (Character.isDigit(line.charAt(0))) {
                // Find where the number ends
                int numberEnd = 0;
                while (numberEnd < line.length() && 
                       (Character.isDigit(line.charAt(numberEnd)) || line.charAt(numberEnd) == '.')) {
                    numberEnd++;
                }
                
                double quantity = Double.parseDouble(line.substring(0, numberEnd));
                p.setQuantity(quantity);
                
                String remaining = line.substring(numberEnd).trim();
                
                // Check for unit (g, kg, cup, tsp, tbsp, etc.)
                String[] commonUnits = {"g", "kg", "cup", "cups", "tsp", "tbsp", "ml", "l", "oz", "lb", "pound", "pounds"};
                String unit = "";
                String name = remaining;
                
                for (String u : commonUnits) {
                    if (remaining.startsWith(u + " ")) {
                        unit = u;
                        name = remaining.substring(u.length()).trim();
                        break;
                    }
                }
                
                p.setUnit(unit);
                p.setName(name);
            } else {
                // No quantity specified, assume 1
                p.setName(line);
                p.setQuantity(1);
                p.setUnit("");
            }
            
            return p;
        } catch (Exception e) { 
            return null; 
        }
    }

    static class OptimizationResult {
        List<Recipe> chosen = new ArrayList<>();
        Set<String> shoppingList = new HashSet<>();
        int score;
        int pantryUsage;
        int totalIngredients;
        
        String toDisplayString() {
            StringBuilder sb = new StringBuilder();
            
            if (chosen.isEmpty()) {
                sb.append("❌ No suitable recipes found with your current pantry items.\n\n");
                sb.append("Try adding more ingredients or check if your recipes have ingredient lists.");
                return sb.toString();
            }
            
            sb.append("🎯 OPTIMIZED MEAL PLAN\n");
            sb.append("Score: ").append(score).append(" | Pantry Usage: ").append(pantryUsage).append("/").append(totalIngredients).append(" ingredients\n\n");
            
            sb.append("📋 RECOMMENDED RECIPES:\n");
            for (int i = 0; i < chosen.size(); i++) {
                Recipe r = chosen.get(i);
                sb.append((i + 1)).append(". ").append(r.getTitle()).append("\n");
                if (r.getCuisine() != null && !r.getCuisine().isEmpty()) {
                    sb.append("   Cuisine: ").append(r.getCuisine()).append("\n");
                }
                if (r.getPrepMinutes() > 0) {
                    sb.append("   Prep: ").append(r.getPrepMinutes()).append(" min");
                }
                if (r.getCookMinutes() > 0) {
                    sb.append(" | Cook: ").append(r.getCookMinutes()).append(" min");
                }
                sb.append("\n\n");
            }
            
            if (!shoppingList.isEmpty()) {
                sb.append("🛒 SHOPPING LIST:\n");
                for (String s : shoppingList) {
                    sb.append("• ").append(capitalizeFirst(s)).append("\n");
                }
            } else {
                sb.append("✅ You have all ingredients needed! No shopping required.\n");
            }
            
            return sb.toString();
        }
        
        private String capitalizeFirst(String str) {
            if (str == null || str.isEmpty()) return str;
            return str.substring(0, 1).toUpperCase() + str.substring(1);
        }
    }

    private OptimizationResult findBestPlan(List<Recipe> recipes, List<PantryItem> pantryItems) {
        Map<String, Double> pantryMap = new HashMap<>();
        for (PantryItem p : pantryItems) {
            pantryMap.put(normalize(p.getName()), pantryMap.getOrDefault(normalize(p.getName()), 0.0) + Math.max(1.0, p.getQuantity()));
        }

        // Preprocess recipe ingredients as normalized tokens list
        List<String[]> recipeTokens = new ArrayList<>();
        for (Recipe r : recipes) {
            String[] tokens = tokenizeIngredients(r.getIngredients());
            recipeTokens.add(tokens);
        }

        OptimizationResult best = new OptimizationResult();
        // Evaluate all pairs and triples (bounded for small lists)
        int n = recipes.size();
        for (int i = 0; i < n; i++) {
            evaluateSet(best, pantryMap, recipes, recipeTokens, i);
            for (int j = i + 1; j < n; j++) {
                evaluateSet(best, pantryMap, recipes, recipeTokens, i, j);
                for (int k = j + 1; k < n; k++) {
                    evaluateSet(best, pantryMap, recipes, recipeTokens, i, j, k);
                }
            }
        }
        return best;
    }

    private void evaluateSet(OptimizationResult best, Map<String, Double> pantry, List<Recipe> recipes, List<String[]> tokens, int... idx) {
        Set<String> used = new HashSet<>();
        Set<String> missing = new HashSet<>();
        int coverage = 0;
        int pantryUsage = 0;
        int totalIngredients = 0;
        
        for (int id : idx) {
            String[] ing = tokens.get(id);
            for (String t : ing) {
                if (t.isEmpty()) continue;
                if (used.contains(t)) continue; // avoid double counting same ingredient multiple times
                
                totalIngredients++;
                if (pantry.getOrDefault(t, 0.0) > 0) {
                    coverage += 2; // reward covered ingredient
                    pantryUsage++;
                } else {
                    missing.add(t);
                    coverage -= 1; // penalty
                }
                used.add(t);
            }
        }
        
        int score = coverage - (missing.size());
        if (best.chosen.isEmpty() || score > best.score) {
            best.score = score;
            best.chosen.clear();
            for (int id : idx) best.chosen.add(recipes.get(id));
            best.shoppingList = missing;
            best.pantryUsage = pantryUsage;
            best.totalIngredients = totalIngredients;
        }
    }

    private String[] tokenizeIngredients(String ingredients) {
        if (ingredients == null) return new String[0];
        String[] raw = ingredients.toLowerCase().split("[,;]");
        List<String> tokens = new ArrayList<>();
        
        for (String ingredient : raw) {
            String normalized = normalize(ingredient);
            if (!normalized.isEmpty()) {
                // Split compound ingredients (e.g., "salt and pepper" -> ["salt", "pepper"])
                String[] parts = normalized.split("\\s+(and|&|with|\\+)\\s+");
                for (String part : parts) {
                    String clean = part.trim();
                    if (!clean.isEmpty()) {
                        tokens.add(clean);
                    }
                }
            }
        }
        
        return tokens.toArray(new String[0]);
    }

    private String normalize(String s) {
        if (s == null) return "";
        
        // Remove common prefixes and suffixes
        s = s.trim().toLowerCase();
        s = s.replaceAll("^(a|an|the)\\s+", ""); // Remove articles
        s = s.replaceAll("\\s+(chopped|diced|sliced|grated|minced|fresh|dried|frozen|canned|optional)\\s*$", ""); // Remove cooking methods
        s = s.replaceAll("\\s+to taste\\s*$", ""); // Remove "to taste"
        s = s.replaceAll("\\s+as needed\\s*$", ""); // Remove "as needed"
        
        // Normalize common variations
        s = s.replaceAll("\\b(eggs?)\\b", "egg");
        s = s.replaceAll("\\b(tomatoes?)\\b", "tomato");
        s = s.replaceAll("\\b(onions?)\\b", "onion");
        s = s.replaceAll("\\b(garlics?)\\b", "garlic");
        s = s.replaceAll("\\b(potatoes?)\\b", "potato");
        s = s.replaceAll("\\b(carrots?)\\b", "carrot");
        s = s.replaceAll("\\b(peppers?)\\b", "pepper");
        s = s.replaceAll("\\b(chilies?|chiles?)\\b", "chili");
        
        // Clean up the string
        s = s.replaceAll("[^a-z\\s]", " "); // Keep only letters and spaces
        s = s.replaceAll("\\s+", " ").trim(); // Normalize whitespace
        
        return s;
    }
}


