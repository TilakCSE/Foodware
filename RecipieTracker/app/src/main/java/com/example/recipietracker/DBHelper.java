package com.example.recipietracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "recipes.db";
    public static final int DATABASE_VERSION = 3;

    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_ID = "id";
    public static final String COL_TITLE = "title";
    public static final String COL_INGREDIENTS = "ingredients";
    public static final String COL_STEPS = "steps";
    public static final String COL_CUISINE = "cuisine";
    public static final String COL_FAVORITE = "favorite";
    public static final String COL_IMAGE_URI = "image_uri";
    public static final String COL_PREP_MIN = "prep_minutes";
    public static final String COL_COOK_MIN = "cook_minutes";
    public static final String COL_SERVINGS = "servings";
    public static final String COL_DIFFICULTY = "difficulty";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create = "CREATE TABLE IF NOT EXISTS " + TABLE_RECIPES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT NOT NULL, " +
                COL_INGREDIENTS + " TEXT NOT NULL, " +
                COL_STEPS + " TEXT NOT NULL, " +
                COL_CUISINE + " TEXT, " +
                COL_FAVORITE + " INTEGER DEFAULT 0, " +
                COL_IMAGE_URI + " TEXT, " +
                COL_PREP_MIN + " INTEGER DEFAULT 0, " +
                COL_COOK_MIN + " INTEGER DEFAULT 0, " +
                COL_SERVINGS + " INTEGER DEFAULT 1, " +
                COL_DIFFICULTY + " TEXT DEFAULT 'Easy'" +
                ")";
        db.execSQL(create);

        insertSampleData(db);

        String pantry = "CREATE TABLE IF NOT EXISTS pantry (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "quantity REAL DEFAULT 0, " +
                "unit TEXT, " +
                "expiry_millis INTEGER DEFAULT 0)";
        db.execSQL(pantry);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            try { db.execSQL("ALTER TABLE " + TABLE_RECIPES + " ADD COLUMN " + COL_IMAGE_URI + " TEXT"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE " + TABLE_RECIPES + " ADD COLUMN " + COL_PREP_MIN + " INTEGER DEFAULT 0"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE " + TABLE_RECIPES + " ADD COLUMN " + COL_COOK_MIN + " INTEGER DEFAULT 0"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE " + TABLE_RECIPES + " ADD COLUMN " + COL_SERVINGS + " INTEGER DEFAULT 1"); } catch (Exception ignored) {}
            try { db.execSQL("ALTER TABLE " + TABLE_RECIPES + " ADD COLUMN " + COL_DIFFICULTY + " TEXT DEFAULT 'Easy'"); } catch (Exception ignored) {}
        }
        if (oldVersion < 3) {
            String pantry = "CREATE TABLE IF NOT EXISTS pantry (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL, " +
                    "quantity REAL DEFAULT 0, " +
                    "unit TEXT, " +
                    "expiry_millis INTEGER DEFAULT 0)";
            db.execSQL(pantry);
        }
    }

    // Pantry CRUD
    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", item.getName());
        v.put("quantity", item.getQuantity());
        v.put("unit", item.getUnit());
        v.put("expiry_millis", item.getExpiryMillis());
        return db.insert("pantry", null, v);
    }

    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", item.getName());
        v.put("quantity", item.getQuantity());
        v.put("unit", item.getUnit());
        v.put("expiry_millis", item.getExpiryMillis());
        return db.update("pantry", v, "id=?", new String[]{String.valueOf(item.getId())});
    }

    public int deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete("pantry", "id=?", new String[]{String.valueOf(id)});
    }

    public List<PantryItem> getPantry() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("pantry", null, null, null, null, null, "expiry_millis ASC");
        List<PantryItem> list = new ArrayList<>();
        if (c == null) return list;
        try (c) {
            while (c.moveToNext()) {
                PantryItem p = new PantryItem();
                p.setId(c.getLong(c.getColumnIndexOrThrow("id")));
                p.setName(c.getString(c.getColumnIndexOrThrow("name")));
                p.setQuantity(c.getDouble(c.getColumnIndexOrThrow("quantity")));
                p.setUnit(c.getString(c.getColumnIndexOrThrow("unit")));
                p.setExpiryMillis(c.getLong(c.getColumnIndexOrThrow("expiry_millis")));
                list.add(p);
            }
        }
        return list;
    }

    private void insertSampleData(SQLiteDatabase db) {
        insertRecipe(db, "Margherita Pizza", "flour, yeast, tomato, mozzarella, basil", "Make dough; add toppings; bake.", "Italian", 0);
        insertRecipe(db, "Chana Masala", "chickpeas, onion, tomato, garam masala", "Saute onions, add spices and tomatoes; simmer with chickpeas.", "Indian", 1);
        insertRecipe(db, "Fried Rice", "rice, egg, soy sauce, carrots, peas, spring onion", "Scramble egg; stir-fry veg; add rice and sauce.", "Chinese", 0);
        insertRecipe(db, "Pasta Alfredo", "pasta, butter, cream, parmesan, garlic", "Boil pasta; make sauce with butter, cream, parmesan.", "Italian", 0);
        insertRecipe(db, "Paneer Tikka", "paneer, yogurt, chili, turmeric, garam masala", "Marinate paneer; grill or bake until charred.", "Indian", 0);
        insertRecipe(db, "Tomato Soup", "tomato, onion, garlic, butter, cream", "Saute; simmer; blend; finish with cream.", "Continental", 0);
        insertRecipe(db, "Veg Sandwich", "bread, cucumber, tomato, onion, cheese", "Layer vegetables and cheese between buttered bread.", "Snacks", 0);
        insertRecipe(db, "Pancakes", "flour, milk, egg, sugar, baking powder", "Mix batter; cook on griddle; serve with syrup.", "American", 0);
        insertRecipe(db, "Hakka Noodles", "noodles, bell pepper, cabbage, soy sauce, vinegar", "Stir-fry veggies; toss with boiled noodles and sauces.", "Chinese", 0);
    }

    private void insertRecipe(SQLiteDatabase db, String title, String ingredients, String steps, String cuisine, int favorite) {
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, title);
        values.put(COL_INGREDIENTS, ingredients);
        values.put(COL_STEPS, steps);
        values.put(COL_CUISINE, cuisine);
        values.put(COL_FAVORITE, favorite);
        values.put(COL_IMAGE_URI, (String) null);
        values.put(COL_PREP_MIN, 15);
        values.put(COL_COOK_MIN, 20);
        values.put(COL_SERVINGS, 2);
        values.put(COL_DIFFICULTY, "Easy");
        db.insert(TABLE_RECIPES, null, values);
    }

    public long addRecipe(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, recipe.getTitle());
        values.put(COL_INGREDIENTS, recipe.getIngredients());
        values.put(COL_STEPS, recipe.getSteps());
        values.put(COL_CUISINE, recipe.getCuisine());
        values.put(COL_FAVORITE, recipe.isFavorite() ? 1 : 0);
        values.put(COL_IMAGE_URI, recipe.getImageUri());
        values.put(COL_PREP_MIN, recipe.getPrepMinutes());
        values.put(COL_COOK_MIN, recipe.getCookMinutes());
        values.put(COL_SERVINGS, recipe.getServings());
        values.put(COL_DIFFICULTY, recipe.getDifficulty());
        long id = db.insert(TABLE_RECIPES, null, values);
        recipe.setId(id);
        return id;
    }

    public int updateRecipe(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITLE, recipe.getTitle());
        values.put(COL_INGREDIENTS, recipe.getIngredients());
        values.put(COL_STEPS, recipe.getSteps());
        values.put(COL_CUISINE, recipe.getCuisine());
        values.put(COL_FAVORITE, recipe.isFavorite() ? 1 : 0);
        values.put(COL_IMAGE_URI, recipe.getImageUri());
        values.put(COL_PREP_MIN, recipe.getPrepMinutes());
        values.put(COL_COOK_MIN, recipe.getCookMinutes());
        values.put(COL_SERVINGS, recipe.getServings());
        values.put(COL_DIFFICULTY, recipe.getDifficulty());
        return db.update(TABLE_RECIPES, values, COL_ID + "=?", new String[]{String.valueOf(recipe.getId())});
    }

    public int deleteRecipe(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_RECIPES, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public List<Recipe> getAllRecipes() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, null, null, null, null, COL_TITLE + " COLLATE NOCASE ASC");
        return mapCursorToRecipes(cursor);
    }

    public List<Recipe> searchRecipes(String query) {
        SQLiteDatabase db = getReadableDatabase();
        String like = "%" + query + "%";
        Cursor cursor = db.query(TABLE_RECIPES, null,
                COL_TITLE + " LIKE ? OR " + COL_INGREDIENTS + " LIKE ?",
                new String[]{like, like}, null, null, COL_TITLE + " COLLATE NOCASE ASC");
        return mapCursorToRecipes(cursor);
    }

    public List<Recipe> getFavorites() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, COL_FAVORITE + "=1", null, null, null, COL_TITLE + " COLLATE NOCASE ASC");
        return mapCursorToRecipes(cursor);
    }

    public List<Recipe> filterByCuisine(String cuisine) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, COL_CUISINE + "=?", new String[]{cuisine}, null, null, COL_TITLE + " COLLATE NOCASE ASC");
        return mapCursorToRecipes(cursor);
    }

    public List<Recipe> suggestByIngredients(String commaSeparatedIngredients) {
        if (commaSeparatedIngredients == null || commaSeparatedIngredients.trim().isEmpty()) {
            return getAllRecipes();
        }
        String[] parts = commaSeparatedIngredients.toLowerCase().split(",");
        StringBuilder selection = new StringBuilder();
        List<String> args = new ArrayList<>();
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i].trim();
            if (p.isEmpty()) continue;
            if (selection.length() > 0) selection.append(" AND ");
            selection.append(COL_INGREDIENTS).append(" LIKE ?");
            args.add("%" + p + "%");
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_RECIPES, null, selection.toString(), args.toArray(new String[0]), null, null, COL_TITLE + " COLLATE NOCASE ASC");
        return mapCursorToRecipes(cursor);
    }

    public void toggleFavorite(long id, boolean favorite) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FAVORITE, favorite ? 1 : 0);
        db.update(TABLE_RECIPES, values, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    private List<Recipe> mapCursorToRecipes(Cursor cursor) {
        List<Recipe> list = new ArrayList<>();
        if (cursor == null) return list;
        try (cursor) {
            while (cursor.moveToNext()) {
                Recipe r = new Recipe();
                r.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)));
                r.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)));
                r.setIngredients(cursor.getString(cursor.getColumnIndexOrThrow(COL_INGREDIENTS)));
                r.setSteps(cursor.getString(cursor.getColumnIndexOrThrow(COL_STEPS)));
                r.setCuisine(cursor.getString(cursor.getColumnIndexOrThrow(COL_CUISINE)));
                r.setFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(COL_FAVORITE)) == 1);
                int idxImg = cursor.getColumnIndex(COL_IMAGE_URI);
                if (idxImg != -1) r.setImageUri(cursor.getString(idxImg));
                int idxPrep = cursor.getColumnIndex(COL_PREP_MIN);
                if (idxPrep != -1) r.setPrepMinutes(cursor.getInt(idxPrep));
                int idxCook = cursor.getColumnIndex(COL_COOK_MIN);
                if (idxCook != -1) r.setCookMinutes(cursor.getInt(idxCook));
                int idxServ = cursor.getColumnIndex(COL_SERVINGS);
                if (idxServ != -1) r.setServings(cursor.getInt(idxServ));
                int idxDiff = cursor.getColumnIndex(COL_DIFFICULTY);
                if (idxDiff != -1) r.setDifficulty(cursor.getString(idxDiff));
                list.add(r);
            }
        }
        return list;
    }
}



